"""
Deploy TG Search to the Hetzner server.
Uploads code, installs deps, configures nginx + supervisor, starts service.
"""
import paramiko
import base64
import os
import time

SERVER_IP   = "65.21.53.29"
SERVER_PASS = "Sanct@Hetz2026#"
REMOTE_DIR  = "/opt/telesearch"
LOCAL_DIR   = os.path.dirname(os.path.abspath(__file__))

FILES = ["main.py", "requirements.txt"]


def get_client():
    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    client.connect(SERVER_IP, username="root", password=SERVER_PASS, timeout=30)
    return client


def run(client, cmd: str, timeout: int = 120) -> str:
    print(f"$ {cmd[:80]}{'...' if len(cmd) > 80 else ''}")
    _, stdout, stderr = client.exec_command(cmd, timeout=timeout)
    exit_code = stdout.channel.recv_exit_status()
    out = stdout.read().decode().strip()
    err = stderr.read().decode().strip()
    if out:
        for line in out.splitlines()[:20]:
            print(f"  {line}")
    if err and exit_code != 0:
        print(f"  ERR: {err[:300]}")
    return out


def upload_file(client, local_path: str, remote_path: str) -> bool:
    try:
        with open(local_path, "rb") as f:
            content = f.read()
        b64 = base64.b64encode(content).decode()
        chunk_size = 60000
        chunks = [b64[i:i+chunk_size] for i in range(0, len(b64), chunk_size)]
        run(client, f"echo -n '{chunks[0]}' > /tmp/_upload_b64")
        for chunk in chunks[1:]:
            run(client, f"echo -n '{chunk}' >> /tmp/_upload_b64")
        run(client, f"base64 -d /tmp/_upload_b64 > {remote_path} && rm /tmp/_upload_b64")
        return True
    except Exception as e:
        print(f"  ERROR: {e}")
        return False


def main():
    print(f"Deploying TG Search to {SERVER_IP}...\n")
    client = get_client()
    print("Connected.\n")

    # Ensure remote dir exists
    run(client, f"mkdir -p {REMOTE_DIR}/static")

    # Upload core files
    print("Uploading files...")
    for fname in FILES:
        local_path = os.path.join(LOCAL_DIR, fname)
        if os.path.exists(local_path):
            ok = upload_file(client, local_path, f"{REMOTE_DIR}/{fname}")
            size = os.path.getsize(local_path)
            print(f"  {'OK' if ok else 'FAIL'}: {fname} ({size:,} bytes)")
        else:
            print(f"  SKIP: {fname}")

    # Upload static/index.html
    static_path = os.path.join(LOCAL_DIR, "static", "index.html")
    if os.path.exists(static_path):
        ok = upload_file(client, static_path, f"{REMOTE_DIR}/static/index.html")
        print(f"  {'OK' if ok else 'FAIL'}: static/index.html")

    # Create virtualenv if missing
    print("\nSetting up Python environment...")
    run(client, f"test -d {REMOTE_DIR}/venv || python3 -m venv {REMOTE_DIR}/venv")
    run(client, f"{REMOTE_DIR}/venv/bin/pip install -q -r {REMOTE_DIR}/requirements.txt", timeout=180)

    # Copy Telegram session from sanctum if available (same account)
    print("\nChecking for Telegram session...")
    session_src = "/opt/sanctum/sanctum_setup.session"
    session_dst = f"{REMOTE_DIR}/session"
    out = run(client, f"test -f {session_src} && echo exists || echo missing")
    if "exists" in out:
        run(client, f"cp {session_src} {session_dst}")
        print(f"  Copied session from {session_src}")
    else:
        print(f"  WARNING: No session found at {session_src}")
        print("  You may need to generate a new session by running main.py locally first.")

    # Create .env with credentials
    print("\nCreating environment config...")
    env_content = (
        "APP_USER=admin\n"
        "APP_PASS=telesearch2024\n"
        "TG_API_ID=31671257\n"
        "TG_API_HASH=ec880ffa330d0c826b77a7ffc3a16255\n"
    )
    b64_env = base64.b64encode(env_content.encode()).decode()
    run(client, f"printf '%s' '{b64_env}' | base64 -d > {REMOTE_DIR}/.env")
    print("  Created .env")

    # Supervisor config
    print("\nConfiguring supervisor...")
    supervisor_conf = f"""[program:telesearch]
command={REMOTE_DIR}/venv/bin/python {REMOTE_DIR}/main.py
directory={REMOTE_DIR}
user=root
autostart=true
autorestart=true
redirect_stderr=true
stdout_logfile=/var/log/telesearch.out.log
stderr_logfile=/var/log/telesearch.err.log
environment=PATH="{REMOTE_DIR}/venv/bin"
"""
    b64_sup = base64.b64encode(supervisor_conf.encode()).decode()
    run(client, f"printf '%s' '{b64_sup}' | base64 -d > /etc/supervisor/conf.d/telesearch.conf")
    run(client, "supervisorctl reread")
    run(client, "supervisorctl update")
    run(client, "supervisorctl start telesearch || supervisorctl restart telesearch")
    time.sleep(2)

    # Nginx config: add /telesearch location
    print("\nUpdating nginx...")
    nginx_addition = """
    location /telesearch {
        proxy_pass http://127.0.0.1:8001;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Prefix /telesearch;
    }
"""
    # Read current nginx config
    current = run(client, "cat /etc/nginx/sites-available/sanctum")
    if "/telesearch" not in current:
        # Insert before the closing }
        new_config = current.rstrip()
        if new_config.endswith("}"):
            new_config = new_config[:-1] + nginx_addition + "}\n"
        else:
            new_config = new_config + nginx_addition
        b64_ngx = base64.b64encode(new_config.encode()).decode()
        run(client, f"printf '%s' '{b64_ngx}' | base64 -d > /etc/nginx/sites-available/sanctum")
        run(client, "nginx -t")
        run(client, "systemctl reload nginx")
        print("  Added /telesearch location to nginx")
    else:
        print("  /telesearch already in nginx config")

    print("\nService status:")
    run(client, "supervisorctl status")

    print(f"\nDone!")
    print(f"  TG Search : http://{SERVER_IP}/telesearch")
    print(f"  Login     : admin / telesearch2024")
    print(f"  Logs      : ssh root@{SERVER_IP} 'tail -f /var/log/telesearch.err.log'")

    client.close()


if __name__ == "__main__":
    main()

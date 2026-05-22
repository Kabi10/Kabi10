import paramiko
import base64

SERVER_IP = "65.21.53.29"
SERVER_PASS = "Sanct@Hetz2026#"

client = paramiko.SSHClient()
client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
client.connect(SERVER_IP, username="root", password=SERVER_PASS, timeout=30)

def run(cmd):
    _, o, e = client.exec_command(cmd)
    out = o.read().decode().strip()
    err = e.read().decode().strip()
    if out: print(f"  {out}")
    if err: print(f"  ERR: {err[:200]}")
    return out

# Read current app-gateway config
run("cat /etc/nginx/sites-available/app-gateway > /tmp/app-gateway-old")

# Create new config with telesearch block
new_config = """server {
    listen 80 default_server;
    server_name 65.21.53.29 _;

    root /var/www/app-gateway;
    index index.html;

    location = / {
        try_files /index.html =404;
    }

    location = /sanctum {
        return 301 /Sanctum/;
    }

    location = /organiverse {
        return 301 /Organiverse/;
    }

    location = /Sanctum {
        return 301 /Sanctum/;
    }

    location /Sanctum/api/ {
        auth_basic "Sanctum Dashboard";
        auth_basic_user_file /etc/nginx/.sanctum_htpasswd;
        proxy_pass http://127.0.0.1:8000/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location = /Sanctum/search {
        auth_basic "Sanctum Dashboard";
        auth_basic_user_file /etc/nginx/.sanctum_htpasswd;
        proxy_pass http://127.0.0.1:8000/search;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header Accept-Encoding "";
        sub_filter_once off;
        sub_filter "'/api" "'/Sanctum/api";
        sub_filter "`/api" "`/Sanctum/api";
        sub_filter '"/api' '"/Sanctum/api';
    }

    location /Sanctum/ {
        auth_basic "Sanctum Dashboard";
        auth_basic_user_file /etc/nginx/.sanctum_htpasswd;
        proxy_pass http://127.0.0.1:8000/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header Accept-Encoding "";
        sub_filter_once off;
        sub_filter "'/api" "'/Sanctum/api";
        sub_filter "`/api" "`/Sanctum/api";
        sub_filter '"/api' '"/Sanctum/api';
        sub_filter 'href="/search"' 'href="/Sanctum/search"';
    }

    location = /Organiverse {
        return 301 /Organiverse/;
    }

    location /Organiverse/ws/ {
        proxy_pass http://127.0.0.1:8010/ws/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_read_timeout 3600;
        proxy_send_timeout 3600;
    }

    location = /Organiverse/health {
        proxy_pass http://127.0.0.1:8010/health;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    location = /Organiverse/organs {
        proxy_pass http://127.0.0.1:8010/organs;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    location /Organiverse/sessions/ {
        proxy_pass http://127.0.0.1:8010/sessions/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    location = /Organiverse/index.html {
        alias /opt/organiverse/frontend/dist/index.html;
    }

    location /Organiverse/ {
        alias /opt/organiverse/frontend/dist/;
        try_files $uri $uri/ /Organiverse/index.html;
    }

    location /telesearch {
        proxy_pass http://127.0.0.1:8001/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
"""

b64 = base64.b64encode(new_config.encode()).decode()
run(f"printf '%s' '{b64}' | base64 -d > /etc/nginx/sites-available/app-gateway")
run("nginx -t")
run("systemctl reload nginx")

# Test
time.sleep(1)
run("curl -s -o /dev/null -w '%{http_code}' http://127.0.0.1/telesearch/")
run("curl -s -o /dev/null -w '%{http_code}' http://127.0.0.1/telesearch")

client.close()
print("Done.")

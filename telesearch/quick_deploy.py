import paramiko
import base64

SERVER_IP = "65.21.53.29"
SERVER_PASS = "Sanct@Hetz2026#"

def upload_file(client, local_path, remote_path):
    with open(local_path, "rb") as f:
        content = f.read()
    b64 = base64.b64encode(content).decode()
    chunk_size = 60000
    chunks = [b64[i:i+chunk_size] for i in range(0, len(b64), chunk_size)]

    client.exec_command(f"echo -n '{chunks[0]}' > /tmp/_upload_b64")
    for chunk in chunks[1:]:
        client.exec_command(f"echo -n '{chunk}' >> /tmp/_upload_b64")
    client.exec_command(f"base64 -d /tmp/_upload_b64 > {remote_path} && rm /tmp/_upload_b64")
    print(f"  OK: {remote_path}")

client = paramiko.SSHClient()
client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
client.connect(SERVER_IP, username="root", password=SERVER_PASS, timeout=30)

print("Uploading main.py...")
upload_file(client, r"C:\Dev\telesearch\main.py", "/opt/telesearch/main.py")

print("Uploading index.html...")
upload_file(client, r"C:\Dev\telesearch\static\index.html", "/opt/telesearch/static/index.html")

print("Uploading background_scanner.py...")
upload_file(client, r"C:\Dev\telesearch\background_scanner.py", "/opt/telesearch/background_scanner.py")

print("Restarting telesearch...")
client.exec_command("supervisorctl restart telesearch")

import time
time.sleep(2)

_, o, _ = client.exec_command("supervisorctl status telesearch")
print("Status:", o.read().decode().strip())

client.close()
print("Done.")

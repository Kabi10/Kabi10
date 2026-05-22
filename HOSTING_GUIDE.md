# Hetzner App Hosting Guide

This server is set up as a path-based app gateway. Nginx is the only public
entrypoint. Each app runs as its own private service on localhost, with static
frontend files served by nginx and dynamic/API/WebSocket traffic proxied to the
app's backend.

## Current Routes

```text
http://65.21.53.29/              App gateway home
http://65.21.53.29/Sanctum/      Sanctum dashboard, password protected
http://65.21.53.29/Organiverse/  Organiverse app
```

Temporary HTTPS is available through a generated hostname:

```text
https://apps.65.21.53.29.sslip.io/
https://apps.65.21.53.29.sslip.io/Sanctum/
https://apps.65.21.53.29.sslip.io/Organiverse/
```

Use a real domain later for clean HTTPS, for example:

```text
https://apps.example.com/Sanctum/
https://apps.example.com/Organiverse/
```

## Current Layout

```text
/var/www/app-gateway/index.html           Gateway home page
/etc/nginx/sites-available/app-gateway    Bare-IP path router
/etc/nginx/sites-available/organiverse    HTTPS gateway host
/etc/nginx/.sanctum_htpasswd              Sanctum nginx password file

/opt/sanctum                              Existing Sanctum app
127.0.0.1:8000                            Existing Sanctum dashboard backend

/opt/organiverse                          Organiverse app
/opt/organiverse/venv                     Organiverse Python venv
/opt/organiverse/.env                     Organiverse runtime env, not committed
/opt/organiverse/frontend/dist            Organiverse static frontend
127.0.0.1:8010                            Organiverse FastAPI backend
supervisor: organiverse-backend           Organiverse process
```

## Rule For Future Apps

For each new app, allocate five things:

```text
Path prefix:      /NewApp/
Remote app dir:   /opt/newapp
Private port:     127.0.0.1:8020
Supervisor name:  newapp-backend
Static build dir: /opt/newapp/frontend/dist
```

Do not expose app ports publicly. Keep ports bound to `127.0.0.1`.

## Nginx Pattern

Add a path block to the gateway config:

```nginx
location = /NewApp {
    return 301 /NewApp/;
}

location /NewApp/ws/ {
    proxy_pass http://127.0.0.1:8020/ws/;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_read_timeout 3600;
    proxy_send_timeout 3600;
}

location /NewApp/api/ {
    proxy_pass http://127.0.0.1:8020/api/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
}

location = /NewApp/index.html {
    alias /opt/newapp/frontend/dist/index.html;
}

location /NewApp/ {
    alias /opt/newapp/frontend/dist/;
    try_files $uri $uri/ /NewApp/index.html;
}
```

For a Vite frontend, build with a matching base path:

```powershell
$env:VITE_BASE_PATH = "/NewApp/"
npm run build
```

The app's production API and WebSocket URLs should use same-origin paths under
the prefix, such as `/NewApp/api/...` and `/NewApp/ws/...`.

## Supervisor Pattern

Create a separate supervisor file per app:

```ini
[program:newapp-backend]
command=/opt/newapp/venv/bin/python -m uvicorn backend.app.main:app --host 127.0.0.1 --port 8020
directory=/opt/newapp
autostart=true
autorestart=true
startretries=5
stderr_logfile=/var/log/newapp.err.log
stdout_logfile=/var/log/newapp.out.log
user=root
environment=PYTHONUNBUFFERED=1
```

Then reload supervisor:

```bash
supervisorctl reread
supervisorctl update
supervisorctl status
```

## Password Protecting An App

Use nginx Basic Auth for private dashboards:

```nginx
auth_basic "Private App";
auth_basic_user_file /etc/nginx/.private_app_htpasswd;
```

Put those lines inside every private location block for that app, including API
routes. Sanctum currently uses `/etc/nginx/.sanctum_htpasswd`.

## Deployment Checklist

1. Pick a unique path prefix and localhost port.
2. Create `/opt/<app>` and a Python venv if needed.
3. Upload the backend and runtime `.env` to `/opt/<app>/.env`.
4. Build the frontend with its path prefix.
5. Upload frontend `dist` to `/opt/<app>/frontend/dist`.
6. Add a dedicated supervisor config.
7. Add nginx path locations to the app gateway.
8. Run `nginx -t`, then `systemctl reload nginx`.
9. Run `supervisorctl reread`, `supervisorctl update`, and start/restart the app.
10. Smoke test the public path, API endpoints, and WebSocket path.

## Verification Commands

```bash
nginx -t
supervisorctl status
curl -I http://65.21.53.29/
curl -I http://65.21.53.29/Organiverse/
curl -I http://65.21.53.29/Sanctum/
ss -tulpn | grep -E ":(8000|8010|8020)"
```

For Organiverse specifically:

```powershell
.\.venv\Scripts\python.exe deploy\smoke_public.py http://65.21.53.29/Organiverse
.\.venv\Scripts\python.exe deploy\smoke_public.py https://apps.65.21.53.29.sslip.io/Organiverse
```

## Notes

- Raw IP HTTPS is not the clean long-term solution. Use a real domain for clean
  TLS.
- Keep `.env` files on the server only. Do not commit secrets.
- Keep apps separate: do not reuse `/opt/sanctum`, port `8000`, or Sanctum's
  supervisor service for new apps.
- Prefer one path prefix per app. Apps with absolute `/api` URLs need either
  frontend changes or nginx response rewriting.

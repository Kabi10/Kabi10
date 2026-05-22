#!/opt/telesearch/venv/bin/python
"""Trigger a background scan via the telesearch API."""
import urllib.request
import base64
import json

auth = base64.b64encode(b"admin:telesearch2024").decode()
body = json.dumps({"max_members": 100, "max_results_per_keyword": 100}).encode()

req = urllib.request.Request(
    "http://127.0.0.1:8001/api/scanner/run",
    data=body,
    headers={
        "Authorization": f"Basic {auth}",
        "Content-Type": "application/json",
    },
    method="POST",
)

try:
    resp = urllib.request.urlopen(req, timeout=3600)  # 1 hour for 400+ keywords
    result = json.loads(resp.read().decode())
    print(json.dumps(result))
except Exception as e:
    print(f"Error: {e}")
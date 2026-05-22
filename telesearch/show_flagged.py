import json
d = json.load(open('/tmp/fg.json'))
print('Total flagged groups:', len(d))
for g in d[:25]:
    t = g["title"].encode("ascii", "replace").decode()
    u = g.get("username", "?") or "?"
    m = g.get("members", "?")
    k = ", ".join(g.get("keywords_matched", []))
    print(f"{t} | @{u} | {m} members | {k}")

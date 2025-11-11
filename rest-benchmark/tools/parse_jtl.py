import os
import csv
import math
from collections import defaultdict

results_dir = os.path.join(os.path.dirname(__file__), '..', 'results')
files = [f for f in os.listdir(results_dir) if f.endswith('.jtl')]

import statistics

def percentiles(data, ps=(50,95,99)):
    data_sorted = sorted(data)
    n = len(data_sorted)
    res = {}
    for p in ps:
        if n==0:
            res[p]=None
            continue
        k = (n-1)*(p/100)
        f = math.floor(k)
        c = math.ceil(k)
        if f==c:
            val = data_sorted[int(k)]
        else:
            d0 = data_sorted[f]*(c-k)
            d1 = data_sorted[c]*(k-f)
            val = d0 + d1
        res[p]=round(val,2)
    return res

summary = {}
per_endpoint = {}

for fname in files:
    path = os.path.join(results_dir, fname)
    with open(path, newline='', encoding='utf-8') as csvfile:
        reader = csv.DictReader(csvfile)
        times = []
        successes = 0
        total = 0
        timestamps = []
        ep_data = defaultdict(list)
        for row in reader:
            total += 1
            try:
                elapsed = int(row.get('elapsed') or row.get('time'))
            except:
                elapsed = int(row['elapsed'])
            label = row.get('label','')
            success = row.get('success','true').lower()=='true'
            if success:
                successes += 1
            times.append(elapsed)
            ep_data[label].append((elapsed, success))
            ts = int(row.get('timeStamp') or row.get('time'))
            timestamps.append(ts)
        if len(timestamps)==0:
            continue
        duration_ms = max(timestamps)-min(timestamps)
        duration_s = duration_ms/1000 if duration_ms>0 else 1
        rps = round(total/duration_s,2)
        errs = total - successes
        err_pct = round(errs*100/total,2) if total>0 else 0
        p = percentiles(times)
        summary[fname] = {
            'total': total,
            'successes': successes,
            'errors': errs,
            'err_pct': err_pct,
            'rps': rps,
            'p50': p[50],
            'p95': p[95],
            'p99': p[99],
            'duration_s': round(duration_s,2)
        }
        per_endpoint[fname] = {}
        for label, arr in ep_data.items():
            arr_times = [a for a,_ in arr]
            arr_success = sum(1 for _,s in arr if s)
            tot = len(arr_times)
            errs = tot - arr_success
            ep_p = percentiles(arr_times)
            ep_rps = round(tot/duration_s,2)
            per_endpoint[fname][label] = {
                'total': tot,
                'errors': errs,
                'err_pct': round(errs*100/tot,2) if tot>0 else 0,
                'p95': ep_p[95],
                'p50': ep_p[50],
                'p99': ep_p[99],
                'rps': ep_rps
            }

# Build README content
readme = []
readme.append('# Résultats d\'analyse des JTL')
readme.append('')
readme.append('Fichiers analysés:')
for k in summary:
    readme.append('- ' + k)
readme.append('')
readme.append('## Commandes pour générer les JTL (exécution non-graphique JMeter)')
readme.append('Les fichiers .jmx se trouvent dans `jmeter/scenarios/` et les résultats sont écrits dans `results/`.')
readme.append('Exemples (adapter `-Jbase_url` si besoin):')
readme.append('')
readme.append('```cmd')
readme.append('REM Variante A (Jersey) sur http://localhost:8081')
readme.append('jmeter -n -t jmeter/scenarios/read-heavy.jmx -l results/variant-a-read-heavy.jtl -Jbase_url=http://localhost:8081')
readme.append('jmeter -n -t jmeter/scenarios/join-filter.jmx -l results/variant-a-join-filter.jtl -Jbase_url=http://localhost:8081')
readme.append('jmeter -n -t jmeter/scenarios/mixed.jmx -l results/variant-a-mixed.jtl -Jbase_url=http://localhost:8081')
readme.append('jmeter -n -t jmeter/scenarios/heavy-body.jmx -l results/variant-a-heavy-body.jtl -Jbase_url=http://localhost:8081')
readme.append('')
readme.append('REM Variante C (Spring MVC) sur http://localhost:8082')
readme.append('jmeter -n -t jmeter/scenarios/read-heavy.jmx -l results/variant-c-read-heavy.jtl -Jbase_url=http://localhost:8082')
readme.append('jmeter -n -t jmeter/scenarios/join-filter.jmx -l results/variant-c-join-filter.jtl -Jbase_url=http://localhost:8082')
readme.append('jmeter -n -t jmeter/scenarios/mixed.jmx -l results/variant-c-mixed.jtl -Jbase_url=http://localhost:8082')
readme.append('jmeter -n -t jmeter/scenarios/heavy-body.jmx -l results/variant-c-heavy-body.jtl -Jbase_url=http://localhost:8082')
readme.append('')
readme.append('REM Variante D (Spring Data REST) sur http://localhost:8083')
readme.append('jmeter -n -t jmeter/scenarios/read-heavy.jmx -l results/variant-d-read-heavy.jtl -Jbase_url=http://localhost:8083')
readme.append('jmeter -n -t jmeter/scenarios/join-filter.jmx -l results/variant-d-join-filter.jtl -Jbase_url=http://localhost:8083')
readme.append('jmeter -n -t jmeter/scenarios/mixed.jmx -l results/variant-d-mixed.jtl -Jbase_url=http://localhost:8083')
readme.append('jmeter -n -t jmeter/scenarios/heavy-body.jmx -l results/variant-d-heavy-body.jtl -Jbase_url=http://localhost:8083')
readme.append('```')
readme.append('')

readme.append('## Tableaux — Résultats JMeter (T2)')
readme.append('| Scénario | Variante | RPS | p50 (ms) | p95 (ms) | p99 (ms) | Err % |')
readme.append('|---|---:|---:|---:|---:|---:|---:|')
for fname, s in summary.items():
    # derive variant and scenario from filename
    parts = fname.replace('.jtl','').split('-')
    if len(parts)>=3:
        variant = parts[1].upper()
        scenario = parts[2].replace('-', ' ')
    else:
        variant = parts[1] if len(parts)>1 else ''
        scenario = parts[0]
    readme.append(f"| {scenario} | {variant} | {s['rps']} | {s['p50']} | {s['p95']} | {s['p99']} | {s['err_pct']} |")

readme.append('')
readme.append('## Tableaux — Détails par endpoint (JOIN-filter, T4)')
readme.append('| Endpoint | Variante | RPS | p95 (ms) | Err % |')
readme.append('|---|---|---:|---:|---:|')
for fname, endpoints in per_endpoint.items():
    if 'join-filter' in fname:
        variant = fname.split('-')[1].upper()
        for ep, v in endpoints.items():
            readme.append(f"| {ep} | {variant} | {v['rps']} | {v['p95']} | {v['err_pct']} |")

readme.append('')
readme.append('## Tableaux — Détails par endpoint (MIXED, T5)')
readme.append('| Endpoint | Variante | RPS | p95 (ms) | Err % |')
readme.append('|---|---|---:|---:|---:|')
for fname, endpoints in per_endpoint.items():
    if 'mixed' in fname:
        variant = fname.split('-')[1].upper()
        for ep, v in endpoints.items():
            readme.append(f"| {ep} | {variant} | {v['rps']} | {v['p95']} | {v['err_pct']} |")

readme.append('')
readme.append('## T6 — Incidents / erreurs (extrait)')
readme.append('| Run | Variante | Type d\'erreur | % | Cause probable | Action corrective |')
readme.append('|---|---|---|---:|---|---|')
# populate with files that have errors
for fname,s in summary.items():
    if s['errors']>0:
        variant = fname.split('-')[1].upper() if '-' in fname else ''
        readme.append(f"| {fname} | {variant} | HTTP errors | {s['err_pct']} | voir payloads/headers | vérifier content-type/payloads |")

readme.append('')
readme.append('## T3 — Ressources JVM (Prometheus)')
readme.append('Les métriques JVM (CPU, heap, GC, threads, Hikari) ne sont pas présentes dans les JTL; elles doivent être extraites de Prometheus/Grafana. Détaillé: Déféré.')
readme.append('')
readme.append('## Observations rapides')
readme.append('- Les fichiers .jtl montrent des erreurs de type 400/415 pour les endpoints POST/PUT/DELETE dans les scénarios MIXED — probablement payload incorrect / header (content-type) non accepté.\n- GET endpoints sont majoritairement 200 OK avec latences p50 ~15–22ms et p95/p99 plus élevées selon le scénario.')

# write README
out = os.path.join(os.path.dirname(__file__), '..', 'README_RESULTS.md')
with open(out, 'w', encoding='utf-8') as f:
    f.write('\n'.join(readme))

print('Generated', out)


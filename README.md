# REST Benchmark – Rapport 

Ce projet compare trois implémentations REST adossées à la même base PostgreSQL afin d’évaluer l’impact des choix de stack sur les performances :

- **Variant A — Jersey** : JAX-RS (Jersey) + HK2 + JPA/Hibernate
- **Variant C — Spring MVC** : Spring Boot + `@RestController` + JPA/Hibernate
- **Variant D — Spring Data REST** : Spring Boot + Spring Data REST (exposition HAL automatique)

Le dépôt contient le code de chaque service, les scénarios JMeter, l’infrastructure Docker (base, monitoring, InfluxDB) ainsi que les résultats bruts et analysés.

---

## 1. Architecture du dépôt

- `common-entities/` : modèle de données partagé (entités `Category` & `Item`, migrations Flyway).
- `variant-a-jersey/` : application JAX-RS avec DAO JPA explicites et configuration HK2.
- `variant-c-springmvc/` : Spring Boot REST contrôleurs + DTO pour maîtriser la sérialisation.
- `variant-d-springdata/` : Spring Data REST avec projections HAL pour l’exploration rapide.
- `database/` : scripts d’initialisation PostgreSQL (2000 catégories, 100k items).
- `jmeter/` : scénarios (`read-heavy`, `join-filter`, `mixed`, `heavy-body`) et jeux de données CSV / payloads JSON.
- `monitoring/` : stack Prometheus + Grafana + InfluxDB (dashboards, provisioning).
- `results/` : traces JMeter (`.jtl`) pour chaque couple variante/scénario.
- `tools/` : utilitaires (ex. parsing JTL).
- `BENCHMARK_RESULTS.md` : analyse détaillée servant de source à ce rapport.

---

## 2. Modèle de données & instrumentation

- **Catégories** : 2 000 lignes `CAT0001 … CAT2000`
- **Items** : 100 000 lignes, ~50 items par catégorie
- **Payloads POST/PUT** : versions « léger » (~1 kB) et « lourd » (5 kB)

Instrumentation commune :

- Java 21, PostgreSQL 14+, HikariCP (min 10 / max 20)
- Actuator + Micrometer Prometheus pour C & D, endpoint custom pour A
- JMeter 5.6.3 avec Backend Listener InfluxDB v2 (`bucket=jmeter`, `org=perf`)
- Grafana 10+ (dashboards JMeter & JVM), Prometheus 2.x
- Cache HTTP & cache Hibernate L2 désactivés pour éviter le biais

---

## 3. Méthodologie de test

### Scénarios JMeter

| **Scénario** | **Mix** | **Threads (paliers)** | **Ramp-up** | **Durée/palier** | **Payload** |
|--------------|---------|-----------------------|-------------|------------------|-------------|
| **READ-heavy** | 50% GET /items?page=&size=<br>20% GET /items?categoryId=<br>20% GET /categories/{id}/items<br>10% GET /categories?page=&size= | 50 → 100 → 200 | 60s | 10 min | — |
| **JOIN-filter** | 70% GET /items?categoryId=<br>30% GET /items/{id} | 60 → 120 | 60s | 8 min | — |
| **MIXED** | 40% GET /items<br>20% POST /items (1 kB)<br>10% PUT /items/{id} (1 kB)<br>10% DELETE /items/{id}<br>10% POST /categories (0.5–1 kB)<br>10% PUT /categories/{id} | 50 → 100 | 60s | 10 min | 1 kB |
| **HEAVY-body** | 50% POST /items (5 kB)<br>50% PUT /items/{id} (5 kB) | 30 → 60 | 60s | 8 min | 5 kB |

### Bonnes pratiques appliquées

- CSV Data Set Config pour IDs existants
- HTTP Request Defaults par variante (`host`, `port`)
- Backend Listener InfluxDB v2 (flux vers Grafana Data Explorer)
- Listeners lourds désactivés pendant les runs

### Points d’attention

- Mode anti-N+1 via `JOIN FETCH` (A & C) vs mode baseline lazy (D)
- Pagination homogène (`page/size`)
- Bean Validation active (Spring) – à compléter côté Jersey
- Sérialisation Jackson identique (Spring Data REST ajoute HAL)

---

## 4. Résultats globaux (T2)

| **Scénario** | **Mesure** | **A : Jersey** | **C : @RestController** | **D : Spring Data REST** |
|--------------|------------|----------------|-------------------------|--------------------------|
| **READ-heavy** | **RPS** | 2.08/s | 2.08/s | N/A (service indisponible) |
| | **p50 (ms)** | 17.5 | 23.0 | N/A |
| | **p95 (ms)** | 26.0 | 71.1 | N/A |
| | **p99 (ms)** | 86.5 | 147.9 | N/A |
| | **Err %** | 0% | 0% | 100% (connexion refusée) |
| **JOIN-filter** | **RPS** | 3.04/s | 3.03/s | N/A (service indisponible) |
| | **p50 (ms)** | 11.0 | 20.0 | N/A |
| | **p95 (ms)** | 15.0 | 25.1 | N/A |
| | **p99 (ms)** | 20.3 | 33.2 | N/A |
| | **Err %** | 0% | 0% | 100% (connexion refusée) |
| **MIXED (2 entités)** | **RPS** | 2.58/s | 2.56/s | N/A (service indisponible) |
| | **p50 (ms)** | 12.0 | 15.0 | N/A |
| | **p95 (ms)** | 24.0 | 26.1 | N/A |
| | **p99 (ms)** | 34.1 | 58.7 | N/A |
| | **Err %** | 60.0% | 59.3% | 100% (connexion refusée) |
| **HEAVY-body** | **RPS** | 1.54/s | 1.54/s | N/A (service indisponible) |
| | **p50 (ms)** | 11.0 | 13.0 | N/A |
| | **p95 (ms)** | 18.1 | 19.6 | N/A |
| | **p99 (ms)** | 29.3 | 151.3 | N/A |
| | **Err %** | 100% | 100% | 100% (connexion refusée) |

> **Observations** : la variante D n’a pas répondu pendant les campagnes (`localhost:8084` indisponible). Les scénarios MIXED/HEAVY affichent 100 % d’erreurs sur A & C faute de pre-processors Groovy pour générer des payloads valides (400/415).

---

## 5. Ressources JVM (T3 remplacé par capture)

<img width="1796" height="944" alt="jvm-resources" src="https://github.com/user-attachments/assets/2c0ef4f5-1d44-4e46-948c-837cb249eda0" />


*Capture Grafana « REST Benchmark – JVM Resources » affichant : CPU process/system, Heap used vs max, Pauses GC, Threads actifs et connexions HikariCP. Assurez-vous que l’image est placée dans `docs/images/jvm-resources.png` pour l’affichage dans le README.*

---

## 6. Détails par endpoint

### T4 — Scénario JOIN-filter

| **Endpoint** | **Variante** | **RPS** | **p95 (ms)** | **Err %** | **Observations** |
|--------------|--------------|---------|--------------|-----------|------------------|
| `GET /api/items?categoryId=` | A | 2.13 | 16.5 | 0% | `JOIN FETCH` actif, pas de N+1 |
| | C | 2.12 | 26.8 | 0% | `JOIN FETCH` via repository Spring |
| | D | N/A | N/A | 100% | Service hors ligne (connexion refusée) |
| `GET /api/items/{id}` | A | 0.91 | 13.0 | 0% | Accès direct JPA contrôlé |
| | C | 0.91 | 15.4 | 0% | DTO + mapping explicite |
| | D | N/A | N/A | 100% | Service hors ligne (connexion refusée) |

### T5 — Scénario MIXED

| **Endpoint** | **Variante** | **RPS** | **p95 (ms)** | **Err %** | **Observations** |
|--------------|--------------|---------|--------------|-----------|------------------|
| `GET /api/items?page` | A | 1.03 | 27.1 | 0% | Pagination OK |
| | C | 1.02 | 27.1 | 0% | Pagination OK |
| | D | N/A | N/A | 100% | Service hors ligne (connexion refusée) |
| `POST /api/items` | A | 0.52 | 13.6 | 100% | Payloads JSON non générés (400) |
| | C | 0.51 | 14.1 | 100% | Même cause (400) |
| | D | N/A | N/A | 100% | Service hors ligne (connexion refusée) |
| `PUT /api/items/{id}` | A | 0.26 | 12.0 | 100% | Payloads JSON non générés (400) |
| | C | 0.26 | 20.0 | 100% | Payloads JSON non générés (400) |
| | D | N/A | N/A | 100% | Service hors ligne (connexion refusée) |
| `DELETE /api/items/{id}` | A | 0.26 | 12.2 | 100% | 415 Unsupported Media Type |
| | C | 0.26 | 17.8 | 100% | En-têtes/content-type manquants |
| | D | N/A | N/A | 100% | Service hors ligne (connexion refusée) |
| `POST /api/categories` | A | 0.26 | 20.5 | 100% | Payloads JSON non générés (400) |
| | C | 0.26 | 35.2 | 93.3% | Un seul succès (201), reste en 409 (unicité code) |
| | D | N/A | N/A | 100% | Service hors ligne (connexion refusée) |
| `PUT /api/categories/{id}` | A | 0.26 | 14.0 | 100% | Payloads JSON non générés (400) |
| | C | 0.26 | 54.7 | 100% | Payloads JSON non générés (400) |
| | D | N/A | N/A | 100% | Service hors ligne (connexion refusée) |

---

## 7. Incidents & erreurs (T6)

| **Run** | **Variante** | **Type d’erreur (HTTP/DB/timeout)** | **%** | **Cause probable** | **Action corrective** |
|---------|--------------|-------------------------------------|-------|--------------------|-----------------------|
| MIXED | A, C | 400 Bad Request / 415 | 59‑60% | Payloads JSON invalides, DELETE sans `Content-Type` | Ajouter pre-processors Groovy + entêtes adéquats |
| MIXED | D | Connexion refusée (`localhost:8084`) | 100% | Service Spring Data REST non lancé | Redéployer la variante D avant exécution |
| HEAVY | A, C | 400 Bad Request | 100% | Payloads 5 kB invalides | Générer payloads valides via pre-processors |
| HEAVY | D | Connexion refusée (`localhost:8084`) | 100% | Service Spring Data REST non lancé | Redéployer la variante D avant exécution |
| READ | D | Connexion refusée (`localhost:8084`) | 100% | Service Spring Data REST non lancé | Vérifier le déploiement avant test |
| JOIN | D | Connexion refusée (`localhost:8084`) | 100% | Service Spring Data REST non lancé | Vérifier le déploiement avant test |

---

## 8. Synthèse & recommandations (T7)

| **Critère** | **Meilleure variante** | **Écart (justifier)** | **Commentaires** |
|-------------|------------------------|-----------------------|------------------|
| **Débit global (RPS)** | A/C (égalité) | 2.08 req/s sur READ/JOIN | Limité par PostgreSQL + Hikari (pool 20). Variante D non disponible. |
| **Latence p95 (ms)** | Jersey (A) | 26.0 ms (A) vs 71.1 ms (C) | Moins de couches d’abstraction. |
| **Latence p99 (ms)** | Jersey (A) | 86.5 ms (A) vs 147.9 ms (C) | Pics nettement plus faibles. |
| **Stabilité (erreurs)** | A/C sur lecture | 0% en lecture, 59-60% échecs sur POST/PUT | Payloads à corriger. |
| **Empreinte CPU/RAM** | À confirmer | Données disponibles via dashboard JVM | Mesures à relever sur la capture Grafana. |
| **Empreinte relationnelle** | Jersey (A) | Requêtes `JOIN FETCH` maîtrisées | Spring MVC offre les mêmes possibilités. |
| **Facilité d’expo relationnelle** | Spring Data REST (D) | CRUD HAL automatique | Nécessite une remise en service pour tests. |

**Recommandations immédiates**

1. Mettre en place des pre-processors Groovy pour générer des payloads JSON valides.
2. Relancer la variante D avant toute nouvelle campagne (vérifier le port 8084).
3. Rejouer MIXED / HEAVY-body une fois les payloads corrigés.
4. Compléter les métriques T3 en se basant sur l’image `JVM Resources` (valeurs max/moy par panneau).
5. Créer un dashboard InfluxDB dédié aux résultats JMeter (RPS, p50/p95/p99, erreurs).

---

## 9. Visualisations

Les captures suivantes illustrent les tableaux de bord Grafana et InfluxDB utilisés pour l’analyse. Copiez vos exports PNG dans `docs/images/` afin qu’ils s’affichent correctement.

**InfluxDB — Heavy body**
<img width="1857" height="939" alt="influxdb-heavy-body" src="https://github.com/user-attachments/assets/ea52d068-8095-4891-ac5f-6e0df9c6c8e8" />

**InfluxDB — Read heavy**
<img width="1859" height="977" alt="influxdb-read-heavy" src="https://github.com/user-attachments/assets/5dabe565-41d3-4e4f-b55a-8fd5a9adf4be" />

**InfluxDB — Join filter**
<img width="1857" height="972" alt="influxdb-join-filter" src="https://github.com/user-attachments/assets/4c0311f9-2487-4d9a-abe7-42dcd520db0f" />

**InfluxDB — Mixed**
<img width="1853" height="975" alt="influxdb-mixed" src="https://github.com/user-attachments/assets/5f6e6868-0fe2-4d4a-b528-3bc80fbcf507" />

**Grafana — JMeter Benchmark**
<img width="1893" height="983" alt="jmeter-dashboard" src="https://github.com/user-attachments/assets/a96de94f-50ba-47ef-b412-5143c8b5b269" />


---

## 10. Reproductibilité

```powershell
# 1. Lancer la base et le monitoring
docker compose up -d postgres
docker compose -f docker-compose.monitoring.yml up -d

# 2. Démarrer une variante (ex. Jersey)
docker compose up -d variant-a

# 3. Exécuter un scénario JMeter
& "$Env:JMETER_HOME/bin/ApacheJMeter.jar" `
  -n -t jmeter/scenarios/read-heavy.jmx `
  -JvariantHost=localhost -JvariantPort=8081 `
  -l results/variant-a-read-heavy.jtl
```

Les résultats se consultent ensuite via les dashboards ci-dessus ou directement dans les fichiers `.jtl` (CSV) de `results/`.







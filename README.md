# Media Store Datenbank-Praktikum

## Datenbank starten

```bash
docker compose up -d db adminer
```

Adminer ist danach unter http://localhost:8080 erreichbar.

- DB: `Postgres`
- Server: `db`
- Username: `postgres`
- Password: `test`
- Database: `postgres`

## Schema und Daten laden

Der Loader baut das Schema neu auf, liest die XML-/CSV-Dateien aus `data/` und schreibt abgelehnte Datensätze nach `build/rejected-records.csv` sowie in die Tabelle `"Ladefehler"` (Doppelt hält besser)

```bash
docker compose --profile tools run --rm loader
```

Alternativ lokal mit Maven:

```bash
mvn -q -DskipTests compile exec:java \
  -Dexec.args="--schema src/dbs-schema.sql --data-dir data --rejects build/rejected-records.csv"
```

Ein Skript um schnell den Container runterzufahren, neu hochzufahren und dann Daten zu laden:

```bash
./scripts/reset-and-load.sh
```

Die Datenbankverbindung kann über Umgebungsvariablen gesetzt werden:

- `DB_URL`, Default `jdbc:postgresql://localhost:5432/postgres`
- `DB_USER`, Default `postgres`
- `DB_PASSWORD`, Default `test`

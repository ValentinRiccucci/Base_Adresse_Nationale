# BAN — Import & API de la Base Adresse Nationale

Projet de formation **Spring Batch** : il importe un fichier CSV d'adresses (Base Adresse
Nationale, département 79 – Deux-Sèvres), nettoie et déduplique les données, les stocke dans
une base **SQLite**, puis les expose via une **API REST paginée** documentée avec Swagger.
Des métriques d'exécution sont remontées via **Micrometer / Spring Actuator**.

---

## Sommaire

- [Fonctionnalités](#fonctionnalités)
- [Stack technique](#stack-technique)
- [Architecture & flux](#architecture--flux)
- [Structure du projet](#structure-du-projet)
- [Modèle de données](#modèle-de-données)
- [Le batch d'import](#le-batch-dimport)
- [L'API REST](#lapi-rest)
- [Configuration](#configuration)
- [Lancement](#lancement)
- [Supervision & métriques](#supervision--métriques)

---

## Fonctionnalités

- **Import CSV en masse** avec Spring Batch (lecture par *chunks* de 4000 lignes).
- **Nettoyage & validation** : rejet des identifiants au mauvais format (regex), gestion des
  doublons, filtre optionnel par code postal.
- **Upsert** en base : insertion ou mise à jour (`ON CONFLICT ... DO UPDATE`) selon l'`id`.
- **Purge** des enregistrements absents du dernier import (synchronisation).
- **API REST paginée** pour consulter les adresses (par code postal, voie, commune).
- **Documentation interactive** via Swagger UI.
- **Métriques métier** (lignes traitées, doublons, lignes filtrées) et **timing** par étape.

---

## Stack technique

| Composant | Version / détail |
|-----------|------------------|
| Java | 25 |
| Spring Boot | 4.0.6 |
| Spring Batch | Import par lots |
| Spring Data JPA / Hibernate | Persistance (dialecte SQLite communautaire) |
| Spring Web MVC | API REST |
| SQLite (`sqlite-jdbc`) | Base de données locale (`ban.db`) |
| springdoc-openapi | Swagger UI |
| Micrometer + Actuator | Métriques & health |
| Lombok | Réduction du boilerplate |
| Maven | Build (`mvnw` inclus) |

---

## Architecture & flux

```
adresses-79.csv
      │
      ▼
┌─────────────┐   ┌──────────────────┐   ┌───────────────────┐
│  csvReader  │──▶│  FranceProcessor │──▶│  jdbcWriter       │
│ (FlatFile)  │   │  (validation,    │   │  (INSERT/UPSERT   │
│             │   │   dédup, filtre) │   │   SQLite)         │
└─────────────┘   └──────────────────┘   └───────────────────┘
   FranceDTO           FranceDTO → France        table `france`
                                                       │
                                          afterJob : purge des ID
                                          absents du dernier import
                                                       │
                                                       ▼
                          ┌──────────────────────────────────┐
                          │  ApiController → ApiService       │
                          │  → FranceRepository (JPA)         │
                          │  Pagination + Swagger UI          │
                          └──────────────────────────────────┘
```

Le job Spring Batch (`importFranceJob`) suit le pattern classique **Reader → Processor → Writer** :

1. **Reader** — lit `adresses-79.csv` (séparateur `;`, 23 colonnes) et mappe chaque ligne vers un `FranceDTO`.
2. **Processor** — valide, déduplique et filtre chaque enregistrement ; convertit le `FranceDTO` en entité `France` (ou `null` pour ignorer la ligne).
3. **Writer** — insère/met à jour les lignes en base via SQL natif (`JdbcBatchItemWriter`, upsert).
4. **afterJob** — supprime de la base les identifiants qui n'étaient pas présents dans l'import courant (synchronisation).

---

## Structure du projet

```
src/main/java/com/natsystem/BAN/
├── BanApplication.java              # Point d'entrée ; lance le job au démarrage (CommandLineRunner)
├── configuration/
│   ├── FranceBatchConfig.java       # Reader / Writer / Step / Job de l'import
│   └── HelloWorldBatchConfig.java   # Exemple de job "Hello World" (démo Spring Batch)
├── controller/
│   └── ApiController.java           # Endpoints REST /79/*
├── services/
│   └── ApiService.java              # Logique de recherche paginée + purge
├── repository/
│   └── FranceRepository.java        # JpaRepository + requêtes natives
├── model/
│   └── France.java                  # Entité JPA (table `france`)
├── dto/
│   └── FranceDTO.java               # Record exposé/lu (mapping CSV & API)
├── processor/
│   └── FranceProcessor.java         # Validation, déduplication, filtre code postal
└── Listener/
    ├── JobProgressListener.java     # Logs & métriques de début/fin de job + purge
    └── StepTimingListener.java      # Logs de timing et compteurs par step

src/main/resources/
├── adresses-79.csv                  # Données source (BAN, dép. 79)
└── application.properties           # Configuration
```

---

## Modèle de données

L'entité `France` (table `france`) reprend les colonnes du fichier BAN :

`id` (clé primaire), `id_fantoir`, `numero`, `rep`, `nom_voie`, `code_postal`, `code_insee`,
`nom_commune`, `code_insee_ancienne_commune`, `nom_ancienne_commune`, `x`, `y`, `lon`, `lat`,
`type_position`, `alias`, `nom_ld`, `libelle_acheminement`, `nom_afnor`, `source_position`,
`source_nom_voie`, `certification_commune`, `cad_parcelles`.

> Le `FranceDTO` est un `record` immuable utilisé à la fois pour le mapping du CSV et pour la
> réponse de l'API.

---

## Le batch d'import

### Règles appliquées par le `FranceProcessor`

- **Doublon en mémoire** : si l'`id` a déjà été traité pendant ce run → ligne ignorée.
- **Mise à jour** : si l'`id` existe déjà en base mais que le contenu diffère → conservé (upsert).
- **Format d'`id`** : validation par regex `^\d{5}_[A-Za-z0-9]{1,9}_\d{5}(?:_...)?$` → rejet sinon.
- **Filtre code postal** (optionnel) : si `CodePostalFilter = 1`, seules les lignes du
  `code_Postal` demandé sont conservées.

### Paramètres du job

Passés au démarrage dans `BanApplication.run(...)` :

| Paramètre | Rôle | Valeur par défaut |
|-----------|------|-------------------|
| `code_Postal` | Code postal à conserver si le filtre est actif | `79400` |
| `CodePostalFilter` | Active (`1`) / désactive (`0`) le filtre | `0` (désactivé) |
| `startAt` | Horodatage pour rendre l'instance de job unique | `System.currentTimeMillis()` |

### Caractéristiques techniques

- **Chunk** de 4000 lignes.
- **Tolérance aux pannes** : `skip(FlatFileParseException)` avec `skipLimit(1000)`.
- Écriture via **SQL natif** (upsert `ON CONFLICT`) plutôt que via JPA, pour la performance.
- L'exécution parallèle (`taskExecutor`) est désactivée : SQLite verrouille la base
  (« database is locked ») en écriture concurrente.

---

## L'API REST

Base : `http://localhost:8081` — préfixe de ressource : `/79`

| Méthode | Endpoint | Paramètres | Description |
|---------|----------|------------|-------------|
| GET | `/79/page` | `page`, `size` (défaut 20) | Toutes les adresses, paginées |
| GET | `/79/page/cp` | `cp` (Integer) + pagination | Recherche par code postal |
| GET | `/79/page/voie` | `voie` (String) + pagination | Recherche par nom de voie (insensible à la casse) |
| GET | `/79/page/commune` | `commune` (String) + pagination | Recherche par nom de commune (insensible à la casse) |

Toutes les réponses sont des `Page<FranceDTO>` (contenu + métadonnées de pagination).

**Swagger UI** : http://localhost:8081/swagger-ui/index.html

---

## Configuration

`src/main/resources/application.properties` (extraits) :

```properties
# Base SQLite locale
spring.datasource.url=jdbc:sqlite:ban.db
spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect
spring.jpa.hibernate.ddl-auto=update

# Batch : pas d'exécution auto au boot (piloté par le CommandLineRunner)
spring.batch.job.enabled=false
spring.batch.jdbc.initialize-schema=always

# Actuator
management.endpoints.web.exposure.include=health,metrics

# Port HTTP
server.port=8081
```

---

## Lancement

Aucun paramètre n'est nécessaire : le job d'import se déclenche automatiquement au démarrage
(via le `CommandLineRunner` de `BanApplication`), puis l'API reste disponible.

```bash
# Windows
mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

Ou packagé :

```bash
mvnw.cmd clean package
java -jar target/BAN-0.0.1-SNAPSHOT.jar
```

**Pour filtrer par code postal**, modifier les paramètres du job dans `BanApplication.java` :
mettre `CodePostalFilter` à `1` et ajuster `code_Postal`.

Une fois démarré :
- API / Swagger : http://localhost:8081/swagger-ui/index.html
- Base générée : `ban.db` (à la racine du projet)

---

## Supervision & métriques

**Actuator** :
- Santé : http://localhost:8081/actuator/health
- Métriques : http://localhost:8081/actuator/metrics

**Métriques métier** (Micrometer, loggées en fin de job) :

| Métrique | Signification |
|----------|---------------|
| `ban.france.lignes` | Nombre de lignes lues |
| `ban.france.duplicates` | Nombre de doublons détectés |
| `ban.france.invalid_postal` | Lignes ignorées par le filtre code postal |

Les *listeners* (`JobProgressListener`, `StepTimingListener`) tracent le début/fin de job et de
step, la durée d'exécution et les compteurs (lus / écrits / ignorés / filtrés / commits).

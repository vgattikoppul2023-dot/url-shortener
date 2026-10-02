# Distributed URL Shortener & Analytics Platform

A scalable URL-shortening service built with **Java 17 / Spring Boot 3**, using **Redis** for caching, **Kafka** for real-time click streaming, and **PostgreSQL** for durable storage. Runs locally with **Docker Compose** and ships with **Kubernetes** manifests for horizontal scaling.

## Architecture

```
            POST /api/urls                     GET /{code}
 client ───────────────────┐        client ─────────────────┐
                           ▼                                ▼
                    ┌──────────────┐   cache hit    ┌──────────────┐
                    │ Spring Boot  │◄──────────────►│    Redis     │
                    │   service    │                └──────────────┘
                    └──────┬───────┘
          save + base62    │   publish click (async, keyed by code)
                           ▼                  ▼
                  ┌──────────────┐     ┌──────────────┐  consume  ┌──────────────┐
                  │  PostgreSQL  │◄────│ Kafka topic  │──────────►│  analytics   │
                  │ urls, clicks │     │  url-clicks  │           │   consumer   │
                  └──────────────┘     └──────────────┘           └──────────────┘
```

## Key design decisions

- **Base62 encoding of database IDs** — each URL gets a unique 7+ character code derived from its auto-increment ID, so there are no collisions and no retry loops.
- **Redis read-through cache** — redirects are served from Redis after the first lookup (24h TTL), keeping hot links off the database.
- **Async analytics via Kafka** — the redirect returns immediately; click events are published to Kafka (keyed by short code for per-link ordering) and persisted by a separate consumer.
- **Stateless service** — any number of replicas can run behind a load balancer; the Kubernetes HPA scales from 3 to 10 pods on CPU.

## API

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/urls` | Create a short URL. Body: `{"url": "https://example.com"}` |
| `GET` | `/{code}` | Redirect (302) to the original URL and record a click |
| `GET` | `/api/urls/{code}/stats` | Total clicks and the 10 most recent clicks |
| `GET` | `/actuator/health` | Health check |

## Run locally

Requires Docker.

```bash
docker compose up --build
```

Then:

```bash
# Create a short link
curl -X POST localhost:8080/api/urls -H "Content-Type: application/json" \
     -d '{"url":"https://github.com"}'

# Follow it
curl -i localhost:8080/<code>

# See analytics
curl localhost:8080/api/urls/<code>/stats
```

## Tests

```bash
mvn test
```

## Load testing

Uses [k6](https://k6.io):

```bash
k6 run -e CODE=<code> loadtest/redirect-test.js
```

## Deploy to Kubernetes

```bash
docker build -t url-shortener:1.0.0 .
kubectl apply -f k8s/deployment.yaml
```

## Project structure

```
src/main/java/com/vamshi/urlshortener
├── util/Base62.java            # ID <-> short code encoding
├── service/UrlService.java     # shorten + cached resolve
├── kafka/                      # click event producer + consumer
├── model/                      # JPA entities (ShortUrl, ClickEvent)
├── repository/                 # Spring Data repositories
└── web/UrlController.java      # REST endpoints
```

# E-Commerce Management System (Spring Boot)

Java 17 · Spring Boot 3.3 · Spring Data JPA · Hibernate · PostgreSQL · Maven

## Configuration (environment variables)
| Variable | Description |
|---|---|
| `DATABASE_URL` | `postgres://user:password@host:5432/dbname` (Render/Neon/Railway format) |
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | Alternative to `DATABASE_URL` (JDBC URL + credentials) |
| `CORS_ORIGINS` | Optional, comma-separated frontend URLs (default: all) |
| `PORT` | Injected by the hosting platform |

## Deploy on Render
1. Push this repo to GitHub.
2. Render -> New -> **Blueprint** -> select the repo (uses `render.yaml`: creates the web service and a PostgreSQL database and wires `DATABASE_URL`).
   Or: New -> Web Service (Docker) + a PostgreSQL database, then set `DATABASE_URL` yourself.
3. Wait for the build. Health check: `GET /health`.

## Main endpoints
`/auth/register`, `/auth/login`, `/users`, `/addresses`, `/payments`, `/products`, `/products/category/{id}`, `/categories`, `/orders`, `/tracking`, `/cart/*`, `/checkout`, `/admin/*`

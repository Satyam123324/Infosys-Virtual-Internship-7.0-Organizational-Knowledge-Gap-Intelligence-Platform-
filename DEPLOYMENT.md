# Deployment Guide — Organizational Knowledge Gap Intelligence Platform

The platform ships as three containers — **PostgreSQL**, the **Spring Boot backend**, and the **React frontend** (served by nginx) — wired together with Docker Compose.

## Prerequisites
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (includes Docker Compose)
- The two project folders sitting **side by side**:
  ```
  Desktop/
  ├── knowledge-gap-platform/     (backend — run compose from here)
  └── knowledge-gap-frontend/     (frontend)
  ```

## Run everything with one command
From inside the **`knowledge-gap-platform`** folder:

```bash
docker compose up --build
```

First run takes a few minutes (it compiles the Maven jar and builds the Vite bundle). When it's ready:

- **Frontend:** http://localhost:5173
- **Backend API / Swagger:** http://localhost:8080/swagger-ui.html
- **PostgreSQL:** localhost:5432 (db `knowledge_gap_db`, user/pass `postgres`)

Stop it with `Ctrl+C`, or fully tear down (including the database volume):

```bash
docker compose down -v
```

## What each piece does
| Service   | Image / build                     | Port  | Notes |
|-----------|-----------------------------------|-------|-------|
| `db`      | `postgres:16-alpine`              | 5432  | Data persists in the `pgdata` volume |
| `backend` | built from `./Dockerfile`         | 8080  | Spring Boot jar on a JRE image; connects to `db` |
| `frontend`| built from `../knowledge-gap-frontend/Dockerfile` | 5173→80 | Static bundle served by nginx |

The backend reads its database URL from `SPRING_DATASOURCE_URL` (set to `jdbc:postgresql://db:5432/...` in compose), so no code changes are needed between local and container runs.

## Optional integrations
Email (OTP), Google/GitHub OAuth, and the AI recommendation engine work once their
credentials are provided. Add them as environment variables under the `backend`
service in `docker-compose.yml` (e.g. `MAIL_USERNAME`, `MAIL_PASSWORD`,
`GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `GITHUB_CLIENT_ID`, `GITHUB_CLIENT_SECRET`,
`GEMINI_API_KEY`). They are optional — the app boots and runs without them.

## Building images individually
```bash
# Backend
docker build -t knowledge-gap-backend ./knowledge-gap-platform

# Frontend
docker build -t knowledge-gap-frontend ./knowledge-gap-frontend
```

## Deploying to the cloud
Push the two images to a registry (Docker Hub, GHCR, AWS ECR) and run them on any
container host (AWS ECS, Azure Container Apps, Render, Railway, a VM with Docker).
Point `SPRING_DATASOURCE_URL` at your managed PostgreSQL and set the frontend's API
base URL to your backend's public URL.

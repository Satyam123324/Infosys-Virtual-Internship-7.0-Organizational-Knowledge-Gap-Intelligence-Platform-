# Organizational Knowledge Gap Intelligence Platform

A full-stack platform that identifies expertise gaps across an organization, benchmarks employees against role-specific competency frameworks, and drives targeted learning through AI-generated recommendations, mentorship, and analytics dashboards.

**Backend** (this repo): Java 17 · Spring Boot 3.3 · Spring Security · JWT + OAuth2 · Spring Data JPA · PostgreSQL
**Frontend** ([knowledge-gap-frontend](https://github.com/Satyam123324/Infosys-Virtual-Internship-7.0-Organizational-Knowledge-Gap-Intelligence-Platform-Frontend)): React 18 · Vite · React Router · Axios · Recharts

---

## Features (12 modules)

1. **Authentication & RBAC** — JWT, OAuth2 (Google + GitHub), password-reset OTP, 6 roles
2. **Employee Profile & Skill Inventory** — profiles, skill self/peer rating, certifications, work experience
3. **Competency Framework & Role Benchmarking** — required skill levels per role/department
4. **Knowledge Gap Analysis** — individual/team/department gaps, severity scoring, **heatmap**, **trend-over-time**
5. **Training Recommendation** — AI-generated learning paths (OpenAI / Gemini) with external resource links
6. **Knowledge-Sharing & Mentorship** — expert directory, session booking, knowledge-article library
7. **Learning Progress Tracking** — enrollments, milestones, certification-expiry reminders, learning-velocity analytics
8. **Assessment & Survey** — skill quizzes, 360° self/peer/manager reviews, historical comparison
9. **Notifications** — gap alerts, deadline & session reminders, milestone achievements (email + in-app + daily scheduler)
10. **Analytics Dashboards** — Employee, Manager, and HR/Admin dashboards
11. **Reports & Export** — individual gap (PDF), department / workforce / training-effectiveness / ROI / strategic (Excel)
12. **Integration, Testing & Deployment** — Dockerized, tested, one-command run

---

## Tech Stack

| Area | Technology |
|------|------------|
| Language / Framework | Java 17, Spring Boot 3.3 |
| Security | Spring Security, JWT (jjwt 0.12), OAuth2 (Google, GitHub) |
| Data | Spring Data JPA, Hibernate, PostgreSQL (prod), H2 (dev) |
| AI / Recommendations | OpenAI / Google Gemini |
| Reports | OpenPDF (PDF), Apache POI (Excel) |
| Email | Spring Mail (JavaMailSender) |
| Build | Maven |
| Testing | JUnit 5, Mockito, Postman |
| Deployment | Docker, Docker Compose, Nginx |

---

## Quick Start

### Option A — Docker (runs everything: DB + backend + frontend)
Requires Docker Desktop, with the frontend repo cloned next to this one (`../knowledge-gap-frontend`).
```bash
docker compose up --build
```
- Frontend → http://localhost:5173
- Backend / Swagger → http://localhost:8080/swagger-ui.html

See [DEPLOYMENT.md](./DEPLOYMENT.md) for details and cloud steps.

### Option B — Local dev (H2 in-memory, no PostgreSQL, no Docker)
```bash
./run-dev.bat          # Windows — forces JDK 17 and the dev profile
# or:
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Option C — Local production (PostgreSQL)
1. Create a database `knowledge_gap_db`
2. Copy `.env.example` → `.env` and fill in the values
3. `./run-prod.bat` (or `mvn spring-boot:run`)

---

## Demo Accounts (seeded on first run)

| Role | Email | Password |
|------|-------|----------|
| System Administrator | `admin@demo.local` | `Admin@1234` |
| Employees (6, various departments) | e.g. `aarav.mehta@demo.local` | `Demo@1234` |

Roles, departments, skills, competency frameworks, assessment questions, and demo gap-trend history are all seeded automatically.

---

## Roles

`EMPLOYEE` · `TEAM_LEAD_MANAGER` · `HR_SPECIALIST` · `DEPARTMENT_HEAD` · `LEARNING_DEVELOPMENT_ADMIN` · `SYSTEM_ADMINISTRATOR`

Self-registration grants `EMPLOYEE`. Elevated roles are assigned by an admin via `/api/v1/admin/users/{id}/roles`.

---

## API

Interactive docs (Swagger UI): **http://localhost:8080/swagger-ui.html**

A ready-to-use **Postman collection** is included: [`postman_collection.json`](./postman_collection.json). Import it, run **Login** first (it captures the JWT), then any other request.

Key endpoint groups (base path `/api/v1`): `auth`, `users`, `admin`, `employee-profile`, `skills`, `competency-frameworks`, `gap-analysis`, `assessments`, `skill-reviews`, `peer-assessments`, `mentorship`, `training/enrollments`, `knowledge-articles`, `notifications`, `reports`.

---

## Testing

```bash
mvn test
```
Unit tests use **JUnit 5 + Mockito** (auth, skill-review authorization, training ownership, knowledge-article permissions). API testing is supported via the included Postman collection and Swagger UI. The frontend uses **React Testing Library** (Vitest) — see the frontend repo.

---

## Project Structure

```
src/main/java/com/infosys/knowledgegap/
├── config/         Security, OpenAPI, WebMvc, DataSeeder
├── controller/     REST controllers (one per module)
├── service/impl/   Business logic
├── repository/     Spring Data JPA repositories
├── entity/         JPA entities
├── dto/            Request/response models
├── enums/          Roles, proficiency levels, etc.
├── security/       JWT filter, OAuth2 handlers, user details
└── scheduler/      Daily notification + gap-snapshot jobs
```

---

## Environment Variables

Copy `.env.example` → `.env`. Key values: `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `MAIL_USERNAME`/`MAIL_PASSWORD` (Gmail app password for OTP email), `GOOGLE_CLIENT_ID`/`SECRET`, `GITHUB_CLIENT_ID`/`SECRET`, `GEMINI_API_KEY`. All integrations are optional — the app boots without them.

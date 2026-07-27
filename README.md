# Knowledge Gap Intelligence Platform — Frontend

React + Vite single-page app for the **Organizational Knowledge Gap Intelligence Platform**. It talks to the Spring Boot backend ([knowledge-gap-platform](https://github.com/Satyam123324/Infosys-Virtual-Internship-7.0-Organizational-Knowledge-Gap-Intelligence-Platform-)) over a JWT-authenticated REST API.

**Stack:** React 18 · Vite · React Router · Axios · Recharts · Context API · lucide-react

---

## Quick Start

```bash
npm install
npm run dev
```
Opens at **http://localhost:5173**. The backend must be running on port **8080** first (`./run-dev.bat` in the backend repo, or `docker compose up` from the backend folder to run everything together).

---

## Features by role

**Every employee**
- Dashboard, My Profile, My Skills
- Assessment Test + **Assessment History** (score trend & comparison)
- My Gap Analysis, Notification Center
- Mentorship, Skill Reviews (360°), Certifications
- Training & Learning + **Learning Analytics** (velocity, milestones)
- Knowledge Library

**Admin / HR (extra)**
- **Admin Dashboard** — org-wide KPIs, user management, report exports
- **Org Gap Dashboard** — department readiness, **skill-gap heatmap**, **gap trend over time**
- Employee Profiles, Competency Frameworks, User Management Console

---

## Testing

```bash
npm test
```
Uses **Vitest + React Testing Library** (`src/**/*.test.{js,jsx}`) — unit tests for utilities and a component render test for the gap heatmap.

---

## Build & Deploy

```bash
npm run build      # outputs static bundle to dist/
```
A `Dockerfile` (Node build → Nginx) is included; the backend repo's `docker-compose.yml` builds and serves this app alongside the API and database. See the backend `DEPLOYMENT.md`.

---

## Project Structure

```
src/
├── api/          Axios API modules (auth token interceptor + refresh)
├── components/   Layout, Sidebar, dashboards, charts (GapHeatmap, ...)
├── context/      AuthContext (JWT + user state)
├── pages/        One component per route
├── utils/        Pure helpers (certification status, notification meta)
└── test/         Vitest setup
```

Configuration lives in `src/api/axios.js` (base URL `http://localhost:8080/api/v1`).

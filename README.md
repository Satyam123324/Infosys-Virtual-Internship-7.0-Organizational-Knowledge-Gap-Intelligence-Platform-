# Organizational Knowledge Gap Intelligence Platform
## Module 1 — User Authentication & Role-Based Access Control

### Tech Stack
- **Backend**: Java 17 + Spring Boot 3.3
- **Security**: Spring Security + JWT (jjwt 0.12) + OAuth2 (Google)
- **Database**: PostgreSQL (prod) / H2 (dev)
- **Build**: Maven

---

### How to Run

#### Option A — Dev mode (H2 in-memory, no PostgreSQL needed)
```bash
cd knowledge-gap-platform
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

#### Option B — Production (PostgreSQL)
1. Create a PostgreSQL database named `knowledge_gap_db`
2. Copy `.env.example` → `.env` and fill in all values
3. Run:
```bash
mvn spring-boot:run
```

---

### API Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/v1/auth/register` | ❌ | Register new employee |
| POST | `/api/v1/auth/login` | ❌ | Login, get JWT tokens |
| POST | `/api/v1/auth/refresh-token` | ❌ | Refresh access token |
| POST | `/api/v1/auth/logout` | ✅ | Revoke refresh token |
| POST | `/api/v1/auth/forgot-password` | ❌ | Request reset email |
| POST | `/api/v1/auth/reset-password` | ❌ | Reset with token |
| POST | `/api/v1/auth/change-password` | ✅ | Change while logged in |
| GET  | `/api/v1/users/me` | ✅ | Get my profile |
| PUT  | `/api/v1/users/me` | ✅ | Update my profile |
| GET  | `/api/v1/admin/users` | ✅ ADMIN | List all users |
| PUT  | `/api/v1/admin/users/{id}/roles` | ✅ ADMIN | Assign roles |
| PATCH| `/api/v1/admin/users/{id}/toggle` | ✅ ADMIN | Enable/disable user |
| DELETE | `/api/v1/admin/users/{id}` | ✅ ADMIN | Delete user |

**Swagger UI**: http://localhost:8080/swagger-ui.html

---

### Roles
| Role | Description |
|------|-------------|
| `EMPLOYEE` | Default self-registration role |
| `TEAM_LEAD_MANAGER` | Team-level gap visibility |
| `HR_SPECIALIST` | Workforce skill management |
| `DEPARTMENT_HEAD` | Department analytics access |
| `LEARNING_DEVELOPMENT_ADMIN` | Training catalog management |
| `SYSTEM_ADMINISTRATOR` | Full platform access |

> Roles are seeded automatically on startup. Self-registration only grants EMPLOYEE. Elevated roles must be assigned by a System Administrator via `/api/v1/admin/users/{id}/roles`.

---

### Running Tests
```bash
mvn test
```

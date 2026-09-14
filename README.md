# Vlog

A small vlog application. Users write dated posts; anyone can browse and filter
them by author, keyword, or date range. Users come in two roles — `USER` and
`MANAGER` — where managers can moderate anyone's posts.

- **Backend** — Spring Boot 4.1, Java 21, MySQL, JWT authentication
- **Frontend** — Angular 22, standalone components, signals

## Prerequisites

| Tool | Version |
|---|---|
| JDK | 21 |
| Node.js | 20+ |
| MySQL | 8.x |
| Angular CLI | 22 (`npm i -g @angular/cli`) |

Gradle isn't needed — the wrapper (`./gradlew`) handles it.

## Setup

### 1. Database

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS vlog;"
```

Tables are created automatically on first run (`ddl-auto: update`).

### 2. Backend secrets

Create `backend/src/main/resources/application-local.properties`. This file is
gitignored and holds the values that must not be committed:

```properties
spring.datasource.password=your_mysql_password
app.jwt.secret=your_generated_secret
```

Generate a secret — HS256 needs at least 256 bits, so don't shorten it:

```bash
openssl rand -base64 48
```

The application fails to start if the JWT secret is blank. That's deliberate.

### 3. Run both servers

Two terminals.

```bash
cd backend
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

```bash
cd frontend
npm install
ng serve
```

Open <http://localhost:4200>. The API is on port 8080 but you never visit it
directly once the UI is running.

## Configuration

All backend settings read from environment variables with local defaults, so
nothing sensitive lives in the committed properties file.

| Variable | Default | Purpose |
|---|---|---|
| `DB_HOST` | `localhost` | MySQL host |
| `DB_NAME` | `vlog` | Database name |
| `DB_USER` | `root` | MySQL user |
| `DB_PASSWORD` | *(empty)* | MySQL password |
| `JWT_SECRET` | *(empty)* | HMAC signing key, 32+ bytes |
| `JWT_EXPIRATION_MINUTES` | `60` | Token lifetime |
| `CORS_ORIGIN` | `http://localhost:4200` | Allowed browser origin |

## API

Base path `/api`. Tokens go in an `Authorization: Bearer <token>` header.

### Auth

| Method | Path | Access | Notes |
|---|---|---|---|
| POST | `/auth/signup` | public | Always creates a `USER`; returns a token |
| POST | `/auth/login` | public | Returns a token |
| GET | `/auth/me` | authenticated | Current user |

### Posts

| Method | Path | Access |
|---|---|---|
| GET | `/posts` | public — supports filters and paging |
| GET | `/posts/{id}` | public |
| POST | `/posts` | authenticated — author taken from the token |
| PUT | `/posts/{id}` | author or manager |
| DELETE | `/posts/{id}` | author or manager |

Filter parameters, all optional and combinable:

```
?username=bo          partial, case-insensitive match on the author
?keyword=hiking       partial match against title OR body
?date=2026-09-13      a single day
?from=2026-09-01&to=2026-09-30   a range (ignored if `date` is set)
?page=0&size=20&sort=postDate,desc
```

Combinations are composed at query time with JPA Specifications, so one endpoint
covers every permutation.

### Users

| Method | Path | Access |
|---|---|---|
| GET | `/users` | manager |
| GET | `/users/{id}` | manager |
| GET | `/users/by-username/{username}` | manager |
| POST | `/users` | manager — can set any role |
| DELETE | `/users/{id}` | manager |

### Errors

Failures return RFC 9457 `application/problem+json`. Validation failures add an
`errors` object mapping field names to messages:

```json
{
  "type": "urn:vlog:validation-error",
  "title": "Validation failed",
  "status": 400,
  "detail": "One or more fields are invalid",
  "errors": { "title": "Title is required" }
}
```

## Creating the first manager

Signup always produces a `USER`, and only a manager can create another manager,
so the first one is promoted directly:

```bash
mysql -u root -p vlog -e "UPDATE users SET role='MANAGER' WHERE username='yourname';"
```

Log out and back in afterwards — the role is baked into the token at issue time,
so an existing token keeps the old role until it expires.

## Project layout

Both halves are organised by feature rather than by technical layer, so
everything for a feature sits in one folder.

```
backend/src/main/java/com/vlog/vlog/
├── auth/      JwtService, filter, UserDetailsService, AuthController
├── user/      entity, repository, service, controller, DTOs
├── post/      entity, repository, specifications, service, controller, DTOs
└── common/    config, error handling, shared exceptions

frontend/src/app/
├── auth/      service, interceptor, guards, login, signup
├── post/      service, model, post-list, post-form
└── core/      API base URL
```

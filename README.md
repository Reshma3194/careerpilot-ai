# CareerPilot AI

Upload your resume, track the jobs you apply to, see how well your resume matches each one, and practise interview questions with AI feedback.

- **Backend:** Spring Boot 3.3 (Java 17+), Spring Security with JWT, JPA, PostgreSQL, Gemini API
- **Frontend:** React 18 + Vite, React Router, Axios

## Run it locally (two terminals)

You need Java 17+, Maven 3.9+ and Node 20+. The `dev` profile uses an in-memory database, so no PostgreSQL is needed.

**1. Backend**

```bash
cd backend
export GEMINI_API_KEY=your-key          # optional: only the AI features need it
mvn test                                 # run the tests
mvn spring-boot:run -Dspring-boot.run.profiles=dev
# check: curl http://localhost:8080/actuator/health
```

On Windows PowerShell use `$env:GEMINI_API_KEY="your-key"`.

**2. Frontend**

```bash
cd frontend
npm install
npm run dev                              # http://localhost:5173
```

Vite proxies `/api` to `http://localhost:8080`.

## Run with Docker (PostgreSQL + backend + frontend)

```bash
cp .env.example .env                     # set JWT_SECRET and GEMINI_API_KEY
docker compose up --build
# app: http://localhost:3000   API: http://localhost:8080
```

## Configuration

| Variable | Purpose | Default |
|---|---|---|
| `JWT_SECRET` | Token signing key, at least 32 characters. **Required** outside the `dev` profile | none |
| `GEMINI_API_KEY` | Enables resume analysis and interview prep | none (AI endpoints return 503) |
| `GEMINI_MODEL` | Gemini model name | `gemini-2.5-flash` |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | PostgreSQL connection | local `careerpilot` database |
| `CORS_ALLOWED_ORIGINS` | Comma separated allowed origins | `http://localhost:5173` |

## Project layout

See `docs/api-documentation.md` for every endpoint (with curl examples) and `docs/database-schema.md` for the tables.

```
backend/   Spring Boot API   (controller, service, repository, entity, dto, security, ai, config)
frontend/  React app         (pages, components, services, context, utils)
docs/      API and schema docs
```

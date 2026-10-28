# CareerPilot AI: API reference

Base URL: `http://localhost:8080/api` (the frontend calls the same-origin `/api`, proxied to the backend).

Every response uses one envelope:

```json
{ "success": true, "message": "OK", "data": { } }
```

Errors return `success: false`, a human-readable `message` and `data: null`.

| Status | Meaning |
|---|---|
| 400 | Validation failed or bad input |
| 401 | Missing/invalid token, or wrong email/password |
| 404 | Not found (also returned for other users' records) |
| 409 | Duplicate (email already registered, job already tracked) |
| 413 | Resume larger than 5 MB |
| 502 | The AI returned something unusable or Gemini failed |
| 503 | `GEMINI_API_KEY` is not configured |

Everything except `/auth/register` and `/auth/login` needs `Authorization: Bearer <token>`.

## Auth

| Method | Path | Body | Notes |
|---|---|---|---|
| POST | `/auth/register` | `{ fullName, email, password }` | Password 8 to 72 chars. Returns `{ token, email, fullName }` (201) |
| POST | `/auth/login` | `{ email, password }` | Returns `{ token, email, fullName }` |
| GET | `/auth/me` | none | Current user profile |

## Resumes

| Method | Path | Notes |
|---|---|---|
| POST | `/resumes` | `multipart/form-data`, field `file` (PDF, DOCX or TXT, max 5 MB). Text is extracted and stored |
| GET | `/resumes` | Newest first. Includes `textPreview` and `characterCount`, never the full text |
| GET | `/resumes/{id}` | One resume |
| DELETE | `/resumes/{id}` | |

## Jobs

| Method | Path | Body |
|---|---|---|
| POST | `/jobs` | `{ title*, company*, location, description, jobUrl }` |
| GET | `/jobs` | |
| GET | `/jobs/{id}` | |
| PUT | `/jobs/{id}` | same as create |
| DELETE | `/jobs/{id}` | Also removes its application |

## Applications (job tracker)

Statuses: `SAVED`, `APPLIED`, `INTERVIEWING`, `OFFER`, `REJECTED`, `WITHDRAWN`.

| Method | Path | Body / params |
|---|---|---|
| POST | `/applications` | `{ jobId*, status, appliedDate, notes }`. One application per job (409 otherwise) |
| GET | `/applications` | optional `?status=APPLIED` |
| GET | `/applications/stats` | `{ SAVED: n, ..., TOTAL: n }` |
| GET | `/applications/{id}` | |
| PATCH | `/applications/{id}` | `{ status, appliedDate, notes }`, any subset. Leaving `SAVED` sets `appliedDate` to today if empty |
| DELETE | `/applications/{id}` | |

## AI (needs `GEMINI_API_KEY`)

| Method | Path | Body | Returns |
|---|---|---|---|
| POST | `/ai/resume-analysis` | `{ resumeId?, jobId? , jobTitle?, jobDescription? }` | `{ matchScore, summary, strengths[], gaps[], missingKeywords[], suggestions[] }` |
| POST | `/ai/interview-questions` | `{ jobId*, resumeId?, count? }` (count 3 to 15, default 8) | `{ questions: [{ question, category }] }` |
| POST | `/ai/evaluate-answer` | `{ question*, answer*, jobId? }` | `{ score (0-10), feedback, strengths[], improvements[], sampleAnswer }` |

For resume analysis, give a `jobId` (a tracked job with a description) or paste a `jobDescription`. `resumeId` defaults to the latest upload. When a `jobId` is given and that job is tracked, its `matchScore` is saved on the application.

## Try it with curl

```bash
BASE=http://localhost:8080/api

# 1. register and keep the token
TOKEN=$(curl -s -X POST $BASE/auth/register -H 'Content-Type: application/json' \
  -d '{"fullName":"Test User","email":"test@example.com","password":"Password123"}' \
  | python3 -c 'import sys,json; print(json.load(sys.stdin)["data"]["token"])')

# 2. add a job and track it
curl -s -X POST $BASE/jobs -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"title":"Java Developer","company":"Acme","description":"Spring Boot, REST, SQL"}'
curl -s -X POST $BASE/applications -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"jobId":1}'

# 3. upload a resume and analyze it
curl -s -X POST $BASE/resumes -H "Authorization: Bearer $TOKEN" -F file=@resume.pdf
curl -s -X POST $BASE/ai/resume-analysis -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' -d '{"jobId":1}'
```

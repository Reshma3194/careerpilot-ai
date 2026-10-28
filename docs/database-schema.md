# Database schema

Tables are created automatically by Hibernate (`spring.jpa.hibernate.ddl-auto=update`). PostgreSQL is used by default; the `dev` profile uses in-memory H2.

```
users 1 ──< resumes
  │
  ├──< jobs 1 ──1 job_applications
  │                      ▲
  └──────────────────────┘ (job_applications also belong to a user)
```

## users
| Column | Type | Notes |
|---|---|---|
| id | bigint PK | |
| full_name | varchar(100) | |
| email | varchar | unique, stored lower-case |
| password | varchar | BCrypt hash |
| created_at | timestamp | |

## resumes
| Column | Type | Notes |
|---|---|---|
| id | bigint PK | |
| user_id | bigint FK users | |
| file_name | varchar | |
| content_type | varchar | |
| extracted_text | text | Plain text pulled from the PDF/DOCX/TXT |
| uploaded_at | timestamp | |

## jobs
| Column | Type | Notes |
|---|---|---|
| id | bigint PK | |
| user_id | bigint FK users | |
| title, company | varchar | required |
| location | varchar | |
| description | text | Used for resume matching and interview questions |
| job_url | varchar(1000) | |
| created_at | timestamp | |

## job_applications
| Column | Type | Notes |
|---|---|---|
| id | bigint PK | |
| user_id | bigint FK users | |
| job_id | bigint FK jobs | unique together with user_id |
| status | varchar(20) | SAVED, APPLIED, INTERVIEWING, OFFER, REJECTED, WITHDRAWN |
| applied_date | date | |
| notes | text | |
| match_score | integer | Latest AI match score (0 to 100) |
| created_at, updated_at | timestamp | |

All reads and writes are scoped to the logged-in user's email, so one user can never see another user's rows.

For a production deployment, replace `ddl-auto=update` with proper migrations (Flyway or Liquibase).

# Secondary Voltage Drop System

## 1. Overview

This repository is a full-stack system for secondary voltage drop inspection workflows.
It includes:

- device archive management
- inspection task submission and real-time pass/fail checks
- standard threshold configuration
- Excel preview/import for tasks and devices
- PDF certificate preview/download
- data correction and automatic re-validation
- operation logging and dashboard analytics

Frontend and backend are separated:

- frontend: `Vue 3 + Vite + Element Plus + ECharts`
- backend: `Spring Boot 3 + Spring Security + JWT + MyBatis + MySQL`

---

## 2. Role Model

- `role=0`: Admin
- `role=1`: Inspector(User)

### 2.1 Inspector

- manage devices (`/device/*`)
- create and submit inspection tasks (`/task/submit`)
- view task list/detail
- import tasks/devices from Excel
- preview/download PDF reports

### 2.2 Admin

- manage users (`/user/*`)
- configure test standards (`/standard`)
- audit operation logs (`/log/page`)
- correct result records (`/correct/result`)
- view dashboard stats (`/dashboard/stats`)
- view all tasks and reports

---

## 3. Tech Stack

### 3.1 Frontend

- `Vue 3.5`
- `Vite 7`
- `Vue Router 4`
- `Element Plus 2`
- `Axios`
- `ECharts 6`

Default API base URL in frontend code:

- `http://localhost:8080/api`
- can be overridden by `VITE_API_BASE_URL`

### 3.2 Backend

- `Java 17`
- `Spring Boot 3.3.4`
- `Spring Security` (JWT stateless auth)
- `MyBatis`
- `MySQL`
- `Apache POI` (Excel parsing)
- `iText7` (PDF generation)

Backend defaults:

- port: `8080`
- context path: `/api`
- JWT expiration: `7200` seconds

---

## 4. Repository Structure

```text
secondary-voltage-drop-system/
|-- backend/
|   |-- src/main/java/com/straykun/svd/svdsys/
|   |   |-- controller/
|   |   |-- service/
|   |   |-- security/
|   |   |-- mapper/
|   |   `-- domain/
|   `-- src/main/resources/
|       |-- application.yml
|       `-- mapper/*.xml
|-- frontend/
|   |-- src/api/
|   |-- src/views/
|   |-- src/router/
|   `-- src/layout/
`-- Readme.md
```

---

## 5. Prerequisites

- `JDK 17+`
- `Maven 3.8+` (or project wrapper)
- `Node.js 20.19+` (per `frontend/package.json`)
- `MySQL 8.x`

---

## 6. Database Setup

### 6.1 Database Name

Create database:

- `svd_sys`

### 6.2 Required Core Tables

Based on mapper XML and domain models, prepare at least:

- `sys_user`
- `sys_test_standard`
- `biz_device`
- `biz_test_task`
- `biz_test_result`
- `sys_log`

Note: this repository currently does not include DDL/seed SQL scripts.

### 6.3 Password Storage

- backend uses custom `SHA-256` password encoder (`Sha256PasswordEncoder`)
- default user password config key: `svd.user.default-password`
- current default password in config: `Cug@2025`

SHA-256 for `Cug@2025`:

- `bd45adf3d150e264931c0b1a2f3e55022d72e39115a322820e5eae2bc8599fe9`

You should seed at least one admin account (`role=0`, `status=1`) before login.

---

## 7. Configuration

Main backend config file:

- `backend/src/main/resources/application.yml`

Local-only override file (recommended for secrets):

- `backend/src/main/resources/application-local.yml` (git ignored)
- template: `backend/src/main/resources/application-local.example.yml`

Important keys:

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`
- `svd.user.default-password`
- `jwt.secret`
- `jwt.expire-seconds`

Frontend optional env file (`frontend/.env.*`):

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

### 7.1 Local Secret Override (Recommended)

1. Copy template:

```powershell
cd backend/src/main/resources
copy application-local.example.yml application-local.yml
```

2. Fill real local secrets only in `application-local.yml`.
3. Run backend with local profile:

```powershell
cd backend
$env:SPRING_PROFILES_ACTIVE='local'
.\mvnw.cmd spring-boot:run
```

`application-local.yml` is ignored by git and should never be committed.

---

## 8. Run the Project

### 8.1 Run Backend

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Backend URL:

- `http://localhost:8080/api`

### 8.2 Run Frontend

```powershell
cd frontend
npm install
npm run dev
```

Frontend URL (default Vite):

- `http://localhost:5173`

---

## 9. API Summary

Response envelope for non-file endpoints:

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 9.1 Auth

- `POST /auth/login` (public)
- `POST /auth/logout`

### 9.2 Device (Inspector)

- `GET /device/page`
- `GET /device/list`
- `POST /device`
- `PUT /device`
- `DELETE /device/{id}`

### 9.3 Task

- `POST /task/submit` (Inspector)
- `GET /task/page` (Admin + Inspector)
- `GET /task/{id}` (Admin + Inspector)

### 9.4 Report

- `GET /report/preview/{taskId}` (PDF inline preview)
- `GET /report/download/{taskId}` (PDF download)

### 9.5 Standard

- `GET /standard/list`
- `GET /standard/groups`
- `GET /standard/match`
- `GET /standard/matchAll`
- `PUT /standard` (Admin)

### 9.6 User (Admin)

- `GET /user/page`
- `POST /user`
- `PUT /user/status`
- `PUT /user/reset-password`
- `PUT /user/password` (Admin + Inspector can change own password)

### 9.7 Admin Audit/Correction/Dashboard

- `GET /log/page`
- `POST /correct/result`
- `GET /dashboard/stats`

### 9.8 Excel Import (Inspector)

- `POST /import/tasks/preview`
- `POST /import/tasks`
- `POST /import/devices/preview`
- `POST /import/devices`

---

## 10. Business Rules

### 10.1 Result Validation

Threshold item keys:

- `f`
- `delta`
- `du`
- `upt`
- `uyb`

Validation logic:

- `PT1/PT2`: validate all five items
- `CT1/CT2`: validate only `f` and `delta`
- if any row item is out of range, that row is fail (`is_pass=0`)
- if any row in a task fails, task-level result is fail

### 10.2 Data Correction

- Admin can modify a result row via `/correct/result`
- system recalculates pass/fail using matched standard group

### 10.3 Operation Logging

- endpoints annotated with `@SysLog` are auto-audited
- captured fields include username, method, params, cost time, IP, and timestamp

---

## 11. Excel Import Rules

Common rules:

- only `.xlsx`
- max file size `10MB`
- import flow is preview-first, then commit

Task import validation highlights:

- phase must be `ao/bo/co`
- project type must be `PT1/PT2/CT1/CT2`
- device product number must already exist
- date formats are validated
- standard group must exist for `projectType + gearLevel + loadPercent`

Device import validation highlights:

- product number must be unique
- allowed pattern: letters, numbers, hyphen, underscore
- production date cannot be a future date

---

## 12. Known Constraints

1. No database migration/seed scripts are included in this repository.
2. PDF Chinese font loading currently depends on Windows font paths in `ReportServiceImpl`.
3. For production, change database credentials and JWT secret before deployment.

---

## 13. Recommended Smoke Test Flow

1. Prepare database and seed one admin user.
2. Start backend and verify `POST /api/auth/login`.
3. Start frontend and log in.
4. As inspector, create devices and submit tasks (or import via Excel).
5. As admin, update standards, review logs, use dashboard, and test correction flow.
6. Open task detail and verify PDF preview/download.

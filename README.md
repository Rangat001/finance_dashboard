# Finance Dashboard Backend (Spring Boot)

This repository contains the backend for a **role-based finance dashboard** system. It demonstrates:
- **User & role management** (Admin creates users; users can be deactivated instead of hard-deleted)
- **Financial records CRUD** (income/expense entries)
- **Dashboard analytics APIs** (totals, net balance, category-wise totals, trends, recent activity)
- **Backend-enforced access control** using **JWT + Spring Security**
- **Validation** with meaningful constraint messages

> Context path: all endpoints are served under `/finance` because `server.servlet.context-path=/finance`.

---

## Tech Stack
- Java 21
- Spring Boot `4.0.5`
- Spring WebMVC
- Spring Data JPA + Hibernate
- MySQL (JDBC driver)
- Spring Security (stateless)
- JWT: `io.jsonwebtoken (jjwt) 0.11.5`
- Lombok
- (Optional) Swagger/OpenAPI via springdoc (currently disabled in properties)

---

## How to Run (Local)
1. Create a MySQL database named: `fianace_dashboard` (note spelling matches your config).
2. Ensure tables exist (because `spring.jpa.hibernate.ddl-auto=none`, the app will **not** create them automatically).
3. Update DB credentials if needed in `application.properties`.
4. Start:
   ```bash
   mvn clean spring-boot:run
   ```

---

## Database Schema (explained from entities)

### Table: `users`
Mapped by: `com.example.finance.finance.entity.userEntity`

Fields:
- `id` (int, primary key)
- `name` (string)
- `email` (string) — **unique in repository logic**
- `password` (string) — stored **BCrypt-hashed**
- `role` (enum string): `ADMIN | ANALYST | VIEWER`
- `status` (enum string): `ACTIVE | INACTIVE`

Important design choice:
- **Soft-deactivation** of users: delete endpoint does not remove a row; it sets `status = INACTIVE`.  
  This is safer than hard delete because you keep history and can prevent accidental account loss.

### Table: `records`
Mapped by: `com.example.finance.finance.entity.records`

Fields:
- `id` (int, primary key)
- `amount` (Float)
- `category` (String)
- `note` (String)
- `date` (Date)
- `type` (enum string): `INCOME | EXPENSE`

---

## Configuration: `application.properties` (what each section does)

### App & Server
- `spring.application.name=finance`  
  Sets the application name (useful for logs/monitoring).
- `server.servlet.context-path=/finance`  
  Prefixes **every endpoint** with `/finance`.
- `server.port=8080`  
  Runs the service on port 8080.
- `spring.jmx.enabled=false`  
  Disables JMX (reduces noise and avoids JMX-related startup issues in some environments).

### Database Connection
- `spring.datasource.url=jdbc:mysql://localhost:3306/fianace_dashboard?...`  
  Connects to MySQL database `fianace_dashboard`.
- `spring.datasource.username=root`
- `spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver`

### JPA / Hibernate Behavior
- `spring.jpa.open-in-view=false`  
  Avoids keeping DB sessions open across the web layer (better separation and prevents lazy-loading surprises).
- `spring.jpa.hibernate.ddl-auto=none`  
  Hibernate will **not** auto-create/update tables; schema must exist.
- `spring.jpa.show-sql=false` and formatting flags disabled  
  Keeps logs clean.
- Batch optimization flags:
  - `hibernate.jdbc.batch_size=20`
  - `hibernate.order_inserts=true`
  - `hibernate.order_updates=true`  
  These improve performance when saving/updating multiple rows (more thoughtful than default configs).
- `hibernate.dialect=org.hibernate.dialect.MySQLDialect`  
  Ensures correct SQL generation for MySQL.

### Hikari Connection Pool
- `maximum-pool-size=20`, `minimum-idle=5`
- timeouts and lifetime values  
  Gives stable DB connectivity and avoids frequent connection churn.

### Swagger / OpenAPI
- `springdoc.api-docs.enabled=false`
- `springdoc.swagger-ui.enabled=false`  
  Swagger is configured in code (OpenAPI bean + scheme), but **disabled** here (useful when Swagger causes startup conflicts or when not required for deployment).

---

## Authentication & Authorization (JWT)

### JWT design
- Token `sub` (subject) is the **email**.
- Token includes a custom claim: `role`
  - Example values: `ADMIN`, `ANALYST`, `VIEWER`
- The request filter converts it to Spring authority format: `ROLE_ADMIN`, etc.

### Why role is stored inside JWT (intentional design)
This removes the need to hit the database on **every request** just to know the user’s role.  
Many fresh implementations load user details from DB on each call; here, the filter builds authorities directly from the token.

Tradeoff (documented):
- If a user’s role changes in DB, existing tokens keep the old role until they expire.

### Token refresh behavior (small but thoughtful)
If a token is expiring soon, the filter generates a new token and attaches it back in the response header:
- `Authorization: Bearer <newToken>`

---

## Access Control Rules (enforced in Spring Security)

Security rules are centralized (not scattered across controllers):
- Public:
  - `/auth/**`
  - Swagger paths (if enabled)
- Admin-only:
  - `/Admin/**`
- Analyst or Admin:
  - `/analyst/**`
- Viewer, Analyst, or Admin:
  - `/view/**`
- Everything else:
  - authenticated

This approach is more maintainable than role-checking inside each controller method.

---

## Controllers & APIs (actual endpoints)

> Remember: all endpoints are under `/finance` due to context path.

### 1) AuthController — `/auth`
File: `com.example.finance.finance.controller.AuthController`

#### `POST /auth/login`
Authenticates user by:
- finding user by email
- rejecting if `status == INACTIVE`
- verifying password using `PasswordEncoder.matches()`
Returns:
- JWT token containing role claim

Possible responses:
- `200 OK` with `{ token, email, role }`
- `401 UNAUTHORIZED` invalid credentials
- `403 FORBIDDEN` inactive user

#### `POST /auth/admin/signup`
Creates an **ADMIN user** only:
- checks email uniqueness
- hashes password with BCrypt
- sets role = `ADMIN`, status = `ACTIVE`
Returns:
- `201 CREATED` with token + admin info
- `409 CONFLICT` if email exists

> Note: this endpoint is used to create admin. Non-admin users are created through admin endpoints.

---

### 2) AdminController — `/Admin` (Admin only)
File: `com.example.finance.finance.controller.AdminController`

#### `GET /Admin/test`
Health-check style endpoint.

#### User Management
- `POST /Admin/addUser`
  - Validates payload (`user_dto`)
  - Creates user via service
  - Returns `201` or `500`
- `PUT /Admin/update-user/{id}`
  - Updates existing user fields
  - Returns `200` or `500`
- `DELETE /Admin/delete-user/{id}`
  - Soft-deactivates the user (sets `INACTIVE`)
  - Returns `200` or `406`

#### Record Management
- `POST /Admin/addRecord`
  - Creates a finance record (income/expense)
  - Returns `201` or `500`
- `PUT /Admin/record/{id}`
  - Updates a record
  - Returns `202 ACCEPTED` or `403`

---

### 3) ViewerController — `/view` (Viewer/Analyst/Admin)
File: `com.example.finance.finance.controller.ViewerController`

#### `GET /view/records`
Returns all records.
- `200 OK` with list
- `204 No Content` if empty

---

### 4) AnalystController — `/analyst` (Analyst/Admin)
File: `com.example.finance.finance.controller.AnalystController`

#### `GET /analyst/dashboard?trend=monthly|weekly`
Returns dashboard analytics in `AnalystDashboardDto`:
- total income
- total expenses
- net balance
- category-wise totals (category + type)
- recent activity (top 10)
- trend series (monthly or weekly buckets)

---

## Services (business logic that’s more than CRUD)

### AdminService
Highlights:
- Hashes password before saving user
- Sets default `status=ACTIVE` on user creation
- Updates only non-blank fields (partial update style)
- Enforces **email uniqueness** during update by checking if another user owns the email
- Deactivates user (soft delete) instead of deleting row

### AnalystService
Highlights:
- Aggregates totals using streams
- Category-wise totals grouped by `category + type` to avoid mixing INCOME and EXPENSE in the same bucket
- Recent activity uses repository method: `findTop10ByOrderByDateDescIdDesc()`
- Supports `trend=weekly` and `trend=monthly`:
  - Weekly bucket format like `2026-W14`
  - Monthly bucket format like `2026-04`
- Uses a `TreeMap` so trends are naturally sorted

### ViewerService
- Simple retrieval with safe fallback to empty list on error

---

## Validation & Error Handling

### Validation
DTOs are validated using Jakarta Validation annotations:
- `record_dto`
  - positive amount
  - required category/note/type
  - date must be past or present
- `user_dto`
  - required name/email/password/role
  - email format validated

### Token error responses (non-fresher detail)
JWT filter returns consistent JSON errors when token is missing/invalid:
- HTTP `401`
- JSON includes: `status`, `error`, `message`, `path`

This avoids generic Spring errors and makes frontend integration easier.

---

## What’s different from a typical “freshers’ backend”
Common fresher pattern:
- controllers call repositories directly
- role checks are done manually inside endpoints (if at all)
- passwords stored as plain text or weak hashing
- token parsing is repeated in controllers
- delete endpoints hard-delete users/records
- analytics is not separated; everything is CRUD only

What this backend does better (based on actual code):
1. **Clear layering**: controller → service → repository.
2. **JWT role claim** to avoid per-request DB calls for authorization.
3. **Centralized access control** using request matchers in Security config.
4. **Soft user deletion** (deactivation) to avoid data loss and preserve auditability.
5. **Partial update logic** (only overwrite non-blank fields).
6. **Analytics endpoints** that return aggregated DTOs, not raw DB rows.
7. **Explicit enum persistence** (`EnumType.STRING`) avoiding common “ordinal enum” bugs.

---

## Notes / Known limitations (intentionally kept simple for assessment)
- `ddl-auto=none`: schema must exist before running.
- Swagger is configured but disabled in properties.
- No pagination on `/view/records` (the current code returns all records).

---

## Quick Test Flow
1. Create admin:
   - `POST /finance/auth/admin/signup`
2. Login:
   - `POST /finance/auth/login`
3. Use returned token:
   - `Authorization: Bearer <token>`
4. Try:
   - Admin create user: `POST /finance/Admin/addUser`
   - Viewer list records: `GET /finance/view/records`
   - Analyst dashboard: `GET /finance/analyst/dashboard?trend=monthly`

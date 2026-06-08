# Task Scheduler — Work & Project Tracker

A full-featured **Work & Project Tracker** built with **Spring Boot 3**, **Spring Security**, **Thymeleaf**, and **Tailwind CSS**. Supports multi-user authentication, task categories, recurring tasks, admin portal, REST API, and email notifications — all at **zero infrastructure cost**.

> **Live Demo Login:** `admin` / `admin123` (change after first login)

---

## Features

| Category | Details |
|---|---|
| **Authentication** | Register / Login, BCrypt passwords, role-based access (User / Admin) |
| **Task Management** | Create, update, delete tasks with priority, status, start/end times |
| **Categories** | Color-coded categories per user |
| **Recurring Tasks** | Daily / Weekly / Monthly auto-recurrence |
| **Dependencies** | Task dependency graph with cycle detection (DFS) |
| **Overlap Detection** | Interval Tree prevents scheduling conflicts |
| **Dashboard** | Stats (total, pending, running, completed, overdue), upcoming & recent tasks |
| **Search & Filter** | Filter by keyword, status, category |
| **Admin Portal** | Manage all users (roles, enable/disable) and all tasks |
| **REST API** | Full CRUD + status patch + dashboard stats endpoints |
| **Email Notifications** | Optional Gmail SMTP reminders (graceful if not configured) |
| **DSA Under the Hood** | Max-Heap (priority), Interval Tree (overlap), Dependency Graph (cycles) |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 3.5, Java 17 |
| Security | Spring Security 6, BCrypt |
| Database | H2 File Mode (local) / PostgreSQL (production) |
| ORM | Spring Data JPA + Hibernate 6 |
| Frontend | Thymeleaf + Tailwind CSS CDN + Font Awesome 6 |
| Build | Maven |
| Scheduler | Spring `@Scheduled` |

---

## Prerequisites (Local Development)

- **Java 17+** — [Download](https://adoptium.net/)
- **Maven** — included via `mvnw` wrapper (no install needed)
- **Git**

---

## Local Setup

```bash
# 1. Clone
git clone https://github.com/your-username/task-scheduler.git
cd task-scheduler

# 2. Run (Windows)
.\mvnw.cmd spring-boot:run

# 2. Run (Linux/Mac)
./mvnw spring-boot:run

# 3. Open
http://localhost:8080
```

> **Note:** On first run the `./data/` folder is created automatically with the H2 database file. Your data persists across restarts.

---

## Default Credentials

| Role | Username | Password |
|---|---|---|
| Admin | `admin` | `admin123` |

**Change the admin password after first login via Admin Portal → Users.**

Register additional users at `/register`.

---

## Optional: Email Notifications

To enable daily task reminders, edit `src/main/resources/application.properties` and uncomment:

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

Generate a Gmail App Password at: https://myaccount.google.com/apppasswords

---

## REST API

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/tasks` | List tasks (search, status, category query params) |
| POST | `/api/tasks` | Create task |
| PUT | `/api/tasks/{id}` | Update task |
| DELETE | `/api/tasks/{id}` | Delete task |
| PATCH | `/api/tasks/{id}/status` | Update status only |
| GET | `/api/tasks/dependencies` | Get dependency graph |
| GET | `/api/dashboard/stats` | Dashboard statistics |

All endpoints return `{ "success": true, "data": ... }`.

---

## Project Structure

```
src/main/java/com/suraj/scheduler/
├── config/          # DataInitializer, CategoryConverter
├── controller/      # TaskController, AuthController, DashboardController,
│                    # CategoryController, AdminController, TaskRestController
├── dsa/             # DependencyGraph, IntervalTree, PriorityHeap
├── dto/             # RegisterDTO, DashboardStats, TaskDTO
├── entity/          # Task, AppUser, Category, TaskStatus, RecurrenceType, UserRole
├── exception/       # GlobalExceptionHandler, custom exceptions
├── repository/      # TaskRepository, UserRepository, CategoryRepository
├── scheduler/       # TaskSchedulerEngine (auto-run + recurrence)
├── security/        # SecurityConfig, CustomUserDetailsService, SecurityUtils
└── service/         # TaskService, CategoryService, NotificationService + impls

src/main/resources/
├── templates/
│   ├── auth/        # login.html, register.html
│   ├── admin/       # dashboard.html, users.html, tasks.html
│   ├── layout.html  # Shared Tailwind layout with sidebar
│   ├── dashboard.html
│   ├── tasks.html
│   └── categories.html
└── application.properties
```

---

## Hosting (Zero Cost) — Deploy to Railway

[Railway](https://railway.app) offers **$5 free credit/month** — enough for a small Spring Boot app + PostgreSQL.

### Step 1 — Push to GitHub

```bash
git add .
git commit -m "initial commit"
git push origin main
```

### Step 2 — Create Railway Project

1. Go to [railway.app](https://railway.app) → **New Project**
2. Choose **Deploy from GitHub repo** → select your repo
3. Railway auto-detects Spring Boot (Java) via `pom.xml`

### Step 3 — Add PostgreSQL

1. In Railway project → click **+ New** → **Database** → **PostgreSQL**
2. Railway automatically sets `DATABASE_URL` env var

### Step 4 — Set Environment Variables

In Railway project → your service → **Variables**, add:

| Variable | Value |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `ADMIN_PASSWORD` | *(your secure admin password)* |

### Step 5 — Deploy

Railway will build and deploy automatically. Your app will be live at a `*.up.railway.app` URL.

> **Free alternatives:** [Render.com](https://render.com) (free tier, spins down after 15 min), [Fly.io](https://fly.io) (3 free VMs)

---

## Hosting — Alternative: Render.com

1. Push to GitHub
2. Go to [render.com](https://render.com) → **New Web Service** → connect repo
3. Set **Build Command:** `./mvnw clean package -DskipTests`
4. Set **Start Command:** `java -jar target/task-scheduler-0.0.1-SNAPSHOT.jar`
5. Add **PostgreSQL** database from Render dashboard
6. Set env var `SPRING_PROFILES_ACTIVE=prod` and `DATABASE_URL` from Render PostgreSQL

---

## Production Profile (`application-prod.properties`)

The `prod` profile uses PostgreSQL and secure settings. Set these environment variables on your hosting platform:

| Env Variable | Description |
|---|---|
| `DATABASE_URL` | PostgreSQL connection URL (set by Railway/Render automatically) |
| `DB_USERNAME` | PostgreSQL username |
| `DB_PASSWORD` | PostgreSQL password |
| `ADMIN_PASSWORD` | Overrides default admin123 password |

---

## DSA Implementation Details

| Component | Class | Algorithm |
|---|---|---|
| Priority queue | `PriorityHeap.java` | Max-heap, O(log n) insert/extract |
| Overlap detection | `IntervalTree.java` | Interval tree, O(log n) query |
| Cycle detection | `DependencyGraph.java` | DFS topological sort, O(V+E) |

---

## License

MIT

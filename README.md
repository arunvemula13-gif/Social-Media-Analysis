g# SPEDO — Social Media Analytics & Competitive Intelligence Platform

> **"Understand Your Social. Outperform Your Competition."**

SPEDO is an enterprise-grade social media intelligence, benchmarking, and competitive analytics platform inspired by the core capabilities of Rival IQ. It features an original modern SaaS design system, a reactive Java 21 & Spring Boot 3 backend, relational MySQL 8 persistence with Flyway migrations, and a responsive frontend dashboard.

---

## 1. High-Level Architecture

SPEDO follows a decoupled, layered architectural blueprint:

```
┌─────────────────────────────────────────────────────────────────┐
│                    SPEDO Client Web Interface                   │
│   (HTML5, CSS3 Custom Tokens, Vanilla JS, Chart.js, Fetch API) │
└───────────────────────────────▲─────────────────────────────────┘
                                │ JSON REST APIs (Stateless JWT)
┌───────────────────────────────▼─────────────────────────────────┐
│              Spring Boot 3.3.4 Backend Application              │
│                                                                 │
│  [ Controller Layer ]                                           │
│    AuthController, DashboardController, SocialAccountController, │
│    PostController, CompetitorController, ReportController,      │
│    NotificationController, DemoController                      │
│                                                                 │
│  [ Service Layer & Platform Adapters ]                          │
│    AuthService, DashboardService, SocialAccountService,         │
│    PostService, CompetitorService, ReportService,               │
│    NotificationService, DemoDataService                         │
│    PlatformAdapterFactory: Instagram, FB, YouTube, LinkedIn, X │
│                                                                 │
│  [ Security & Data Layer ]                                      │
│    Spring Security 6 (Stateless JWT filter, BCrypt 10 rounds)   │
│    Spring Data JPA / Hibernate ORM (Tenant Isolation)           │
│    Flyway Database Migrations (V1 Schema, V2 Demo Seed)         │
│    Automated Metric Sync Scheduler (@Scheduled)                 │
└───────────────────────────────▲─────────────────────────────────┘
                                │ JDBC
┌───────────────────────────────▼─────────────────────────────────┐
│                     MySQL 8 Database Engine                     │
│  (users, social_accounts, social_metrics, posts, competitors,  │
│   competitor_metrics, reports, notifications)                   │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2. Technology Stack

### Frontend
- **HTML5 & CSS3**: Custom design tokens, dark navy sidebar (`#111a36`), violet/indigo accents (`#4361ee`, `#7209b7`), rounded cards, and responsive layout grids.
- **Vanilla JavaScript (ES6+)**: Zero bulky framework dependencies, modular controllers (`api.js`, `auth.js`, `navbar.js`, `dashboard.js`, `social-accounts.js`, `posts.js`, `competitors.js`, `reports.js`, `notifications.js`, `settings.js`).
- **Chart.js 4.x**: Line charts for follower momentum, bar charts for engagement velocity, doughnut charts for audience distribution, and head-to-head competitor comparison charts.
- **Printable Dossier Engine**: Dedicated print stylesheet formatted for board presentation and PDF report generation.

### Backend
- **Java 21 LTS**
- **Spring Boot 3.3.4**
- **Spring Web (REST Controllers)**
- **Spring Data JPA & Hibernate 6**
- **Spring Security 6 (JWT stateless authorization)**
- **Spring Validation (Jakarta Bean Validation)**
- **Flyway Database Migrations**
- **JJWT 0.12.6 (HMAC-SHA-256 tokens)**
- **Maven 3.9+**

### Database
- **MySQL 8** (Production & Local)
- **H2 in-memory MySQL emulation mode** (Unit & Integration Testing)

### Testing
- **JUnit 5**
- **Mockito**
- **Spring Boot Test & MockMvc**
- **13 automated unit & integration tests** covering security, calculations, and data endpoints.

---

## 3. Project Directory Structure

```
social media analysis/
├── .env.example                                  # Environment variables configuration template
├── mvnw                                          # Maven wrapper script (Unix)
├── mvnw.cmd                                      # Maven wrapper script (Windows)
├── pom.xml                                       # Maven build and dependencies
├── README.md                                     # Full platform documentation
│
└── src/
    ├── main/
    │   ├── java/com/spedo/analytics/
    │   │   ├── SpedoAnalyticsApplication.java    # Application entrypoint (@EnableScheduling)
    │   │   ├── config/
    │   │   │   ├── DataInitializer.java          # Baseline demo user seeding
    │   │   │   └── SecurityConfig.java           # Stateless JWT, CORS, BCrypt, public routes
    │   │   ├── controller/
    │   │   │   ├── AuthController.java           # /api/auth (register, login, profile, password)
    │   │   │   ├── CompetitorController.java     # /api/competitors (CRUD, ranking, comparison)
    │   │   │   ├── DashboardController.java      # /api/dashboard (summary, growth, breakdown)
    │   │   │   ├── DemoController.java           # /api/demo (reset dataset)
    │   │   │   ├── NotificationController.java   # /api/notifications (list, read, unread count)
    │   │   │   ├── PostController.java           # /api/posts (paginated search, top-performing)
    │   │   │   ├── ReportController.java         # /api/reports (generate, CSV export, view)
    │   │   │   └── SocialAccountController.java  # /api/social-accounts (connect, sync, disconnect)
    │   │   ├── dto/                              # Strict DTO layer (no entity leaks)
    │   │   │   ├── ApiResponse.java, AuthResponse.java, CompetitorBenchmarkDto.java, ...
    │   │   ├── entity/                           # JPA Relational Entities & Enums
    │   │   │   ├── AccountStatus.java, Competitor.java, CompetitorMetric.java,
    │   │   │   ├── Notification.java, NotificationType.java, Post.java, PostType.java,
    │   │   │   ├── Report.java, ReportFormat.java, ReportStatus.java, ReportType.java,
    │   │   │   ├── Role.java, SocialAccount.java, SocialMetric.java, SocialPlatform.java, User.java
    │   │   ├── exception/
    │   │   │   ├── BadRequestException.java, ErrorResponse.java,
    │   │   │   ├── GlobalExceptionHandler.java, ResourceNotFoundException.java, UnauthorizedException.java
    │   │   ├── integration/                      # Platform Adapter Pattern
    │   │   │   ├── FacebookAdapter.java, InstagramAdapter.java, LinkedInAdapter.java,
    │   │   │   ├── PlatformAdapterFactory.java, SocialPlatformAdapter.java,
    │   │   │   ├── SyncResult.java, XTwitterAdapter.java, YouTubeAdapter.java
    │   │   ├── mapper/
    │   │   │   └── EntityDtoMapper.java          # Clean bi-directional conversions
    │   │   ├── repository/                       # Spring Data JPA Repositories
    │   │   │   ├── CompetitorMetricRepository.java, CompetitorRepository.java,
    │   │   │   ├── NotificationRepository.java, PostRepository.java, ReportRepository.java,
    │   │   │   ├── SocialAccountRepository.java, SocialMetricRepository.java, UserRepository.java
    │   │   ├── scheduler/
    │   │   │   └── MetricsSyncScheduler.java     # Hourly automated metrics sync
    │   │   └── security/
    │   │       ├── CustomUserDetailsService.java, JwtAuthenticationEntryPoint.java,
    │   │       ├── JwtAuthenticationFilter.java, JwtTokenProvider.java, UserPrincipal.java
    │   │   └── service/
    │   │       ├── AuthService.java, CompetitorService.java, DashboardService.java,
    │   │       ├── DemoDataService.java, NotificationService.java, PostService.java,
    │   │       ├── ReportService.java, SocialAccountService.java
    │   │
    │   └── resources/
    │       ├── application.yml                   # Production / MySQL configuration
    │       ├── db/migration/
    │       │   ├── V1__init_schema.sql           # Schema definition with indexes and constraints
    │       │   └── V2__seed_demo_data.sql        # Realistic demo baseline telemetry
    │       └── static/                           # Responsive Frontend Assets
    │           ├── css/styles.css                # SPEDO SaaS design system
    │           ├── js/
    │           │   ├── api.js, auth.js, navbar.js, dashboard.js,
    │           │   ├── social-accounts.js, posts.js, competitors.js,
    │           │   ├── reports.js, notifications.js, settings.js
    │           ├── index.html                    # Polished product landing page
    │           ├── login.html                    # User sign-in with 1-click demo fill
    │           ├── register.html                 # User signup with workspace initializer
    │           ├── dashboard.html                # Executive analytics dashboard
    │           ├── social-accounts.html          # Connected channels & sync manager
    │           ├── posts.html                    # Post analytics & deep-dive audit
    │           ├── competitors.html              # Head-to-head benchmarking & leaderboard
    │           ├── reports.html                  # Executive CSV & PDF printable reports
    │           ├── notifications.html            # Alert telemetry center
    │           └── settings.html                 # Profile, password & API keys overview
    │
    └── test/
        ├── java/com/spedo/analytics/
        │   ├── controller/
        │   │   ├── AuthControllerIntegrationTest.java      # Auth & registration integration test
        │   │   └── DashboardControllerIntegrationTest.java # Dashboard & security integration test
        │   └── service/
        │       ├── AuthServiceTest.java                    # Unit tests with Mockito
        │       ├── CompetitorServiceTest.java              # Competitor ranking logic unit test
        │       └── PostServiceTest.java                    # Engagement rate mathematical formulas
        └── resources/
            └── application-test.yml              # Test profile using in-memory H2
```

---

## 4. Database Setup & Migrations

### MySQL 8 Installation & Database Creation
Log in to your local or cloud MySQL instance and run:
```sql
CREATE DATABASE IF NOT EXISTS spedo_analytics CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Automated Flyway Migrations
When SPEDO starts, Flyway executes the numbered migration scripts located in `src/main/resources/db/migration`:
1. `V1__init_schema.sql`:
   - `users`: Stores emails, BCrypt password hashes, organizations, system roles.
   - `social_accounts`: Linked profiles across Instagram, Facebook, YouTube, LinkedIn, X.
   - `social_metrics`: Daily historical snapshots (followers, impressions, reach, likes, comments, shares, engagement rate).
   - `posts`: Published media assets with metadata, likes, comments, shares, impressions, reach, calculated engagement rate.
   - `competitors`: Tracked rival brands and social handles.
   - `competitor_metrics`: Monitored metrics (posting frequency, average interactions, engagement rate).
   - `reports`: Saved report archives with compiled JSON payloads and download links.
   - `notifications`: Alerts, sync confirmations, milestone triggers.
2. `V2__seed_demo_data.sql`:
   - Populates initial demo account (`demo@spedo.io`), connected multi-channel handles, 30 days of metrics, sample posts, 4 competitive brands (`ApexMetrics`, `PulseTrack`, `OmniVibe Media`, `DataEcho`), and recent notifications.

---

## 5. Quick Start Instructions

### Prerequisites
- **Java 21 LTS** installed (`java -version`)
- **MySQL 8** running on port 3306 (or configured in `.env` / `application.yml`)

### Step 1: Configure Environment Variables
Copy `.env.example` to `.env` in the project root and set your values. On Windows:
```powershell
Copy-Item .env.example .env
```
The backend loads `.env` at startup; never commit it. For a YouTube API key, set `APP_INTEGRATIONS_YOUTUBE_API_KEY` and keep `DEMO_MODE_ENABLED=false`.
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/spedo_analytics?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8
    username: root
    password: root
```

### Step 2: Compile & Run the Application
Run using the bundled Maven wrapper:
```bash
# On Windows:
.\mvnw.cmd spring-boot:run

# On Linux / macOS:
./mvnw spring-boot:run
```

Alternatively, build an executable JAR and execute:
```bash
.\mvnw.cmd package -DskipTests
java -jar target/spedo-analytics-1.0.0.jar
```

### Step 3: Access the Application
Open your browser and navigate to:
- **Landing Page**: [http://localhost:8080](http://localhost:8080)
- **Sign In**: [http://localhost:8080/login.html](http://localhost:8080/login.html)
- **Register**: [http://localhost:8080/register.html](http://localhost:8080/register.html)
- **Dashboard**: [http://localhost:8080/dashboard.html](http://localhost:8080/dashboard.html)

### Default Demo Credentials
- **Email**: `demo@spedo.io`
- **Password**: `Password123!`
*(The login page provides a 1-click "Fill Demo" shortcut for instant access)*

---

## 6. Engagement Rate Calculation

SPEDO calculates engagement rates systematically across all posts and social metrics:

$$\text{Engagement Rate (\%)} = \left( \frac{\text{Likes} + \text{Comments} + \text{Shares}}{\text{Denominator}} \right) \times 100$$

### Denominator Priority:
1. **Impressions** (Standard when impression telemetry is available).
2. **Reach** (Used if impressions are zero or unexposed by platform permissions).
3. **Follower Count** (Standard fallback for public competitor accounts where impressions/reach are private).
4. If all are 0, the engagement rate returns `0.0000%` safely without divide-by-zero errors.

---

## 7. REST API Endpoints

All protected endpoints require the HTTP header:
`Authorization: Bearer <jwt-token>`

| Category | Method | Endpoint | Description |
| :--- | :--- | :--- | :--- |
| **Auth** | `POST` | `/api/auth/register` | Register new user & seed workspace |
| **Auth** | `POST` | `/api/auth/login` | Authenticate & obtain JWT token |
| **Auth** | `GET` | `/api/auth/me` | Get authenticated user profile |
| **Auth** | `POST` | `/api/auth/change-password` | Update account password |
| **Dashboard**| `GET` | `/api/dashboard/summary?days=30` | Key metrics, top post & activity |
| **Dashboard**| `GET` | `/api/dashboard/growth?days=30` | Daily follower growth & reach series |
| **Dashboard**| `GET` | `/api/dashboard/platform-comparison` | Performance breakdown by channel |
| **Accounts** | `GET` | `/api/social-accounts` | List connected social accounts |
| **Accounts** | `POST` | `/api/social-accounts` | Connect new account |
| **Accounts** | `POST` | `/api/social-accounts/{id}/sync` | Trigger metric synchronization |
| **Accounts** | `DELETE`| `/api/social-accounts/{id}` | Disconnect social account |
| **Posts** | `GET` | `/api/posts` | Paginated search, filter by network |
| **Posts** | `GET` | `/api/posts/{id}` | Deep dive post analytics |
| **Posts** | `GET` | `/api/posts/top-performing` | Top posts sorted by engagement rate |
| **Competitors**| `GET` | `/api/competitors` | List monitored competitors |
| **Competitors**| `POST` | `/api/competitors` | Add competitor brand & handles |
| **Competitors**| `PUT` | `/api/competitors/{id}` | Update competitor profile |
| **Competitors**| `DELETE`| `/api/competitors/{id}` | Remove competitor |
| **Competitors**| `GET` | `/api/competitors/compare` | Head-to-head ranking & benchmark |
| **Reports** | `GET` | `/api/reports` | List generated report history |
| **Reports** | `POST` | `/api/reports` | Compile new CSV or PDF report |
| **Reports** | `GET` | `/api/reports/{id}` | Retrieve report JSON content |
| **Reports** | `GET` | `/api/reports/{id}/csv` | Download direct CSV file |
| **Notifications**| `GET` | `/api/notifications` | List user notifications |
| **Notifications**| `GET` | `/api/notifications/unread-count`| Get unread alerts count |
| **Notifications**| `PATCH`| `/api/notifications/{id}/read` | Mark individual notification as read |
| **Notifications**| `POST` | `/api/notifications/read-all` | Mark all notifications as read |
| **Demo** | `POST` | `/api/demo/reset` | Reset demo account data |

---

## 8. Social Platform Adapter Architecture

SPEDO isolates each third-party network integration using the **Platform Adapter Pattern**:
- `InstagramAdapter`: Instagram Graph API / Meta Business suite.
- `FacebookAdapter`: Facebook Page Insights API.
- `YouTubeAdapter`: Google YouTube Data API v3.
- `LinkedInAdapter`: LinkedIn Community Management API.
- `XTwitterAdapter`: X API v2.

### Configured vs. Demo Mode Handling
Each adapter implements:
```java
public interface SocialPlatformAdapter {
    SocialPlatform getPlatform();
    boolean isConfigured();
    SyncResult syncMetrics(SocialAccount account);
}
```
The YouTube adapter calls the official YouTube Data API v3 and does not fall back to generated metrics. It displays the channel's public subscriber count (when visible), lifetime channel views, and published video count. The channel endpoint does not provide daily reach, impressions, likes, comments, revenue, or private YouTube Analytics; those values are not fabricated. Private owner analytics require a separate OAuth flow and YouTube Analytics API integration. Other platforms need their own credentials and are not activated by a YouTube key.

To connect a channel, select **YouTube** and enter its `@handle`, channel ID (`UC...`), or YouTube channel URL, then press **Connect Account**. Use **Sync** to refresh the live values. If sample accounts were already seeded into the local database, disconnect those accounts in **Social Accounts**; turning demo mode off prevents new demo seeding but does not erase existing rows.

---

## 9. Security & Data Protection

- **BCrypt Password Hashing**: Passwords are salted and hashed using BCrypt (strength 10). Plaintext passwords are never persisted or logged.
- **Stateless JWT Authorization**: Signed with HMAC-SHA256. Tokens are transmitted in `Authorization: Bearer` headers.
- **Tenant Data Isolation**: All repository queries enforce user-level isolation (`userId` parameter binding), ensuring users can never inspect another tenant's metrics or posts.
- **CSRF Consideration**: Because authentication is stateless and uses Bearer tokens stored in browser memory/localStorage (instead of ambient session cookies), traditional cross-site request forgery attacks are rendered ineffective.
- **SQL Injection Prevention**: All queries utilize Spring Data JPA parameter binding or JPQL named parameters.
- **Standardized Exception Sanitization**: Handled centrally by `GlobalExceptionHandler`. Internal database traces are masked from client responses.

---

## 10. Automated Testing

Run the test suite with:
```bash
.\mvnw.cmd test
```

### Test Coverage Highlights:
- **`PostServiceTest`**: Verifies exact engagement rate calculation across denominator fallbacks (impressions -> reach -> followers -> zero handling).
- **`AuthServiceTest`**: Verifies BCrypt registration, duplicate email rejection (`BadRequestException`), and JWT token generation.
- **`CompetitorServiceTest`**: Verifies brand ranking algorithm and benchmark comparison sorting.
- **`AuthControllerIntegrationTest`**: End-to-end MockMvc verification of user registration, valid login, and invalid credential rejection (HTTP 401).
- **`DashboardControllerIntegrationTest`**: End-to-end MockMvc verification of summary endpoints, platform breakdowns, and unauthorized route access blocks.

---

## 11. Production Deployment

### Production Packaging
```bash
.\mvnw.cmd clean package -DskipTests
```
The resulting executable JAR is generated at `target/spedo-analytics-1.0.0.jar`.

### Running in Production
```bash
export DB_HOST=prod-mysql.internal
export DB_PORT=3306
export DB_NAME=spedo_analytics
export DB_USER=spedo_prod_user
export DB_PASSWORD=SecretProductionPassword!
export JWT_SECRET=YourSuperLongProductionSecretKeyMustBeAtLeast256BitsLong12345!
export CORS_ALLOWED_ORIGINS=https://app.spedo.io

java -jar target/spedo-analytics-1.0.0.jar
```

### Reverse Proxy (Nginx Example)
```nginx
server {
    listen 80;
    server_name app.spedo.io;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

---

## 12. License
© 2026 SPEDO Analytics Inc. All rights reserved.

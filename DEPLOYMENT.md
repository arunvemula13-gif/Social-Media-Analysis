# SPEDO Analytics & Competitive Intelligence — Production Deployment Guide

This guide details how to build, containerize, configure, and deploy the SPEDO Social Media Analytics platform to production environments.

---

## 1. Quick Start with Docker & Docker Compose

### Prerequisites
- Docker Engine 24+ & Docker Compose v2+
- Git

### Deployment Steps
1. **Clone repository and configure environment:**
   ```bash
   cp .env.example .env
   # Edit .env with your production secrets (JWT_SECRET, admin credentials, API keys)
   ```

2. **Build and launch with Docker Compose:**
   ```bash
   docker compose up -d --build
   ```

3. **Verify running status:**
   ```bash
   docker compose ps
   docker compose logs -f spedo-app
   ```

4. **Access the application:**
   - Production URL: `http://localhost:8080/login.html`
   - Sign in with your configured admin email and password.

---

## 2. Deploying to Cloud Platforms

### A. Railway (Recommended for instant deployment)
1. Fork or push this repository to GitHub.
2. In Railway Dashboard, click **New Project** &rarr; **Deploy from GitHub repo**.
3. Railway automatically detects the `Dockerfile`.
4. In the Railway Service **Variables** tab, add:
   - `PORT`: (Provided automatically by Railway)
   - `SPRING_PROFILES_ACTIVE`: `prod`
   - `DEMO_MODE_ENABLED`: `false`
   - `JWT_SECRET`: `<Generate-Secure-64-Char-Hex>`
   - `DEFAULT_ADMIN_EMAIL`: `arunvemula13@gmail.com`
   - `DEFAULT_ADMIN_PASSWORD`: `<Your-Strong-Admin-Password>`
   - `APP_INTEGRATIONS_YOUTUBE_API_KEY`: `<Your-Google-Cloud-YouTube-Key>`
5. Attach a Railway persistent volume mounted at `/app/data` to persist your embedded database across redeploys, or connect a managed MySQL database.

---

### B. Render
1. Create a **New Web Service** connected to your repository.
2. Set Environment: **Docker**.
3. Under **Disks**, add a persistent disk:
   - Name: `spedo-data`
   - Mount Path: `/app/data`
   - Size: `1 GB` (or larger)
4. Add environment variables matching `.env.example`.
5. Render will automatically build the multi-stage Docker image and deploy.

---

### C. AWS / Google Cloud Run / DigitalOcean App Platform
1. Build and push container to your container registry (ECR, GCR, Docker Hub):
   ```bash
   docker build -t your-org/spedo-analytics:1.0.0 .
   docker push your-org/spedo-analytics:1.0.0
   ```
2. Deploy container with container port `8080`.
3. Set environment variable `PORT` to the port passed by your cloud provider.
4. Mount storage or connect an external managed MySQL database (e.g. AWS RDS Aurora / Google Cloud SQL).

---

## 3. Production External Database Setup (MySQL / PostgreSQL)

For high-throughput enterprise deployments with millions of posts and events, switch from embedded H2 to managed MySQL:

```env
SPRING_DATASOURCE_URL=jdbc:mysql://your-rds-host:3306/spedo_analytics?useSSL=true&serverTimezone=UTC&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_DRIVER=com.mysql.cj.jdbc.Driver
SPRING_DATASOURCE_USERNAME=spedo_app_user
SPRING_DATASOURCE_PASSWORD=YourComplexDbPassword!
SPRING_JPA_HIBERNATE_DDL_AUTO=update
```

---

## 4. YouTube API & Social Integrations

1. Navigate to [Google Cloud Console](https://console.cloud.google.com/).
2. Enable **YouTube Data API v3**.
3. Create API Key under Credentials and restrict by IP / Referrer.
4. Set `APP_INTEGRATIONS_YOUTUBE_API_KEY=<Your-Key>` in your production environment.
5. Channels benchmarked in the Competitor Matrix or selected in the Giveaway Picker will fetch live telemetry directly through this key.

---

## 5. Security Checklist for Production

- [x] **Remove Login Demo Data**: Mock 1-click test credentials completely removed from client UI.
- [x] **Secure JWT Key**: Update `JWT_SECRET` from default to a cryptographically random 256-bit token.
- [x] **Disable H2 Web Console**: `H2_CONSOLE_ENABLED=false` is enforced by default.
- [x] **Non-Root Execution**: Container executes under unprivileged `appuser:appgroup`.
- [x] **SSL / HTTPS**: Terminate TLS at your Cloudflare / ALB / Reverse Proxy layer.

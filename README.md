# SignalForge

**Premium Uptime Monitoring SaaS** — Monitor your websites every 60 seconds. Get instant alerts when sites go down or recover.

Built with **Spring Boot 4** · **Java 21** · **PostgreSQL** · **Redis** · **JWT + OAuth2**

---

## Features

- 🔐 **JWT Authentication** — Stateless auth with Redis session management and refresh token rotation
- 📧 **Email OTP Verification** — Every account verified via 6-digit OTP before creation
- 🔑 **Google OAuth2** — One-click sign in with Google SSO
- 📡 **60-Second Monitoring** — 20-thread scheduler pings all monitored URLs every minute
- 🔔 **Instant Alerts** — Email notifications on down/recovery events via Brevo API
- ⚡ **Alert History** — Full audit trail of all status change events
- 📊 **Modern Dashboard** — Real-time stats, search, filter, and responsive tables
- 🎨 **Premium Dark UI** — Glassmorphism design inspired by Linear, Stripe, and Vercel

---

## Quick Start

### Prerequisites

- Java 21+
- PostgreSQL 14+
- Redis 7+
- Brevo (Sendinblue) API key
- Google OAuth2 credentials (optional)

### Option 1: Docker Compose (Recommended)

```bash
# Clone and enter the directory
cd signalforge

# Copy environment template
cp .env.example .env
# Edit .env with your API keys

# Start everything
docker-compose up -d
```

The app will be available at **http://localhost:1111**

### Option 2: Manual Setup

```bash
# 1. Create PostgreSQL database
createdb signalforge

# 2. Set environment variables (see .env.example)
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/signalforge
export SPRING_DATASOURCE_USERNAME=your_user
export SPRING_DATASOURCE_PASSWORD=your_password
export SPRING_DATA_REDIS_HOST=localhost
export SPRING_DATA_REDIS_PORT=6379
export JWT_SECRET=your-secret-key-minimum-32-chars
export BREVO_API_KEY=your_brevo_key
export COOKIE_SECURE=false
export FRONTEND_URL=http://localhost:1111

# 3. Build and run
./mvnw clean package -DskipTests
java -jar target/signalforge-1.0.0.jar
```

---

## Project Structure

```
src/main/java/com/signalforge/
├── SignalForgeApplication.java      # Main entry point
├── configuration/
│   ├── AppConfig.java               # RestTemplate config
│   ├── RedisConfig.java             # Redis template
│   └── WebConfig.java               # Security filter chain, CORS
├── controller/
│   ├── AuthController.java          # Login, register, OTP, logout, refresh
│   ├── MonitorController.java       # CRUD for monitors
│   ├── OAuthController.java         # Google OAuth2 callback
│   └── StatsController.java         # Public stats endpoint
├── dto/
│   ├── LoginRequest.java
│   ├── OtpRequest.java
│   ├── PingResult.java
│   ├── RegisterRequest.java
│   └── StatsResponse.java
├── entity/
│   ├── AlertHistory.java            # Down/recovery event log
│   ├── MonitoredUrl.java            # URL monitor entity
│   └── User.java
├── jwt/
│   ├── JwtFilter.java               # Auth filter
│   └── JwtUtil.java                 # Token generation/validation
├── repository/
│   ├── AlertHistoryRepository.java
│   ├── MonitorRepository.java
│   └── UserRepository.java
├── scheduler/
│   └── MonitorScheduler.java        # 60-second ping scheduler
├── services/
│   ├── AlertService.java            # Email alerts for status changes
│   ├── MonitorService.java          # Monitor CRUD business logic
│   ├── MonitoredUrlService.java     # Ping orchestration
│   ├── OtpService.java              # OTP generation/verification
│   ├── PingService.java             # HTTP health check
│   ├── SessionService.java          # Redis session management
│   └── UserDetailsImpl.java         # Spring Security user loader
└── util/
    └── CookieUtil.java              # Shared cookie builder

src/main/resources/
├── application.properties           # Configuration
├── application.yml                  # OAuth2 config
├── db/migration/
│   └── V1__init_schema.sql          # Flyway migration
└── static/
    ├── index.html                   # SPA frontend
    ├── style.css                    # Design system
    └── script.js                    # App logic
```

---

## API Endpoints

### Authentication (Public)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/send-otp` | Send OTP to email |
| POST | `/api/auth/verify-otp` | Verify OTP & create account |
| POST | `/api/auth/login` | Login with email/password |
| POST | `/api/auth/logout` | Logout & invalidate session |
| POST | `/api/auth/refresh` | Refresh access token |

### Monitors (JWT Required)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/monitors` | List user's monitors |
| POST | `/api/monitors` | Add new monitor |
| DELETE | `/api/monitors/{id}` | Delete a monitor |

### Public
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/stats` | Platform statistics |

---

## Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `SPRING_DATASOURCE_URL` | ✅ | PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | ✅ | Database username |
| `SPRING_DATASOURCE_PASSWORD` | ✅ | Database password |
| `SPRING_DATA_REDIS_HOST` | ✅ | Redis host |
| `SPRING_DATA_REDIS_PORT` | ✅ | Redis port |
| `JWT_SECRET` | ✅ | JWT signing key (min 32 chars) |
| `BREVO_API_KEY` | ✅ | Brevo email API key |
| `SENDER_EMAIL` | ❌ | Sender email (default: signalforge@gmail.com) |
| `GOOGLE_CLIENT_ID` | ❌ | Google OAuth2 client ID |
| `GOOGLE_CLIENT_SECRET` | ❌ | Google OAuth2 secret |
| `FRONTEND_URL` | ❌ | Frontend URL for redirects |
| `COOKIE_SECURE` | ❌ | Set true for HTTPS |
| `PORT` | ❌ | Server port (default: 1111) |

---

## Tech Stack

| Technology | Version | Purpose |
|-----------|---------|---------|
| Spring Boot | 4.0.2 | Backend framework |
| Java | 21 LTS | Language |
| PostgreSQL | 16 | Primary database |
| Redis | 7 | Session & refresh token store |
| Flyway | — | Database migrations |
| JJWT | 0.12.6 | JWT authentication |
| Brevo | 7.0.0 | Transactional email |
| Docker | — | Containerization |

---

## License

MIT License — feel free to use, modify, and build on top.

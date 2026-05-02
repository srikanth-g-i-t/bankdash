# 🏦 BankDash — Banking Microservices Platform

> Full stack digital banking platform built with Java Spring Boot microservices + React frontend.  
> **Portfolio project by Srikanth Koka** — demonstrating enterprise-grade architecture at scale.

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    React Frontend                        │
│          (Dashboard · Accounts · Transactions)          │
└────────────────────┬────────────────────────────────────┘
                     │ HTTP
┌────────────────────▼────────────────────────────────────┐
│                   API Gateway :8080                      │
│         (JWT Auth Filter · Route Forwarding)             │
└──────┬──────────────┬──────────────────┬────────────────┘
       │              │                  │
┌──────▼──────┐ ┌─────▼──────┐ ┌────────▼──────┐
│Auth Service │ │Account Svc │ │Transaction Svc│
│   :8081     │ │   :8082    │ │    :8083      │
│  JWT·OAuth  │ │  Accounts  │ │ Kafka Events  │
│  BCrypt     │ │  Balances  │ │ Transfers     │
└──────┬──────┘ └─────┬──────┘ └────────┬──────┘
       │              │                  │
       └──────────────┴──────────────────┘
                      │
         ┌────────────▼────────────┐
         │     PostgreSQL :5432    │
         │  Redis :6379 (cache)    │
         │  Kafka :9092 (events)   │
         └─────────────────────────┘
                      │ Kafka
         ┌────────────▼────────────┐
         │  Notification Service   │
         │       :8084             │
         └─────────────────────────┘
```

---

## 📁 Project Structure

```
bankdash/
├── docker-compose.yml
├── scripts/
│   └── init.sql
├── backend/
│   ├── api-gateway/          ← Spring Cloud Gateway + JWT filter
│   ├── auth-service/         ← Register, login, refresh, JWT, Redis blacklist
│   ├── account-service/      ← Account CRUD, balance management
│   ├── transaction-service/  ← Transfer, deposit, withdraw, Kafka producer
│   └── notification-service/ ← Kafka consumer, notification handler
└── frontend/
    ├── src/
    │   ├── api/api.js          ← Axios clients (auth, account, transaction)
    │   ├── context/AuthContext.jsx
    │   ├── pages/
    │   │   ├── Login.jsx
    │   │   ├── Register.jsx
    │   │   ├── Dashboard.jsx   ← Charts, stats, recent transactions
    │   │   ├── Accounts.jsx    ← Account cards + create flow
    │   │   └── Transactions.jsx ← Full history + transfer/deposit/withdraw
    │   └── components/
    │       ├── Sidebar.jsx
    │       ├── AppLayout.jsx
    │       └── ProtectedRoute.jsx
    └── Dockerfile
```

---

## 🚀 Quick Start

### 1. Run the full stack with Docker Compose

```bash
git clone https://github.com/srikanthkoka/bankdash
cd bankdash

docker compose up --build
```

Services start on:
| Service              | URL                              |
|----------------------|----------------------------------|
| Frontend             | http://localhost:3000             |
| API Gateway          | http://localhost:8080             |
| Auth Service         | http://localhost:8081/swagger-ui.html |
| Account Service      | http://localhost:8082/swagger-ui.html |
| Transaction Service  | http://localhost:8083/swagger-ui.html |

### 2. Run locally (without Docker)

**Prerequisites:** Java 17+, Node 18+, PostgreSQL, Redis, Kafka

```bash
# Start each service
cd backend/auth-service        && mvn spring-boot:run
cd backend/account-service     && mvn spring-boot:run
cd backend/transaction-service && mvn spring-boot:run
cd backend/api-gateway         && mvn spring-boot:run

# Start frontend
cd frontend && npm install && npm run dev
```

---

## 🔑 Key Features

| Feature | Implementation |
|---------|---------------|
| **Authentication** | JWT + Refresh tokens + Redis blacklist on logout |
| **Password Security** | BCrypt (cost 12) |
| **API Security** | Spring Security + JWT filter on all protected routes |
| **Event Streaming** | Apache Kafka — transaction events → notification service |
| **Caching** | Redis (token blacklist + future query caching) |
| **API Documentation** | Swagger/OpenAPI on every service |
| **Database** | PostgreSQL with JPA/Hibernate, schema auto-update |
| **Container** | Docker + Docker Compose, multi-stage builds |
| **Frontend Auth** | Axios interceptors for token refresh + auto-logout |

---

## 📡 API Reference

### Auth Service (`/api/v1/auth`)
| Method | Path | Description |
|--------|------|-------------|
| POST | `/register` | Register new user |
| POST | `/login` | Login, returns JWT |
| POST | `/refresh` | Refresh access token |
| POST | `/validate` | Validate JWT (used by gateway) |
| POST | `/logout` | Blacklist token |

### Account Service (`/api/v1/accounts`)
| Method | Path | Description |
|--------|------|-------------|
| POST | `/` | Create account |
| GET | `/` | Get all accounts |
| GET | `/summary` | Total balance + accounts |
| GET | `/{id}` | Get account by ID |
| PATCH | `/balance` | Update balance (internal) |
| DELETE | `/{id}` | Close account |

### Transaction Service (`/api/v1/transactions`)
| Method | Path | Description |
|--------|------|-------------|
| POST | `/transfer` | Transfer between accounts |
| POST | `/deposit` | Deposit funds |
| POST | `/withdraw` | Withdraw funds |
| GET | `/` | Paginated history |
| GET | `/{id}` | Get transaction by ID |

---

## 🛠️ Tech Stack

**Backend:** Java 17 · Spring Boot 3.2 · Spring Security · Spring Cloud Gateway  
**Messaging:** Apache Kafka · Zookeeper  
**Database:** PostgreSQL 15 · Spring Data JPA · Hibernate  
**Cache:** Redis 7  
**Security:** JWT (jjwt 0.11) · BCrypt · OAuth 2.0 ready  
**Docs:** SpringDoc OpenAPI 3  
**Frontend:** React 18 · Vite · React Router · Axios · Recharts  
**DevOps:** Docker · Docker Compose · multi-stage builds  

---

*Built by Srikanth Koka — Java Full Stack Developer*

# 🕊️ SilentSignals

**SilentSignals** is a secure and real-time **panic alert system** that allows users to **silently trigger SOS alerts** during emergencies.  
Alerts are instantly delivered to **trusted contacts via WebSocket**, ensuring immediate response.  
If contacts are offline, the system automatically falls back to **SMS** and **Email notifications**, guaranteeing reliable delivery.

---

## 🚨 Features

- **Silent SOS Trigger** – Send emergency alerts without drawing attention.  
- **Real-Time Communication** – Instant alert delivery through **WebSocket** connections.  
- **Fallback Notifications** – Automatic **SMS** and **Email** alerts when recipients are offline.  
- **Secure Authentication** – Protected using **JWT (JSON Web Tokens)**.  
- **Scalable Storage** – Built with **PostgreSQL** for data persistence.  
- **Fast Caching & Rate Limiting** – Powered by **Redis** for performance and control.  
- **Comprehensive Logging** – Every alert and notification is tracked for audit and reliability.  

---

## 🧠 System Architecture

**Tech Stack:**
- **Backend Framework:** Spring Boot (Java)
- **Database:** PostgreSQL
- **Cache & Pub/Sub:** Redis
- **Security:** JWT-based Authentication
- **Real-time Communication:** WebSocket
- **Notifications:** Twilio (SMS), JavaMail (Email)

---

## ⚙️ How It Works

1. **User triggers an SOS signal** through the web or mobile interface.  
2. The **WebSocket server** sends alerts in real time to trusted contacts who are online.  
3. If any contact is offline:
   - A **fallback mechanism** sends SMS and Email notifications automatically.  
4. **Redis** handles caching and message queuing to ensure reliability and prevent flooding.  
5. All events are **logged in PostgreSQL** for analytics and monitoring.

---

## 🧩 Project Modules

| Module | Description |
|--------|-------------|
| `security/` | JWT authentication, role-based access control |
| `alert/` | Core SOS alert logic, including WebSocket broadcasting |
| `notification/` | Fallback SMS and Email services |
| `redis/` | Redis configuration for caching and pub/sub |
| `database/` | PostgreSQL entities, repositories, migrations |
| `logging/` | Event logging and monitoring services |

---

## 🚀 Getting Started

### 1. Clone the Repository
```bash
git clone https://github.com/<your-username>/SilentSignals.git
cd SilentSignals

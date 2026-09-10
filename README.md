# Toll Plaza Route Finder Service

A production-grade **Spring Boot REST API** that determines toll plazas located along the driving route between two Indian pincodes.

live link : https://toll-plazas-between-two-pincodes.onrender.com

---

## 🚀 Key Features

- **Route Calculation & Spatial Matching**: Determines driving routes and projects toll plaza coordinates onto route polyline corridors to find all along-the-way toll plazas.
- **In-Memory Caching (`@Cacheable`)**: High-performance caching for route calculations. Identical pincode queries execute in `<5ms`.
- **Database Seeding & Upserts**: Auto-seeds Indian postal pincodes and NHAI Toll Plazas from CSV files into H2 database using batch upsert operations.
- **Validation & Exception Handling**: Enforces 6-digit Indian pincode format (`^[1-9][0-9]{5}$`) and handles edge cases like same source/destination pincodes or unknown pincodes.
- **Interactive UI Dashboard & OpenAPI**: Includes built-in OpenAPI / Swagger UI at `/swagger-ui.html` and an interactive web playground at `/`.
- **100% Test Coverage**: Full TDD implementation with JUnit 5, Mockito, and Spring `@WebMvcTest`.

---

## 🛠️ Architecture & Tech Stack

- **Java Version**: Java 17
- **Framework**: Spring Boot 3.2.3 (Spring Web, Spring Data JPA, Spring Cache, Validation)
- **Database**: H2 Embedded In-Memory Database (with H2 Console at `/h2-console`)
- **Routing Engine**: OSRM (Open Source Routing Machine) API with Haversine Spatial Corridor Fallback
- **Documentation**: Springdoc OpenAPI 3.0 / Swagger UI
- **Testing**: JUnit 5, Mockito, Spring Boot Starter Test

---

## 📋 API Specification

### Endpoint: `POST /api/v1/toll-plazas`

#### Request Body:
```json
{
  "sourcePincode": "110001",
  "destinationPincode": "560001"
}
```

#### Success Response (`200 OK`):
```json
{
  "route": {
    "sourcePincode": "110001",
    "destinationPincode": "560001",
    "distanceInKm": 2050
  },
  "tollPlazas": [
    {
      "name": "Jewar Toll Plaza",
      "latitude": 28.12,
      "longitude": 77.55,
      "distanceFromSource": 43
    },
    {
      "name": "Kherki Daula Toll Plaza",
      "latitude": 28.378,
      "longitude": 76.974,
      "distanceFromSource": 64
    }
  ]
}
```

#### Error Response - Same Source & Destination (`400 Bad Request`):
```json
{
  "error": "Source and destination pincodes cannot be the same"
}
```

#### Error Response - Invalid Pincode (`400 Bad Request`):
```json
{
  "error": "Invalid source or destination pincode"
}
```

---

## ⚡ Quick Start & Running the Project

### Prerequisites
- JDK 17+ installed
- Maven 3.8+ installed

### Build & Run Unit/Integration Tests
```bash
mvn clean test
```

### Run Application
```bash
mvn spring-boot:run
```
The application will start on **http://localhost:8081** (or the value of `PORT` if set).

---

## Deploy on Render

This service is a JVM app, so Render builds it from the included `Dockerfile`. The GitHub repo is already connected as `origin`.

1. Push the latest `main` branch to GitHub.
2. In the [Render Dashboard](https://dashboard.render.com/), choose **New → Blueprint** and select `fabiomughilan/Freight-Fox-Toll-Plazas-Between-Two-Pincodes`, **or** create a **Web Service** from that repo and set:
   - **Runtime**: Docker
   - **Dockerfile path**: `./Dockerfile`
   - **Health check path**: `/`
3. After the first deploy succeeds, open the Render URL. The dashboard is at `/`, Swagger at `/swagger-ui.html`, and the API at `POST /api/v1/toll-plazas`.

Render injects `PORT` automatically. Locally the app still defaults to `8081`.

---

## 🧪 Postman & UI Testing

1. **Interactive Dashboard**: Open `http://localhost:8081` in your browser.
2. **Postman Collection**: Import `TollPlazaAPI.postman_collection.json` located in the root directory into Postman.

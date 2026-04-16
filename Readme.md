# BookCircle Backend

Backend API for geolocation-based book clubs with spoiler-safe discussions.

## Problem Statement

Readers often struggle to find nearby communities for the same book, and group discussions easily leak spoilers. This backend solves both issues by combining location-based room discovery with chapter-based comment visibility.

## Features

- JWT authentication and role-based access (`USER`, `MODERATOR`, `ADMIN`)
- Book catalog management
- Room creation and joining for reading clubs
- H3 geospatial indexing for nearby room discovery
- Reading progress tracking by chapter
- Spoiler-safe comments filtered by user progress
- Swagger/OpenAPI documentation

## Installation

### Prerequisites

- Java 17
- Maven 3.9+
- PostgreSQL 14+

### Setup

1. Clone the repository:

```bash
git clone https://github.com/danniyeres/bookcircle-capstone.git
cd bookcircle-capstone-backend
```

2. Create environment file from template:

```bash
cp .env.example .env
```

3. Update `.env` values for your PostgreSQL instance.

4. Run the app:

```bash
mvn spring-boot:run
```

## Usage

- Base URL: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

### Authentication flow

1. `POST /auth/register` to create an account.
2. `POST /auth/login` to receive JWT access token.
3. Add header: `Authorization: Bearer <token>` for protected endpoints.

### Docker (optional)

```bash
docker compose up --build
```

## Screenshots

Not applicable for backend-only repository. API exploration can be done via Swagger UI.

## Technology Stack

- Java 17
- Spring Boot 3.3.x
- Spring Security
- Spring Data JPA
- PostgreSQL
- JWT (`jjwt`)
- H3 (`com.uber:h3`)
- Springdoc OpenAPI
- Docker / Docker Compose

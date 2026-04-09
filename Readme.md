# BookCircle Project Overview

**BookCircle** is a geolocation-based social platform for book enthusiasts, designed to help users create and discover local book clubs, and discuss books collaboratively without the risk of encountering spoilers.

## Core Concept and Features

Based on the repository analysis (especially the backend API architecture), the project solves two main problems of collaborative reading: finding like-minded people nearby and ensuring spoiler-free discussions.

### Key Features:

1. **Local Book Clubs (Rooms)**
   * Users can create and join rooms (clubs) that are tied to a specific book and precise geographical coordinates (latitude and longitude).
   * To find nearby clubs, the system uses **H3** (a hexagonal hierarchical spatial index initially developed by Uber). This allows the algorithm to quickly locate reading groups on the map within a given radius.

2. **Smart Spoiler Protection (Spoiler-safe comments)**
   * The most interesting mechanic of the project: the comment system is strictly tied to the individual user's reading progress.
   * A club member only sees messages in the chat or discussion related to the chapters they have already finished reading (`comment.chapterNumber <= user.progress.chapterNumber`). If someone discusses the ending, a reader who is only halfway through the book will not see it.

3. **Progress Tracking**
   * Inside each room, users regularly update their current status (which chapter they have finished), which directly syncs with the comment visibility system.

4. **Role System and Administration**
   * The platform uses JWT authentication and supports three access levels: `USER` (regular member), `MODERATOR` (club management), and `ADMIN` (adding new books to the global database, viewing analytics, and checking audit logs).

## Technology Stack

### Backend (`danniyeres/bookcircle-capstone`)
* **Language & Framework:** Written entirely in Java. Based on the file structure (`pom.xml`) and endpoint styles, it is powered by **Spring Boot**.
* **Database & API:** API documentation is generated via Swagger / OpenAPI. It features comprehensive REST controllers for authentication, books, rooms, comments, and progress.
* **Geo-indexing:** Implements a dedicated utility endpoint (`/h3/encode`) to convert latitude and longitude into an H3 index.

### Frontend (`Zhasulann06/bookcircle-frontend`)
* **Tools:** Built with **React** using the **Vite** bundler.
* **Language:** JavaScript (with basic ESLint configuration).
* **State:** Currently, the repository contains a foundational app template. Its main goal is to provide the user interface for interacting with the backend API (maps for finding clubs, reading progress interfaces, registration forms, and chats).




# BookCircle Backend --- API Documentation

Base URL (local): `http://localhost:8080`

## Authentication (JWT)

1.  POST `/auth/login` → receive `accessToken`
2.  Add header to protected endpoints:

Authorization: Bearer `<accessToken>`{=html}

Roles: - USER - MODERATOR - ADMIN

------------------------------------------------------------------------

## Swagger

-   Swagger UI: `/swagger-ui/index.html`
-   OpenAPI JSON: `/v3/api-docs`

------------------------------------------------------------------------

# ENDPOINTS

## AUTH

### Register

POST `/auth/register`

Request:

``` json
{
  "email": "user@mail.com",
  "password": "123456"
}
```

Response:

``` json
{
  "accessToken": "jwt...",
  "tokenType": "Bearer",
  "userId": 1,
  "role": "USER"
}
```

------------------------------------------------------------------------

### Login

POST `/auth/login`

Request:

``` json
{
  "email": "user@mail.com",
  "password": "123456"
}
```

Response:

``` json
{
  "accessToken": "jwt...",
  "tokenType": "Bearer",
  "userId": 1,
  "role": "USER"
}
```

------------------------------------------------------------------------

## BOOKS

### Get books

GET `/books?query=<optional>`

Header: Authorization: Bearer ...

Response:

``` json
[
  {
    "id": 1,
    "title": "Clean Code",
    "author": "Robert C. Martin",
    "isbn": "123",
    "totalChapters": 17
  }
]
```

------------------------------------------------------------------------

### Create book (ADMIN)

POST `/books`

Header: Authorization: Bearer ...

Request:

``` json
{
  "title": "Clean Architecture",
  "author": "Robert C. Martin",
  "isbn": "456",
  "totalChapters": 20
}
```

------------------------------------------------------------------------

## ROOMS

### Create room

POST `/rooms`

Request:

``` json
{
  "name": "My Book Club",
  "bookId": 1,
  "lat": 43.238949,
  "lon": 76.889709,
  "resolution": 9
}
```

------------------------------------------------------------------------

### Join room

POST `/rooms/{roomId}/join`

------------------------------------------------------------------------

### Find rooms by H3

GET `/rooms/by-h3?h3Index=...`

------------------------------------------------------------------------

## PROGRESS

### Update progress

POST `/progress`

Request:

``` json
{
  "roomId": 10,
  "chapterNumber": 5
}
```

------------------------------------------------------------------------

## COMMENTS

### Create comment

POST `/comments`

Request:

``` json
{
  "roomId": 10,
  "chapterNumber": 5,
  "content": "Interesting chapter!"
}
```

------------------------------------------------------------------------

### Get visible comments (spoiler-safe)

GET `/comments?roomId=10`

Returns only comments where: comment.chapterNumber \<=
user.progress.chapterNumber

------------------------------------------------------------------------

## ADMIN

### Room stats

GET `/admin/stats/{roomId}`

### Audit log (ADMIN only)

GET `/admin/audit`

------------------------------------------------------------------------

## H3 Utility

### Encode lat/lon

GET `/h3/encode?lat=...&lon=...&res=9`

Response:

``` json
{
  "h3Index": "8928308280fffff"
}
```

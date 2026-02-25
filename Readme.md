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

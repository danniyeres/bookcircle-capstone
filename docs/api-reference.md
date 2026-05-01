# BookCircle Backend API Reference

This document is a practical API guide for frontend developers.

## Base Information

- Base URL: `http://localhost:8080`
- Auth type: `Bearer JWT`
- Swagger UI: `/swagger-ui/index.html`
- OpenAPI JSON: `/v3/api-docs`
- Content-Type for requests with body: `application/json`
- All timestamps are ISO-8601 (example: `2026-05-01T10:30:00Z`)

## Authentication

All endpoints are protected except:

- `POST /auth/register`
- `POST /auth/login`
- `GET /swagger-ui/**`
- `GET /v3/api-docs/**`
- `GET /h2/**` (dev only)

Use this header for protected endpoints:

```http
Authorization: Bearer <accessToken>
```

## Roles and Permissions

Roles in system:

- `USER`
- `MODERATOR`
- `ADMIN`

High-level permissions:

- `ADMIN`: can promote users to `ADMIN`/`MODERATOR`, create/delete books, delete rooms, delete comments, view audit logs.
- `MODERATOR`: can delete rooms and comments, can view room stats.
- `USER`: regular participant features (join rooms, progress, comments, own profile update, see room members/progress if member).

## Error Response Format

Business/API errors:

```json
{
  "error": "Room not found"
}
```

Validation errors:

```json
{
  "error": "validation_failed",
  "details": "..."
}
```

Unexpected server error:

```json
{
  "error": "internal_server_error"
}
```

## Endpoint Summary

| Method | Path | Auth | Role | Purpose |
|---|---|---|---|---|
| POST | `/auth/register` | No | Public | Register user |
| POST | `/auth/login` | No | Public | Login and get JWT |
| GET | `/users/me` | Yes | Any authenticated | Get current profile |
| PATCH | `/users/me` | Yes | Any authenticated | Update email/nickname/phone/password |
| GET | `/books` | Yes | Any authenticated | Get books (optional search) |
| GET | `/books/{id}` | Yes | Any authenticated | Get one book by id |
| POST | `/books` | Yes | ADMIN | Create book |
| DELETE | `/books/{id}` | Yes | ADMIN | Delete book (and related rooms data) |
| POST | `/rooms` | Yes | Any authenticated | Create room |
| POST | `/rooms/{roomId}/join` | Yes | Any authenticated | Join room |
| GET | `/rooms` | Yes | Any authenticated | List all rooms |
| GET | `/rooms/by-h3?h3Index=...` | Yes | Any authenticated | List rooms by H3 index |
| GET | `/rooms/{roomId}/members/progress` | Yes | Room member only | Members and their progress |
| DELETE | `/rooms/{roomId}` | Yes | ADMIN, MODERATOR | Delete room |
| POST | `/progress` | Yes | Room member only | Update chapter progress |
| POST | `/comments` | Yes | Room member only | Create comment |
| GET | `/comments?roomId=...` | Yes | Room member only | Get visible comments (spoiler-safe) |
| DELETE | `/comments/{commentId}` | Yes | ADMIN, MODERATOR | Delete comment |
| GET | `/admin/audit` | Yes | ADMIN | Last 100 audit records |
| GET | `/admin/stats/{roomId}` | Yes | ADMIN, MODERATOR | Room activity stats |
| PATCH | `/admin/users/{userId}/role` | Yes | ADMIN | Set user role (`ADMIN`/`MODERATOR`) |
| GET | `/h3/encode?lat=...&lon=...&res=...` | Yes | Any authenticated | Convert coordinates to H3 |

## Detailed Endpoints

### 1) Auth

#### Register

- `POST /auth/register`

Request:

```json
{
  "email": "reader@example.com",
  "password": "secret123"
}
```

Response:

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI...",
  "tokenType": "Bearer",
  "userId": 10,
  "role": "USER"
}
```

#### Login

- `POST /auth/login`

Request:

```json
{
  "email": "reader@example.com",
  "password": "secret123"
}
```

Response: same shape as register response.

### 2) Users

#### Get my profile

- `GET /users/me`

Response:

```json
{
  "userId": 10,
  "email": "reader@example.com",
  "nickname": "bookworm",
  "phoneNumber": "+77001234567",
  "role": "USER",
  "createdAt": "2026-05-01T10:30:00Z"
}
```

#### Update my profile

- `PATCH /users/me`

Request fields are optional:

- `email`
- `nickname`
- `phoneNumber`
- `currentPassword`
- `newPassword`

Important:

- To change password, both `currentPassword` and `newPassword` are required.
- `email`, `nickname`, and `phoneNumber` must be unique.
- `phoneNumber` format: `+` optional, then digits only, length 7..20.

Request example:

```json
{
  "email": "reader.new@example.com",
  "nickname": "new_reader",
  "phoneNumber": "+77001234567",
  "currentPassword": "secret123",
  "newPassword": "secret456"
}
```

Response: same shape as `GET /users/me`.

### 3) Books

#### Get books

- `GET /books`
- Optional query param: `query`

Examples:

- `GET /books`
- `GET /books?query=harry`

Response:

```json
[
  {
    "id": 1,
    "title": "Sample Book",
    "author": "Author Name",
    "isbn": "9781234567890",
    "coverUrl": "https://...",
    "description": "Book description",
    "totalChapters": 20
  }
]
```

#### Get book by id

- `GET /books/{id}`

Response: one `Book` object.

#### Create book (ADMIN)

- `POST /books`

Request:

```json
{
  "title": "New Book",
  "author": "Author Name",
  "isbn": "9781234567890",
  "coverUrl": "https://...",
  "description": "Text",
  "totalChapters": 30
}
```

Response: created `Book`.

#### Delete book (ADMIN)

- `DELETE /books/{id}`

Behavior:

- Deletes book.
- Also deletes related rooms and room data (members, comments, progress, stats).

Response: empty body.

### 4) Rooms

#### Create room

- `POST /rooms`

Request:

```json
{
  "name": "Evening Readers",
  "bookId": 1,
  "h3Index": "8928308280fffff"
}
```

Alternative request (without `h3Index`):

```json
{
  "name": "Evening Readers",
  "bookId": 1,
  "lat": 43.2389,
  "lon": 76.8897,
  "resolution": 9
}
```

Response:

```json
{
  "id": 12,
  "name": "Evening Readers",
  "bookId": 1,
  "bookTitle": "Sample Book",
  "h3Index": "8928308280fffff",
  "ownerId": 10
}
```

#### Join room

- `POST /rooms/{roomId}/join`

Response: empty body.

#### Get all rooms

- `GET /rooms`

Response: array of `RoomResponse`.

#### Get rooms by H3

- `GET /rooms/by-h3?h3Index=...`

Response: array of `RoomResponse`.

#### Get room members and progress

- `GET /rooms/{roomId}/members/progress`

Access rule:

- Caller must be a member of the room.

Response:

```json
{
  "roomId": 12,
  "members": [
    {
      "userId": 10,
      "email": "reader@example.com",
      "nickname": "bookworm",
      "roomRole": "OWNER",
      "chapterNumber": 7,
      "progressUpdatedAt": "2026-05-01T10:45:00Z"
    },
    {
      "userId": 11,
      "email": "friend@example.com",
      "nickname": "friend",
      "roomRole": "MEMBER",
      "chapterNumber": 0,
      "progressUpdatedAt": null
    }
  ]
}
```

#### Delete room (ADMIN or MODERATOR)

- `DELETE /rooms/{roomId}`

Response: empty body.

### 5) Progress

#### Update progress

- `POST /progress`

Request:

```json
{
  "roomId": 12,
  "chapterNumber": 7
}
```

Response:

```json
{
  "roomId": 12,
  "userId": 10,
  "chapterNumber": 7
}
```

### 6) Comments

#### Create comment

- `POST /comments`

Request:

```json
{
  "roomId": 12,
  "chapterNumber": 6,
  "content": "I love this chapter ending."
}
```

Response:

```json
{
  "id": 101,
  "roomId": 12,
  "authorId": 10,
  "chapterNumber": 6,
  "content": "I love this chapter ending.",
  "createdAt": "2026-05-01T10:50:00Z"
}
```

#### Get visible comments (spoiler-safe)

- `GET /comments?roomId=12`

Access rule:

- Caller must be room member.

Visibility rule:

- User sees only comments where `comment.chapterNumber <= user.chapterNumber` in this room.

Response: array of `CommentResponse`.

#### Delete comment (ADMIN or MODERATOR)

- `DELETE /comments/{commentId}`

Response: empty body.

### 7) Admin

#### Get audit log (ADMIN)

- `GET /admin/audit`

Response:

- Array of latest 100 audit entries.

#### Get room stats (ADMIN or MODERATOR)

- `GET /admin/stats/{roomId}`

Response:

```json
{
  "roomId": 12,
  "commentsCount": 50,
  "progressUpdatesCount": 40,
  "lastEventAt": "2026-05-01T10:58:00Z"
}
```

#### Update user role (ADMIN)

- `PATCH /admin/users/{userId}/role`

Request:

```json
{
  "role": "MODERATOR"
}
```

Allowed values:

- `ADMIN`
- `MODERATOR`

Response:

```json
{
  "userId": 11,
  "email": "friend@example.com",
  "role": "MODERATOR"
}
```

### 8) H3 Utility

#### Encode coordinates to H3

- `GET /h3/encode?lat=43.2389&lon=76.8897&res=9`

Response:

```json
{
  "h3Index": "8928308280fffff",
  "lat": 43.2389,
  "lon": 76.8897,
  "resolution": 9
}
```

## Frontend Integration Notes

- Save JWT from `/auth/login` and send it in `Authorization` header.
- Refresh user profile after successful `/users/me` update.
- For room UI, call `/rooms/{roomId}/members/progress` to render participant list and reading state.
- For spoiler-safe comments, always read comments via `GET /comments?roomId=...`, not from cached local data only.
- Hide admin/moderator actions in UI based on `role` from auth/profile response.

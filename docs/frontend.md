# BookCircle Frontend

Frontend SPA for the BookCircle capstone project.

## Problem Context

Readers need a simple interface to discover nearby reading rooms and discuss books without spoilers.
This frontend provides that workflow and integrates with the BookCircle backend API.

## What This Module Does

- User authentication (register/login)
- Dashboard with books and rooms overview
- Book browsing and search
- Room discovery and joining
- Room creation with map/location inputs
- Chapter progress updates
- Spoiler-safe comment feed in room pages
- Profile management
- Admin panel (role-restricted)

## Tech Specs

- React 19
- Vite 8
- React Router
- Zustand
- Axios
- Leaflet + React Leaflet
- Playwright (E2E testing)

## Environment

Create `.env` from template:

```bash
cp .env.example .env
```

Required variable:

- `VITE_API_URL=http://127.0.0.1:8080`

## Run Locally

```bash
npm install
npm run dev
```

App is available at:

- `http://localhost:5173`

## Build

```bash
npm run build
npm run preview
```

## Deployment

- Live app: [https://bookcircle-frontend.onrender.com](https://bookcircle-frontend.onrender.com)

## Tests

```bash
npx playwright test
```

## Student IDs

- Daniyar Yeleussiz — `230103200`
- Nuray Praliyeva — `230103057`
- Anuarbek Zhasulan — `230103166`

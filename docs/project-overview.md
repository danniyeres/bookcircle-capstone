# BookCircle Backend Overview

This document provides extra context for the backend architecture and the core product idea.

## Concept

BookCircle is a social reading platform where users can:

- discover reading rooms near their location,
- join ongoing book discussions,
- avoid spoilers through chapter-aware comment filtering.

## Backend Responsibilities

- Authentication and authorization with JWT
- Book and room management
- Reading progress persistence
- Chapter-based spoiler filtering for comments
- Geospatial indexing with H3 for location-aware room discovery
- API documentation via Swagger/OpenAPI

For setup and endpoint usage, refer to the main repository README.

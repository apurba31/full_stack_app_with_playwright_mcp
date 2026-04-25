# CLAUDE.md

## Project: FullStack App
## Stack: React (Vite + TailwindCSS), Spring Boot, Kafka, Docker, gRPC

## Architecture
- /frontend      → React app (Vite, Tailwind, Shadcn/ui)
- /backend       → Spring Boot (REST + gRPC + Kafka)
- /proto         → Protobuf definitions
- /docker        → Docker Compose configs
- /tests         → Integration + E2E tests

## Conventions
- Java 21, Spring Boot 3.x
- React 18 with TypeScript
- All services must be containerized
- Every new feature must have unit + integration tests
- REST controllers → /api/v1/**
- gRPC services → port 9090
- Kafka topics follow: {domain}.{event}.{version}

## Agent Rules
- Never skip tests
- Prefer reversible actions (git commit before major changes)
- Run `./mvnw verify` to validate backend
- Run `npm run test` to validate frontend
- Always check Docker Compose health before declaring done
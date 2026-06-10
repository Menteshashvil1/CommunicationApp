# Communications App

A Java 21 Spring Boot communications app built step by step.

## Current Milestone

- Spring Boot application skeleton
- REST health endpoint
- First controller test
- PostgreSQL Docker Compose service for later milestones

## Run PostgreSQL

```powershell
docker compose up -d
```

## Run Tests

```powershell
mvn test
```

## Run The App

```powershell
mvn spring-boot:run
```

Then call:

```powershell
Invoke-RestMethod http://localhost:8080/api/health
```

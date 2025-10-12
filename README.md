# Licensing Service

## Overview
The Licensing Service manages license records and enriches them with organization metadata retrieved through discovery, REST, Feign, or Spring interface clients. It starts on port `8080` (default) and registers with Eureka once configuration is loaded from the config server.

## REST API
- `GET http://localhost:8080/v1/organization/{organizationId}/license/{licenseId}` – return a license with HATEOAS links.
- `GET http://localhost:8080/v1/organization/{organizationId}/license/{licenseId}/{clientType}` – retrieve a license enriched via the specified client (`DISCOVERY`, `REST`, `FEIGN`, `SPRING`).
- `POST http://localhost:8080/v1/organization/{organizationId}/license/` – create a license.
- `PUT http://localhost:8080/v1/organization/{organizationId}/license/` – update a license.
- `DELETE http://localhost:8080/v1/organization/{organizationId}/license/{licenseId}` – delete a license.

## Actuator Endpoints

- `GET http://localhost:8080/actuator/health`
- `GET http://localhost:8080/actuator/env`
- `POST http://localhost:8080/actuator/refresh`

## Configuration & Profiles
The service defaults to the `dev` profile:

```
spring.profiles.active=dev
spring.config.import=optional:configserver:http://localhost:8071
```

- `docker` profile: resolves the config server via service discovery (`http://config-service:8071`).
- `cds` profile: disables schema initialization for the CDS training build and points to the training database stub.

## Running Locally
1. Start Config Server
2. Start Eureka Server
3. Start Organization Service (required so license responses are enriched with organization data)
4. Launch this service with `./mvnw spring-boot:run`.

## Compose Support

Spring Boot's Docker Compose support is already enabled via the `spring-boot-docker-compose` dependency. 

Running `mvnw spring-boot:run` also launches the postgres defined on `compose.yml` and provide the scripts found on
`./compose/` to postgres' entrypoint.
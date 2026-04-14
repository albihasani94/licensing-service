# Licensing Service

## Overview
The Licensing Service manages license records and enriches them with organization metadata retrieved through discovery, REST, Feign, or Spring interface clients. It starts on port `8080` (default) and registers with Eureka once configuration is loaded from the config server.

## REST API
- `GET http://localhost:8080/v1/organization/{organizationId}/license/{licenseId}` – return a license with HATEOAS links.
- `GET http://localhost:8080/v1/organization/{organizationId}/license/{licenseId}/{clientType}` – retrieve a license enriched via the specified client (`DISCOVERY`, `REST`, `FEIGN`, `SPRING`).
Up- `POST http://localhost:8080/v1/organization/{organizationId}/license` – create a license.
- `PUT http://localhost:8080/v1/organization/{organizationId}/license` – update a license.
- `DELETE http://localhost:8080/v1/organization/{organizationId}/license/{licenseId}` – delete a license.

## Actuator Endpoints

- `GET http://localhost:8080/actuator/health`
- `GET http://localhost:8080/actuator/env` if that endpoint is exposed by external config
- `POST http://localhost:8080/actuator/refresh` if that endpoint is exposed by external config

## Configuration & Profiles
The service defaults to the `dev` profile:

```
spring.profiles.active=dev
spring.config.import=optional:configserver:http://localhost:8071
```

- `docker` profile: points to the config server at `http://config-service:8071`.
- `cds` profile: disables schema initialization for the CDS training build and points to the training database stub.

## Running Locally
1. Use Java `25`.
2. Start Config Server.
3. Start Eureka Server.
4. Start Organization Service (required so license responses are enriched with organization data).
5. Launch this service with `./mvnw spring-boot:run`.

## Compose Support

Spring Boot's Docker Compose support is already enabled via the `spring-boot-docker-compose` dependency.

Running `./mvnw spring-boot:run` also launches the Postgres service defined in `compose.yml` and mounts the scripts in
`./compose/` into Postgres' entrypoint initialization directory.

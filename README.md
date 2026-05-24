# Licensing Service

## Overview
The Licensing Service manages license records and enriches them with organization metadata retrieved through discovery, REST, Feign, or Spring interface clients. It starts on port `8080` (default), registers with Eureka once configuration is loaded from the config server, and uses Redis-backed caching for organization lookups.

## REST API
- `GET http://localhost:8080/v1/organization/{organizationId}/license/{licenseId}` – return a license with HATEOAS links.
- `GET http://localhost:8080/v1/organization/{organizationId}/license/{licenseId}/{clientType}` – retrieve a license enriched via the specified client (`DISCOVERY`, `REST`, `FEIGN`, `SPRING`).
- `POST http://localhost:8080/v1/organization/{organizationId}/license` – create a license.
- `PUT http://localhost:8080/v1/organization/{organizationId}/license` – update a license.
- `DELETE http://localhost:8080/v1/organization/{organizationId}/license/{licenseId}` – delete a license.

Requests require a bearer JWT. Read, create, and update operations require a `roles` claim containing `user` or `admin`; delete requires `admin`.

## Actuator Endpoints

- `GET http://localhost:8080/actuator/health`
- `GET http://localhost:8080/actuator/prometheus`
- `GET http://localhost:8080/actuator/env` if that endpoint is exposed by external config and the request is authenticated
- `POST http://localhost:8080/actuator/refresh` if that endpoint is exposed by external config and the request is authenticated

## Configuration & Profiles
The service defaults to the `dev` profile:

```
spring.profiles.active=dev
spring.config.import=optional:configserver:http://localhost:8071
```

- `docker` profile: points to the config server at `http://config-service:8071`.
- `cds` profile: disables schema initialization for the CDS training build and points to the training database stub.
- OAuth2 resource server, service discovery, Redis, and Spring Cloud Stream settings are expected from the config server or local overrides.

## Running Locally
1. Use Java `26`.
2. Start Config Server.
3. Start Eureka Server.
4. Start Organization Service (required so license responses are enriched with organization data).
5. Start Redis, or provide Redis connection settings through external config.
6. Start Kafka if the active external config binds the `organizationEvents` Spring Cloud Stream consumer.
7. Launch this service with `./mvnw spring-boot:run`.

## Compose Support

Spring Boot's Docker Compose support is already enabled via the `spring-boot-docker-compose` dependency.

Running `./mvnw spring-boot:run` also launches the Postgres service defined in `compose.yml` and mounts the scripts in
`./compose/` into Postgres' entrypoint initialization directory.

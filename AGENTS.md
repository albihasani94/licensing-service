# Engineering Preferences

Optimize for the simplest end-to-end design, not only the smallest local code change.

Before adding custom code, check whether the same outcome can be achieved by using existing framework conventions, configuration, or established project patterns.

For Spring applications, prefer the simplest Spring-native solution. Use Spring Boot auto-configuration, Spring Security conventions, standard properties, and built-in extension points before adding custom adapters or framework bypasses.

When adding dependencies, check the Spring Boot managed dependency list first. Prefer managed coordinates and omit explicit versions when possible; explicit versions should be intentional and briefly justified.

When there are multiple viable approaches, briefly compare:
- local code changes
- configuration changes
- built-in framework behavior
- existing project conventions

Prefer the option with the least long-term complexity, as long as it remains clear, testable, and maintainable.

# Project Notes

This service currently targets Java `26`, Spring Boot `4.0.6`, and Spring Cloud `2025.1.1`. Use the Maven wrapper (`./mvnw`) for builds, tests, and local runs.

The application depends on externalized configuration through Spring Cloud Config. Prefer profile-specific properties, config server values, and Spring Boot conventions over hardcoded local service URLs or custom bootstrap code.

Security is OAuth2 resource-server based. Preserve the existing Spring Security model unless there is a clear reason to change it: health and Prometheus actuator endpoints are public, other endpoints require authentication, and controller methods enforce role checks through method security.

Organization lookups combine load-balanced clients, bearer-token relay, Resilience4j policies, and Redis caching. When changing this area, keep those cross-cutting concerns intact or explicitly document why they are no longer needed.

If ports, profiles, required backing services, security behavior, or managed dependency versions change, update `README.md` in the same change.

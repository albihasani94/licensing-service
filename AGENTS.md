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

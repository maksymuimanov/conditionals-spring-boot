# conditionals-spring-boot

[![Maven Central](https://img.shields.io/maven-central/v/io.github.w4t3rcs/spring-boot-python-executor-starter?style=for-the-badge&logo=apache-maven&color=dgreen)](https://central.sonatype.com/artifact/io.github.maksymuimanov/conditionals-spring-boot)
![Java](https://img.shields.io/badge/Java-17%2B-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.11%2B-brightgreen?style=for-the-badge&logo=spring-boot)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge&logo=open-source-initiative)](https://opensource.org/licenses/MIT)
![Tests Passed](https://img.shields.io/badge/Tests%20Passed-100%25-green?style=for-the-badge&logo=checkmarx)

A small Java library providing additional Spring Boot conditional annotations for environment-driven configuration selection.

## Motivation

Spring Boot provides general-purpose condition mechanisms such as `@ConditionalOnProperty` and `@ConditionalOnExpression`. These cover many scenarios, but they can be insufficient when you need:

- Explicit, type-oriented comparisons (for example, numeric comparisons rather than string equality).
- Predictable evaluation rules and error handling without relying on dynamic expression languages.
- Repeatable conditional declarations with well-defined aggregation semantics.

Use this library when:

- You need simple, explicit conditions for configuration classes or `@Bean` methods based on property values.
- You want comparison behavior to be expressed declaratively in annotations (including ordering, case handling, and negation where applicable).

Do not use this library when:

- You need to express complex boolean logic across multiple unrelated inputs (prefer explicit configuration code or Spring Expression Language).
- You require conditions that depend on arbitrary runtime state beyond the Spring `Environment`.
- You require the full flexibility of SpEL or custom condition implementations.

## Features

- **Property-based conditions**
  - String property matching.
  - Comparable/numeric property comparisons for integer, long, float, double.
  - Duration property comparisons with Spring Boot duration parsing.
  - Collection and map matching semantics.
  - Enum property matching with explicit enum type conversion.
- **OS-based condition**
  - Substring match against the resolved `os.name` value.
- **Port availability condition**
  - Matches when all specified TCP ports can be bound.
- **Repeatable annotation support**
  - Repeatable variants for property-based conditions using a container annotation.
- **Explicit semantics**
  - Documented property key composition, missing-property behavior, and conversion handling.

## Installation

### Maven

```xml
<dependency>
    <groupId>io.github.maksymuimanov</groupId>
    <artifactId>conditionals-spring-boot</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

```gradle
dependencies {
    implementation "io.github.maksymuimanov:conditionals-spring-boot:1.0.0"
}
```

## Usage

### Example configuration properties

```properties
app.mode=prod
app.threads=8
app.timeout=30s
app.tags=red,green,blue
app.labels.env=prod
app.labels.region=eu
app.ratio=0.75
app.level=INFO
```

### Example annotated class

```java
import io.conditionals.condition.ConditionalOnEnumProperty;
import io.conditionals.condition.ConditionalOnDurationProperty;
import io.conditionals.condition.ConditionalOnFloatProperty;
import io.conditionals.condition.ConditionalOnIntegerProperty;
import io.conditionals.condition.ConditionalOnMapProperty;
import io.conditionals.condition.ConditionalOnCollectionProperty;
import io.conditionals.condition.ConditionalOnStringProperty;
import io.conditionals.condition.spec.ComparableMatchType;
import io.conditionals.condition.spec.MapMatchType;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnStringProperty(prefix = "app", name = "mode", havingValue = "prod")
@ConditionalOnIntegerProperty(
        prefix = "app",
        name = "threads",
        havingValue = 4,
        matchType = ComparableMatchType.GREATER_THAN_OR_EQUAL
)
@ConditionalOnFloatProperty(prefix = "app", name = "ratio", havingValue = 0.75f)
@ConditionalOnDurationProperty(prefix = "app", name = "timeout", havingValue = "10s", matchType = ComparableMatchType.GREATER_THAN)
@ConditionalOnCollectionProperty(prefix = "app", name = "tags", havingValue = {"red", "blue"})
@ConditionalOnMapProperty(prefix = "app", name = "labels", havingValue = {"env", "prod", "region", "eu"}, matchType = MapMatchType.CONTAINS_ALL)
@ConditionalOnEnumProperty(prefix = "app", name = "level", havingValue = "INFO", enumType = Level.class)
class ConditionalConfiguration {
    enum Level { TRACE, DEBUG, INFO, WARN, ERROR }
}
```

### Explanation of behavior

- Conditions are evaluated by Spring Boot during configuration processing for `@Configuration` classes and `@Bean` methods.
- For repeatable annotations, each annotation instance is evaluated and outcomes are aggregated with AND semantics: if any evaluated instance is a non-match, the final outcome is a non-match.
- Within a single annotation instance, configured property names are evaluated in declared array order.
- Missing properties yield a non-match unless the annotation provides `matchIfMissing=true`.

## Supported Conditions

| Annotation                         | Description                                                                   | Notes                                                                                                                                                                                                       |
|------------------------------------|-------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `@ConditionalOnStringProperty`     | Matches based on one or more string properties using a selectable match mode. | Supports `ignoreCase`, `trim`, `not`, and match modes `EQUALS`, `CONTAINS`, `STARTS_WITH`, `ENDS_WITH`, `MATCHES` (regex via `String#matches`). Repeatable; container: `@ConditionalOnStringProperties`.    |
| `@ConditionalOnIntegerProperty`    | Matches based on integer property comparison.                                 | Supports `not` and `ComparableMatchType` comparison modes. Repeatable; container: `@ConditionalOnIntegerProperties`.                                                                                        |
| `@ConditionalOnLongProperty`       | Matches based on long property comparison.                                    | Supports `not` and `ComparableMatchType` comparison modes. Repeatable; container: `@ConditionalOnLongProperties`.                                                                                           |
| `@ConditionalOnFloatProperty`      | Matches based on float property comparison.                                   | Supports `not` and `ComparableMatchType` comparison modes. Repeatable; container: `@ConditionalOnFloatProperties`.                                                                                          |
| `@ConditionalOnDoubleProperty`     | Matches based on double property comparison.                                  | Supports `not` and `ComparableMatchType` comparison modes. Repeatable; container: `@ConditionalOnDoubleProperties`.                                                                                         |
| `@ConditionalOnDurationProperty`   | Matches based on duration property comparison.                                | Parses both the resolved property and the candidate via `DurationStyle.detectAndParse`. Supports `not` and `ComparableMatchType`. Repeatable; container: `@ConditionalOnDurationProperties`.                |
| `@ConditionalOnCharacterProperty`  | Matches based on character property comparison.                               | Backed by a comparable condition. Supports `not` and `ComparableMatchType`. Repeatable; container: `@ConditionalOnCharacterProperties`.                                                                     |
| `@ConditionalOnCollectionProperty` | Matches based on `String[]` collection semantics.                             | Supports `not`, `size`, and `CollectionMatchType`. Repeatable; container: `@ConditionalOnCollectionProperties`.                                                                                             |
| `@ConditionalOnMapProperty`        | Matches based on map-like properties under `prefix + name`.                   | Candidate is provided as key/value pairs. Supports `not` and `MapMatchType`. Repeatable; container: `@ConditionalOnMapProperties`.                                                                          |
| `@ConditionalOnEnumProperty`       | Matches based on enum constant equality.                                      | Property and candidate are normalized using `Locale.ROOT` upper-casing and converted via `Enum#valueOf`. Invalid values are treated as non-matching. Repeatable; container: `@ConditionalOnEnumProperties`. |
| `@ConditionalOnOs`                 | Matches based on the current OS name.                                         | Resolves `os.name` from the Spring `Environment`, falling back to `System.getProperty`. Matches if any configured token is a substring of the resolved OS name (case-insensitive via `Locale.ROOT`).        |
| `@ConditionalOnPortAvailable`      | Matches when all specified ports are available for binding.                   | Probes each port by attempting to bind a `ServerSocket`.                                                                                                                                                    |

## Design Principles

- **Type-safety**
  - Conditions are expressed using typed annotation attributes (for example, `int havingValue()` and `float havingValue()`) rather than string-only comparisons.
- **Predictability**
  - Evaluation order and aggregation rules are explicit and documented.
  - Missing-property behavior is explicit via `matchIfMissing`.
- **AOT / Native Image friendliness**
  - Conditions rely on Spring Boot’s standard conditional infrastructure and environment property conversion.
- **Fail-fast philosophy**
  - Invalid annotation configuration (such as specifying both `name` and `value`, or specifying neither) is rejected during condition evaluation.
- **Explicit semantics over dynamic expressions**
  - Comparisons are provided as dedicated match modes rather than arbitrary expressions.

## Thread Safety

- Annotation types are immutable metadata.
- Condition implementations provided by this library are stateless and therefore thread-safe.
- Thread-safety of the Spring `Environment` and `ConditionContext` is managed by the Spring container.

## Error Handling

- **Invalid annotation configuration** (for example, specifying both `name` and `value`, or specifying neither) results in an `IllegalStateException` during condition evaluation.
- **Property conversion errors** (for example, a non-numeric value for an integer condition) are treated as non-matching for the affected property key.
- **Regex patterns** used with `@ConditionalOnStringProperty(matchType = MATCHES)` are evaluated via `String#matches`; invalid patterns may raise `PatternSyntaxException`.

## AOT / Native Image Support

This library does not introduce custom reflection requirements beyond Spring Boot’s conditional processing. Behavior under AOT/native builds depends on your Spring Boot and GraalVM configuration, and on whether the relevant `Environment` property sources are available at runtime.

## Contributing

- Keep changes focused and scoped to a single concern per pull request.
- Maintain existing public API compatibility unless a major version increment is justified.
- Ensure tests cover behavior changes and include edge cases.

## License

MIT License. See `LICENSE`.

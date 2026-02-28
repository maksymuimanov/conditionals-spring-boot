/**
 * Conditional annotations and supporting types for Spring Boot configuration.
 *
 * <p>
 * This package defines {@code @Conditional} meta-annotations (for example,
 * {@link io.conditionals.condition.ConditionalOnStringProperty}) and their container annotations. Each conditional
 * is backed by a {@link org.springframework.boot.autoconfigure.condition.SpringBootCondition} implementation in
 * {@link io.conditionals.condition.impl}.
 * </p>
 *
 * <p><b>Semantics</b></p>
 * <ul>
 *     <li><b>Condition evaluation pipeline</b>: Spring Boot evaluates conditions while processing configuration
 *     classes and {@code @Bean} methods. For each annotated element, Spring Boot calls
 *     {@link org.springframework.boot.autoconfigure.condition.SpringBootCondition#getMatchOutcome(org.springframework.context.annotation.ConditionContext, org.springframework.core.type.AnnotatedTypeMetadata)}
 *     on the backing condition type and aggregates match outcomes according to Spring's conditional processing
 *     rules.</li>
 *     <li><b>Repeatable conditions</b>: where a condition annotation is repeatable, direct and container
 *     declarations are aggregated with AND semantics by the backing implementation.</li>
 * </ul>
 *
 * <p><b>Thread Safety</b></p>
 * <p>
 * All annotations are immutable metadata. Backing condition implementations in this project are stateless and
 * therefore thread-safe.
 * </p>
 *
 * <p><b>Null Handling</b></p>
 * <p>
 * This package is {@link org.jspecify.annotations.NullMarked null-marked}. Unless otherwise specified, reference
 * types are non-null by default.
 * </p>
 *
 * @author Maksym Uimanov
 * @since 1.0
 * @see io.conditionals.condition.impl
 * @see io.conditionals.condition.utils
 */
@NullMarked
package io.conditionals.condition;

import org.jspecify.annotations.NullMarked;
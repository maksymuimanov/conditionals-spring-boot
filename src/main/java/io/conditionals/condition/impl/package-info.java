/**
 * {@link org.springframework.boot.autoconfigure.condition.SpringBootCondition} implementations backing conditional annotations.
 *
 * <p>
 * Each condition implementation corresponds to a meta-annotation in {@link io.conditionals.condition} and is
 * invoked by Spring Boot during conditional evaluation of configuration classes and {@code @Bean} methods.
 * Implementations produce {@link org.springframework.boot.autoconfigure.condition.ConditionOutcome} instances
 * that are consumed by Spring Boot for decision making and diagnostics.
 * </p>
 *
 * <p><b>Semantics</b></p>
 * <ul>
 *     <li><b>Pipeline entry point</b>: {@link org.springframework.boot.autoconfigure.condition.SpringBootCondition#getMatchOutcome(org.springframework.context.annotation.ConditionContext, org.springframework.core.type.AnnotatedTypeMetadata)}.</li>
 *     <li><b>Evaluation order</b>: for repeatable annotations, attribute instances are evaluated in encounter order
 *     (direct annotation instance first, then container values in declared order) and aggregated with AND
 *     semantics by the helper utilities.</li>
 *     <li><b>Diagnostics</b>: outcomes include a {@link org.springframework.boot.autoconfigure.condition.ConditionMessage}
 *     describing match/no-match reasons.</li>
 * </ul>
 *
 * <p><b>Thread Safety</b></p>
 * <p>
 * All condition implementations in this package are stateless and therefore thread-safe.
 * </p>
 *
 * <p><b>Null Handling</b></p>
 * <p>
 * This package is {@link org.jspecify.annotations.NullMarked null-marked}. Inputs provided by Spring may be
 * non-null by contract, while resolved property values may be {@code null} and are handled explicitly.
 * </p>
 *
 * @author Maksym Uimanov
 * @since 1.0
 * @see io.conditionals.condition
 * @see io.conditionals.condition.utils.ConditionUtils
 */
@NullMarked
package io.conditionals.condition.impl;

import org.jspecify.annotations.NullMarked;
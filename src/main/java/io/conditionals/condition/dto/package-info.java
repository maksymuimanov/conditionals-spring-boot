/**
 * DTOs and strategy interfaces used by condition implementations.
 *
 * <p>
 * Types in this package represent condition specifications derived from annotation attributes (for example,
 * {@link io.conditionals.condition.dto.PropertySpec}) and comparison strategies used when evaluating environment
 * properties.
 * </p>
 *
 * <p><b>Semantics</b></p>
 * <ul>
 *     <li><b>Lifecycle</b>: specifications are instantiated during
 *     {@link org.springframework.boot.autoconfigure.condition.SpringBootCondition#getMatchOutcome(org.springframework.context.annotation.ConditionContext, org.springframework.core.type.AnnotatedTypeMetadata)}
 *     and are used to evaluate the {@link org.springframework.core.env.Environment}.</li>
 *     <li><b>Evaluation</b>: specifications iterate configured property names in encounter order and classify names
 *     as missing or non-matching according to annotation-defined rules.</li>
 * </ul>
 *
 * <p><b>Thread Safety</b></p>
 * <p>
 * Specifications in this package are immutable after construction and therefore thread-safe.
 * </p>
 *
 * <p><b>Null Handling</b></p>
 * <p>
 * This package is {@link org.jspecify.annotations.NullMarked null-marked}. Resolved property values may be
 * {@code null} and are explicitly annotated as such where applicable.
 * </p>
 *
 * @author Maksym Uimanov
 * @since 1.0
 * @see io.conditionals.condition.impl
 */
@NullMarked
package io.conditionals.condition.dto;

import org.jspecify.annotations.NullMarked;
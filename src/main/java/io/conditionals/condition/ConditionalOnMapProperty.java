package io.conditionals.condition;

import io.conditionals.condition.impl.OnMapPropertyCondition;
import io.conditionals.condition.spec.MapMatchType;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * Declares a Spring Boot {@link org.springframework.context.annotation.Condition} that matches based on
 * map-like configuration properties.
 *
 * <p>This annotation is backed by {@link io.conditionals.condition.impl.OnMapPropertyCondition}. The resolved
 * map is obtained by reading properties under {@code prefix + name + "." + key} for each candidate map key.
 * Matching is performed according to {@link #matchType()} and may be negated using {@link #not()}.</p>
 *
 * <p><b>Candidate format</b></p>
 * <p>{@link #havingValue()} is interpreted as a sequence of key/value pairs:
 * {@code {k1, v1, k2, v2, ...}}. The array length must be even; otherwise condition evaluation fails with an
 * {@link IllegalArgumentException}.</p>
 *
 * <p><b>Property resolution</b></p>
 * <ul>
 *     <li>For each configured property {@code name}, and each candidate key {@code k}, the effective key is
 *     {@code prefix + name + "." + k}.</li>
 *     <li>Missing sub-keys are treated as missing properties unless {@link #matchIfMissing()} is {@code true}.</li>
 * </ul>
 *
 * <p><b>Matching and negation</b></p>
 * <p>Comparison semantics are defined by {@link MapMatchType} and evaluated by
 * {@link io.conditionals.condition.impl.OnMapPropertyCondition.Matcher}. Negation is applied as
 * {@code (comparisonResult XOR not)}.</p>
 *
 * <p><b>Repeatable semantics</b></p>
 * <p>This annotation is {@link Repeatable} via {@link ConditionalOnMapProperties}. Each instance is evaluated
 * independently and outcomes are aggregated with AND semantics.</p>
 *
 * @author Maksym Uimanov
 * @since 1.0
 * @see io.conditionals.condition.impl.OnMapPropertyCondition
 * @see ConditionalOnMapProperties
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnMapPropertyCondition.class)
@Repeatable(ConditionalOnMapProperties.class)
public @interface ConditionalOnMapProperty {
    /**
     * Alias for {@link #name()}.
     *
     * @return property names (without prefix)
     */
    String[] value() default {};

    /**
     * Prefix to apply to each configured property name.
     *
     * @return normalized prefix for property keys
     */
    String prefix() default "";

    /**
     * One or more property names (without {@link #prefix()}).
     *
     * @return property names (without prefix)
     */
    String[] name() default {};

    /**
     * Candidate map as an array of key/value pairs.
     *
     * @return array containing an even number of elements
     */
    String[] havingValue() default {};

    /**
     * Whether to negate the comparison result.
     *
     * @return {@code true} to invert the outcome
     */
    boolean not() default false;

    /**
     * Map matching strategy.
     *
     * @return match type
     */
    MapMatchType matchType() default MapMatchType.EQUALS;

    /**
     * Whether to consider missing properties as matching.
     *
     * @return {@code true} if missing properties are treated as matching
     */
    boolean matchIfMissing() default false;
}
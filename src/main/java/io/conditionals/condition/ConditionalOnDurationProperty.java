package io.conditionals.condition;

import io.conditionals.condition.impl.OnDurationPropertyCondition;
import io.conditionals.condition.spec.ComparableMatchType;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Declares a Spring Boot {@link org.springframework.context.annotation.Condition} that matches based on
 * {@link java.time.Duration}-valued configuration properties.
 *
 * <p>This annotation is backed by {@link io.conditionals.condition.impl.OnDurationPropertyCondition}.
 * The resolved property value is interpreted as a {@link java.time.Duration} and compared against a candidate
 * duration derived from {@link #havingValue()}.</p>
 *
 * <p><b>Duration parsing</b></p>
 * <p>The candidate {@link #havingValue()} and resolved property values are parsed using Spring Boot's
 * {@link org.springframework.boot.convert.DurationStyle#detectAndParse(String)}. Any parsing/conversion failures
 * are treated as non-matching values.</p>
 *
 * <p><b>Property resolution</b></p>
 * <ul>
 *     <li>Property keys are built as {@code prefix + name} where {@link #prefix()} is normalized (trimmed;
 *     a trailing {@code '.'} is appended when the prefix is non-empty).</li>
 *     <li>{@link #value()} and {@link #name()} are mutually exclusive; exactly one must be specified.</li>
 * </ul>
 *
 * <p><b>Comparison and negation</b></p>
 * <ul>
 *     <li>The resolved duration is compared against the parsed candidate using {@link #matchType()}.</li>
 *     <li>Negation is applied as {@code (comparisonResult XOR not)}.</li>
 * </ul>
 *
 * <p><b>Missing properties</b></p>
 * <p>If a configured property is absent, the condition does not match unless {@link #matchIfMissing()} is
 * {@code true}.</p>
 *
 * <p><b>Repeatable semantics</b></p>
 * <p>This annotation is {@link Repeatable} via {@link ConditionalOnDurationProperties}. Each instance is
 * evaluated independently and outcomes are aggregated with AND semantics.</p>
 *
 * @author Maksym Uimanov
 * @since 1.0
 * @see io.conditionals.condition.impl.OnDurationPropertyCondition
 * @see ConditionalOnDurationProperties
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnDurationPropertyCondition.class)
@Repeatable(ConditionalOnDurationProperties.class)
public @interface ConditionalOnDurationProperty {
    /**
     * Alias for {@link #name()}.
     *
     * @return property names (without prefix)
     */
    @AliasFor("name")
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
    @AliasFor("value")
    String[] name() default {};

    /**
     * Candidate duration value.
     *
     * <p>The string is parsed using {@link org.springframework.boot.convert.DurationStyle#detectAndParse(String)}.
     * The default value {@code "0"} corresponds to {@code Duration.ZERO}.</p>
     *
     * @return candidate duration string
     */
    String havingValue() default "0";

    /**
     * Whether to negate the comparison result.
     *
     * @return {@code true} to invert the outcome
     */
    boolean not() default false;

    /**
     * Comparison strategy to apply.
     *
     * @return match type
     */
    ComparableMatchType matchType() default ComparableMatchType.EQUALS;

    /**
     * Whether to consider missing properties as matching.
     *
     * @return {@code true} if missing properties are treated as matching
     */
    boolean matchIfMissing() default false;
}
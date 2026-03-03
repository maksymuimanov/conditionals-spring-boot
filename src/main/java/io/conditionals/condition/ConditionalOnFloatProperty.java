package io.conditionals.condition;

import io.conditionals.condition.impl.OnFloatPropertyCondition;
import io.conditionals.condition.spec.ComparableMatchType;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Declares a Spring Boot {@link org.springframework.context.annotation.Condition} that matches based on
 * {@code float}-valued configuration properties.
 *
 * <p>This annotation is backed by {@link io.conditionals.condition.impl.OnFloatPropertyCondition} and is
 * evaluated using Spring Boot's {@link org.springframework.boot.autoconfigure.condition.SpringBootCondition}
 * infrastructure.</p>
 *
 * <p><b>Property resolution</b></p>
 * <ul>
 *     <li>Property keys are built as {@code prefix + name} where {@link #prefix()} is normalized (trimmed;
 *     a trailing {@code '.'} is appended when the prefix is non-empty).</li>
 *     <li>{@link #value()} and {@link #name()} are mutually exclusive; exactly one must be specified.</li>
 *     <li>Values are resolved from {@link org.springframework.core.env.Environment} and converted to {@link Float}
 *     using Spring's conversion service.</li>
 * </ul>
 *
 * <p><b>Comparison and negation</b></p>
 * <ul>
 *     <li>The resolved property value is compared against {@link #havingValue()} using {@link #matchType()}.</li>
 *     <li>Negation is applied as {@code (comparisonResult XOR not)}.</li>
 * </ul>
 *
 * <p><b>Missing properties</b></p>
 * <p>If a configured property is absent, the condition does not match unless {@link #matchIfMissing()} is
 * {@code true}.</p>
 *
 * <p><b>Repeatable semantics</b></p>
 * <p>This annotation is {@link Repeatable} via {@link ConditionalOnFloatProperties}. Each instance is
 * evaluated independently and outcomes are aggregated with AND semantics.</p>
 *
 * @author Maksym Uimanov
 * @since 1.0
 * @see io.conditionals.condition.impl.OnFloatPropertyCondition
 * @see ConditionalOnFloatProperties
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnFloatPropertyCondition.class)
@Repeatable(ConditionalOnFloatProperties.class)
public @interface ConditionalOnFloatProperty {
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
     * Candidate value to compare against the resolved property value.
     *
     * @return candidate numeric value
     */
    float havingValue() default 0;

    /**
     * Whether to negate the comparison result.
     *
     * @return {@code true} to invert the outcome
     */
    boolean not() default false;

    /**
     * Numeric comparison strategy to apply.
     *
     * @return comparison strategy
     */
    ComparableMatchType matchType() default ComparableMatchType.EQUALS;

    /**
     * Whether to consider missing properties as matching.
     *
     * @return {@code true} if missing properties are treated as matching
     */
    boolean matchIfMissing() default false;
}
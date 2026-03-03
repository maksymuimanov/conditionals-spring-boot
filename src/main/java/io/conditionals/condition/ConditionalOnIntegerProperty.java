package io.conditionals.condition;

import io.conditionals.condition.impl.OnIntegerPropertyCondition;
import io.conditionals.condition.spec.ComparableMatchType;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * Declares a Spring Boot {@link org.springframework.context.annotation.Condition} that matches based on
 * {@code int}-valued configuration properties.
 *
 * <p>This annotation is backed by {@link io.conditionals.condition.impl.OnIntegerPropertyCondition} and is
 * evaluated using Spring Boot's {@link org.springframework.boot.autoconfigure.condition.SpringBootCondition}
 * infrastructure.</p>
 *
 * <p><b>Property resolution</b></p>
 * <ul>
 *     <li>Property keys are built as {@code prefix + name} where {@link #prefix()} is normalized (trimmed;
 *     a trailing {@code '.'} is appended when the prefix is non-empty).</li>
 *     <li>{@link #value()} and {@link #name()} are mutually exclusive; exactly one must be specified.</li>
 *     <li>Values are resolved from {@link org.springframework.core.env.Environment} and converted to
 *     {@link Integer} using Spring's conversion service.</li>
 * </ul>
 *
 * <p><b>Comparison and negation</b></p>
 * <ul>
 *     <li>The resolved property value is compared against {@link #havingValue()} using {@link #matchType()}.</li>
 *     <li>Negation is applied as {@code (comparisonResult XOR not)}.</li>
 *     <li>Conversion failures are treated as non-matching values.</li>
 * </ul>
 *
 * <p><b>Missing properties</b></p>
 * <p>If a configured property is absent, the condition does not match unless {@link #matchIfMissing()} is
 * {@code true}.</p>
 *
 * <p><b>Repeatable semantics</b></p>
 * <p>This annotation is {@link Repeatable} via {@link ConditionalOnIntegerProperties}. Each instance is
 * evaluated independently and outcomes are aggregated with AND semantics by the underlying condition.</p>
 *
 * <p><b>Thread safety</b></p>
 * <p>This annotation is immutable metadata and is therefore thread-safe.</p>
 *
 * @author Maksym Uimanov
 * @since 1.0
 * @see io.conditionals.condition.impl.OnIntegerPropertyCondition
 * @see ConditionalOnIntegerProperties
 * @see ComparableMatchType
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnIntegerPropertyCondition.class)
@Repeatable(ConditionalOnIntegerProperties.class)
public @interface ConditionalOnIntegerProperty {
    /**
     * Alias for {@link #name()}.
     *
     * <p>Specifies one or more property names (without {@link #prefix()}). Exactly one of {@link #value()} or
     * {@link #name()} must be specified.</p>
     *
     * @return property names (without prefix)
     */
    String[] value() default {};

    /**
     * Prefix to apply to each configured property name.
     *
     * <p>The value is trimmed. When non-empty and not ending with {@code '.'}, a trailing dot is appended.
     * The resulting key is {@code prefix + name}.</p>
     *
     * @return normalized prefix for property keys
     */
    String prefix() default "";

    /**
     * One or more property names (without {@link #prefix()}).
     *
     * <p>Exactly one of {@link #value()} or {@link #name()} must be specified.</p>
     *
     * @return property names (without prefix)
     */
    String[] name() default {};

    /**
     * Candidate value to compare against the resolved property value.
     *
     * @return candidate numeric value
     */
    int havingValue() default 0;

    /**
     * Whether to negate the comparison result.
     *
     * <p>The final predicate is computed as {@code (comparisonResult XOR not)}.</p>
     *
     * @return {@code true} to invert the outcome for each compared property
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
     * <p>When {@code true}, a missing property does not contribute to a non-match outcome. When {@code false},
     * any missing property causes the condition to not match.</p>
     *
     * @return {@code true} to match when a configured property is missing
     */
    boolean matchIfMissing() default false;
}
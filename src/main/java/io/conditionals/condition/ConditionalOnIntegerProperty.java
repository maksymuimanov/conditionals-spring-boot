package io.conditionals.condition;

import io.conditionals.condition.impl.OnIntegerPropertyCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;
/**
 * Declares a Spring Boot condition that matches based on integer-valued properties.
 *
 * <p>
 * When placed on a {@code @Configuration} class or {@code @Bean} method, Spring Boot evaluates the associated
 * {@link io.conditionals.condition.impl.OnIntegerPropertyCondition} during the condition evaluation phase of
 * configuration processing.
 * </p>
 *
 * <p><b>Semantics</b></p>
 * <ul>
 *     <li><b>Binding</b>: this annotation is backed by {@link io.conditionals.condition.impl.OnIntegerPropertyCondition}.</li>
 *     <li><b>Repeatable evaluation</b>: multiple instances (via {@link Repeatable} or {@link ConditionalOnIntegerProperties})
 *     are aggregated with AND semantics.</li>
 *     <li><b>Name resolution</b>: property keys are composed as {@code prefix + name} with prefix normalization
 *     (trim; append {@code '.'} when non-empty).</li>
 *     <li><b>Property name selection</b>: {@link #value()} and {@link #name()} are mutually exclusive; exactly one must
 *     be non-empty.</li>
 *     <li><b>Comparison</b>: the resolved property value is compared against {@link #havingValue()} using {@link #matchType()}.</li>
 *     <li><b>Negation</b>: the final predicate is {@code (comparisonResult XOR not)}.</li>
 *     <li><b>Missing behavior</b>: missing properties produce a no-match outcome unless {@link #matchIfMissing()} is {@code true}.</li>
 * </ul>
 *
 * <p><b>Thread Safety</b></p>
 * <p>This annotation is thread-safe. It declares immutable metadata.</p>
 *
 * <p><b>Null Handling</b></p>
 * <p>Annotation attributes are never {@code null}; resolved property values may be {@code null} and are treated as non-matching.</p>
 *
 * <p><b>Edge Cases</b></p>
 * <ul>
 *     <li>Type conversion failures when resolving integer properties are treated as non-matching.</li>
 *     <li>Empty {@link #value()} and {@link #name()} arrays are invalid and will result in an {@link IllegalStateException}
 *     during evaluation.</li>
 * </ul>
 *
 * <p><b>Performance Characteristics</b></p>
 * <p>Evaluation is linear in the number of configured property names and annotation instances.</p>
 *
 * <p><b>Usage Example</b></p>
 * <pre>{@code
 * @ConditionalOnIntegerProperty(prefix = "app", name = "port", havingValue = 8080)
 * @Configuration
 * class PortConfiguration {
 * }
 * }</pre>
 *
 * @author Maksym Uimanov
 * @since 1.0
 * @see io.conditionals.condition.impl.OnIntegerPropertyCondition
 * @see ConditionalOnIntegerProperties
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnIntegerPropertyCondition.class)
@Repeatable(ConditionalOnIntegerProperties.class)
public @interface ConditionalOnIntegerProperty {
    /**
     * Alias for {@link #name()}.
     *
     * <p><b>Semantics</b></p>
     * <p>Provides property names evaluated in encounter order.</p>
     *
     * <p><b>Thread Safety</b></p>
     * <p>Thread-safe.</p>
     *
     * @return property names
     */
    String[] value() default {};

    /**
     * Prefix applied when constructing each property key.
     *
     * <p><b>Semantics</b></p>
     * <p>Normalized as trim; append {@code '.'} when non-empty.</p>
     *
     * <p><b>Thread Safety</b></p>
     * <p>Thread-safe.</p>
     *
     * @return property key prefix
     */
    String prefix() default "";

    /**
     * Property names to evaluate.
     *
     * <p><b>Semantics</b></p>
     * <p>Mutually exclusive with {@link #value()}.</p>
     *
     * <p><b>Thread Safety</b></p>
     * <p>Thread-safe.</p>
     *
     * @return property names
     */
    String[] name() default {};

    /**
     * Candidate integer value used for comparison.
     *
     * <p><b>Semantics</b></p>
     * <p>Compared to the resolved property value according to {@link #matchType()}.</p>
     *
     * <p><b>Thread Safety</b></p>
     * <p>Thread-safe.</p>
     *
     * @return required value
     */
    int havingValue() default 0;

    /**
     * Whether to invert the match result.
     *
     * <p><b>Semantics</b></p>
     * <p>The final predicate is {@code (comparisonResult XOR not)}.</p>
     *
     * <p><b>Thread Safety</b></p>
     * <p>Thread-safe.</p>
     *
     * @return {@code true} to negate the comparison result
     */
    boolean not() default false;

    /**
     * Integer comparison mode.
     *
     * <p><b>Semantics</b></p>
     * <ul>
     *     <li>{@link MatchType#EQUALS}: equality comparison</li>
     *     <li>{@link MatchType#GREATER_THAN}: strict greater-than comparison</li>
     *     <li>{@link MatchType#LESS_THAN}: strict less-than comparison</li>
     *     <li>{@link MatchType#GREATER_THAN_OR_EQUAL}: inclusive greater-than comparison</li>
     *     <li>{@link MatchType#LESS_THAN_OR_EQUAL}: inclusive less-than comparison</li>
     * </ul>
     *
     * <p><b>Thread Safety</b></p>
     * <p>Thread-safe.</p>
     *
     * @return comparison mode
     */
    MatchType matchType() default MatchType.EQUALS;

    /**
     * Whether to consider missing properties as matching.
     *
     * <p><b>Semantics</b></p>
     * <p>If {@code true}, a missing property key does not contribute to a no-match outcome.</p>
     *
     * <p><b>Thread Safety</b></p>
     * <p>Thread-safe.</p>
     *
     * @return {@code true} to match when a property is missing
     */
    boolean matchIfMissing() default false;

    enum MatchType {
        EQUALS,
        GREATER_THAN,
        LESS_THAN,
        GREATER_THAN_OR_EQUAL,
        LESS_THAN_OR_EQUAL
    }
}
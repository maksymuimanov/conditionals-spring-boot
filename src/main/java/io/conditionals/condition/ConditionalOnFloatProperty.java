package io.conditionals.condition;

import io.conditionals.condition.impl.OnFloatPropertyCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;
/**
 * Declares a Spring Boot condition that matches based on float-valued properties.
 *
 * <p>
 * When placed on a {@code @Configuration} class or {@code @Bean} method, Spring Boot evaluates the associated
 * {@link io.conditionals.condition.impl.OnFloatPropertyCondition} during the condition evaluation phase of
 * configuration processing.
 * </p>
 *
 * <p><b>Semantics</b></p>
 * <ul>
 *     <li><b>Binding</b>: this annotation is backed by {@link io.conditionals.condition.impl.OnFloatPropertyCondition}.</li>
 *     <li><b>Repeatable evaluation</b>: multiple instances (via {@link Repeatable} or {@link ConditionalOnFloatProperties})
 *     are aggregated with AND semantics.</li>
 *     <li><b>Name resolution</b>: property keys are composed as {@code prefix + name} with prefix normalization
 *     (trim; append {@code '.'} when non-empty).</li>
 *     <li><b>Property name selection</b>: {@link #value()} and {@link #name()} are mutually exclusive; exactly one must
 *     be non-empty.</li>
 *     <li><b>Comparison</b>: the resolved property value is compared against {@link #havingValue()} using {@link #matchType()}.</li>
 *     <li><b>Equals tolerance</b>: for {@link MatchType#EQUALS}, the backing condition uses an absolute tolerance of
 *     {@code 0.00001F} when comparing values.</li>
 *     <li><b>NaN handling</b>: if either the resolved property value or {@link #havingValue()} is {@link Float#NaN}, the
 *     comparison is treated as non-matching.</li>
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
 *     <li>Type conversion failures when resolving float properties are treated as non-matching.</li>
 *     <li>Empty {@link #value()} and {@link #name()} arrays are invalid and will result in an {@link IllegalStateException}
 *     during evaluation.</li>
 * </ul>
 *
 * <p><b>Performance Characteristics</b></p>
 * <p>Evaluation is linear in the number of configured property names and annotation instances.</p>
 *
 * <p><b>Usage Example</b></p>
 * <pre>{@code
 * @ConditionalOnFloatProperty(prefix = "app", name = "ratio", havingValue = 0.5f,
 *         matchType = ConditionalOnFloatProperty.MatchType.GREATER_THAN)
 * @Configuration
 * class RatioConfiguration {
 * }
 * }</pre>
 *
 * @author Maksym Uimanov
 * @since 1.0
 * @see io.conditionals.condition.impl.OnFloatPropertyCondition
 * @see ConditionalOnFloatProperties
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnFloatPropertyCondition.class)
@Repeatable(ConditionalOnFloatProperties.class)
public @interface ConditionalOnFloatProperty {
    /**
     * Alias for {@link #name()}.
     *
     * @return property names
     */
    String[] value() default {};

    /**
     * Prefix applied when constructing each property key.
     *
     * @return property key prefix
     */
    String prefix() default "";

    /**
     * Property names to evaluate.
     *
     * @return property names
     */
    String[] name() default {};

    /**
     * Candidate float value used for comparison.
     *
     * @return required value
     */
    float havingValue() default 0;

    /**
     * Whether to invert the match result.
     *
     * @return {@code true} to negate the comparison result
     */
    boolean not() default false;

    /**
     * Float comparison mode.
     *
     * @return comparison mode
     */
    MatchType matchType() default MatchType.EQUALS;

    /**
     * Whether to consider missing properties as matching.
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
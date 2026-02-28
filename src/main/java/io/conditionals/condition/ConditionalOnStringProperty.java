package io.conditionals.condition;

import io.conditionals.condition.impl.OnStringPropertyCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;
/**
 * Declares a Spring Boot condition that matches based on the value of one or more string-valued properties.
 *
 * <p>
 * When placed on a {@code @Configuration} class or {@code @Bean} method, Spring Boot evaluates the associated
 * {@link io.conditionals.condition.impl.OnStringPropertyCondition} during the condition evaluation phase of
 * configuration processing.
 * </p>
 *
 * <p><b>Semantics</b></p>
 * <ul>
 *     <li><b>Binding</b>: this annotation is meta-annotated with {@link Conditional} and is backed by
 *     {@link io.conditionals.condition.impl.OnStringPropertyCondition}.</li>
 *     <li><b>Repeatable evaluation</b>: when multiple instances are declared (via Java repeatable annotations or the
 *     {@link ConditionalOnStringProperties} container), Spring Boot evaluates each instance and aggregates with AND
 *     semantics: the annotated element matches only if all instances match.</li>
 *     <li><b>Name resolution</b>: property keys are composed as {@code prefix + name}. If {@code prefix} is non-empty
 *     and does not end with {@code '.'}, a dot is appended before composition.</li>
 *     <li><b>Property name selection</b>: {@link #value()} and {@link #name()} are mutually exclusive; exactly one must
 *     be non-empty.</li>
 *     <li><b>Evaluation order</b>: within an annotation instance, property names are evaluated in the declared array
 *     order. For repeatable declarations, annotation instances are evaluated in encounter order.</li>
 *     <li><b>Normalization</b>: if {@link #ignoreCase()} is {@code true}, property and candidate values are
 *     lower-cased using {@link java.util.Locale#ROOT}. If {@link #trim()} is {@code true}, values are trimmed
 *     after case normalization.</li>
 *     <li><b>Matching</b>: the normalized property value is compared to {@link #havingValue()} using
 *     {@link #matchType()}.</li>
 *     <li><b>Negation</b>: when {@link #not()} is {@code true}, the match result is logically inverted.</li>
 *     <li><b>Missing behavior</b>: missing properties produce a no-match outcome unless {@link #matchIfMissing()} is
 *     {@code true}.</li>
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
 *     <li>For {@link MatchType#MATCHES}, the candidate value is treated as a regular expression pattern and is
 *     evaluated via {@link String#matches(String)}; invalid patterns result in {@link java.util.regex.PatternSyntaxException}.</li>
 *     <li>Empty strings are valid candidates. If {@link #trim()} is enabled, whitespace-only property values become
 *     empty strings before comparison.</li>
 * </ul>
 *
 * <p><b>Performance Characteristics</b></p>
 * <p>Evaluation is linear in the number of configured property names and annotation instances.</p>
 *
 * <p><b>Usage Example</b></p>
 * <pre>{@code
 * @ConditionalOnStringProperty(prefix = "app", name = "mode", havingValue = "prod")
 * @Configuration
 * class ProdConfiguration {
 * }
 * }</pre>
 *
 * @author Maksym Uimanov
 * @since 1.0
 * @see io.conditionals.condition.impl.OnStringPropertyCondition
 * @see ConditionalOnStringProperties
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnStringPropertyCondition.class)
@Repeatable(ConditionalOnStringProperties.class)
public @interface ConditionalOnStringProperty {
    /**
     * Alias for {@link #name()}.
     *
     * <p><b>Semantics</b></p>
     * <p>Provides property names that will be evaluated in encounter order.</p>
     *
     * <p><b>Thread Safety</b></p>
     * <p>Thread-safe.</p>
     *
     * <p><b>Null Handling</b></p>
     * <p>Never {@code null}. Default is an empty array.</p>
     *
     * @return property names
     */
    String[] value() default {};

    /**
     * Prefix applied when constructing each property key.
     *
     * <p><b>Semantics</b></p>
     * <p>
     * The runtime key is {@code normalizedPrefix + name}, where {@code normalizedPrefix} is trimmed and, when
     * non-empty, ends with {@code '.'}.
     * </p>
     *
     * <p><b>Thread Safety</b></p>
     * <p>Thread-safe.</p>
     *
     * <p><b>Null Handling</b></p>
     * <p>Never {@code null}. Default is the empty string.</p>
     *
     * @return prefix for property keys
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
     * <p><b>Null Handling</b></p>
     * <p>Never {@code null}. Default is an empty array.</p>
     *
     * @return property names
     */
    String[] name() default {};

    /**
     * Candidate string value to compare against resolved property values.
     *
     * <p><b>Semantics</b></p>
     * <p>The candidate participates in normalization (case/trim) and is used by {@link #matchType()}.</p>
     *
     * <p><b>Thread Safety</b></p>
     * <p>Thread-safe.</p>
     *
     * <p><b>Null Handling</b></p>
     * <p>Never {@code null}. Default is the empty string.</p>
     *
     * @return required value
     */
    String havingValue() default "";

    /**
     * Whether to perform case-insensitive matching.
     *
     * <p><b>Semantics</b></p>
     * <p>If enabled, values are lower-cased using {@link java.util.Locale#ROOT} before any trimming.</p>
     *
     * <p><b>Thread Safety</b></p>
     * <p>Thread-safe.</p>
     *
     * @return {@code true} for case-insensitive matching
     */
    boolean ignoreCase() default false;

    /**
     * Whether to trim both property and candidate values before comparison.
     *
     * <p><b>Semantics</b></p>
     * <p>Trimming is applied after optional case normalization.</p>
     *
     * <p><b>Thread Safety</b></p>
     * <p>Thread-safe.</p>
     *
     * @return {@code true} to apply {@link String#trim()}
     */
    boolean trim() default false;

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
     * String comparison mode.
     *
     * <p><b>Semantics</b></p>
     * <ul>
     *     <li>{@link MatchType#EQUALS}: {@link String#equals(Object)}</li>
     *     <li>{@link MatchType#CONTAINS}: {@link String#contains(CharSequence)}</li>
     *     <li>{@link MatchType#STARTS_WITH}: {@link String#startsWith(String)}</li>
     *     <li>{@link MatchType#ENDS_WITH}: {@link String#endsWith(String)}</li>
     *     <li>{@link MatchType#MATCHES}: {@link String#matches(String)}</li>
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
        CONTAINS,
        STARTS_WITH,
        ENDS_WITH,
        MATCHES
    }
}
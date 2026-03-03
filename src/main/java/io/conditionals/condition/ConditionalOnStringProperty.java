package io.conditionals.condition;

import io.conditionals.condition.impl.OnStringPropertyCondition;
import io.conditionals.condition.spec.StringMatchType;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * Declares a Spring Boot {@link org.springframework.context.annotation.Condition} that matches based on
 * {@link String}-valued configuration properties.
 *
 * <p>This annotation is a type-safe alternative to Spring Boot's
 * {@code @ConditionalOnProperty} for string comparison use-cases. It is backed by
 * {@link io.conditionals.condition.impl.OnStringPropertyCondition}, which is a
 * {@link org.springframework.boot.autoconfigure.condition.SpringBootCondition} evaluated during configuration
 * processing.</p>
 *
 * <p><b>Property resolution</b></p>
 * <ul>
 *     <li>Property keys are built as {@code prefix + name} where {@link #prefix()} is normalized (trimmed;
 *     a trailing {@code '.'} is appended when the prefix is non-empty).</li>
 *     <li>{@link #value()} and {@link #name()} are mutually exclusive; exactly one of them must contain at least
 *     one element.</li>
 *     <li>Values are resolved from {@link org.springframework.core.env.Environment} using Spring's type conversion
 *     for {@link String}.</li>
 * </ul>
 *
 * <p><b>Matching</b></p>
 * <ul>
 *     <li>The resolved property value is compared to {@link #havingValue()} according to {@link #matchType()}.</li>
 *     <li>If {@link #trim()} is {@code true}, both the resolved property value and {@link #havingValue()} are
 *     trimmed before comparison.</li>
 *     <li>If {@link #ignoreCase()} is {@code true}, case-insensitive comparison is applied for match types where
 *     case is relevant.</li>
 *     <li>Negation is applied as {@code (comparisonResult XOR not)}.</li>
 * </ul>
 *
 * <p><b>Missing properties</b></p>
 * <p>If a configured property is absent, the condition does not match unless {@link #matchIfMissing()} is
 * {@code true}.</p>
 *
 * <p><b>Repeatable semantics</b></p>
 * <p>This annotation is {@link Repeatable} via {@link ConditionalOnStringProperties}. Each instance is evaluated
 * independently and outcomes are aggregated with AND semantics by the underlying condition implementation.</p>
 *
 * <p><b>Thread safety</b></p>
 * <p>This annotation is immutable metadata and is therefore thread-safe.</p>
 *
 * @author Maksym Uimanov
 * @since 1.0
 * @see io.conditionals.condition.impl.OnStringPropertyCondition
 * @see ConditionalOnStringProperties
 * @see StringMatchType
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
     * Candidate string to compare against the resolved property value.
     *
     * <p>When {@link #trim()} is {@code true} the candidate is trimmed before comparison.</p>
     *
     * @return candidate value used for comparison
     */
    String havingValue() default "";

    /**
     * Whether to ignore case when performing the comparison.
     *
     * <p>Case-insensitivity applies to match types where it is meaningful (for example {@link StringMatchType#EQUALS},
     * {@link StringMatchType#CONTAINS}, {@link StringMatchType#STARTS_WITH}, {@link StringMatchType#ENDS_WITH}).</p>
     *
     * @return {@code true} to compare case-insensitively
     */
    boolean ignoreCase() default false;

    /**
     * Whether to trim the resolved property value and {@link #havingValue()} before comparison.
     *
     * @return {@code true} to trim values prior to comparison
     */
    boolean trim() default false;

    /**
     * Whether to negate the comparison result.
     *
     * <p>The final predicate is computed as {@code (comparisonResult XOR not)}.</p>
     *
     * @return {@code true} to invert the outcome for each compared property
     */
    boolean not() default false;

    /**
     * String comparison strategy to apply.
     *
     * @return comparison strategy
     */
    StringMatchType matchType() default StringMatchType.EQUALS;

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
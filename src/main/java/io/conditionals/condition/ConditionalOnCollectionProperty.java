package io.conditionals.condition;

import io.conditionals.condition.impl.OnCollectionPropertyCondition;
import io.conditionals.condition.spec.CollectionMatchType;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * Declares a Spring Boot {@link org.springframework.context.annotation.Condition} that matches based on
 * collection-like configuration properties.
 *
 * <p>This annotation is backed by {@link io.conditionals.condition.impl.OnCollectionPropertyCondition}. The
 * resolved property is expected to be convertible to {@code String[]} (for example a comma-delimited property
 * value). Matching is performed according to {@link #matchType()} and an optional size constraint.</p>
 *
 * <p><b>Collection matching</b></p>
 * <p>Comparison semantics are defined by {@link CollectionMatchType} and are evaluated by
 * {@link io.conditionals.condition.impl.OnCollectionPropertyCondition.Matcher}. For match types involving
 * order (for example {@link CollectionMatchType#EQUALS}, {@link CollectionMatchType#CONTAINS_SEQUENCE},
 * {@link CollectionMatchType#STARTS_WITH_ALL}, {@link CollectionMatchType#ENDS_WITH_ALL}) the array ordering
 * is significant.</p>
 *
 * <p><b>Size constraint</b></p>
 * <p>If {@link #size()} is not {@code -1}, the resolved array length must equal {@code size}. The size check is
 * performed before evaluating {@link #matchType()}.</p>
 *
 * <p><b>Negation</b></p>
 * <p>Negation is applied as {@code (comparisonResult XOR not)}.</p>
 *
 * <p><b>Missing properties</b></p>
 * <p>If a configured property is absent, the condition does not match unless {@link #matchIfMissing()} is
 * {@code true}.</p>
 *
 * <p><b>Repeatable semantics</b></p>
 * <p>This annotation is {@link Repeatable} via {@link ConditionalOnCollectionProperties}. Each instance is
 * evaluated independently and outcomes are aggregated with AND semantics.</p>
 *
 * @author Maksym Uimanov
 * @since 1.0
 * @see io.conditionals.condition.impl.OnCollectionPropertyCondition
 * @see ConditionalOnCollectionProperties
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnCollectionPropertyCondition.class)
@Repeatable(ConditionalOnCollectionProperties.class)
public @interface ConditionalOnCollectionProperty {
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
     * Candidate elements used for matching.
     *
     * @return candidate array
     */
    String[] havingValue() default {};

    /**
     * Optional expected size of the resolved collection.
     *
     * <p>A value of {@code -1} disables size checking.</p>
     *
     * @return expected size or {@code -1}
     */
    int size() default -1;

    /**
     * Whether to negate the comparison result.
     *
     * @return {@code true} to invert the outcome
     */
    boolean not() default false;

    /**
     * Collection matching strategy.
     *
     * @return match type
     */
    CollectionMatchType matchType() default CollectionMatchType.EQUALS;

    /**
     * Whether to consider missing properties as matching.
     *
     * @return {@code true} if missing properties are treated as matching
     */
    boolean matchIfMissing() default false;
}
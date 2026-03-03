package io.conditionals.condition.impl;

import io.conditionals.condition.spec.ComparablePropertySpec;
import io.conditionals.condition.spec.PropertySpecMatcher;
import io.conditionals.condition.utils.ConditionUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.core.annotation.AnnotationAttributes;

import java.lang.annotation.Annotation;

/**
 * {@link PropertySpringBootCondition} specialization for properties whose values are {@link Comparable}.
 *
 * <p>This base class implements comparison-based matching (equals, greater/less-than, etc.) driven by
 * {@link io.conditionals.condition.spec.ComparableMatchType}. Concrete conditions typically bind a specific
 * property type (for example {@link Integer}, {@link Long}, {@link java.time.Duration}).</p>
 *
 * <p><b>Comparison semantics</b></p>
 * <p>The comparison is performed by invoking {@link Comparable#compareTo(Object)} and interpreting the result
 * according to the configured match type. Negation is applied using
 * {@link ConditionUtils#revert(boolean, boolean)} with the spec's {@code not} attribute.</p>
 *
 * <p><b>Thread safety</b></p>
 * <p>Implementations are expected to be stateless. This base class creates a new matcher per evaluation and does
 * not store mutable state.</p>
 *
 * @param <V> resolved property value type
 * @param <T> type accepted by {@link Comparable#compareTo(Object)} for {@code V}
 * @author Maksym Uimanov
 * @since 1.0
 */
@SuppressWarnings("unchecked")
public abstract class ComparablePropertySpringBootCondition<V extends Comparable<T>, T> extends PropertySpringBootCondition<V, ComparablePropertySpec<V, T>> {
    /**
     * Evaluate {@code property == candidate} using {@link Comparable#compareTo(Object)}.
     *
     * @param property resolved property value (never {@code null})
     * @param candidate candidate value from annotation attributes
     * @return {@code true} if values are equal
     */
    protected boolean checkEquals(V property, V candidate) {
        return property.compareTo((T) candidate) == 0;
    }

    /**
     * Evaluate {@code property > candidate} using {@link Comparable#compareTo(Object)}.
     *
     * @param property resolved property value (never {@code null})
     * @param candidate candidate value from annotation attributes
     * @return {@code true} if {@code property} is greater than {@code candidate}
     */
    protected boolean checkGreaterThan(V property, V candidate) {
        return property.compareTo((T) candidate) > 0;
    }

    /**
     * Evaluate {@code property < candidate} using {@link Comparable#compareTo(Object)}.
     *
     * @param property resolved property value (never {@code null})
     * @param candidate candidate value from annotation attributes
     * @return {@code true} if {@code property} is less than {@code candidate}
     */
    protected boolean checkLessThan(V property, V candidate) {
        return property.compareTo((T) candidate) < 0;
    }

    /**
     * Evaluate {@code property >= candidate} using {@link Comparable#compareTo(Object)}.
     *
     * @param property resolved property value (never {@code null})
     * @param candidate candidate value from annotation attributes
     * @return {@code true} if {@code property} is greater than or equal to {@code candidate}
     */
    protected boolean checkGreaterThanOrEqual(V property, V candidate) {
        return property.compareTo((T) candidate) >= 0;
    }

    /**
     * Evaluate {@code property <= candidate} using {@link Comparable#compareTo(Object)}.
     *
     * @param property resolved property value (never {@code null})
     * @param candidate candidate value from annotation attributes
     * @return {@code true} if {@code property} is less than or equal to {@code candidate}
     */
    protected boolean checkLessThanOrEqual(V property, V candidate) {
        return property.compareTo((T) candidate) <= 0;
    }

    /**
     * Create a {@link ComparablePropertySpec} for the current annotation instance.
     *
     * @param annotationType annotation type producing the attributes (may be {@code null} depending on metadata)
     * @param annotationAttributes resolved attributes
     * @return comparable spec
     */
    @Override
    protected ComparablePropertySpec<V, T> createSpec(@Nullable Class<? extends Annotation> annotationType, AnnotationAttributes annotationAttributes) {
        return new ComparablePropertySpec<>(annotationType, annotationAttributes);
    }

    /**
     * Create a matcher that dispatches to one of the {@code check*} methods based on the spec's match type.
     *
     * @return matcher instance
     */
    @Override
    protected PropertySpecMatcher<V, ComparablePropertySpec<V, T>> createPropertySpecMatcher() {
        return new Matcher();
    }

    /**
     * Matcher implementation applying {@link io.conditionals.condition.spec.ComparableMatchType}-driven comparison
     * and {@code not}-based negation.
     */
    public class Matcher implements PropertySpecMatcher<V, ComparablePropertySpec<V, T>> {
        @Override
        public boolean compare(ComparablePropertySpec<V, T> spec, @Nullable V property, V candidate) {
            if (property == null) return false;

            boolean result = switch (spec.getMatchType()) {
                case EQUALS -> checkEquals(property, candidate);
                case GREATER_THAN -> checkGreaterThan(property, candidate);
                case LESS_THAN -> checkLessThan(property, candidate);
                case GREATER_THAN_OR_EQUAL -> checkGreaterThanOrEqual(property, candidate);
                case LESS_THAN_OR_EQUAL -> checkLessThanOrEqual(property, candidate);
            };
            return ConditionUtils.revert(result, spec.isNot());
        }
    }
}
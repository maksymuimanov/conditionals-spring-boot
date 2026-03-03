package io.conditionals.condition.spec;

import org.springframework.core.annotation.AnnotationAttributes;

import java.lang.annotation.Annotation;
import java.util.function.Function;

/**
 * {@link MatchingPropertySpec} implementation for comparable property values.
 *
 * <p>This spec binds the {@code matchType} attribute to {@link ComparableMatchType} and is used by
 * {@link io.conditionals.condition.impl.ComparablePropertySpringBootCondition} to evaluate numeric/ordered
 * comparisons.</p>
 *
 * <p><b>Having value mapping</b></p>
 * <p>Some annotations represent the candidate value using a different attribute type than the resolved property
 * value (for example, a {@link java.time.Duration} property may use a {@link String} candidate that is parsed).
 * The constructor accepting {@code havingValueMapper} supports such transformations.</p>
 *
 * <p><b>Thread safety</b></p>
 * <p>Instances are immutable after construction and are thread-safe.</p>
 *
 * @param <V> resolved property value type
 * @param <T> type accepted by {@link Comparable#compareTo(Object)} for {@code V}
 * @author Maksym Uimanov
 * @since 1.0
 */
public class ComparablePropertySpec<V extends Comparable<T>, T> extends MatchingPropertySpec<V, ComparablePropertySpec<V, T>, ComparableMatchType> {
    /**
     * Create a spec from annotation attributes.
     *
     * @param annotationType annotation type producing the attributes
     * @param annotationAttributes resolved annotation attributes
     */
    public ComparablePropertySpec(Class<? extends Annotation> annotationType, AnnotationAttributes annotationAttributes) {
        super(annotationType, annotationAttributes);
    }

    /**
     * Create a spec from annotation attributes using a mapper for the {@code havingValue} attribute.
     *
     * @param annotationType annotation type producing the attributes
     * @param annotationAttributes resolved annotation attributes
     * @param havingValueMapper mapper applied to the raw {@code havingValue} attribute
     */
    public ComparablePropertySpec(Class<? extends Annotation> annotationType, AnnotationAttributes annotationAttributes, Function<Object, V> havingValueMapper) {
        super(annotationType, annotationAttributes, havingValueMapper);
    }
}

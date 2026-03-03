package io.conditionals.condition.spec;

import org.springframework.core.annotation.AnnotationAttributes;

import java.lang.annotation.Annotation;
import java.util.function.Function;

/**
 * {@link PropertySpec} specialization that introduces a match strategy and optional negation.
 *
 * <p>Many conditional annotations in this library share a common shape:</p>
 * <ul>
 *     <li>{@code value}/{@code name}/{@code prefix}/{@code havingValue}/{@code matchIfMissing} from
 *     {@link PropertySpec}</li>
 *     <li>A {@code matchType} enum controlling comparison semantics</li>
 *     <li>A {@code not} flag that inverts the comparison result</li>
 * </ul>
 *
 * <p>This type captures the shared {@code not} and {@code matchType} attributes. Concrete specs typically add
 * additional configuration needed by their matchers (for example case handling for strings).</p>
 *
 * <p><b>Negation semantics</b></p>
 * <p>Conditions apply negation using {@link io.conditionals.condition.utils.ConditionUtils#revert(boolean, boolean)}
 * with the value returned by {@link #isNot()}.</p>
 *
 * <p><b>Thread safety</b></p>
 * <p>Instances are immutable after construction and are thread-safe.</p>
 *
 * @param <V> resolved property value type and candidate value type
 * @param <S> self type
 * @param <E> match type enum
 * @author Maksym Uimanov
 * @since 1.0
 */
public abstract class MatchingPropertySpec<V, S extends MatchingPropertySpec<V, S, E>, E extends Enum<E>> extends PropertySpec<V, S> {
    private static final String NOT = "not";
    private static final String MATCH_TYPE = "matchType";
    private final boolean not;
    private final E matchType;

    /**
     * Create a new instance from the given annotation attributes.
     *
     * @param annotationType annotation type producing the attributes
     * @param annotationAttributes resolved annotation attributes
     */
    protected MatchingPropertySpec(Class<? extends Annotation> annotationType, AnnotationAttributes annotationAttributes) {
        super(annotationType, annotationAttributes);
        this.not = annotationAttributes.getBoolean(NOT);
        this.matchType = annotationAttributes.getEnum(MATCH_TYPE);
    }

    /**
     * Create a new instance from the given annotation attributes using a mapper to convert the raw
     * {@code havingValue} attribute to the desired type.
     *
     * @param annotationType annotation type producing the attributes
     * @param annotationAttributes resolved annotation attributes
     * @param havingValueMapper mapper applied to the raw {@code havingValue} attribute
     */
    protected MatchingPropertySpec(Class<? extends Annotation> annotationType, AnnotationAttributes annotationAttributes, Function<Object, V> havingValueMapper) {
        super(annotationType, annotationAttributes, havingValueMapper);
        this.not = annotationAttributes.getBoolean(NOT);
        this.matchType = annotationAttributes.getEnum(MATCH_TYPE);
    }

    /**
     * Whether to negate the comparison result.
     *
     * @return {@code true} if comparison results should be inverted
     */
    public boolean isNot() {
        return not;
    }

    /**
     * Return the configured match type controlling comparison semantics.
     *
     * @return match type
     */
    public E getMatchType() {
        return matchType;
    }
}

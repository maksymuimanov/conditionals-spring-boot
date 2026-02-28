package io.conditionals.condition.dto;

import org.jspecify.annotations.Nullable;

/**
 * Strategy interface for comparing a resolved property value against a candidate value from a {@link PropertySpec}.
 *
 * <p>
 * Implementations are used by {@link PropertySpec} during the Spring Boot condition evaluation pipeline to
 * determine whether a single property satisfies the specification derived from an annotation.
 * </p>
 *
 * <p><b>Semantics</b></p>
 * <ul>
 *     <li><b>Inputs</b>: {@code spec} defines matching parameters; {@code property} is the resolved value from
 *     the {@code PropertyResolver}; {@code candidate} is the required value declared by the annotation.</li>
 *     <li><b>Evaluation</b>: the return value indicates whether the property satisfies the specification.</li>
 *     <li><b>Determinism</b>: implementations should be deterministic for the same input tuple.</li>
 * </ul>
 *
 * <p><b>Thread Safety</b></p>
 * <p>
 * Implementations must document their own thread-safety. Callers may reuse the same instance across threads.
 * Stateless implementations are inherently thread-safe.
 * </p>
 *
 * <p><b>Null Handling</b></p>
 * <ul>
 *     <li>{@code property} may be {@code null} when the resolver returns {@code null} for an existing key or when
 *     the property type conversion yields {@code null}.</li>
 *     <li>{@code spec} and {@code candidate} are expected to be non-null.</li>
 * </ul>
 *
 * <p><b>Edge Cases</b></p>
 * <ul>
 *     <li>Implementations should treat {@code null} properties explicitly (typically as a non-match) and avoid
 *     throwing {@link NullPointerException}.</li>
 * </ul>
 *
 * <p><b>Performance Characteristics</b></p>
 * <p>
 * Matching is typically invoked once per configured property name. Implementations should avoid expensive
 * operations where possible.
 * </p>
 *
 * @author Maksym Uimanov
 * @since 1.0
 * @see PropertySpec
 * @param <V> resolved property value type
 * @param <S> concrete {@link PropertySpec} type supplying matching parameters
 */
@FunctionalInterface
public interface PropertySpecMatcher<V, S extends PropertySpec<V, S>> {
    /**
     * Compares a resolved property value to a candidate value.
     *
     * <p><b>Semantics</b></p>
     * <ul>
     *     <li>Returns {@code true} if and only if {@code property} satisfies the matching rules expressed by
     *     {@code spec} with respect to {@code candidate}.</li>
     *     <li>Callers interpret {@code false} as a non-matching property value.</li>
     * </ul>
     *
     * <p><b>Thread Safety</b></p>
     * <p>Must be safe to call concurrently if the implementation is shared.</p>
     *
     * <p><b>Null Handling</b></p>
     * <p>{@code property} may be {@code null}; implementors must define the behavior in that case.</p>
     *
     * @param spec typed specification derived from an annotation instance
     * @param property resolved property value, potentially {@code null}
     * @param candidate candidate value declared in the annotation
     * @return {@code true} if the property matches; otherwise {@code false}
     */
    boolean compare(S spec, @Nullable V property, V candidate);
}

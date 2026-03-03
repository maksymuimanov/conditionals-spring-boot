package io.conditionals.condition.spec;

import org.jspecify.annotations.Nullable;

/**
 * Strategy interface used to compare a resolved property value to a candidate ("having") value.
 *
 * <p>Matchers are used by {@link PropertySpec#collectProperties(org.springframework.core.env.PropertyResolver, java.util.List, java.util.List, PropertySpecMatcher)}
 * and are created by {@link io.conditionals.condition.impl.PropertySpringBootCondition} implementations to
 * apply type-specific matching semantics (numeric comparison, string matching, collection semantics, etc.).</p>
 *
 * <p><b>Nullability</b></p>
 * <p>The {@code property} argument may be {@code null} (for example if a resolver returns {@code null} for a
 * present key). Implementations should treat {@code null} as non-matching unless the specific condition
 * semantics require otherwise.</p>
 *
 * <p><b>Thread safety</b></p>
 * <p>Implementations should be stateless or otherwise thread-safe. The library typically creates matcher
 * instances per evaluation.</p>
 *
 * @param <V> resolved property value type
 * @param <S> spec type
 * @author Maksym Uimanov
 * @since 1.0
 */
@FunctionalInterface
public interface PropertySpecMatcher<V, S extends PropertySpec<V, S>> {
    /**
     * Compare a resolved property value to the candidate value configured by the annotation.
     *
     * @param spec specification describing the annotation configuration
     * @param property resolved property value (may be {@code null})
     * @param candidate candidate value (never {@code null} by construction of the spec)
     * @return {@code true} if the resolved property value matches the candidate according to the spec
     */
    boolean compare(S spec, @Nullable V property, V candidate);
}

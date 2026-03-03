package io.conditionals.condition.impl;

import io.conditionals.condition.ConditionalOnMapProperties;
import io.conditionals.condition.ConditionalOnMapProperty;
import io.conditionals.condition.spec.MapMatchType;
import io.conditionals.condition.spec.MatchingPropertySpec;
import io.conditionals.condition.spec.PropertySpecMatcher;
import io.conditionals.condition.utils.ConditionUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.env.PropertyResolver;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * {@link org.springframework.boot.autoconfigure.condition.SpringBootCondition} implementation backing
 * {@link io.conditionals.condition.ConditionalOnMapProperty}.
 *
 * <p>This condition evaluates map-like configuration by reading a set of sub-keys beneath
 * {@code prefix + name}. Candidate entries are provided as key/value pairs and converted into a
 * {@link java.util.Map}. Matching is performed according to {@link MapMatchType} and may be negated using
 * the {@code not} attribute.</p>
 *
 * <p><b>Property resolution</b></p>
 * <p>The {@link Spec} overrides
 * {@link io.conditionals.condition.spec.PropertySpec#collectProperties(PropertyResolver, List, List, PropertySpecMatcher)}
 * to materialize a map by reading {@code prefix + name + "." + key} for each candidate key.</p>
 *
 * <p><b>Thread safety</b></p>
 * <p>This type is stateless and thread-safe. Matcher/spec instances are created per evaluation.</p>
 *
 * @author Maksym Uimanov
 * @since 1.0
 */
public class OnMapPropertyCondition extends PropertySpringBootCondition<Map<String, String>, OnMapPropertyCondition.Spec> {
    @Override
    protected Class<? extends Annotation> getAnnotationClass() {
        return ConditionalOnMapProperty.class;
    }

    @Override
    protected Class<? extends Annotation> getAnnotationContainerClass() {
        return ConditionalOnMapProperties.class;
    }

    @Override
    protected Spec createSpec(@Nullable Class<? extends Annotation> annotationType, AnnotationAttributes annotationAttributes) {
        return new Spec(annotationType, annotationAttributes);
    }

    @Override
    protected PropertySpecMatcher<Map<String, String>, Spec> createPropertySpecMatcher() {
        return new Matcher();
    }

    /**
     * Matcher implementing {@link MapMatchType} semantics for {@code Map<String, String>} values.
     */
    public static class Matcher implements PropertySpecMatcher<Map<String, String>, Spec> {
        @Override
        public boolean compare(Spec spec, @Nullable Map<String, String> property, Map<String, String> candidate) {
            if (property == null) return false;
            boolean result = switch (spec.getMatchType()) {
                case EQUALS -> equals(property, candidate);
                case CONTAINS_ANY -> containsAny(property, candidate);
                case CONTAINS_ALL -> containsAll(property, candidate);
            };
            return ConditionUtils.revert(result, spec.isNot());
        }

        /**
         * Compare maps for equality.
         */
        private static boolean equals(Map<String, String> property, Map<String, String> candidate) {
            int candidateSize = candidate.size();
            if (candidateSize == 0) return true;

            return Objects.equals(property, candidate);
        }

        /**
         * Match if at least one candidate entry is contained in the resolved map.
         */
        private static boolean containsAny(Map<String, String> property, Map<String, String> candidate) {
            int candidateSize = candidate.size();
            if (candidateSize == 0) return true;

            Iterable<Map.Entry<String, String>> entrySet = candidate.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                if (property.containsKey(entry.getKey()) && property.get(entry.getKey()).equals(entry.getValue())) {
                    return true;
                }
            }

            return false;
        }

        /**
         * Match if all candidate entries are contained in the resolved map.
         */
        private static boolean containsAll(Map<String, String> property, Map<String, String> candidate) {
            int candidateSize = candidate.size();
            if (candidateSize == 0) return true;
            int propertySize = property.size();
            if (propertySize < candidateSize) return false;

            for (Map.Entry<String, String> entry : candidate.entrySet()) {
                if (!property.containsKey(entry.getKey()) || !property.get(entry.getKey()).equals(entry.getValue())) {
                    return false;
                }
            }

            return true;
        }
    }

    /**
     * {@link io.conditionals.condition.spec.PropertySpec} specialization for
     * {@link io.conditionals.condition.ConditionalOnMapProperty}.
     *
     * <p>The {@code havingValue} attribute is interpreted as alternating key/value elements
     * ({@code k1, v1, k2, v2, ...}). The resulting map drives which sub-keys are resolved from the environment.</p>
     */
    public static class Spec extends MatchingPropertySpec<Map<String, String>, Spec, MapMatchType> {
        /**
         * Create a new spec parsing key/value pairs from the {@code havingValue} attribute.
         *
         * @param annotationType annotation type producing the attributes
         * @param annotationAttributes resolved annotation attributes
         * @throws IllegalArgumentException if the provided pair array has an odd number of elements
         */
        private Spec(Class<? extends Annotation> annotationType, AnnotationAttributes annotationAttributes) {
            super(annotationType, annotationAttributes, attribute -> {
                String[] pairs = (String[]) attribute;
                Assert.state(pairs.length % 2 == 0,
                        () -> "The havingValue attribute of @%s must be specified as pairs of key-value".formatted(annotationType.getSimpleName()));
                Map<String, String> map = new HashMap<>();
                for (int i = 0; i < pairs.length; i += 2) {
                    map.put(pairs[i], pairs[i + 1]);
                }
                return map;
            });
        }

        /**
         * Resolve a map for each configured property name by reading sub-keys for each candidate key.
         *
         * <p>For each property {@code name} and each candidate key {@code k}, the effective key is
         * {@code prefix + name + "." + k}. Missing sub-keys are reported as missing unless
         * {@link #isMatchIfMissing()} is {@code true}.</p>
         *
         * @param resolver property resolver
         * @param missing output list of missing property names
         * @param nonMatching output list of non-matching property names
         * @param matcher matcher used for comparison
         */
        @Override
        public void collectProperties(PropertyResolver resolver, List<String> missing, List<String> nonMatching, PropertySpecMatcher<Map<String, String>, Spec> matcher) {
            for (String name : this.getNames()) {
                try {
                    Map<String, String> propertyMap = new HashMap<>();
                    boolean anyKeyFound = false;
                    for (String mapKey : this.getHavingValue().keySet()) {
                        String key = this.getPrefix() + name + "." + mapKey;
                        String property = resolver.getProperty(key, String.class);
                        if (property != null) {
                            propertyMap.put(mapKey, property);
                            anyKeyFound = true;
                        }
                    }

                    if (!anyKeyFound) {
                        if (!this.isMatchIfMissing()) {
                            missing.add(name);
                        }
                        continue;
                    }

                    if (!this.isMatch(propertyMap, matcher)) {
                        nonMatching.add(name);
                    }
                } catch (ConversionException e) {
                    nonMatching.add(name);
                }
            }
        }
    }
}

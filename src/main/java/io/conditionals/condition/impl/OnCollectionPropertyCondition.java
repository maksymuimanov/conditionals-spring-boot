package io.conditionals.condition.impl;

import io.conditionals.condition.ConditionalOnCollectionProperties;
import io.conditionals.condition.ConditionalOnCollectionProperty;
import io.conditionals.condition.spec.CollectionMatchType;
import io.conditionals.condition.spec.MatchingPropertySpec;
import io.conditionals.condition.spec.PropertySpecMatcher;
import io.conditionals.condition.utils.ConditionUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.core.annotation.AnnotationAttributes;

import java.lang.annotation.Annotation;
import java.util.Arrays;

/**
 * {@link org.springframework.boot.autoconfigure.condition.SpringBootCondition} implementation backing
 * {@link io.conditionals.condition.ConditionalOnCollectionProperty}.
 *
 * <p>This condition resolves properties as {@code String[]} and compares them to the configured candidate array
 * using a {@link CollectionMatchType}-driven algorithm implemented by {@link Matcher}. An optional size
 * constraint is applied before evaluating the match type.</p>
 *
 * <p><b>Collection resolution</b></p>
 * <p>Property values are resolved through {@link org.springframework.core.env.Environment} using Spring's
 * conversion facilities for {@code String[]}.</p>
 *
 * <p><b>Negation</b></p>
 * <p>After computing the match predicate, negation is applied via
 * {@link ConditionUtils#revert(boolean, boolean)} using {@link Spec#isNot()}.</p>
 *
 * <p><b>Thread safety</b></p>
 * <p>This type is stateless and thread-safe. Matcher/spec instances are created per evaluation.</p>
 *
 * @author Maksym Uimanov
 * @since 1.0
 */
public class OnCollectionPropertyCondition extends PropertySpringBootCondition<String[], OnCollectionPropertyCondition.Spec> {
    @Override
    protected Class<? extends Annotation> getAnnotationClass() {
        return ConditionalOnCollectionProperty.class;
    }

    @Override
    protected Class<? extends Annotation> getAnnotationContainerClass() {
        return ConditionalOnCollectionProperties.class;
    }

    @Override
    protected Spec createSpec(@Nullable Class<? extends Annotation> annotationType, AnnotationAttributes annotationAttributes) {
        return new Spec(annotationType, annotationAttributes);
    }

    @Override
    protected PropertySpecMatcher<String[], Spec> createPropertySpecMatcher() {
        return new Matcher();
    }

    /**
     * Matcher implementing {@link CollectionMatchType} semantics for {@code String[]}.
     *
     * <p><b>Size constraint</b></p>
     * <p>If {@link Spec#size} is not {@code -1}, the resolved array length must equal the configured size.
     * If the size check fails, the result is the negation flag ({@link Spec#isNot()}) to preserve
     * {@code not}-inversion semantics for the size predicate.</p>
     */
    @SuppressWarnings("InnerClassMayBeStatic")
    public class Matcher implements PropertySpecMatcher<String[], Spec> {
        @Override
        public boolean compare(Spec spec, String @Nullable[] property, String[] candidate) {
            if (property == null) return false;
            if (spec.size != -1 && property.length != spec.size) return spec.isNot();
            boolean result = switch (spec.getMatchType()) {
                case EQUALS -> equals(property, candidate);
                case CONTAINS_ANY -> containsAny(property, candidate);
                case CONTAINS_ALL -> containsAll(property, candidate);
                case CONTAINS_SEQUENCE -> containsSequence(property, candidate);
                case STARTS_WITH_ANY -> startsWithAny(property, candidate);
                case STARTS_WITH_ALL -> startsWithAll(property, candidate);
                case ENDS_WITH_ANY -> endsWithAny(property, candidate);
                case ENDS_WITH_ALL -> endsWithAll(property, candidate);
            };
            return ConditionUtils.revert(result, spec.isNot());
        }

        /**
         * Compare arrays for equality using {@link Arrays#equals(Object[], Object[])}.
         */
        private static boolean equals(String[] property, String[] candidate) {
            return Arrays.equals(property, candidate);
        }

        /**
         * Match if any element of {@code candidate} is contained in {@code property}.
         */
        private static boolean containsAny(String[] property, String[] candidate) {
            int candidateLength = candidate.length;
            if (candidateLength == 0) return true;

            for (String candidateElement : candidate) {
                for (String propertyElement : property) {
                    if (propertyElement.equals(candidateElement)) {
                        return true;
                    }
                }
            }

            return false;
        }

        /**
         * Match if all elements of {@code candidate} are contained in {@code property} (order-insensitive).
         */
        private static boolean containsAll(String[] property, String[] candidate) {
            int candidateLength = candidate.length;
            if (candidateLength == 0) return true;
            int propertyLength = property.length;
            if (propertyLength < candidateLength) return false;

            for (String candidateElement : candidate) {
                boolean found = false;
                for (String propertyElement : property) {
                    if (propertyElement.equals(candidateElement)) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    return false;
                }
            }

            return true;
        }

        /**
         * Match if {@code property} contains {@code candidate} as a contiguous subsequence.
         */
        private static boolean containsSequence(String[] property, String[] candidate) {
            int candidateLength = candidate.length;
            if (candidateLength == 0) return true;
            int propertyLength = property.length;
            if (propertyLength < candidateLength) return false;

            outer:
            for (int i = 0; i <= property.length - candidate.length; i++) {
                for (int j = 0; j < candidate.length; j++) {
                    if (!property[i + j].equals(candidate[j])) {
                        continue outer;
                    }
                }
                return true;
            }
            return false;
        }

        /**
         * Match if the first element of {@code property} equals any element of {@code candidate}.
         */
        private static boolean startsWithAny(String[] property, String[] candidate) {
            int candidateLength = candidate.length;
            if (candidateLength == 0) return true;

            for (String candidateElement : candidate) {
                if (property[0].equals(candidateElement)) {
                    return true;
                }
            }

            return false;
        }

        /**
         * Match if {@code property} begins with the entire {@code candidate} sequence.
         */
        private static boolean startsWithAll(String[] property, String[] candidate) {
            int candidateLength = candidate.length;
            if (candidateLength == 0) return true;
            int propertyLength = property.length;
            if (propertyLength < candidateLength) return false;

            for (int i = 0; i < candidateLength; i++) {
                if (!property[i].equals(candidate[i])) {
                    return false;
                }
            }

            return true;
        }

        /**
         * Match if the last element of {@code property} equals any element of {@code candidate}.
         */
        private static boolean endsWithAny(String[] property, String[] candidate) {
            int candidateLength = candidate.length;
            if (candidateLength == 0) return true;
            int propertyLength = property.length;

            for (String candidateElement : candidate) {
                if (property[propertyLength - 1].equals(candidateElement)) {
                    return true;
                }
            }

            return false;
        }

        /**
         * Match if {@code property} ends with the entire {@code candidate} sequence.
         */
        private static boolean endsWithAll(String[] property, String[] candidate) {
            int candidateLength = candidate.length;
            if (candidateLength == 0) return true;
            int propertyLength = property.length;
            if (propertyLength < candidateLength) return false;

            for (int i = 0; i < candidateLength; i++) {
                if (!property[propertyLength - candidateLength + i].equals(candidate[i])) {
                    return false;
                }
            }

            return true;
        }
    }

    /**
     * {@link io.conditionals.condition.spec.PropertySpec} specialization for
     * {@link io.conditionals.condition.ConditionalOnCollectionProperty}.
     *
     * <p>This spec adds an optional size constraint to the shared {@code not} and {@code matchType} attributes
     * provided by {@link MatchingPropertySpec}.</p>
     */
    public class Spec extends MatchingPropertySpec<String[], Spec, CollectionMatchType> {
        private static final String SIZE = "size";
        private final int size;

        /**
         * Create a new spec from annotation attributes.
         *
         * @param annotationType annotation type producing the attributes
         * @param annotationAttributes resolved annotation attributes
         */
        private Spec(Class<? extends Annotation> annotationType, AnnotationAttributes annotationAttributes) {
            super(annotationType, annotationAttributes);
            this.size = annotationAttributes.getNumber(SIZE).intValue();
        }
    }
}

package io.conditionals.condition.impl;

import io.conditionals.condition.ConditionalOnStringProperties;
import io.conditionals.condition.ConditionalOnStringProperty;
import io.conditionals.condition.spec.MatchingPropertySpec;
import io.conditionals.condition.spec.PropertySpecMatcher;
import io.conditionals.condition.spec.StringMatchType;
import io.conditionals.condition.utils.ConditionUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.core.annotation.AnnotationAttributes;

import java.lang.annotation.Annotation;
import java.util.Locale;
import java.util.regex.PatternSyntaxException;

/**
 * {@link org.springframework.boot.autoconfigure.condition.SpringBootCondition} implementation backing
 * {@link io.conditionals.condition.ConditionalOnStringProperty}.
 *
 * <p><b>Integration with Spring Boot</b></p>
 * <p>Spring calls {@link #getMatchOutcome(org.springframework.context.annotation.ConditionContext, org.springframework.core.type.AnnotatedTypeMetadata)}
 * (inherited from {@link PropertySpringBootCondition}) during configuration class parsing and bean definition
 * processing. This condition inspects {@link ConditionalOnStringProperty} and its repeatable container
 * {@link ConditionalOnStringProperties} on the current {@link org.springframework.core.type.AnnotatedTypeMetadata}.</p>
 *
 * <p><b>Evaluation algorithm</b></p>
 * <ul>
 *     <li>Annotation attributes are resolved (including repeatable instances) and converted into a {@link Spec}.</li>
 *     <li>For each configured property name, the effective key is {@code prefix + name} (prefix normalization is
 *     handled by {@link io.conditionals.condition.spec.PropertySpec}).</li>
 *     <li>Each resolved string property value is compared to the configured candidate using {@link Matcher}.</li>
 *     <li>The per-property comparison result is optionally negated using the {@code not} attribute.
 *     Missing properties are handled according to {@code matchIfMissing}.</li>
 * </ul>
 *
 * <p><b>Thread safety</b></p>
 * <p>Instances are stateless and therefore thread-safe. Nested matcher/spec objects are created per evaluation.</p>
 *
 * @author Maksym Uimanov
 * @since 1.0
 * @see io.conditionals.condition.ConditionalOnStringProperty
 * @see io.conditionals.condition.ConditionalOnStringProperties
 */
public class OnStringPropertyCondition extends PropertySpringBootCondition<String, OnStringPropertyCondition.Spec> {
    @Override
    protected Class<? extends Annotation> getAnnotationClass() {
        return ConditionalOnStringProperty.class;
    }

    @Override
    protected Class<? extends Annotation> getAnnotationContainerClass() {
        return ConditionalOnStringProperties.class;
    }

    @Override
    protected Spec createSpec(@Nullable Class<? extends Annotation> annotationType, AnnotationAttributes annotationAttributes) {
        return new Spec(annotationType, annotationAttributes);
    }

    @Override
    protected PropertySpecMatcher<String, Spec> createPropertySpecMatcher() {
        return new Matcher();
    }

    /**
     * Performs string comparison according to {@link Spec#getMatchType()} and applies the
     * {@link io.conditionals.condition.ConditionalOnStringProperty#not()} flag via
     * {@link ConditionUtils#revert(boolean, boolean)}.
     *
     * <p><b>Normalization</b></p>
     * <ul>
     *     <li>If {@link io.conditionals.condition.ConditionalOnStringProperty#ignoreCase()} is enabled,
     *     both the property value and candidate value are lower-cased using {@link Locale#ROOT}.</li>
     *     <li>If {@link io.conditionals.condition.ConditionalOnStringProperty#trim()} is enabled,
     *     both values are trimmed prior to comparison.</li>
     * </ul>
     */
    public static class Matcher implements PropertySpecMatcher<String, Spec> {
        @Override
        public boolean compare(Spec spec, @Nullable String property, String candidate) {
            if (property == null) return false;
            if (spec.ignoreCase) {
                property = property.toLowerCase(Locale.ROOT);
                candidate = candidate.toLowerCase(Locale.ROOT);
            }
            if (spec.trim) {
                property = property.trim();
                candidate = candidate.trim();
            }
            boolean result = switch (spec.getMatchType()) {
                case EQUALS -> equals(property, candidate);
                case CONTAINS -> contains(property, candidate);
                case STARTS_WITH -> startsWith(property, candidate);
                case ENDS_WITH -> endsWith(property, candidate);
                case MATCHES -> matches(property, candidate);
            };
            return ConditionUtils.revert(result, spec.isNot());
        }

        private static boolean equals(String property, String candidate) {
            return property.equals(candidate);
        }

        private static boolean contains(String property, String candidate) {
            return property.contains(candidate);
        }

        private static boolean startsWith(String property, String candidate) {
            return property.startsWith(candidate);
        }

        private static boolean endsWith(String property, String candidate) {
            return property.endsWith(candidate);
        }

        private static boolean matches(String property, String candidate) {
            try {
                return property.matches(candidate);
            } catch (PatternSyntaxException e) {
                return false;
            }
        }
    }

    /**
     * {@link io.conditionals.condition.spec.PropertySpec} specialization for
     * {@link io.conditionals.condition.ConditionalOnStringProperty}.
     *
     * <p>In addition to the common attributes handled by {@link MatchingPropertySpec}, this spec captures
     * {@code ignoreCase} and {@code trim} options used by {@link Matcher}.</p>
     */
    public static class Spec extends MatchingPropertySpec<String, Spec, StringMatchType> {
        private static final String IGNORE_CASE = "ignoreCase";
        private static final String TRIM = "trim";
        private final boolean ignoreCase;
        private final boolean trim;

        /**
         * Create a new spec from annotation attributes.
         *
         * @param annotationType concrete annotation type (never {@code null} when created from metadata)
         * @param annotationAttributes attributes of the conditional annotation
         */
        private Spec(Class<? extends Annotation> annotationType, AnnotationAttributes annotationAttributes) {
            super(annotationType, annotationAttributes);
            this.ignoreCase = annotationAttributes.getBoolean(IGNORE_CASE);
            this.trim = annotationAttributes.getBoolean(TRIM);
        }
    }
}

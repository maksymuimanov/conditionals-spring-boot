package io.conditionals.condition.impl;

import io.conditionals.condition.ConditionalOnStringProperties;
import io.conditionals.condition.ConditionalOnStringProperty;
import io.conditionals.condition.dto.PropertySpec;
import io.conditionals.condition.utils.ConditionUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.lang.annotation.Annotation;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Spring Boot {@link org.springframework.context.annotation.Condition} implementation backing
 * {@link io.conditionals.condition.ConditionalOnStringProperty}.
 *
 * <p>
 * This condition is evaluated by Spring Boot during the condition evaluation phase of configuration class
 * processing. The evaluation is triggered by the presence of {@code @ConditionalOnStringProperty} (or its
 * container {@code @ConditionalOnStringProperties}) on a configuration class or {@code @Bean} method.
 * </p>
 *
 * <p><b>Semantics</b></p>
 * <ul>
 *     <li><b>Pipeline integration</b>: Spring Boot calls {@link #getMatchOutcome(ConditionContext, AnnotatedTypeMetadata)}
 *     and expects a {@link ConditionOutcome} describing match/no-match and a diagnostic message.</li>
 *     <li><b>Attribute discovery</b>: annotation attributes are obtained via
 *     {@link ConditionUtils#mergedStream(AnnotatedTypeMetadata, Class, Class)}. The stream yields the direct
 *     annotation instance (if present) followed by container elements, in encounter order.</li>
 *     <li><b>Aggregation</b>: multiple annotation instances are aggregated with AND semantics via
 *     {@link ConditionUtils#evaluateConditions(ConditionMessage.Builder, Stream, java.util.function.Function)}.
 *     Each annotation instance must match for the overall outcome to match.</li>
 *     <li><b>Property evaluation order</b>: within each annotation instance, configured property names are evaluated
 *     in the order provided by {@link io.conditionals.condition.dto.PropertySpec}.</li>
 *     <li><b>Comparison</b>: for each configured property, the resolved value is compared against
 *     {@code havingValue} using {@code matchType}. Optional normalization is applied in the following order:
 *     {@code ignoreCase} (lower-case with {@link Locale#ROOT}) and then {@code trim}.</li>
 *     <li><b>Negation</b>: the comparison result is XOR'ed with {@code not}; when {@code not=true} the predicate is
 *     logically inverted.</li>
 *     <li><b>Missing behavior</b>: missing properties produce a no-match outcome unless {@code matchIfMissing=true}.</li>
 * </ul>
 *
 * <p><b>Thread Safety</b></p>
 * <p>
 * This class is thread-safe. It is stateless and delegates to thread-safe infrastructure. Thread-safety of
 * {@link ConditionContext} and {@link AnnotatedTypeMetadata} instances is managed by the Spring container.
 * </p>
 *
 * <p><b>Null Handling</b></p>
 * <ul>
 *     <li>If annotation attributes are absent, the condition yields a no-match outcome.</li>
 *     <li>A resolved property value of {@code null} is treated as non-matching.</li>
 * </ul>
 *
 * <p><b>Edge Cases</b></p>
 * <ul>
 *     <li>Regular expression evaluation for {@link io.conditionals.condition.ConditionalOnStringProperty.MatchType#MATCHES}
 *     delegates to {@link String#matches(String)} and may throw {@link java.util.regex.PatternSyntaxException}
 *     if {@code havingValue} is not a valid pattern; such an exception will propagate.</li>
 *     <li>When {@code trim=true}, leading and trailing whitespace is removed before comparison; internal whitespace
 *     is preserved.</li>
 * </ul>
 *
 * <p><b>Performance Characteristics</b></p>
 * <p>
 * Evaluation is O(n) in the number of configured property names. For {@code MATCHES}, runtime depends on the
 * regular expression complexity.
 * </p>
 *
 * <p><b>Usage Example</b></p>
 * <pre>{@code
 * @Configuration
 * @ConditionalOnStringProperty(prefix = "app", name = "mode", havingValue = "prod")
 * class ProdOnlyConfiguration {
 * }
 * }</pre>
 *
 * @author Maksym Uimanov
 * @since 1.0
 * @see io.conditionals.condition.ConditionalOnStringProperty
 * @see io.conditionals.condition.ConditionalOnStringProperties
 * @see ConditionUtils
 */
public class OnStringPropertyCondition extends SpringBootCondition {
    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context,
                                            AnnotatedTypeMetadata metadata) {
        ConditionMessage.Builder message = ConditionMessage.forCondition(ConditionalOnStringProperty.class);
        Stream<@Nullable AnnotationAttributes> annotationAttributes = ConditionUtils.mergedStream(metadata, ConditionalOnStringProperty.class, ConditionalOnStringProperties.class);
        return ConditionUtils.evaluateConditions(message, annotationAttributes, attributes ->
                ConditionUtils.evaluatePropertyConditions(message, attributes, Spec::new, context.getEnvironment(), (spec, property, candidate) -> {
                    if (property == null) return false;
                    if (spec.ignoreCase) {
                        property = property.toLowerCase(Locale.ROOT);
                        candidate = candidate.toLowerCase(Locale.ROOT);
                    }
                    if (spec.trim) {
                        property = property.trim();
                        candidate = candidate.trim();
                    }
                    boolean result = switch (spec.matchType) {
                        case EQUALS -> property.equals(candidate);
                        case CONTAINS -> property.contains(candidate);
                        case STARTS_WITH -> property.startsWith(candidate);
                        case ENDS_WITH -> property.endsWith(candidate);
                        case MATCHES -> property.matches(candidate);
                    };
                    return result ^ spec.not;
                })
        );
    }

    private static class Spec extends PropertySpec<String, Spec> {
        private static final String IGNORE_CASE = "ignoreCase";
        private static final String TRIM = "trim";
        private static final String NOT = "not";
        private static final String MATCH_TYPE = "matchType";
        private final boolean ignoreCase;
        private final boolean trim;
        private final boolean not;
        private final ConditionalOnStringProperty.MatchType matchType;

        protected Spec(Class<? extends Annotation> annotationType, AnnotationAttributes annotationAttributes) {
            super(annotationType, annotationAttributes);
            this.ignoreCase = annotationAttributes.getBoolean(IGNORE_CASE);
            this.trim = annotationAttributes.getBoolean(TRIM);
            this.not = annotationAttributes.getBoolean(NOT);
            this.matchType = annotationAttributes.getEnum(MATCH_TYPE);
        }
    }
}

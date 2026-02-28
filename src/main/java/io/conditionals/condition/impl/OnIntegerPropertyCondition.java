package io.conditionals.condition.impl;

import io.conditionals.condition.ConditionalOnIntegerProperties;
import io.conditionals.condition.ConditionalOnIntegerProperty;
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
import java.util.stream.Stream;

/**
 * Spring Boot {@link org.springframework.context.annotation.Condition} implementation backing
 * {@link io.conditionals.condition.ConditionalOnIntegerProperty}.
 *
 * <p>
 * This condition participates in Spring Boot's condition evaluation pipeline. It is invoked for configuration
 * classes and {@code @Bean} methods annotated with {@code @ConditionalOnIntegerProperty} (including repeatable
 * declarations via {@code @ConditionalOnIntegerProperties}).
 * </p>
 *
 * <p><b>Semantics</b></p>
 * <ul>
 *     <li><b>Pipeline integration</b>: evaluation is performed in
 *     {@link #getMatchOutcome(ConditionContext, AnnotatedTypeMetadata)} and reported using {@link ConditionOutcome}.</li>
 *     <li><b>Attribute discovery</b>: annotation instances are obtained using
 *     {@link ConditionUtils#mergedStream(AnnotatedTypeMetadata, Class, Class)}. Encounter order is preserved.</li>
 *     <li><b>Aggregation</b>: multiple annotation instances are aggregated with AND semantics; any no-match outcome
 *     yields a no-match overall outcome.</li>
 *     <li><b>Property evaluation</b>: within an annotation instance, property names are evaluated in encounter order.
 *     Each name contributes to the outcome independently.</li>
 *     <li><b>Comparison</b>: resolved integer values are compared to {@code havingValue} according to {@code matchType}
 *     ({@code EQUALS}, {@code GREATER_THAN}, {@code LESS_THAN}, {@code GREATER_THAN_OR_EQUAL},
 *     {@code LESS_THAN_OR_EQUAL}).</li>
 *     <li><b>Negation</b>: the comparison result is XOR'ed with {@code not}.</li>
 *     <li><b>Missing behavior</b>: a missing property yields no-match unless {@code matchIfMissing=true}.</li>
 * </ul>
 *
 * <p><b>Thread Safety</b></p>
 * <p>
 * This class is thread-safe. It is stateless and depends on Spring-managed inputs.
 * </p>
 *
 * <p><b>Null Handling</b></p>
 * <ul>
 *     <li>Absent annotation attributes yield a no-match outcome.</li>
 *     <li>A resolved property value of {@code null} is treated as non-matching.</li>
 * </ul>
 *
 * <p><b>Edge Cases</b></p>
 * <ul>
 *     <li>Type conversion failures while resolving integer properties are treated as non-matching.</li>
 *     <li>Comparison uses Java integer ordering semantics; overflow is not possible in comparisons but may occur
 *     externally when configuring {@code havingValue}.</li>
 * </ul>
 *
 * <p><b>Performance Characteristics</b></p>
 * <p>Evaluation is O(n) in the number of configured property names.</p>
 *
 * <p><b>Usage Example</b></p>
 * <pre>{@code
 * @ConditionalOnIntegerProperty(prefix = "app", name = "threads", havingValue = 4,
 *         matchType = ConditionalOnIntegerProperty.MatchType.GREATER_THAN_OR_EQUAL)
 * @Configuration
 * class ConcurrencyConfiguration {
 * }
 * }</pre>
 *
 * @author Maksym Uimanov
 * @since 1.0
 * @see io.conditionals.condition.ConditionalOnIntegerProperty
 * @see io.conditionals.condition.ConditionalOnIntegerProperties
 */
public class OnIntegerPropertyCondition extends SpringBootCondition {
    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context,
                                            AnnotatedTypeMetadata metadata) {
        ConditionMessage.Builder message = ConditionMessage.forCondition(ConditionalOnIntegerProperty.class);
        Stream<@Nullable AnnotationAttributes> annotationAttributes = ConditionUtils.mergedStream(metadata, ConditionalOnIntegerProperty.class, ConditionalOnIntegerProperties.class);
        return ConditionUtils.evaluateConditions(message, annotationAttributes, attributes ->
                ConditionUtils.evaluatePropertyConditions(message, attributes, Spec::new, context.getEnvironment(), (spec, property, candidate) -> {
                    if (property == null) return false;
                    boolean result = switch (spec.matchType) {
                        case EQUALS -> property.equals(candidate);
                        case GREATER_THAN -> property > candidate;
                        case LESS_THAN -> property < candidate;
                        case GREATER_THAN_OR_EQUAL -> property >= candidate;
                        case LESS_THAN_OR_EQUAL -> property <= candidate;
                    };
                    return result ^ spec.not;
                })
        );
    }

    private static class Spec extends PropertySpec<Integer, Spec> {
        private static final String NOT = "not";
        private static final String MATCH_TYPE = "matchType";
        private final boolean not;
        private final ConditionalOnIntegerProperty.MatchType matchType;

        protected Spec(Class<? extends Annotation> annotationType, AnnotationAttributes annotationAttributes) {
            super(annotationType, annotationAttributes);
            this.not = annotationAttributes.getBoolean(NOT);
            this.matchType = annotationAttributes.getEnum(MATCH_TYPE);
        }
    }
}

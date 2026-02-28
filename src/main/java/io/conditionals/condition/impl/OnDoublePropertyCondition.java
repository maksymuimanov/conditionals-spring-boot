package io.conditionals.condition.impl;

import io.conditionals.condition.ConditionalOnDoubleProperties;
import io.conditionals.condition.ConditionalOnDoubleProperty;
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

public class OnDoublePropertyCondition extends SpringBootCondition {
    private static final double PRECISION = 0.000000001D;

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context,
                                            AnnotatedTypeMetadata metadata) {
        ConditionMessage.Builder message = ConditionMessage.forCondition(ConditionalOnDoubleProperty.class);
        Stream<@Nullable AnnotationAttributes> annotationAttributes = ConditionUtils.mergedStream(metadata, ConditionalOnDoubleProperty.class, ConditionalOnDoubleProperties.class);
        return ConditionUtils.evaluateConditions(message, annotationAttributes, attributes ->
                ConditionUtils.evaluatePropertyConditions(message, attributes, Spec::new, context.getEnvironment(), (spec, property, candidate) -> {
                    if (property == null || Double.isNaN(candidate) || Double.isNaN(property)) return false;
                    boolean result = switch (spec.matchType) {
                        case EQUALS -> Math.abs(property - candidate) < PRECISION;
                        case GREATER_THAN -> property > candidate;
                        case LESS_THAN -> property < candidate;
                        case GREATER_THAN_OR_EQUAL -> property >= candidate;
                        case LESS_THAN_OR_EQUAL -> property <= candidate;
                    };
                    return result ^ spec.not;
                })
        );
    }

    private static class Spec extends PropertySpec<Double, Spec> {
        private static final String NOT = "not";
        private static final String MATCH_TYPE = "matchType";
        private final boolean not;
        private final ConditionalOnDoubleProperty.MatchType matchType;

        protected Spec(Class<? extends Annotation> annotationType, AnnotationAttributes annotationAttributes) {
            super(annotationType, annotationAttributes);
            this.not = annotationAttributes.getBoolean(NOT);
            this.matchType = annotationAttributes.getEnum(MATCH_TYPE);
        }
    }
}

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

public class OnStringPropertyCondition extends SpringBootCondition {
    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context,
                                            AnnotatedTypeMetadata metadata) {
        ConditionMessage.Builder message = ConditionMessage.forCondition(ConditionalOnStringProperty.class);
        Stream<@Nullable AnnotationAttributes> annotationAttributes = ConditionUtils.mergedStream(metadata, ConditionalOnStringProperty.class, ConditionalOnStringProperties.class);
        return ConditionUtils.evaluateConditions(message, annotationAttributes, attributes ->
                ConditionUtils.evaluatePropertyConditions(message, attributes, Spec::new, context.getEnvironment(), (spec, property, candidate) -> {
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

package io.conditionals.condition.impl;

import io.conditionals.condition.ConditionalOnEnumProperties;
import io.conditionals.condition.ConditionalOnEnumProperty;
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

public class OnEnumPropertyCondition extends SpringBootCondition {
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public ConditionOutcome getMatchOutcome(ConditionContext context,
                                            AnnotatedTypeMetadata metadata) {
        ConditionMessage.Builder message = ConditionMessage.forCondition(ConditionalOnEnumProperty.class);
        Stream<@Nullable AnnotationAttributes> annotationAttributes = ConditionUtils.mergedStream(metadata, ConditionalOnEnumProperty.class, ConditionalOnEnumProperties.class);
        return ConditionUtils.evaluateConditions(message, annotationAttributes, attributes ->
                ConditionUtils.evaluatePropertyConditions(message, attributes, Spec::new, context.getEnvironment(), (spec, property, candidate) -> {
                    Class<? extends Enum> enumClass = spec.enumType;
                    String propertyName = property.toUpperCase(Locale.ROOT);
                    Enum propertyEnum = Enum.valueOf(enumClass, propertyName);
                    String candidateName = candidate.toUpperCase(Locale.ROOT);
                    Enum candidateEnum = Enum.valueOf(enumClass, candidateName);
                    return propertyEnum.equals(candidateEnum);
                })
        );
    }

    @SuppressWarnings("rawtypes")
    private static class Spec extends PropertySpec<String, Spec> {
        private static final String ENUM_TYPE = "enumType";
        private final Class<? extends Enum> enumType;

        protected Spec(Class<? extends Annotation> annotationType, AnnotationAttributes annotationAttributes) {
            super(annotationType, annotationAttributes);
            this.enumType = annotationAttributes.getClass(ENUM_TYPE);
        }
    }
}

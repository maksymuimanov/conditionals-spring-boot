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

/**
 * Spring Boot {@link org.springframework.context.annotation.Condition} implementation backing
 * {@link io.conditionals.condition.ConditionalOnEnumProperty}.
 *
 * <p>
 * This condition is evaluated by Spring Boot when processing conditional configuration elements annotated with
 * {@code @ConditionalOnEnumProperty}. Repeatable declarations are supported via
 * {@link io.conditionals.condition.ConditionalOnEnumProperties}.
 * </p>
 *
 * <p><b>Semantics</b></p>
 * <ul>
 *     <li><b>Attribute discovery</b>: annotation instances are obtained via
 *     {@link ConditionUtils#mergedStream(AnnotatedTypeMetadata, Class, Class)} in encounter order.</li>
 *     <li><b>Aggregation</b>: multiple annotation instances are aggregated with AND semantics.</li>
 *     <li><b>Property evaluation order</b>: within each annotation instance, property names are evaluated in
 *     encounter order.</li>
 *     <li><b>Normalization</b>: both the resolved string value and the candidate {@code havingValue} are upper-cased
 *     using {@link Locale#ROOT} before enum conversion.</li>
 *     <li><b>Enum conversion</b>: conversion uses {@link Enum#valueOf(Class, String)} against the annotation-provided
 *     {@code enumType}. If either value cannot be converted, the property is treated as non-matching.</li>
 *     <li><b>Missing behavior</b>: a missing property yields no-match unless {@code matchIfMissing=true}.</li>
 * </ul>
 *
 * <p><b>Thread Safety</b></p>
 * <p>This class is thread-safe. It is stateless.</p>
 *
 * <p><b>Null Handling</b></p>
 * <ul>
 *     <li>Absent annotation attributes yield a no-match outcome.</li>
 *     <li>A resolved property value of {@code null} is treated as non-matching.</li>
 * </ul>
 *
 * <p><b>Edge Cases</b></p>
 * <ul>
 *     <li>If {@code enumType} is not an enum type, {@link Enum#valueOf(Class, String)} will throw; behavior is
 *     undefined by this class and may propagate depending on JVM checks.</li>
 *     <li>Invalid enum names (after normalization) are treated as non-matching via {@link IllegalArgumentException}
 *     handling.</li>
 * </ul>
 *
 * <p><b>Performance Characteristics</b></p>
 * <p>Evaluation is O(n) in the number of configured property names; enum conversion is O(1) per comparison.</p>
 *
 * <p><b>Usage Example</b></p>
 * <pre>{@code
 * @ConditionalOnEnumProperty(prefix = "app", name = "level", havingValue = "INFO", enumType = MyLevel.class)
 * @Configuration
 * class LoggingConfiguration {
 * }
 * }</pre>
 *
 * @author Maksym Uimanov
 * @since 1.0
 * @see io.conditionals.condition.ConditionalOnEnumProperty
 * @see io.conditionals.condition.ConditionalOnEnumProperties
 */
public class OnEnumPropertyCondition extends SpringBootCondition {
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public ConditionOutcome getMatchOutcome(ConditionContext context,
                                            AnnotatedTypeMetadata metadata) {
        ConditionMessage.Builder message = ConditionMessage.forCondition(ConditionalOnEnumProperty.class);
        Stream<@Nullable AnnotationAttributes> annotationAttributes = ConditionUtils.mergedStream(metadata, ConditionalOnEnumProperty.class, ConditionalOnEnumProperties.class);
        return ConditionUtils.evaluateConditions(message, annotationAttributes, attributes ->
                ConditionUtils.evaluatePropertyConditions(message, attributes, Spec::new, context.getEnvironment(), (spec, property, candidate) -> {
                    if (property == null) return false;
                    Class<? extends Enum> enumClass = spec.enumType;
                    String propertyName = property.toUpperCase(Locale.ROOT);
                    String candidateName = candidate.toUpperCase(Locale.ROOT);
                    try {
                        Enum propertyEnum = Enum.valueOf(enumClass, propertyName);
                        Enum candidateEnum = Enum.valueOf(enumClass, candidateName);
                        return propertyEnum.equals(candidateEnum);
                    } catch (IllegalArgumentException ex) {
                        return false;
                    }
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

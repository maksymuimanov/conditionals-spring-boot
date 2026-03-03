package io.conditionals.condition;

import io.conditionals.condition.impl.OnEnumPropertyCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * Declares a Spring Boot condition that matches based on enum-valued properties.
 *
 * <p>
 * The property is resolved as a {@link String} and then converted to an enum constant of the supplied
 * {@link #enumType()}. Spring Boot evaluates the associated
 * {@link io.conditionals.condition.impl.OnEnumPropertyCondition} during the condition evaluation phase of
 * configuration processing.
 * </p>
 *
 * <p><b>Semantics</b></p>
 * <ul>
 *     <li><b>Binding</b>: this annotation is backed by {@link io.conditionals.condition.impl.OnEnumPropertyCondition}.</li>
 *     <li><b>Repeatable evaluation</b>: multiple instances (via {@link Repeatable} or {@link ConditionalOnEnumProperties})
 *     are aggregated with AND semantics.</li>
 *     <li><b>Name resolution</b>: property keys are composed as {@code prefix + name} with prefix normalization
 *     (trim; append {@code '.'} when non-empty).</li>
 *     <li><b>Property name selection</b>: {@link #value()} and {@link #name()} are mutually exclusive; exactly one must
 *     be non-empty.</li>
 *     <li><b>Normalization</b>: both the resolved property value and {@link #havingValue()} are upper-cased using
 *     {@link java.util.Locale#ROOT} before enum conversion.</li>
 *     <li><b>Enum conversion</b>: conversion uses {@link Enum#valueOf(Class, String)}. If either side cannot be
 *     converted, the comparison is treated as non-matching.</li>
 *     <li><b>Missing behavior</b>: missing properties produce a no-match outcome unless {@link #matchIfMissing()} is {@code true}.</li>
 * </ul>
 *
 * <p><b>Thread Safety</b></p>
 * <p>This annotation is thread-safe. It declares immutable metadata.</p>
 *
 * <p><b>Null Handling</b></p>
 * <p>Annotation attributes are never {@code null}; resolved property values may be {@code null} and are treated as non-matching.</p>
 *
 * <p><b>Edge Cases</b></p>
 * <ul>
 *     <li>If {@link #enumType()} is not an enum type, evaluation may throw at runtime.</li>
 *     <li>Empty {@link #value()} and {@link #name()} arrays are invalid and will result in an {@link IllegalStateException}
 *     during evaluation.</li>
 * </ul>
 *
 * <p><b>Performance Characteristics</b></p>
 * <p>Evaluation is linear in the number of configured property names and annotation instances.</p>
 *
 * <p><b>Usage Example</b></p>
 * <pre>{@code
 * @ConditionalOnEnumProperty(prefix = "app", name = "level", havingValue = "INFO", enumType = Level.class)
 * @Configuration
 * class LoggingConfiguration {
 * }
 * }</pre>
 *
 * @author Maksym Uimanov
 * @since 1.0
 * @see io.conditionals.condition.impl.OnEnumPropertyCondition
 * @see ConditionalOnEnumProperties
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnEnumPropertyCondition.class)
@Repeatable(ConditionalOnEnumProperties.class)
public @interface ConditionalOnEnumProperty {
    /**
     * Alias for {@link #name()}.
     *
     * <p>Specifies one or more property names (without {@link #prefix()}). Exactly one of {@link #value()} or
     * {@link #name()} must be specified.</p>
     *
     * @return property names (without prefix)
     */
    String[] value() default {};

    /**
     * Prefix to apply to each configured property name.
     *
     * <p>The value is trimmed. When non-empty and not ending with {@code '.'}, a trailing dot is appended.
     * The resulting key is {@code prefix + name}.</p>
     *
     * @return normalized prefix for property keys
     */
    String prefix() default "";

    /**
     * One or more property names (without {@link #prefix()}).
     *
     * <p>Exactly one of {@link #value()} or {@link #name()} must be specified.</p>
     *
     * @return property names (without prefix)
     */
    String[] name() default {};

    /**
     * Candidate enum constant name to compare against the resolved property value.
     *
     * <p>The property value is resolved as a {@link String}. Both the resolved value and this candidate value
     * are normalized by upper-casing using {@link java.util.Locale#ROOT} before conversion via
     * {@link Enum#valueOf(Class, String)}.</p>
     *
     * @return candidate enum constant name
     */
    String havingValue() default "";

    /**
     * Enum type used to convert and compare values.
     *
     * <p>The backing condition uses {@link Enum#valueOf(Class, String)} for conversion. If conversion fails,
     * the value is treated as non-matching.</p>
     *
     * @return enum type used for conversion
     */
    Class<? extends Enum<?>> enumType();

    /**
     * Whether to consider missing properties as matching.
     *
     * <p>When {@code true}, a missing property does not contribute to a non-match outcome. When {@code false},
     * any missing property causes the condition to not match.</p>
     *
     * @return {@code true} to match when a configured property is missing
     */
    boolean matchIfMissing() default false;
}
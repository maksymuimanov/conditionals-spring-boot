package io.conditionals.condition;

import io.conditionals.condition.impl.OnCharacterPropertyCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * Repeatable container for {@link ConditionalOnCharacterProperty}.
 *
 * <p>This annotation is used as the container type for {@link java.lang.annotation.Repeatable}. When present,
 * {@link io.conditionals.condition.impl.OnCharacterPropertyCondition} evaluates each contained
 * {@link ConditionalOnCharacterProperty} instance and aggregates outcomes with AND semantics.</p>
 *
 * @author Maksym Uimanov
 * @since 1.0
 * @see ConditionalOnCharacterProperty
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnCharacterPropertyCondition.class)
public @interface ConditionalOnCharacterProperties {
    /**
     * Contained repeatable annotation instances.
     *
     * @return contained {@link ConditionalOnCharacterProperty} declarations
     */
    ConditionalOnCharacterProperty[] value();
}
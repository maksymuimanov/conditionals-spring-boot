package io.conditionals.condition;

import io.conditionals.condition.impl.OnEnumPropertyCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * Repeatable container for {@link ConditionalOnEnumProperty}.
 *
 * <p>This annotation is used as the container type for {@link java.lang.annotation.Repeatable}. When present,
 * {@link io.conditionals.condition.impl.OnEnumPropertyCondition} evaluates each contained
 * {@link ConditionalOnEnumProperty} instance and aggregates outcomes with AND semantics.</p>
 *
 * @author Maksym Uimanov
 * @since 1.0
 * @see ConditionalOnEnumProperty
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnEnumPropertyCondition.class)
public @interface ConditionalOnEnumProperties {
    /**
     * Contained repeatable annotation instances.
     *
     * @return contained {@link ConditionalOnEnumProperty} declarations
     */
    ConditionalOnEnumProperty[] value();
}
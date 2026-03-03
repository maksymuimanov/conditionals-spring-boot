package io.conditionals.condition;

import io.conditionals.condition.impl.OnFloatPropertyCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * Repeatable container for {@link ConditionalOnFloatProperty}.
 *
 * <p>This annotation is used as the container type for {@link java.lang.annotation.Repeatable}. When present,
 * {@link io.conditionals.condition.impl.OnFloatPropertyCondition} evaluates each contained
 * {@link ConditionalOnFloatProperty} instance and aggregates outcomes with AND semantics.</p>
 *
 * @author Maksym Uimanov
 * @since 1.0
 * @see ConditionalOnFloatProperty
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnFloatPropertyCondition.class)
public @interface ConditionalOnFloatProperties {
    /**
     * Contained repeatable annotation instances.
     *
     * @return contained {@link ConditionalOnFloatProperty} declarations
     */
    ConditionalOnFloatProperty[] value();
}
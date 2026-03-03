package io.conditionals.condition;

import io.conditionals.condition.impl.OnLongPropertyCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * Repeatable container for {@link ConditionalOnLongProperty}.
 *
 * <p>This annotation is used as the container type for {@link java.lang.annotation.Repeatable}. When present,
 * {@link io.conditionals.condition.impl.OnLongPropertyCondition} evaluates each contained
 * {@link ConditionalOnLongProperty} instance and aggregates outcomes with AND semantics.</p>
 *
 * @author Maksym Uimanov
 * @since 1.0
 * @see ConditionalOnLongProperty
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnLongPropertyCondition.class)
public @interface ConditionalOnLongProperties {
    /**
     * Contained repeatable annotation instances.
     *
     * @return contained {@link ConditionalOnLongProperty} declarations
     */
    ConditionalOnLongProperty[] value();
}
package io.conditionals.condition;

import io.conditionals.condition.impl.OnDoublePropertyCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * Repeatable container for {@link ConditionalOnDoubleProperty}.
 *
 * <p>This annotation is used as the container type for {@link java.lang.annotation.Repeatable}. When present,
 * {@link io.conditionals.condition.impl.OnDoublePropertyCondition} evaluates each contained
 * {@link ConditionalOnDoubleProperty} instance and aggregates outcomes with AND semantics.</p>
 *
 * @author Maksym Uimanov
 * @since 1.0
 * @see ConditionalOnDoubleProperty
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnDoublePropertyCondition.class)
public @interface ConditionalOnDoubleProperties {
    /**
     * Contained repeatable annotation instances.
     *
     * @return contained {@link ConditionalOnDoubleProperty} declarations
     */
    ConditionalOnDoubleProperty[] value();
}
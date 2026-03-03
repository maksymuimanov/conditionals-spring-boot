package io.conditionals.condition;

import io.conditionals.condition.impl.OnMapPropertyCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * Repeatable container for {@link ConditionalOnMapProperty}.
 *
 * <p>This annotation is used as the container type for {@link java.lang.annotation.Repeatable}. When present,
 * {@link io.conditionals.condition.impl.OnMapPropertyCondition} evaluates each contained
 * {@link ConditionalOnMapProperty} instance and aggregates outcomes with AND semantics.</p>
 *
 * @author Maksym Uimanov
 * @since 1.0
 * @see ConditionalOnMapProperty
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnMapPropertyCondition.class)
public @interface ConditionalOnMapProperties {
    /**
     * Contained repeatable annotation instances.
     *
     * @return contained {@link ConditionalOnMapProperty} declarations
     */
    ConditionalOnMapProperty[] value();
}
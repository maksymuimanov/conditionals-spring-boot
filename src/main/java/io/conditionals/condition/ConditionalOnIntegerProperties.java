package io.conditionals.condition;

import io.conditionals.condition.impl.OnIntegerPropertyCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * Repeatable container for {@link ConditionalOnIntegerProperty}.
 *
 * <p>This annotation is used as the container type for {@link java.lang.annotation.Repeatable}. When present,
 * {@link io.conditionals.condition.impl.OnIntegerPropertyCondition} evaluates each contained
 * {@link ConditionalOnIntegerProperty} instance and aggregates outcomes with AND semantics.</p>
 *
 * @author Maksym Uimanov
 * @since 1.0
 * @see ConditionalOnIntegerProperty
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnIntegerPropertyCondition.class)
public @interface ConditionalOnIntegerProperties {
    /**
     * Contained repeatable annotation instances.
     *
     * @return contained {@link ConditionalOnIntegerProperty} declarations
     */
    ConditionalOnIntegerProperty[] value();
}
package io.conditionals.condition;

import io.conditionals.condition.impl.OnDurationPropertyCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * Repeatable container for {@link ConditionalOnDurationProperty}.
 *
 * <p>This annotation is used as the container type for {@link java.lang.annotation.Repeatable}. When present,
 * {@link io.conditionals.condition.impl.OnDurationPropertyCondition} evaluates each contained
 * {@link ConditionalOnDurationProperty} instance and aggregates outcomes with AND semantics.</p>
 *
 * @author Maksym Uimanov
 * @since 1.0
 * @see ConditionalOnDurationProperty
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnDurationPropertyCondition.class)
public @interface ConditionalOnDurationProperties {
    /**
     * Contained repeatable annotation instances.
     *
     * @return contained {@link ConditionalOnDurationProperty} declarations
     */
    ConditionalOnDurationProperty[] value();
}
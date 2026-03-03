package io.conditionals.condition;

import io.conditionals.condition.impl.OnStringPropertyCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * Repeatable container for {@link ConditionalOnStringProperty}.
 *
 * <p>This annotation is used as the container type for {@link java.lang.annotation.Repeatable}. When present,
 * {@link io.conditionals.condition.impl.OnStringPropertyCondition} evaluates each contained
 * {@link ConditionalOnStringProperty} instance and aggregates outcomes with AND semantics.</p>
 *
 * @author Maksym Uimanov
 * @since 1.0
 * @see ConditionalOnStringProperty
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnStringPropertyCondition.class)
public @interface ConditionalOnStringProperties {
    /**
     * Contained repeatable annotation instances.
     *
     * @return contained {@link ConditionalOnStringProperty} declarations
     */
    ConditionalOnStringProperty[] value();
}
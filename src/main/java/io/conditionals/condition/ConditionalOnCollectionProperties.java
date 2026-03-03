package io.conditionals.condition;

import io.conditionals.condition.impl.OnCollectionPropertyCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * Repeatable container for {@link ConditionalOnCollectionProperty}.
 *
 * <p>This annotation is used as the container type for {@link java.lang.annotation.Repeatable}. When present,
 * {@link io.conditionals.condition.impl.OnCollectionPropertyCondition} evaluates each contained
 * {@link ConditionalOnCollectionProperty} instance and aggregates outcomes with AND semantics.</p>
 *
 * @author Maksym Uimanov
 * @since 1.0
 * @see ConditionalOnCollectionProperty
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnCollectionPropertyCondition.class)
public @interface ConditionalOnCollectionProperties {
    /**
     * Contained repeatable annotation instances.
     *
     * @return contained {@link ConditionalOnCollectionProperty} declarations
     */
    ConditionalOnCollectionProperty[] value();
}
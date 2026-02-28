package io.conditionals.condition;

import io.conditionals.condition.impl.OnEnumPropertyCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * Container annotation for repeatable {@link ConditionalOnEnumProperty} declarations.
 *
 * <p>
 * This annotation is used by Java's {@link Repeatable} mechanism and may also be declared explicitly. Spring Boot
 * evaluates its {@link #value()} elements via {@link io.conditionals.condition.impl.OnEnumPropertyCondition}
 * during the condition evaluation phase of configuration processing.
 * </p>
 *
 * <p><b>Semantics</b></p>
 * <ul>
 *     <li><b>Aggregation</b>: contained {@link ConditionalOnEnumProperty} instances are aggregated with AND
 *     semantics together with any directly declared instances.</li>
 *     <li><b>Evaluation order</b>: contained values are evaluated in the declared array order. If a direct
 *     {@link ConditionalOnEnumProperty} is also present, it is evaluated before container values.</li>
 * </ul>
 *
 * <p><b>Thread Safety</b></p>
 * <p>This annotation is thread-safe. It declares immutable metadata.</p>
 *
 * <p><b>Null Handling</b></p>
 * <p>{@link #value()} is never {@code null}.</p>
 *
 * <p><b>Edge Cases</b></p>
 * <p>An empty {@link #value()} array results in no evaluable attribute instances and therefore a no-match outcome.</p>
 *
 * @author Maksym Uimanov
 * @since 1.0
 * @see ConditionalOnEnumProperty
 * @see io.conditionals.condition.impl.OnEnumPropertyCondition
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnEnumPropertyCondition.class)
public @interface ConditionalOnEnumProperties {
    ConditionalOnEnumProperty[] value();
}
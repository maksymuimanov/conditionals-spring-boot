package io.conditionals.condition;

import io.conditionals.condition.impl.OnStringPropertyCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * Container annotation for repeatable {@link ConditionalOnStringProperty} declarations.
 *
 * <p>
 * This annotation is used by Java's {@link Repeatable} mechanism and may also be declared explicitly. Spring Boot
 * evaluates its {@link #value()} elements via {@link io.conditionals.condition.impl.OnStringPropertyCondition}
 * during the condition evaluation phase of configuration processing.
 * </p>
 *
 * <p><b>Semantics</b></p>
 * <ul>
 *     <li><b>Pipeline integration</b>: the backing condition discovers instances of {@link ConditionalOnStringProperty}
 *     both directly and through this container using merged attribute lookup.</li>
 *     <li><b>Aggregation</b>: contained {@link ConditionalOnStringProperty} instances are aggregated with AND
 *     semantics together with any directly declared instances.</li>
 *     <li><b>Evaluation order</b>: contained values are evaluated in the declared array order. If a direct
 *     {@link ConditionalOnStringProperty} is also present, it is evaluated before container values.</li>
 * </ul>
 *
 * <p><b>Thread Safety</b></p>
 * <p>This annotation is thread-safe. It declares immutable metadata.</p>
 *
 * <p><b>Null Handling</b></p>
 * <p>{@link #value()} is never {@code null}.</p>
 *
 * <p><b>Edge Cases</b></p>
 * <ul>
 *     <li>An empty {@link #value()} array results in no evaluable attribute instances and therefore a no-match
 *     outcome for the backing condition.</li>
 * </ul>
 *
 * <p><b>Performance Characteristics</b></p>
 * <p>Evaluation is linear in the number of contained annotation instances.</p>
 *
 * <p><b>Usage Example</b></p>
 * <pre>{@code
 * @Configuration
 * @ConditionalOnStringProperty(prefix = "app", name = "mode", havingValue = "prod")
 * @ConditionalOnStringProperty(prefix = "app", name = "region", havingValue = "eu")
 * class ProdEuConfiguration {
 * }
 * }</pre>
 *
 * @author Maksym Uimanov
 * @since 1.0
 * @see ConditionalOnStringProperty
 * @see io.conditionals.condition.impl.OnStringPropertyCondition
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnStringPropertyCondition.class)
public @interface ConditionalOnStringProperties {
    /**
     * Contained repeatable {@link ConditionalOnStringProperty} declarations.
     *
     * @return contained conditional specifications
     */
    ConditionalOnStringProperty[] value();
}
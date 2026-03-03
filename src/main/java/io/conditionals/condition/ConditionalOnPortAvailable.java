package io.conditionals.condition;

import io.conditionals.condition.impl.OnPortAvailableCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * Declares a Spring Boot {@link org.springframework.context.annotation.Condition} that matches when all
 * specified TCP ports are available for binding on the current machine.
 *
 * <p>This annotation is backed by {@link io.conditionals.condition.impl.OnPortAvailableCondition}.
 * The condition attempts to open a {@link java.net.ServerSocket} for each port in {@link #value()}.
 * If any port cannot be bound, the condition does not match.</p>
 *
 * <p><b>Notes</b></p>
 * <ul>
 *     <li>The check is inherently race-prone: availability may change after evaluation.</li>
 *     <li>The check uses the default bind address and relies on OS permissions and networking configuration.</li>
 * </ul>
 *
 * @author Maksym Uimanov
 * @since 1.0
 * @see io.conditionals.condition.impl.OnPortAvailableCondition
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnPortAvailableCondition.class)
public @interface ConditionalOnPortAvailable {
    /**
     * Ports that must be available for the condition to match.
     *
     * @return ports to check
     */
    int[] value();
}
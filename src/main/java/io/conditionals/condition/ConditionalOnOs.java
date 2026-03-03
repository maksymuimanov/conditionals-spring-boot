package io.conditionals.condition;

import io.conditionals.condition.impl.OnOsCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * Declares a Spring Boot {@link org.springframework.context.annotation.Condition} that matches based on the
 * current operating system name.
 *
 * <p>This annotation is backed by {@link io.conditionals.condition.impl.OnOsCondition}. The condition reads the
 * current OS name from the Spring {@link org.springframework.core.env.Environment} using the key
 * {@code os.name}, falling back to {@link System#getProperty(String)} when not present. Matching is performed by
 * checking whether the lower-cased OS name contains any of the provided {@link #value()} fragments.</p>
 *
 * <p><b>Semantics</b></p>
 * <ul>
 *     <li>Values are treated as case-insensitive substrings.</li>
 *     <li>If any fragment matches, the condition matches.</li>
 * </ul>
 *
 * @author Maksym Uimanov
 * @since 1.0
 * @see io.conditionals.condition.impl.OnOsCondition
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnOsCondition.class)
public @interface ConditionalOnOs {
    /**
     * Case-insensitive OS name fragments to match against the current {@code os.name} value.
     *
     * @return OS name fragments
     */
    String[] value();
}
package io.conditionals.condition;

import io.conditionals.condition.impl.OnOsCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * Declares a Spring Boot condition that matches based on the current operating system name.
 *
 * <p>
 * When placed on a {@code @Configuration} class or {@code @Bean} method, Spring Boot evaluates the associated
 * {@link io.conditionals.condition.impl.OnOsCondition} during the condition evaluation phase of configuration
 * processing.
 * </p>
 *
 * <p><b>Semantics</b></p>
 * <ul>
 *     <li><b>Binding</b>: this annotation is backed by {@link io.conditionals.condition.impl.OnOsCondition}.</li>
 *     <li><b>OS name resolution</b>: the backing condition resolves {@code os.name} from the Spring
 *     {@link org.springframework.core.env.Environment} and falls back to {@link System#getProperty(String, String)}.</li>
 *     <li><b>Normalization</b>: the resolved OS name and each configured token are lower-cased using
 *     {@link java.util.Locale#ROOT}.</li>
 *     <li><b>Matching</b>: the condition matches if any configured token is a substring of the resolved OS name.</li>
 *     <li><b>Evaluation order</b>: tokens are evaluated in the declared array order; evaluation may short-circuit on
 *     the first matching token.</li>
 * </ul>
 *
 * <p><b>Thread Safety</b></p>
 * <p>This annotation is thread-safe. It declares immutable metadata.</p>
 *
 * <p><b>Null Handling</b></p>
 * <p>Annotation attributes are never {@code null}. If {@code os.name} is absent, an empty string is used by the backing condition.</p>
 *
 * <p><b>Edge Cases</b></p>
 * <ul>
 *     <li>Empty token arrays will always produce a no-match outcome.</li>
 *     <li>Substring matching may yield matches for partial tokens (e.g. {@code "win"} matches {@code "windows"}).</li>
 * </ul>
 *
 * <p><b>Performance Characteristics</b></p>
 * <p>Evaluation is linear in the number of tokens.</p>
 *
 * <p><b>Usage Example</b></p>
 * <pre>{@code
 * @ConditionalOnOs("linux")
 * @Configuration
 * class UnixConfiguration {
 * }
 * }</pre>
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
    String[] value();
}
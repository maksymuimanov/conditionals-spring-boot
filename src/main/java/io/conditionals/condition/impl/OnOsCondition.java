package io.conditionals.condition.impl;

import io.conditionals.condition.ConditionalOnOs;
import io.conditionals.condition.utils.ConditionUtils;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Arrays;
import java.util.Locale;

/**
 * Spring Boot {@link org.springframework.context.annotation.Condition} implementation backing
 * {@link io.conditionals.condition.ConditionalOnOs}.
 *
 * <p>
 * This condition is evaluated during Spring Boot's condition evaluation pipeline when
 * {@code @ConditionalOnOs} is present on a configuration class or {@code @Bean} method.
 * </p>
 *
 * <p><b>Semantics</b></p>
 * <ul>
 *     <li><b>Attribute discovery</b>: reads the {@code value} attribute from {@code @ConditionalOnOs}.</li>
 *     <li><b>OS name resolution order</b>:
 *     <ol>
 *         <li>Resolve {@code os.name} from {@link org.springframework.core.env.Environment}.</li>
 *         <li>If absent, fall back to {@link System#getProperty(String, String)} with an empty-string default.</li>
 *     </ol>
 *     </li>
 *     <li><b>Normalization</b>: the resolved OS name and each candidate token are lower-cased using
 *     {@link Locale#ROOT}.</li>
 *     <li><b>Matching</b>: the condition matches if any configured candidate token is a substring of the resolved
 *     OS name (i.e. {@code osName.contains(token)}).</li>
 * </ul>
 *
 * <p><b>Thread Safety</b></p>
 * <p>This class is thread-safe. It is stateless.</p>
 *
 * <p><b>Null Handling</b></p>
 * <ul>
 *     <li>If annotation attributes are absent, a no-match outcome is returned.</li>
 *     <li>If the resolved OS name is {@code null}, it is treated as an empty string by the resolution logic.</li>
 * </ul>
 *
 * <p><b>Edge Cases</b></p>
 * <ul>
 *     <li>Empty {@code value} arrays yield a no-match outcome because no token can match.</li>
 *     <li>Candidate tokens are matched as substrings; tokens such as {@code "win"} will match
 *     {@code "windows"}.</li>
 * </ul>
 *
 * <p><b>Performance Characteristics</b></p>
 * <p>Runs in O(n) in the number of candidate tokens.</p>
 *
 * <p><b>Usage Example</b></p>
 * <pre>{@code
 * @ConditionalOnOs({"windows"})
 * @Configuration
 * class WindowsOnlyConfiguration {
 * }
 * }</pre>
 *
 * @author Maksym Uimanov
 * @since 1.0
 * @see io.conditionals.condition.ConditionalOnOs
 */
public class OnOsCondition extends SpringBootCondition {
    public static final String OS_NAME_PROPERTY_KEY = "os.name";

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context,
                                            AnnotatedTypeMetadata metadata) {
        ConditionMessage.Builder message = ConditionMessage.forCondition(ConditionalOnOs.class);
        AnnotationAttributes annotationAttributes = ConditionUtils.attributes(metadata, ConditionalOnOs.class);
        return ConditionUtils.checkAttributes(message, annotationAttributes, attributes -> {
            String osName = context.getEnvironment()
                    .getProperty(OS_NAME_PROPERTY_KEY, System.getProperty(OS_NAME_PROPERTY_KEY, ""))
                    .toLowerCase(Locale.ROOT);
            String[] values = attributes.getStringArray("value");
            boolean matched = Arrays.stream(values)
                    .map(value -> value.toLowerCase(Locale.ROOT))
                    .anyMatch(osName::contains);
            return matched
                    ? ConditionOutcome.match(message.found("OS").items(osName))
                    : ConditionUtils.noMatchBecause(message, "OS '", osName, "' did not match any of ", Arrays.toString(values));
        });
    }
}

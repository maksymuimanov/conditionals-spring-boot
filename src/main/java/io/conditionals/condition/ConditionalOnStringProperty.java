package io.conditionals.condition;

import io.conditionals.condition.impl.OnStringPropertyCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnStringPropertyCondition.class)
@Repeatable(ConditionalOnStringProperties.class)
public @interface ConditionalOnStringProperty {
    String[] value() default {};

    String prefix() default "";

    String[] name() default {};

    String havingValue() default "";

    boolean ignoreCase() default false;

    boolean trim() default false;

    boolean not() default false;

    MatchType matchType() default MatchType.EQUALS;

    boolean matchIfMissing() default false;

    enum MatchType {
        EQUALS,
        CONTAINS,
        STARTS_WITH,
        ENDS_WITH,
        MATCHES
    }
}
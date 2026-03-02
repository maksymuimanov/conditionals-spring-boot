package io.conditionals.condition;

import io.conditionals.condition.dto.NumericMatchType;
import io.conditionals.condition.impl.OnCharacterPropertyCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnCharacterPropertyCondition.class)
@Repeatable(ConditionalOnCharacterProperties.class)
public @interface ConditionalOnCharacterProperty {
    String[] value() default {};

    String prefix() default "";

    String[] name() default {};

    double havingValue() default 0;

    boolean not() default false;

    NumericMatchType matchType() default NumericMatchType.EQUALS;

    boolean matchIfMissing() default false;
}
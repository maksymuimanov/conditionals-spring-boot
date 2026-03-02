package io.conditionals.condition;

import io.conditionals.condition.impl.OnCharacterPropertyCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnCharacterPropertyCondition.class)
public @interface ConditionalOnCharacterProperties {
    ConditionalOnCharacterProperty[] value();
}
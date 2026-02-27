package io.conditionals.condition;

import io.conditionals.condition.impl.OnIntegerPropertyCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnIntegerPropertyCondition.class)
public @interface ConditionalOnIntegerProperties {
    ConditionalOnIntegerProperty[] value();
}
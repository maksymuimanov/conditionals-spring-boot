package io.conditionals.condition;

import io.conditionals.condition.impl.OnFloatPropertyCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnFloatPropertyCondition.class)
public @interface ConditionalOnFloatProperties {
    ConditionalOnFloatProperty[] value();
}
package io.conditionals.condition;

import io.conditionals.condition.impl.OnEnumPropertyCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnEnumPropertyCondition.class)
public @interface ConditionalOnEnumProperties {
    ConditionalOnEnumProperty[] value();
}
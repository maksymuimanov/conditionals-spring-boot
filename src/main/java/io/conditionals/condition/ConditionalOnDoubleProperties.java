package io.conditionals.condition;

import io.conditionals.condition.impl.OnDoublePropertyCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnDoublePropertyCondition.class)
public @interface ConditionalOnDoubleProperties {
    ConditionalOnDoubleProperty[] value();
}
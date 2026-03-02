package io.conditionals.condition;

import io.conditionals.condition.impl.OnMapPropertyCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnMapPropertyCondition.class)
public @interface ConditionalOnMapProperties {
    ConditionalOnMapProperty[] value();
}
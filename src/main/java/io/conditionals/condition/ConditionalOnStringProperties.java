package io.conditionals.condition;

import io.conditionals.condition.impl.OnStringPropertyCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnStringPropertyCondition.class)
public @interface ConditionalOnStringProperties {
    ConditionalOnStringProperty[] value();
}
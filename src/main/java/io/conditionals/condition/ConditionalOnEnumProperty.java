package io.conditionals.condition;

import io.conditionals.condition.impl.OnEnumPropertyCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnEnumPropertyCondition.class)
@Repeatable(ConditionalOnEnumProperties.class)
public @interface ConditionalOnEnumProperty {
    String[] value() default {};

    String prefix() default "";

    String[] name() default {};

    String havingValue() default "";

    Class<? extends Enum<?>> enumClass();

    boolean matchIfMissing() default false;
}
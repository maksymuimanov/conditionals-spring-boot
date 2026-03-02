package io.conditionals.condition;

import io.conditionals.condition.dto.CollectionMatchType;
import io.conditionals.condition.impl.OnMapPropertyCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnMapPropertyCondition.class)
@Repeatable(ConditionalOnMapProperties.class)
public @interface ConditionalOnMapProperty {
    String[] value() default {};

    String prefix() default "";

    String[] name() default {};

    String[] havingKey() default {};

    String[] havingValue() default {};

    int size() default -1;

    boolean not() default false;

    CollectionMatchType matchType() default CollectionMatchType.EQUALS;

    boolean matchIfMissing() default false;
}
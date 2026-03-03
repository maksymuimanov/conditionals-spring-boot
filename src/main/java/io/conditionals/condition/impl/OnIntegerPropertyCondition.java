package io.conditionals.condition.impl;

import io.conditionals.condition.ConditionalOnIntegerProperties;
import io.conditionals.condition.ConditionalOnIntegerProperty;

import java.lang.annotation.Annotation;

/**
 * {@link org.springframework.boot.autoconfigure.condition.SpringBootCondition} implementation backing
 * {@link io.conditionals.condition.ConditionalOnIntegerProperty}.
 *
 * <p>This condition delegates comparison semantics to
 * {@link ComparablePropertySpringBootCondition} using {@link io.conditionals.condition.spec.ComparableMatchType}
 * and the {@code not} attribute. Repeatable declarations are supported via
 * {@link io.conditionals.condition.ConditionalOnIntegerProperties}.</p>
 *
 * <p><b>Thread safety</b></p>
 * <p>This type is stateless and thread-safe.</p>
 *
 * @author Maksym Uimanov
 * @since 1.0
 */
public class OnIntegerPropertyCondition extends ComparablePropertySpringBootCondition<Integer, Integer> {
    @Override
    protected Class<? extends Annotation> getAnnotationClass() {
        return ConditionalOnIntegerProperty.class;
    }

    @Override
    protected Class<? extends Annotation> getAnnotationContainerClass() {
        return ConditionalOnIntegerProperties.class;
    }
}

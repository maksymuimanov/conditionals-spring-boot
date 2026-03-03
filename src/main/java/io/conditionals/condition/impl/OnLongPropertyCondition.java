package io.conditionals.condition.impl;

import io.conditionals.condition.ConditionalOnLongProperties;
import io.conditionals.condition.ConditionalOnLongProperty;

import java.lang.annotation.Annotation;

/**
 * {@link org.springframework.boot.autoconfigure.condition.SpringBootCondition} implementation backing
 * {@link io.conditionals.condition.ConditionalOnLongProperty}.
 *
 * <p>This condition delegates comparison semantics to
 * {@link ComparablePropertySpringBootCondition} using {@link io.conditionals.condition.spec.ComparableMatchType}
 * and the {@code not} attribute. Repeatable declarations are supported via
 * {@link io.conditionals.condition.ConditionalOnLongProperties}.</p>
 *
 * <p><b>Thread safety</b></p>
 * <p>This type is stateless and thread-safe.</p>
 *
 * @author Maksym Uimanov
 * @since 1.0
 */
public class OnLongPropertyCondition extends ComparablePropertySpringBootCondition<Long, Long> {
    @Override
    protected Class<? extends Annotation> getAnnotationClass() {
        return ConditionalOnLongProperty.class;
    }

    @Override
    protected Class<? extends Annotation> getAnnotationContainerClass() {
        return ConditionalOnLongProperties.class;
    }
}

package io.conditionals.condition.impl;

import io.conditionals.condition.ConditionalOnEnumProperties;
import io.conditionals.condition.ConditionalOnEnumProperty;
import io.conditionals.condition.spec.PropertySpec;
import io.conditionals.condition.spec.PropertySpecMatcher;
import org.jspecify.annotations.Nullable;
import org.springframework.core.annotation.AnnotationAttributes;

import java.lang.annotation.Annotation;
import java.util.Locale;

/**
 * {@link org.springframework.boot.autoconfigure.condition.SpringBootCondition} implementation backing
 * {@link io.conditionals.condition.ConditionalOnEnumProperty}.
 *
 * <p>This condition resolves the configured property as a {@link String} and attempts to convert both the
 * resolved value and the candidate value to an enum constant of the configured {@code enumType}.
 * Conversion uses {@link Enum#valueOf(Class, String)}. Values are normalized using
 * {@link String#toUpperCase(Locale)} with {@link Locale#ROOT} before conversion.</p>
 *
 * <p><b>Thread safety</b></p>
 * <p>This type is stateless and thread-safe. Matcher/spec instances are created per evaluation.</p>
 *
 * @author Maksym Uimanov
 * @since 1.0
 */
public class OnEnumPropertyCondition extends PropertySpringBootCondition<String, OnEnumPropertyCondition.Spec> {
    @Override
    protected Class<? extends Annotation> getAnnotationClass() {
        return ConditionalOnEnumProperty.class;
    }

    @Override
    protected Class<? extends Annotation> getAnnotationContainerClass() {
        return ConditionalOnEnumProperties.class;
    }

    @Override
    protected Spec createSpec(@Nullable Class<? extends Annotation> annotationType, AnnotationAttributes annotationAttributes) {
        return new Spec(annotationType, annotationAttributes);
    }

    @Override
    protected PropertySpecMatcher<String, Spec> createPropertySpecMatcher() {
        return new Matcher();
    }

    /**
     * Matcher converting both sides to enum constants and comparing them for equality.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public class Matcher implements PropertySpecMatcher<String, Spec> {
        @Override
        public boolean compare(Spec spec, @Nullable String property, String candidate) {
            if (property == null) return false;
            Class<? extends Enum> enumClass = spec.enumType;
            String propertyName = property.toUpperCase(Locale.ROOT);
            String candidateName = candidate.toUpperCase(Locale.ROOT);
            try {
                Enum propertyEnum = Enum.valueOf(enumClass, propertyName);
                Enum candidateEnum = Enum.valueOf(enumClass, candidateName);
                return propertyEnum.equals(candidateEnum);
            } catch (IllegalArgumentException ex) {
                return false;
            }
        }
    }

    /**
     * {@link io.conditionals.condition.spec.PropertySpec} specialization capturing the enum type to use for
     * conversion.
     */
    @SuppressWarnings("rawtypes")
    public class Spec extends PropertySpec<String, Spec> {
        private static final String ENUM_TYPE = "enumType";
        private final Class<? extends Enum> enumType;

        /**
         * Create a new spec from annotation attributes.
         *
         * @param annotationType annotation type producing the attributes
         * @param annotationAttributes resolved annotation attributes
         */
        private Spec(Class<? extends Annotation> annotationType, AnnotationAttributes annotationAttributes) {
            super(annotationType, annotationAttributes);
            this.enumType = annotationAttributes.getClass(ENUM_TYPE);
        }
    }
}

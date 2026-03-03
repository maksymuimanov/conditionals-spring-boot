package io.conditionals.condition.impl;

import io.conditionals.condition.ConditionalOnDurationProperties;
import io.conditionals.condition.ConditionalOnDurationProperty;
import io.conditionals.condition.spec.ComparablePropertySpec;
import io.conditionals.condition.spec.PropertySpecMatcher;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.env.PropertyResolver;

import java.lang.annotation.Annotation;
import java.time.Duration;
import java.util.List;

/**
 * {@link org.springframework.boot.autoconfigure.condition.SpringBootCondition} implementation backing
 * {@link io.conditionals.condition.ConditionalOnDurationProperty}.
 *
 * <p>This condition extends {@link ComparablePropertySpringBootCondition} and compares resolved
 * {@link Duration} values using {@link io.conditionals.condition.spec.ComparableMatchType}. Candidate values are
 * parsed from the {@code havingValue} attribute using
 * {@link DurationStyle#detectAndParse(String)}.</p>
 *
 * <p><b>Property resolution and parsing</b></p>
 * <p>The {@link Spec} overrides
 * {@link ComparablePropertySpec#collectProperties(PropertyResolver, List, List, PropertySpecMatcher)} to read the
 * raw property as a {@link String} and parse it via {@link DurationStyle#detectAndParse(String)}. This ensures
 * consistent parsing regardless of conversion service configuration.</p>
 *
 * <p><b>Thread safety</b></p>
 * <p>This type is stateless and thread-safe. Spec instances are created per evaluation.</p>
 *
 * @author Maksym Uimanov
 * @since 1.0
 */
public class OnDurationPropertyCondition extends ComparablePropertySpringBootCondition<Duration, Duration> {
    @Override
    protected Class<? extends Annotation> getAnnotationClass() {
        return ConditionalOnDurationProperty.class;
    }

    @Override
    protected Class<? extends Annotation> getAnnotationContainerClass() {
        return ConditionalOnDurationProperties.class;
    }

    @Override
    protected ComparablePropertySpec<Duration, Duration> createSpec(@Nullable Class<? extends Annotation> annotationType, AnnotationAttributes annotationAttributes) {
        return new Spec(annotationType, annotationAttributes);
    }

    /**
     * Specification that parses the {@code havingValue} attribute and resolved property values as
     * {@link Duration}.
     */
    public class Spec extends ComparablePropertySpec<Duration, Duration> {
        /**
         * Create a new spec using {@link DurationStyle#detectAndParse(String)} for candidate parsing.
         *
         * @param annotationType annotation type producing the attributes
         * @param annotationAttributes resolved annotation attributes
         */
        public Spec(Class<? extends Annotation> annotationType, AnnotationAttributes annotationAttributes) {
            super(annotationType, annotationAttributes, attribute -> DurationStyle.detectAndParse(String.valueOf(attribute)));
        }

        /**
         * Collect missing and non-matching properties by parsing each present value as a {@link Duration}.
         *
         * <p>Parsing failures are treated as non-matching values.</p>
         *
         * @param resolver property resolver
         * @param missing output list of missing property names
         * @param nonMatching output list of non-matching property names
         * @param matcher matcher used for comparison
         */
         @Override
         public void collectProperties(PropertyResolver resolver, List<String> missing, List<String> nonMatching, PropertySpecMatcher<Duration, ComparablePropertySpec<Duration, Duration>> matcher) {
             for (String name : this.getNames()) {
                 try {
                     String key = this.getPrefix() + name;
                     if (resolver.containsProperty(key)) {
                         String property = resolver.getProperty(key);
                         Duration duration = DurationStyle.detectAndParse(property);
                         if (!this.isMatch(duration, matcher)) {
                             nonMatching.add(name);
                         }
                     } else if (!this.isMatchIfMissing()) {
                         missing.add(name);
                     }
                 } catch (ConversionException e) {
                     nonMatching.add(name);
                 }
             }
         }
    }
}

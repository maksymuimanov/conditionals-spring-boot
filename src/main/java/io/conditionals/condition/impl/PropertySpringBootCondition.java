package io.conditionals.condition.impl;

import io.conditionals.condition.spec.PropertySpec;
import io.conditionals.condition.spec.PropertySpecMatcher;
import io.conditionals.condition.utils.ConditionUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Base {@link MatchingSpringBootCondition} for property-based conditional annotations.
 *
 * <p>This abstraction evaluates annotations that describe one or more property names, a candidate
 * ("having") value, and optional matching rules. It is responsible for:</p>
 * <ul>
 *     <li>Resolving annotation attributes, including repeatable instances via a container annotation.</li>
 *     <li>Creating a {@link PropertySpec} representation of the annotation attributes.</li>
 *     <li>Resolving properties from the {@link org.springframework.core.env.Environment} (via
 *     {@link ConditionContext#getEnvironment()}).</li>
 *     <li>Reporting missing properties and value mismatches using {@link ConditionMessage} diagnostics.</li>
 * </ul>
 *
 * <p><b>Repeatable handling</b></p>
 * <p>{@link #getAttributesStream(AnnotatedTypeMetadata)} uses
 * {@link ConditionUtils#mergedStream(AnnotatedTypeMetadata, Class, Class)} to provide a stream containing the
 * primary annotation's attributes as well as any instances from the container annotation returned by
 * {@link #getAnnotationContainerClass()}.</p>
 *
 * <p><b>Thread safety</b></p>
 * <p>Implementations are expected to be stateless. This base class allocates only per-evaluation state.</p>
 *
 * @param <V> resolved property value type
 * @param <S> specification type representing annotation configuration
 * @author Maksym Uimanov
 * @since 1.0
 */
public abstract class PropertySpringBootCondition<V, S extends PropertySpec<V, S>> extends MatchingSpringBootCondition {
    /**
     * Return a stream of annotation attributes for evaluation, including repeatable container values.
     *
     * @param metadata annotated type metadata
     * @return stream of annotation attributes for the primary annotation and its container
     */
    @Override
    protected Stream<@Nullable AnnotationAttributes> getAttributesStream(AnnotatedTypeMetadata metadata) {
        return ConditionUtils.mergedStream(metadata, this.getAnnotationClass(), this.getAnnotationContainerClass());
    }

    /**
     * Return the container annotation type used for repeatable declarations.
     *
     * @return container annotation class
     */
    protected abstract Class<? extends Annotation> getAnnotationContainerClass();

    /**
     * Determine the outcome for a single annotation instance by evaluating configured properties.
     *
     * <p>This implementation:</p>
     * <ul>
     *     <li>Creates a {@link PropertySpec} via {@link #createSpec(Class, AnnotationAttributes)}.</li>
     *     <li>Resolves property values from {@link PropertyResolver} obtained from {@link ConditionContext}.</li>
     *     <li>Collects missing properties and non-matching properties via
     *     {@link PropertySpec#collectProperties(PropertyResolver, List, List, PropertySpecMatcher)}.</li>
     *     <li>Constructs a {@link ConditionOutcome} with structured diagnostics.</li>
     * </ul>
     *
     * <p><b>Decision criteria</b></p>
     * <ul>
     *     <li>If any required property is missing, the outcome is {@code noMatch}.</li>
     *     <li>Else if any present property does not match, the outcome is {@code noMatch}.</li>
     *     <li>Otherwise, the outcome is {@code match}.</li>
     * </ul>
     *
     * @param message message builder for diagnostics
     * @param context condition context providing access to {@link org.springframework.core.env.Environment}
     * @param annotationAttributes resolved, non-null attributes of the conditional annotation
     * @return outcome for this annotation instance
     */
    @Override
    protected ConditionOutcome determineOutcome(ConditionMessage.Builder message, ConditionContext context, AnnotationAttributes annotationAttributes) {
        Class<? extends Annotation> annotationType = annotationAttributes.annotationType();
        S spec = this.createSpec(annotationType, annotationAttributes);
        List<String> missingProperties = new ArrayList<>();
        List<String> nonMatchingProperties = new ArrayList<>();

        PropertyResolver resolver = context.getEnvironment();
        spec.collectProperties(resolver, missingProperties, nonMatchingProperties, this.createPropertySpecMatcher());
        if (!missingProperties.isEmpty()) {
            return ConditionOutcome.noMatch(message.didNotFind("property", "properties")
                    .items(ConditionMessage.Style.QUOTE, missingProperties));
        } else {
            return !nonMatchingProperties.isEmpty()
                    ? ConditionOutcome.noMatch(message.found("different value in property", "different value in properties")
                            .items(ConditionMessage.Style.QUOTE, nonMatchingProperties))
                    : ConditionOutcome.match(message.because("matched"));
        }
    }

    /**
     * Create a specification object from the given annotation attributes.
     *
     * @param annotationType concrete annotation type for the current attributes (may be {@code null} depending on
     *                       metadata source)
     * @param annotationAttributes resolved annotation attributes
     * @return spec representing the annotation configuration
     */
    protected abstract S createSpec(@Nullable Class<? extends Annotation> annotationType, AnnotationAttributes annotationAttributes);

    /**
     * Create the matcher used to compare resolved property values to the candidate value.
     *
     * @return matcher instance
     */
    protected abstract PropertySpecMatcher<V, S> createPropertySpecMatcher();
}
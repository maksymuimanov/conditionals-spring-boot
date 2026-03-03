package io.conditionals.condition.impl;

import io.conditionals.condition.utils.ConditionUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Base {@link SpringBootCondition} implementation providing common evaluation and message aggregation logic.
 *
 * <p>This condition supports evaluation of single and repeatable conditional annotations by iterating over a
 * {@link #getAttributesStream(AnnotatedTypeMetadata) stream} of {@link AnnotationAttributes} and aggregating
 * per-annotation {@link ConditionOutcome} instances.</p>
 *
 * <p><b>Aggregation semantics</b></p>
 * <ul>
 *     <li>If <em>any</em> evaluated annotation instance results in {@link ConditionOutcome#noMatch(ConditionMessage)},
 *     the final outcome is {@code noMatch}.</li>
 *     <li>Otherwise, the final outcome is {@code match}.</li>
 * </ul>
 *
 * <p><b>Integration with Spring Boot</b></p>
 * <p>Spring Boot calls {@link #getMatchOutcome(ConditionContext, AnnotatedTypeMetadata)} during configuration
 * processing. Subclasses implement {@link #determineOutcome(ConditionMessage.Builder, ConditionContext, AnnotationAttributes)}
 * to perform type-specific evaluation.</p>
 *
 * <p><b>Thread safety</b></p>
 * <p>Implementations are expected to be stateless. This base class does not store per-evaluation state and is
 * safe to reuse across evaluations.</p>
 *
 * @author Maksym Uimanov
 * @since 1.0
 */
public abstract class MatchingSpringBootCondition extends SpringBootCondition {
    /**
     * Evaluate the condition for the annotated element represented by {@code metadata}.
     *
     * <p>This implementation:</p>
     * <ul>
     *     <li>Obtains annotation attributes via {@link #getAttributesStream(AnnotatedTypeMetadata)}.</li>
     *     <li>Evaluates each instance via {@link #checkAttributes(ConditionMessage.Builder, ConditionContext, AnnotationAttributes)}.</li>
     *     <li>Aggregates individual {@link ConditionMessage} instances into the final {@link ConditionOutcome}.</li>
     * </ul>
     *
     * @param context the current condition context (never {@code null})
     * @param metadata annotation metadata for the item being evaluated (never {@code null})
     * @return aggregated condition outcome
     */
    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        List<ConditionMessage> noMatch = new ArrayList<>();
        List<ConditionMessage> match = new ArrayList<>();

        this.getAttributesStream(metadata)
                .forEach(annotation -> {
                    ConditionOutcome outcome = this.checkAttributes(this.getParentMessage(), context, annotation);
                    (outcome.isMatch() ? match : noMatch).add(outcome.getConditionMessage());
                });

        return noMatch.isEmpty()
                ? ConditionOutcome.match(ConditionMessage.of(match))
                : ConditionOutcome.noMatch(ConditionMessage.of(noMatch));
    }

    /**
     * Provide a stream of annotation attribute sets to evaluate.
     *
     * <p>The default implementation returns a single element stream for {@link #getAnnotationClass()}.
     * Subclasses may override to support repeatable annotations and/or container annotations.</p>
     *
     * @param metadata annotated type metadata
     * @return stream of attributes; may contain {@code null} elements to represent absent attributes
     */
    protected Stream<@Nullable AnnotationAttributes> getAttributesStream(AnnotatedTypeMetadata metadata) {
        return Stream.of(ConditionUtils.attributes(metadata, this.getAnnotationClass()));
    }

    /**
     * Validates that attributes are present and delegates evaluation to a caller-provided function.
     *
     * <p><b>Semantics</b></p>
     * <ul>
     *     <li>If {@code attributes} is {@code null}, returns {@link ConditionUtils#noAttributesFound(ConditionMessage.Builder)}.</li>
     *     <li>Otherwise, invokes {@code outcomeFunction} with the non-null attributes.</li>
     * </ul>
     *
     * <p><b>Null Handling</b></p>
     * <p>{@code attributes} may be {@code null} and is treated as absent.</p>
     *
     * @param message message builder used to construct the no-attributes message
     * @param attributes annotation attributes, or {@code null}
     * @return outcome based on presence and evaluation of attributes
     */
    protected ConditionOutcome checkAttributes(ConditionMessage.Builder message, ConditionContext context, @Nullable AnnotationAttributes attributes) {
        boolean empty = attributes == null;
        return empty
                ? ConditionUtils.noAttributesFound(message)
                : this.determineOutcome(message, context, attributes);
    }

    /**
     * Create the root {@link ConditionMessage.Builder} used for all outcomes produced by this condition.
     *
     * @return message builder initialized for {@link #getAnnotationClass()}
     */
    protected ConditionMessage.Builder getParentMessage() {
        return ConditionMessage.forCondition(this.getAnnotationClass());
    }

    /**
     * Return the primary annotation type supported by this condition.
     *
     * @return annotation class
     */
    protected abstract Class<? extends Annotation> getAnnotationClass();

    /**
     * Determine the outcome for a single set of annotation attributes.
     *
     * <p>Implementations should construct either {@link ConditionOutcome#match(ConditionMessage)} or
     * {@link ConditionOutcome#noMatch(ConditionMessage)} and include diagnostics in the {@code message}.</p>
     *
     * @param message condition message builder for the current annotation instance
     * @param context current evaluation context
     * @param annotationAttributes resolved, non-null annotation attributes
     * @return outcome for this annotation instance
     */
    protected abstract ConditionOutcome determineOutcome(ConditionMessage.Builder message, ConditionContext context, AnnotationAttributes annotationAttributes);
}
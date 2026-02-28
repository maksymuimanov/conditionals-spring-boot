package io.conditionals.condition.utils;

import io.conditionals.condition.dto.PropertySpec;
import io.conditionals.condition.dto.PropertySpecMatcher;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Static utilities for evaluating Spring Boot {@link org.springframework.context.annotation.Condition} metadata.
 *
 * <p>
 * Provides shared evaluation helpers used by {@link org.springframework.boot.autoconfigure.condition.SpringBootCondition}
 * implementations in this project. These methods are intended to be invoked from
 * {@link org.springframework.boot.autoconfigure.condition.SpringBootCondition#getMatchOutcome(org.springframework.context.annotation.ConditionContext, AnnotatedTypeMetadata)}
 * during Spring Boot's condition evaluation pipeline.
 * </p>
 *
 * <p><b>Semantics</b></p>
 * <ul>
 *     <li><b>Attribute discovery</b>: If no annotation attributes are present for the queried annotation type,
 *     the evaluation yields a {@link ConditionOutcome#noMatch(String) no-match} outcome via
 *     {@link #noAttributesFound(ConditionMessage.Builder)}.</li>
 *     <li><b>Repeatable composition</b>: For repeatable annotations, evaluation is typically performed over a
 *     stream produced by {@link #mergedStream(AnnotatedTypeMetadata, Class, Class)}, which yields at most one
 *     direct annotation instance followed by any container instances.</li>
 *     <li><b>Aggregation</b>: {@link #evaluateConditions(ConditionMessage.Builder, Stream, Function)} evaluates each
 *     attribute instance independently and aggregates results with AND semantics: the overall outcome is a match
 *     only if all evaluated attribute instances match; otherwise the overall outcome is a no-match.</li>
 *     <li><b>Property evaluation</b>: {@link #evaluatePropertyConditions(ConditionMessage.Builder, AnnotationAttributes, BiFunction, PropertyResolver, PropertySpecMatcher)}
 *     evaluates the presence and value of each configured property name. Missing properties cause a no-match
 *     when {@code matchIfMissing=false}; non-matching values and conversion failures cause a no-match.</li>
 * </ul>
 *
 * <p><b>Thread Safety</b></p>
 * <p>
 * This class is thread-safe. It is stateless and all methods are pure with respect to shared state.
 * Thread-safety of the provided {@link PropertyResolver} and {@link AnnotatedTypeMetadata} instances is governed
 * by the Spring container.
 * </p>
 *
 * <p><b>Null Handling</b></p>
 * <ul>
 *     <li>{@code @Nullable} {@link AnnotationAttributes} inputs are treated as absent attributes and result in
 *     {@link #noAttributesFound(ConditionMessage.Builder)}.</li>
 *     <li>{@code @Nullable} elements in streams are evaluated via {@link #checkAttributes(ConditionMessage.Builder, AnnotationAttributes, Function)}
 *     and therefore produce a no-match outcome for that element.</li>
 * </ul>
 *
 * <p><b>Edge Cases</b></p>
 * <ul>
 *     <li>Empty attribute streams cause an immediate no-match outcome.</li>
 *     <li>Container annotation presence without any contained values is treated as an empty stream.</li>
 *     <li>Callers must ensure {@code outcomeFunction} and other functional parameters are non-null; otherwise a
 *     {@link NullPointerException} will be thrown by the JVM.</li>
 * </ul>
 *
 * <p><b>Performance Characteristics</b></p>
 * <p>
 * Evaluation is linear in the number of attribute instances and configured property names. Some methods
 * materialize the provided stream into a list to support empty-stream detection.
 * </p>
 *
 * <p><b>Usage Example</b></p>
 * <pre>{@code
 * ConditionMessage.Builder message = ConditionMessage.forCondition(MyConditional.class);
 * Stream<AnnotationAttributes> attrs = ConditionUtils.mergedStream(metadata, MyConditional.class, MyConditionals.class);
 * ConditionOutcome outcome = ConditionUtils.evaluateConditions(message, attrs, a -> evaluateOne(a));
 * }</pre>
 *
 * @author Maksym Uimanov
 * @since 1.0
 * @see org.springframework.boot.autoconfigure.condition.SpringBootCondition
 * @see ConditionOutcome
 * @see ConditionMessage
 */
public final class ConditionUtils {
    private static final String VALUE = "value";

    private ConditionUtils() {
    }

    /**
     * Evaluates a stream of annotation attribute instances and aggregates their outcomes.
     *
     * <p>
     * Intended for repeatable conditions where multiple annotation instances can be present on the same
     * {@link AnnotatedTypeMetadata}. The returned outcome is the conjunction of all evaluated outcomes.
     * </p>
     *
     * <p><b>Semantics</b></p>
     * <ul>
     *     <li>Evaluation order is the encounter order of {@code attributesStream}.</li>
     *     <li>Each element (including {@code null}) is evaluated using {@link #checkAttributes(ConditionMessage.Builder, AnnotationAttributes, Function)}.</li>
     *     <li>The overall outcome is a match iff no element yields a no-match outcome.</li>
     * </ul>
     *
     * <p><b>Thread Safety</b></p>
     * <p>Thread-safe provided that {@code outcomeFunction} is thread-safe.</p>
     *
     * <p><b>Null Handling</b></p>
     * <ul>
     *     <li>{@code null} stream elements are treated as absent attributes and yield no-match outcomes.</li>
     * </ul>
     *
     * <p><b>Edge Cases</b></p>
     * <ul>
     *     <li>If the stream is empty, the returned outcome is a no-match outcome stating that no attributes were found.</li>
     * </ul>
     *
     * <p><b>Performance Characteristics</b></p>
     * <p>Runs in O(n) with respect to number of attribute instances.</p>
     *
     * @param message message builder used to construct no-attribute and aggregation messages
     * @param attributesStream stream of annotation attribute instances, potentially containing {@code null}
     * @param outcomeFunction function computing the outcome for a single non-null attribute instance
     * @return aggregated condition outcome
     */
    public static ConditionOutcome evaluateConditions(ConditionMessage.Builder message,
                                                      Stream<@Nullable AnnotationAttributes> attributesStream,
                                                      Function<AnnotationAttributes, ConditionOutcome> outcomeFunction) {
        List<ConditionMessage> noMatch = new ArrayList<>();
        List<ConditionMessage> match = new ArrayList<>();

        attributesStream.forEach(annotation -> {
            ConditionOutcome outcome = checkAttributes(message, annotation, outcomeFunction);
            (outcome.isMatch() ? match : noMatch).add(outcome.getConditionMessage());
        });

        return noMatch.isEmpty()
                ? ConditionOutcome.match(ConditionMessage.of(match))
                : ConditionOutcome.noMatch(ConditionMessage.of(noMatch));
    }

    /**
     * Evaluates a property-based condition described by an annotation attribute instance.
     *
     * <p>
     * This method provides the common property resolution and reporting logic. It is typically called from
     * within a {@link org.springframework.boot.autoconfigure.condition.SpringBootCondition#getMatchOutcome(org.springframework.context.annotation.ConditionContext, AnnotatedTypeMetadata)}
     * implementation.
     * </p>
     *
     * <p><b>Semantics</b></p>
     * <ul>
     *     <li>When {@code attributes} are absent, the outcome is a no-match outcome as if the annotation was not present.</li>
     *     <li>A {@link PropertySpec} is instantiated via {@code specProvider} to interpret annotation attributes.</li>
     *     <li>Each configured property name is resolved as {@code prefix + name} and compared using {@code matcher}.</li>
     *     <li>If a property is missing and {@code matchIfMissing=false}, the outcome is a no-match.</li>
     *     <li>If a property exists but comparison fails or conversion fails, the outcome is a no-match.</li>
     *     <li>If all property names satisfy the matcher (or are missing with {@code matchIfMissing=true}), the outcome is a match.</li>
     * </ul>
     *
     * <p><b>Thread Safety</b></p>
     * <p>Thread-safe provided that {@code resolver}, {@code specProvider}, and {@code matcher} are thread-safe.</p>
     *
     * <p><b>Null Handling</b></p>
     * <ul>
     *     <li>{@code attributes} may be {@code null} and is treated as absent.</li>
     *     <li>{@code matcher} is invoked with {@code @Nullable} property values when a property exists but resolves to {@code null}.</li>
     * </ul>
     *
     * <p><b>Edge Cases</b></p>
     * <ul>
     *     <li>Conversion failures while reading a property are treated as a non-match for that property name.</li>
     *     <li>Multiple property names are evaluated independently; any missing/non-matching name causes a no-match outcome.</li>
     * </ul>
     *
     * <p><b>Performance Characteristics</b></p>
     * <p>Runs in O(m) with respect to number of configured property names.</p>
     *
     * @param message message builder used to build match/no-match messages
     * @param attributes annotation attributes of the conditional annotation, or {@code null} if absent
     * @param specProvider factory creating a typed {@link PropertySpec} from the annotation type and its attributes
     * @param resolver property resolver used to query and convert property values
     * @param matcher comparison strategy to apply to resolved values
     * @param <V> property value type resolved from the environment
     * @param <S> concrete {@link PropertySpec} type
     * @return condition outcome with a diagnostic {@link ConditionMessage}
     */
    public static <V, S extends PropertySpec<V, S>> ConditionOutcome evaluatePropertyConditions(ConditionMessage.Builder message,
                                                                                                @Nullable AnnotationAttributes attributes,
                                                                                                BiFunction<Class<? extends Annotation>, AnnotationAttributes, S> specProvider,
                                                                                                PropertyResolver resolver,
                                                                                                PropertySpecMatcher<V, S> matcher) {
        return checkAttributes(message, attributes, annotationAttributes -> {
            Class<? extends Annotation> annotationType = annotationAttributes.annotationType();
            S spec = specProvider.apply(annotationType, annotationAttributes);
            List<String> missingProperties = new ArrayList<>();
            List<String> nonMatchingProperties = new ArrayList<>();
            spec.collectProperties(resolver, missingProperties, nonMatchingProperties, matcher);
            if (!missingProperties.isEmpty()) {
                return ConditionOutcome.noMatch(ConditionMessage.forCondition(annotationType, spec)
                        .didNotFind("property", "properties")
                        .items(ConditionMessage.Style.QUOTE, missingProperties));
            } else {
                return !nonMatchingProperties.isEmpty()
                        ? ConditionOutcome.noMatch(ConditionMessage.forCondition(annotationType, spec)
                                .found("different value in property", "different value in properties")
                                .items(ConditionMessage.Style.QUOTE, nonMatchingProperties))
                        : ConditionOutcome.match(ConditionMessage.forCondition(annotationType, spec)
                                .because("matched"));
            }
        });
    }

    /**
     * Validates that a stream of attributes is non-empty and delegates evaluation to a caller-provided function.
     *
     * <p><b>Semantics</b></p>
     * <ul>
     *     <li>The stream is consumed and materialized into a list.</li>
     *     <li>If the list is empty, the outcome is a no-match outcome with a "no attributes" reason.</li>
     *     <li>Otherwise, evaluation is delegated to {@code outcomeFunction} using a new stream over the collected list.</li>
     * </ul>
     *
     * <p><b>Thread Safety</b></p>
     * <p>Thread-safe provided that {@code outcomeFunction} is thread-safe.</p>
     *
     * <p><b>Null Handling</b></p>
     * <p>Stream elements may be {@code null}; handling is the responsibility of {@code outcomeFunction}.</p>
     *
     * <p><b>Edge Cases</b></p>
     * <p>Calling this method with a stream that has already been consumed will yield an empty list and therefore a no-match outcome.</p>
     *
     * @param message message builder used to construct the no-attributes message
     * @param attributesStream stream of attributes (possibly empty)
     * @param outcomeFunction function computing the outcome from the non-empty attribute stream
     * @return a no-match outcome if the stream is empty; otherwise the delegated outcome
     */
    public static ConditionOutcome checkAttributes(ConditionMessage.Builder message,
                                                   Stream<@Nullable AnnotationAttributes> attributesStream,
                                                   Function<Stream<@Nullable AnnotationAttributes>, ConditionOutcome> outcomeFunction) {
        List<@Nullable AnnotationAttributes> attributes = attributesStream.toList();
        return attributes.isEmpty()
                ? noAttributesFound(message)
                : outcomeFunction.apply(attributes.stream());
    }

    /**
     * Validates that attributes are present and delegates evaluation to a caller-provided function.
     *
     * <p><b>Semantics</b></p>
     * <ul>
     *     <li>If {@code attributes} is {@code null}, returns {@link #noAttributesFound(ConditionMessage.Builder)}.</li>
     *     <li>Otherwise, invokes {@code outcomeFunction} with the non-null attributes.</li>
     * </ul>
     *
     * <p><b>Thread Safety</b></p>
     * <p>Thread-safe provided that {@code outcomeFunction} is thread-safe.</p>
     *
     * <p><b>Null Handling</b></p>
     * <p>{@code attributes} may be {@code null} and is treated as absent.</p>
     *
     * @param message message builder used to construct the no-attributes message
     * @param attributes annotation attributes, or {@code null}
     * @param outcomeFunction function computing the outcome when attributes are present
     * @return outcome based on presence and evaluation of attributes
     */
    public static ConditionOutcome checkAttributes(ConditionMessage.Builder message,
                                                   @Nullable AnnotationAttributes attributes,
                                                   Function<AnnotationAttributes, ConditionOutcome> outcomeFunction) {
        boolean empty = attributes == null;
        return empty
                ? noAttributesFound(message)
                : outcomeFunction.apply(attributes);
    }

    /**
     * Builds a stream of attributes for a repeatable annotation by concatenating the direct annotation attributes
     * (if present) with the values obtained from the container annotation.
     *
     * <p><b>Semantics</b></p>
     * <ul>
     *     <li>First yields the direct annotation attributes (single instance) if present.</li>
     *     <li>Then yields each element contained in the container annotation in declared order.</li>
     *     <li>If the direct annotation is absent, only container values are yielded.</li>
     * </ul>
     *
     * <p><b>Thread Safety</b></p>
     * <p>Thread-safe as it does not mutate shared state.</p>
     *
     * <p><b>Null Handling</b></p>
     * <p>May yield an empty stream; never yields {@code null} unless the underlying metadata returns {@code null} container values.</p>
     *
     * @param metadata annotated type metadata
     * @param annotationClass repeatable annotation type
     * @param containerAnnotationClass container annotation type
     * @return concatenated stream of attribute instances
     */
    public static Stream<@Nullable AnnotationAttributes> mergedStream(AnnotatedTypeMetadata metadata,
                                                                      Class<? extends Annotation> annotationClass,
                                                                      Class<? extends Annotation> containerAnnotationClass) {
        AnnotationAttributes spec = attributes(metadata, annotationClass);
        Stream<@Nullable AnnotationAttributes> stream = stream(metadata, containerAnnotationClass);
        return spec == null
                ? stream
                : Stream.concat(Stream.of(spec), stream);
    }

    /**
     * Returns a stream over the values of a container annotation's {@code value} attribute.
     *
     * <p><b>Semantics</b></p>
     * <ul>
     *     <li>If the container annotation is not present on {@code metadata}, returns an empty stream.</li>
     *     <li>Otherwise, reads the container's {@code value} attribute and returns its elements as a stream.</li>
     * </ul>
     *
     * <p><b>Thread Safety</b></p>
     * <p>Thread-safe as it does not mutate shared state.</p>
     *
     * <p><b>Null Handling</b></p>
     * <p>If the annotation attributes map is {@code null}, an empty stream is returned.</p>
     *
     * <p><b>Edge Cases</b></p>
     * <p>If the {@code value} attribute is not an {@code AnnotationAttributes[]} as expected, a {@link ClassCastException} may occur.</p>
     *
     * @param metadata annotated type metadata
     * @param annotationClass container annotation type
     * @return stream of contained annotation attributes
     */
    public static Stream<@Nullable AnnotationAttributes> stream(AnnotatedTypeMetadata metadata,
                                                                Class<? extends Annotation> annotationClass) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(annotationClass.getName());
        if (attributes == null)
            return Stream.of();
        AnnotationAttributes[] annotationAttributes = (AnnotationAttributes[]) attributes.get(VALUE);
        return Arrays.stream(annotationAttributes);
    }

    /**
     * Reads annotation attributes for a single (non-container) annotation type.
     *
     * <p><b>Semantics</b></p>
     * <ul>
     *     <li>Delegates to {@link AnnotatedTypeMetadata#getAnnotationAttributes(String)} and wraps the map via
     *     {@link AnnotationAttributes#fromMap(Map)}.</li>
     *     <li>If the annotation is not present, returns {@code null}.</li>
     * </ul>
     *
     * <p><b>Thread Safety</b></p>
     * <p>Thread-safe as it does not mutate shared state.</p>
     *
     * <p><b>Null Handling</b></p>
     * <p>Returns {@code null} when attributes are absent.</p>
     *
     * @param metadata annotated type metadata
     * @param annotationClass annotation type
     * @return annotation attributes, or {@code null} if the annotation is not present
     */
    @Nullable
    public static AnnotationAttributes attributes(AnnotatedTypeMetadata metadata,
                                                  Class<? extends Annotation> annotationClass) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(annotationClass.getName());
        return AnnotationAttributes.fromMap(attributes);
    }

    /**
     * Creates a matching {@link ConditionOutcome} with a message composed by concatenating parts.
     *
     * <p><b>Semantics</b></p>
     * <p>Concatenates all {@code message} fragments without a delimiter.</p>
     *
     * <p><b>Thread Safety</b></p>
     * <p>Thread-safe.</p>
     *
     * <p><b>Null Handling</b></p>
     * <p>Passing {@code null} fragments will result in the literal string {@code "null"} in the composed message.</p>
     *
     * @param message message fragments
     * @return matching outcome
     */
    public static ConditionOutcome match(String... message) {
        return ConditionOutcome.match(String.join("", message));
    }

    /**
     * Creates a no-match {@link ConditionOutcome} indicating that no relevant annotation attributes were found.
     *
     * <p><b>Semantics</b></p>
     * <p>Produces {@link ConditionOutcome#noMatch(ConditionMessage)} with the provided builder.</p>
     *
     * <p><b>Thread Safety</b></p>
     * <p>Thread-safe.</p>
     *
     * <p><b>Null Handling</b></p>
     * <p>{@code message} must be non-null.</p>
     *
     * @param message condition message builder
     * @return no-match outcome
     */
    public static ConditionOutcome noAttributesFound(ConditionMessage.Builder message) {
        return noMatchBecause(message, "no annotation attributes were found");
    }

    /**
     * Creates a no-match {@link ConditionOutcome} with a reason appended to the provided message builder.
     *
     * <p><b>Semantics</b></p>
     * <p>Concatenates the {@code reason} fragments without a delimiter and uses them as the {@code because(...)} argument.</p>
     *
     * <p><b>Thread Safety</b></p>
     * <p>Thread-safe.</p>
     *
     * <p><b>Null Handling</b></p>
     * <p>Passing {@code null} fragments will result in the literal string {@code "null"} in the composed reason.</p>
     *
     * @param message condition message builder
     * @param reason reason fragments
     * @return no-match outcome
     */
    public static ConditionOutcome noMatchBecause(ConditionMessage.Builder message, String... reason) {
        return ConditionOutcome.noMatch(message.because(String.join("", reason)));
    }

    /**
     * Creates a no-match {@link ConditionOutcome} with a message composed by concatenating parts.
     *
     * <p><b>Semantics</b></p>
     * <p>Concatenates all {@code message} fragments without a delimiter.</p>
     *
     * <p><b>Thread Safety</b></p>
     * <p>Thread-safe.</p>
     *
     * <p><b>Null Handling</b></p>
     * <p>Passing {@code null} fragments will result in the literal string {@code "null"} in the composed message.</p>
     *
     * @param message message fragments
     * @return no-match outcome
     */
    public static ConditionOutcome noMatch(String... message) {
        return ConditionOutcome.noMatch(String.join("", message));
    }
}

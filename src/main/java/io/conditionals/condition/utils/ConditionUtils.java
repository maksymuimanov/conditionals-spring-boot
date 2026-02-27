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

public final class ConditionUtils {
    private static final String VALUE = "value";

    private ConditionUtils() {
    }

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

    public static ConditionOutcome checkAttributes(ConditionMessage.Builder message,
                                                   Stream<@Nullable AnnotationAttributes> attributesStream,
                                                   Function<Stream<@Nullable AnnotationAttributes>, ConditionOutcome> outcomeFunction) {
        List<@Nullable AnnotationAttributes> attributes = attributesStream.toList();
        return attributes.isEmpty()
                ? noAttributesFound(message)
                : outcomeFunction.apply(attributes.stream());
    }

    public static ConditionOutcome checkAttributes(ConditionMessage.Builder message,
                                                   @Nullable AnnotationAttributes attributes,
                                                   Function<AnnotationAttributes, ConditionOutcome> outcomeFunction) {
        boolean empty = attributes == null;
        return empty
                ? noAttributesFound(message)
                : outcomeFunction.apply(attributes);
    }

    public static Stream<@Nullable AnnotationAttributes> mergedStream(AnnotatedTypeMetadata metadata,
                                                                      Class<? extends Annotation> annotationClass,
                                                                      Class<? extends Annotation> containerAnnotationClass) {
        AnnotationAttributes spec = attributes(metadata, annotationClass);
        Stream<@Nullable AnnotationAttributes> stream = stream(metadata, containerAnnotationClass);
        return spec == null
                ? stream
                : Stream.concat(Stream.of(spec), stream);
    }

    public static Stream<@Nullable AnnotationAttributes> stream(AnnotatedTypeMetadata metadata,
                                                                Class<? extends Annotation> annotationClass) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(annotationClass.getName());
        if (attributes == null)
            return Stream.of();
        Annotation[] annotations = (Annotation[]) attributes.get(VALUE);
        return Arrays.stream(annotations)
                .map(annotation -> attributes(metadata, annotation.getClass()));
    }

    @Nullable
    public static AnnotationAttributes attributes(AnnotatedTypeMetadata metadata,
                                                  Class<? extends Annotation> annotationClass) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(annotationClass.getName());
        return AnnotationAttributes.fromMap(attributes);
    }

    public static ConditionOutcome match(String... message) {
        return ConditionOutcome.match(String.join("", message));
    }

    public static ConditionOutcome noAttributesFound(ConditionMessage.Builder message) {
        return noMatchBecause(message, "no annotation attributes were found");
    }

    public static ConditionOutcome noMatchBecause(ConditionMessage.Builder message, String... reason) {
        return ConditionOutcome.noMatch(message.because(String.join("", reason)));
    }

    public static ConditionOutcome noMatch(String... message) {
        return ConditionOutcome.noMatch(String.join("", message));
    }
}

package io.conditionals.condition.utils;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Utility methods supporting this library's {@link org.springframework.boot.autoconfigure.condition.SpringBootCondition}
 * implementations.
 *
 * <p>The utilities in this type are intentionally small and focused:</p>
 * <ul>
 *     <li>Extraction of {@link AnnotationAttributes} from {@link AnnotatedTypeMetadata}, including support for
 *     repeatable container annotations using Spring's standard container attribute name ({@code value}).</li>
 *     <li>Convenience factory methods for {@link ConditionOutcome} creation with consistent message formatting.</li>
 *     <li>Shared boolean negation helper used to apply the {@code not} attribute across conditions.</li>
 * </ul>
 *
 * <p><b>Thread safety</b></p>
 * <p>This type is stateless and thread-safe.</p>
 *
 * @author Maksym Uimanov
 * @since 1.0
 */
public final class ConditionUtils {
    private static final String VALUE = "value";

    private ConditionUtils() {
    }

    /**
     * Return a stream of attributes containing the primary annotation's attributes (if present) and all contained
     * attributes from a repeatable container annotation.
     *
     * <p>The resulting stream preserves order by emitting the primary annotation first (when present) followed by
     * container elements in their declared order.</p>
     *
     * @param metadata annotated type metadata
     * @param annotationClass primary annotation type
     * @param containerAnnotationClass container annotation type declared by {@link java.lang.annotation.Repeatable}
     * @return stream of attribute sets; may be empty
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
     * Extract a stream of {@link AnnotationAttributes} from a container annotation.
     *
     * <p>This method expects the container annotation to expose its elements in an attribute named {@code value}
     * of type {@code AnnotationAttributes[]} (as produced by Spring's merged annotation model).
     * If the container annotation is not present, an empty stream is returned.</p>
     *
     * @param metadata annotated type metadata
     * @param annotationClass container annotation type
     * @return stream of contained annotation attributes
     */
    public static Stream<@Nullable AnnotationAttributes> stream(AnnotatedTypeMetadata metadata,
                                                                Class<? extends Annotation> annotationClass) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(annotationClass.getName());
        if (attributes == null)
            return Stream.empty();
        Object value = attributes.get(VALUE);
        if (!(value instanceof AnnotationAttributes[] annotationAttributes)) {
            return Stream.empty();
        }
        return Arrays.stream(annotationAttributes);
    }

    /**
     * Extract attributes for a single annotation.
     *
     * @param metadata annotated type metadata
     * @param annotationClass annotation type
     * @return attributes for the annotation, or {@code null} if the annotation is not present
     */
    @Nullable
    public static AnnotationAttributes attributes(AnnotatedTypeMetadata metadata,
                                                  Class<? extends Annotation> annotationClass) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(annotationClass.getName());
        return AnnotationAttributes.fromMap(attributes);
    }

    /**
     * Create a matching {@link ConditionOutcome} with a message obtained by concatenating the provided fragments.
     *
     * @param message message fragments
     * @return matching outcome
     */
    public static ConditionOutcome match(String... message) {
        return ConditionOutcome.match(String.join("", message));
    }

    /**
     * Create a {@code noMatch} outcome indicating that no annotation attributes were found.
     *
     * @param message message builder used to construct the outcome message
     * @return non-matching outcome
     */
    public static ConditionOutcome noAttributesFound(ConditionMessage.Builder message) {
        return noMatchBecause(message, "no annotation attributes were found");
    }

    /**
     * Create a {@code noMatch} outcome with a {@code because(...)} message whose reason is obtained by
     * concatenating the provided fragments.
     *
     * @param message message builder used to construct the outcome message
     * @param reason reason fragments
     * @return non-matching outcome
     */
    public static ConditionOutcome noMatchBecause(ConditionMessage.Builder message, String... reason) {
        return ConditionOutcome.noMatch(message.because(String.join("", reason)));
    }

    /**
     * Create a non-matching {@link ConditionOutcome} with a message obtained by concatenating the provided
     * fragments.
     *
     * @param message message fragments
     * @return non-matching outcome
     */
    public static ConditionOutcome noMatch(String... message) {
        return ConditionOutcome.noMatch(String.join("", message));
    }

    /**
     * Conditionally invert a boolean predicate.
     *
     * <p>This method uses XOR semantics and is used by conditions to apply a {@code not} flag to a predicate:
     * {@code condition XOR revert}.</p>
     *
     * @param condition predicate result
     * @param revert whether to invert the predicate
     * @return predicate result with optional inversion
     */
    public static boolean revert(boolean condition, boolean revert) {
        return condition ^ revert;
    }
}

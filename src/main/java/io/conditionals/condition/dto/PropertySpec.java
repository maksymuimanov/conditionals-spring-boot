package io.conditionals.condition.dto;

import org.jspecify.annotations.Nullable;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.env.PropertyResolver;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Encapsulates a property-based condition specification derived from a single annotation instance.
 *
 * <p>
 * Instances are created by {@code SpringBootCondition} implementations to interpret annotation attributes such as
 * {@code prefix}, {@code name}/{@code value}, {@code havingValue}, and {@code matchIfMissing}. The spec is then
 * applied against a {@link PropertyResolver} during Spring Boot's condition evaluation pipeline.
 * </p>
 *
 * <p><b>Semantics</b></p>
 * <ul>
 *     <li><b>Key composition</b>: each configured name is evaluated as the key {@code prefix + name}. If the
 *     configured prefix is non-empty and does not end with {@code '.'}, the dot is appended.</li>
 *     <li><b>Name selection</b>: the {@code value} and {@code name} attributes are mutually exclusive; exactly one
 *     must be provided with at least one element.</li>
 *     <li><b>Candidate value</b>: {@code havingValue} is the required candidate value and defines the conversion
 *     type used when reading from the {@link PropertyResolver}.</li>
 *     <li><b>Evaluation</b>: {@link #collectProperties(PropertyResolver, List, List, PropertySpecMatcher)} iterates
 *     configured names in encounter order and classifies each as missing or non-matching. The overall condition is
 *     typically treated as a match only when both lists are empty.</li>
 *     <li><b>Missing handling</b>: if a property key is not present and {@code matchIfMissing=false}, the name is
 *     classified as missing; when {@code matchIfMissing=true}, missing keys are not classified as missing.</li>
 * </ul>
 *
 * <p><b>Thread Safety</b></p>
 * <p>
 * This class is immutable after construction and therefore thread-safe. Thread-safety of the provided
 * {@link PropertyResolver} is determined by the Spring container.
 * </p>
 *
 * <p><b>Null Handling</b></p>
 * <ul>
 *     <li>{@link PropertyResolver#getProperty(String, Class)} may return {@code null}; the matcher receives the
 *     resolved value as {@code @Nullable}.</li>
 *     <li>Annotation attributes used to construct the spec are expected to be non-null.</li>
 * </ul>
 *
 * <p><b>Edge Cases</b></p>
 * <ul>
 *     <li>Type conversion failures while resolving a property are treated as non-matching for the relevant name.</li>
 *     <li>If {@code havingValue} is {@code null}, this implementation will throw {@link NullPointerException}
 *     when determining {@code havingValueType}.</li>
 * </ul>
 *
 * <p><b>Performance Characteristics</b></p>
 * <p>Resolution is O(n) in the number of configured property names.</p>
 *
 * @author Maksym Uimanov
 * @since 1.0
 * @see io.conditionals.condition.utils.ConditionUtils
 * @see io.conditionals.condition.impl
 * @see PropertySpecMatcher
 * @param <V> resolved property value type
 * @param <S> concrete {@link PropertySpec} subtype
 */
public class PropertySpec<V, S extends PropertySpec<V, S>> {
    protected static final String VALUE = "value";
    protected static final String PREFIX = "prefix";
    protected static final String NAME = "name";
    protected static final String HAVING_VALUE = "havingValue";
    protected static final String MATCH_IF_MISSING = "matchIfMissing";
    private final Class<? extends Annotation> annotationType;
    private final String prefix;
    private final String[] names;
    private final V havingValue;
    private final Class<V> havingValueType;
    private final boolean matchIfMissing;

    @SuppressWarnings("unchecked")
    protected PropertySpec(Class<? extends Annotation> annotationType,
                           AnnotationAttributes annotationAttributes) {
        this.annotationType = annotationType;
        this.prefix = this.resolvePrefix(annotationAttributes);
        this.names = this.resolveNames(annotationAttributes);
        this.havingValue = (V) annotationAttributes.get(HAVING_VALUE);
        this.havingValueType = (Class<V>) this.havingValue.getClass();
        this.matchIfMissing = annotationAttributes.getBoolean(MATCH_IF_MISSING);
    }

    /**
     * Resolves the {@code prefix} attribute and normalizes it for key composition.
     *
     * <p><b>Semantics</b></p>
     * <ul>
     *     <li>If {@code prefix} is not present in {@code annotationAttributes}, returns an empty string.</li>
     *     <li>Leading/trailing whitespace is removed.</li>
     *     <li>If the resulting prefix is non-empty and does not end with {@code '.'}, a dot is appended.</li>
     * </ul>
     *
     * <p><b>Thread Safety</b></p>
     * <p>Thread-safe.</p>
     *
     * <p><b>Null Handling</b></p>
     * <p>{@code annotationAttributes} must be non-null.</p>
     *
     * @param annotationAttributes annotation attributes
     * @return normalized prefix, possibly empty
     */
    protected String resolvePrefix(AnnotationAttributes annotationAttributes) {
        if (!annotationAttributes.containsKey(PREFIX)) return "";
        String prefix = annotationAttributes.getString(PREFIX).trim();
        if (StringUtils.hasText(prefix) && !prefix.endsWith(".")) {
            prefix = prefix + ".";
        }

        return prefix;
    }

    /**
     * Resolves configured property names from mutually exclusive {@code value} and {@code name} attributes.
     *
     * <p><b>Semantics</b></p>
     * <ul>
     *     <li>At least one of {@code value} or {@code name} must contain at least one element.</li>
     *     <li>{@code value} and {@code name} are exclusive; specifying both yields an {@link IllegalStateException}.</li>
     *     <li>If {@code value} is non-empty, it is used; otherwise {@code name} is used.</li>
     * </ul>
     *
     * <p><b>Thread Safety</b></p>
     * <p>Thread-safe.</p>
     *
     * <p><b>Null Handling</b></p>
     * <p>{@code annotationAttributes} must be non-null and must provide array values for both attributes.</p>
     *
     * @param annotationAttributes annotation attributes
     * @return resolved names array
     * @throws IllegalStateException if neither attribute is specified or if both are specified
     */
    protected String[] resolveNames(AnnotationAttributes annotationAttributes) {
        String[] value = (String[]) annotationAttributes.get(VALUE);
        String[] name = (String[]) annotationAttributes.get(NAME);
        Assert.state(value.length > 0 || name.length > 0,
                () -> "The name or value attribute of @%s must be specified".formatted(ClassUtils.getShortName(this.getAnnotationType())));
        Assert.state(value.length == 0 || name.length == 0,
                () -> "The name and value attributes of @%s are exclusive".formatted(ClassUtils.getShortName(this.getAnnotationType())));
        return value.length > 0
                ? value
                : name;
    }

    /**
     * Collects missing and non-matching property names for this specification.
     *
     * <p>
     * This method performs the concrete evaluation of the spec against the provided {@link PropertyResolver}.
     * It is typically invoked as part of a {@code SpringBootCondition} implementation.
     * </p>
     *
     * <p><b>Semantics</b></p>
     * <ul>
     *     <li>Names are evaluated in the order returned by {@link #getNames()}.</li>
     *     <li>For each name, a key is computed as {@code getPrefix() + name}.</li>
     *     <li>If {@code resolver.containsProperty(key)} is {@code true}, the property is resolved using
     *     {@link PropertyResolver#getProperty(String, Class)} with {@link #getHavingValueType()} and compared using
     *     {@link #isMatch(Object, PropertySpecMatcher)}.</li>
     *     <li>If the property exists but does not match, the name is added to {@code nonMatching}.</li>
     *     <li>If the property does not exist and {@code matchIfMissing=false}, the name is added to {@code missing}.</li>
     *     <li>If conversion fails when resolving a property, the name is added to {@code nonMatching}.</li>
     * </ul>
     *
     * <p><b>Thread Safety</b></p>
     * <p>Thread-safe provided that {@code resolver}, lists, and {@code matcher} are used safely by the caller.</p>
     *
     * <p><b>Null Handling</b></p>
     * <p>
     * {@code missing}, {@code nonMatching}, and {@code matcher} must be non-null. Resolved property values may be
     * {@code null} and are passed to the matcher.
     * </p>
     *
     * <p><b>Edge Cases</b></p>
     * <ul>
     *     <li>Duplicate names may produce duplicate entries in the lists.</li>
     *     <li>Empty {@link #getNames()} results in no list modifications.</li>
     * </ul>
     *
     * @param resolver property resolver used to query presence and resolve values
     * @param missing output list receiving property names that are missing when {@code matchIfMissing=false}
     * @param nonMatching output list receiving property names whose values do not match or could not be converted
     * @param matcher comparison strategy
     */
    public void collectProperties(PropertyResolver resolver,
                                  List<String> missing,
                                  List<String> nonMatching,
                                  PropertySpecMatcher<V, S> matcher) {
        for(String name : this.getNames()) {
            try {
                String key = this.getPrefix() + name;
                if (resolver.containsProperty(key)) {
                    if (!this.isMatch(resolver.getProperty(key, this.getHavingValueType()), matcher)) {
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

    /**
     * Evaluates whether a resolved property value matches this specification.
     *
     * <p><b>Semantics</b></p>
     * <p>Delegates to {@link PropertySpecMatcher#compare(PropertySpec, Object, Object)} using this spec and the configured candidate value.</p>
     *
     * <p><b>Thread Safety</b></p>
     * <p>Thread-safe provided that {@code matcher} is thread-safe.</p>
     *
     * <p><b>Null Handling</b></p>
     * <p>{@code value} may be {@code null} and is forwarded to the matcher.</p>
     *
     * @param value resolved property value, possibly {@code null}
     * @param matcher comparison strategy
     * @return {@code true} if the property matches; otherwise {@code false}
     */
    @SuppressWarnings("unchecked")
    protected boolean isMatch(@Nullable V value, PropertySpecMatcher<V, S> matcher) {
        return matcher.compare((S) this, value, this.getHavingValue());
    }

    protected Class<? extends Annotation> getAnnotationType() {
        return annotationType;
    }

    protected String getPrefix() {
        return prefix;
    }

    protected String[] getNames() {
        return names;
    }

    protected V getHavingValue() {
        return havingValue;
    }

    protected Class<V> getHavingValueType() {
        return havingValueType;
    }

    protected boolean isMatchIfMissing() {
        return matchIfMissing;
    }

    public String toString() {
        StringBuilder result = new StringBuilder()
                .append("(")
                .append(this.getPrefix());
        if (this.getNames().length == 1) {
            result.append(this.getNames()[0]);
        } else {
            result.append("[")
                    .append(StringUtils.arrayToCommaDelimitedString(this.getNames()))
                    .append("]");
        }
        return result.append("=")
                .append(this.getHavingValue())
                .append(")")
                .toString();
    }
}

package io.conditionals.condition.spec;

import org.jspecify.annotations.Nullable;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.env.PropertyResolver;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.function.Function;

/**
 * Internal specification object representing the configuration of a property-based conditional annotation.
 *
 * <p>A {@code PropertySpec} is created from {@link AnnotationAttributes} and is used by
 * {@link io.conditionals.condition.impl.PropertySpringBootCondition} implementations to evaluate whether an
 * annotated element should match based on one or more configuration properties obtained from a
 * {@link PropertyResolver} (typically a Spring {@link org.springframework.core.env.Environment}).</p>
 *
 * <p><b>Key responsibilities</b></p>
 * <ul>
 *     <li>Resolve and normalize the property key prefix ({@link #resolvePrefix(AnnotationAttributes)}).</li>
 *     <li>Resolve the set of property names to evaluate ({@link #resolveNames(AnnotationAttributes)}), enforcing
 *     the {@code value}/{@code name} exclusivity common to this library's annotations.</li>
 *     <li>Hold the candidate "having" value and its runtime type for conversion-based resolution.</li>
 *     <li>Collect missing and non-matching property names for structured match diagnostics
 *     ({@link #collectProperties(PropertyResolver, List, List, PropertySpecMatcher)}).</li>
 * </ul>
 *
 * <p><b>Evaluation model</b></p>
 * <p>For each configured name, the effective property key is {@code prefix + name}. If the key exists, the
 * property value is retrieved as {@link #getHavingValueType()} and compared against {@link #getHavingValue()}
 * using a caller-provided {@link PropertySpecMatcher}. Missing properties are treated as no-match unless
 * {@link #isMatchIfMissing()} is {@code true}. Conversion failures are treated as non-matching values.</p>
 *
 * <p><b>Thread safety</b></p>
 * <p>Instances are immutable after construction. Provided that {@link PropertySpecMatcher} implementations are
 * stateless or thread-safe, this type is safe to share across threads.</p>
 *
 * @param <V> resolved property value type and the candidate ("having") value type
 * @param <S> self type used to provide the concrete spec type to matchers
 * @author Maksym Uimanov
 * @since 1.0
 * @see PropertySpecMatcher
 * @see io.conditionals.condition.impl.PropertySpringBootCondition
 */
public abstract class PropertySpec<V, S extends PropertySpec<V, S>> {
    /**
     * Attribute name used for the alias property list.
     */
    protected static final String VALUE = "value";
    /**
     * Attribute name used for the property prefix.
     */
    protected static final String PREFIX = "prefix";
    /**
     * Attribute name used for the primary property list.
     */
    protected static final String NAME = "name";
    /**
     * Attribute name used for the candidate value.
     */
    protected static final String HAVING_VALUE = "havingValue";
    /**
     * Attribute name controlling missing-property behavior.
     */
    protected static final String MATCH_IF_MISSING = "matchIfMissing";
    private final Class<? extends Annotation> annotationType;
    private final String prefix;
    private final String[] names;
    private final V havingValue;
    private final Class<V> havingValueType;
    private final boolean matchIfMissing;

    /**
     * Create a new {@code PropertySpec} instance from the provided annotation attributes.
     *
     * @param annotationType annotation type that produced this spec
     * @param annotationAttributes attributes of the conditional annotation
     */
    @SuppressWarnings("unchecked")
    protected PropertySpec(Class<? extends Annotation> annotationType,
                           AnnotationAttributes annotationAttributes) {
        this(annotationType, annotationAttributes, attribute -> (V) attribute);
    }

    @SuppressWarnings("unchecked")
    protected PropertySpec(Class<? extends Annotation> annotationType,
                           AnnotationAttributes annotationAttributes,
                           Function<Object, V> havingValueMapper) {
        this.annotationType = annotationType;
        this.prefix = this.resolvePrefix(annotationAttributes);
        this.names = this.resolveNames(annotationAttributes);
        this.havingValue = havingValueMapper.apply(annotationAttributes.get(HAVING_VALUE));
        this.havingValueType = (Class<V>) this.havingValue.getClass();
        this.matchIfMissing = annotationAttributes.getBoolean(MATCH_IF_MISSING);
    }

    /**
     * Resolve and normalize the prefix configured by the conditional annotation.
     *
     * <p>If the underlying annotation does not define a {@code prefix} attribute, an empty prefix is returned.
     * Otherwise, the value is trimmed. When the trimmed prefix is non-empty and does not end with {@code '.'}, a
     * trailing dot is appended so that callers can form property keys as {@code prefix + name}.</p>
     *
     * @param annotationAttributes attributes of the conditional annotation (never {@code null})
     * @return normalized prefix to apply to each property name
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
     * Resolve property names from the {@code value} and {@code name} attributes.
     *
     * <p>This library uses the Spring Boot convention where {@code value} acts as an alias of {@code name}.
     * Exactly one of them must be specified and it must contain at least one element. Violations are reported via
     * {@link Assert#state(boolean, java.util.function.Supplier)} to fail fast during condition evaluation.</p>
     *
     * @param annotationAttributes attributes of the conditional annotation (never {@code null})
     * @return the resolved property names (without {@link #getPrefix()})
     * @throws IllegalStateException if neither {@code value} nor {@code name} is specified, or if both are specified
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
     * Evaluate each configured property name against this specification and collect mismatches.
     *
     * <p>For each {@link #getNames() name}:</p>
     * <ul>
     *     <li>If {@code resolver} contains the property key {@code prefix + name}, the value is retrieved using
     *     {@link PropertyResolver#getProperty(String, Class)} with {@link #getHavingValueType()} and compared using
     *     {@link #isMatch(Object, PropertySpecMatcher)}.</li>
     *     <li>If the key is absent and {@link #isMatchIfMissing()} is {@code false}, the name is added to
     *     {@code missing}.</li>
     *     <li>If conversion fails (for example due to an invalid format), the name is added to {@code nonMatching}.</li>
     * </ul>
     *
     * @param resolver property resolver used to access configuration properties (never {@code null})
     * @param missing output list receiving names for which the property key was not present and missing is not allowed
     * @param nonMatching output list receiving names for which the property was present but did not match
     * @param matcher comparison strategy used to decide whether a resolved value matches the candidate
     */
    public void collectProperties(PropertyResolver resolver,
                                  List<String> missing,
                                  List<String> nonMatching,
                                  PropertySpecMatcher<V, S> matcher) {
        for (String name : this.getNames()) {
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
     * Compare a single resolved property value against the candidate value using the provided matcher.
     *
     * <p>The default implementation delegates to {@link PropertySpecMatcher#compare(PropertySpec, Object, Object)}.
     * The {@code value} may be {@code null} and matchers are expected to treat {@code null} as non-matching unless
     * the specific condition semantics require otherwise.</p>
     *
     * @param value resolved property value (may be {@code null})
     * @param matcher matcher used to perform the comparison
     * @return {@code true} if the property value matches the candidate value
     */
    @SuppressWarnings("unchecked")
    protected boolean isMatch(@Nullable V value, PropertySpecMatcher<V, S> matcher) {
        return matcher.compare((S) this, value, this.getHavingValue());
    }

    /**
     * Return the annotation type that produced this spec.
     *
     * @return conditional annotation type
     */
    public Class<? extends Annotation> getAnnotationType() {
        return annotationType;
    }

    /**
     * Return the normalized prefix used to build property keys.
     *
     * @return prefix (possibly empty), guaranteed to either be empty or end with {@code '.'}
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Return property names as configured on the annotation.
     *
     * @return property names (without prefix)
     */
    public String[] getNames() {
        return names;
    }

    /**
     * Return the candidate ("having") value used for matching.
     *
     * @return candidate value
     */
    public V getHavingValue() {
        return havingValue;
    }

    /**
     * Return the runtime type of the candidate value.
     *
     * <p>This type is used to request conversion when resolving properties from a {@link PropertyResolver}.</p>
     *
     * @return runtime class of {@link #getHavingValue()}
     */
    public Class<V> getHavingValueType() {
        return havingValueType;
    }

    /**
     * Whether the condition should match when a configured property is missing.
     *
     * @return {@code true} if missing properties are treated as matching
     */
    public boolean isMatchIfMissing() {
        return matchIfMissing;
    }

    /**
     * Return a concise representation of this specification for diagnostics.
     *
     * @return string representation containing the effective key(s) and candidate value
     */
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

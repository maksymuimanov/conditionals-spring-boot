package io.conditionals.condition.spec;

/**
 * String matching strategy used by {@link io.conditionals.condition.ConditionalOnStringProperty}.
 *
 * <p>Interpretation of these constants is performed by
 * {@link io.conditionals.condition.impl.OnStringPropertyCondition}.</p>
 *
 * @author Maksym Uimanov
 * @since 1.0
 */
public enum StringMatchType {
    /** Match when {@code property.equals(candidate)} (optionally normalized by the condition). */
    EQUALS,
    /** Match when {@code property.contains(candidate)} (optionally normalized by the condition). */
    CONTAINS,
    /** Match when {@code property.startsWith(candidate)} (optionally normalized by the condition). */
    STARTS_WITH,
    /** Match when {@code property.endsWith(candidate)} (optionally normalized by the condition). */
    ENDS_WITH,
    /** Match when {@code property.matches(candidate)} treating {@code candidate} as a regular expression. */
    MATCHES
}

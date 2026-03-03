package io.conditionals.condition.spec;

/**
 * Map matching strategy used by {@link io.conditionals.condition.ConditionalOnMapProperty}.
 *
 * <p>Interpretation of these constants is performed by
 * {@link io.conditionals.condition.impl.OnMapPropertyCondition}.</p>
 *
 * @author Maksym Uimanov
 * @since 1.0
 */
public enum MapMatchType {
    /** Match when the resolved map is equal to the candidate map. */
    EQUALS,
    /** Match when the resolved map contains at least one entry from the candidate map. */
    CONTAINS_ANY,
    /** Match when the resolved map contains all entries from the candidate map. */
    CONTAINS_ALL
}

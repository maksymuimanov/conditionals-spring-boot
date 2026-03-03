package io.conditionals.condition.spec;

/**
 * Collection matching strategy used by {@link io.conditionals.condition.ConditionalOnCollectionProperty}.
 *
 * <p>Interpretation of these constants is performed by
 * {@link io.conditionals.condition.impl.OnCollectionPropertyCondition}.</p>
 *
 * @author Maksym Uimanov
 * @since 1.0
 */
public enum CollectionMatchType {
    /** Match when the resolved collection is equal to the candidate collection (order-sensitive). */
    EQUALS,
    /** Match when the resolved collection contains at least one element from the candidate collection. */
    CONTAINS_ANY,
    /** Match when the resolved collection contains all elements from the candidate collection. */
    CONTAINS_ALL,
    /** Match when the resolved collection contains the candidate sequence as a contiguous subsequence. */
    CONTAINS_SEQUENCE,
    /** Match when the resolved collection starts with at least one of the candidate elements. */
    STARTS_WITH_ANY,
    /** Match when the resolved collection starts with the entire candidate sequence. */
    STARTS_WITH_ALL,
    /** Match when the resolved collection ends with at least one of the candidate elements. */
    ENDS_WITH_ANY,
    /** Match when the resolved collection ends with the entire candidate sequence. */
    ENDS_WITH_ALL
}

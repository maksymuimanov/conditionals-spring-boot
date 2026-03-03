package io.conditionals.condition.spec;

/**
 * Comparison strategy for {@link Comparable}-based property conditions.
 *
 * <p>Enum constants are interpreted by condition implementations such as
 * {@link io.conditionals.condition.impl.ComparablePropertySpringBootCondition} by evaluating the
 * {@link Comparable#compareTo(Object)} result against {@code 0}.</p>
 *
 * @author Maksym Uimanov
 * @since 1.0
 */
public enum ComparableMatchType {
    /** Match when {@code property.compareTo(candidate) == 0}. */
    EQUALS,
    /** Match when {@code property.compareTo(candidate) > 0}. */
    GREATER_THAN,
    /** Match when {@code property.compareTo(candidate) < 0}. */
    LESS_THAN,
    /** Match when {@code property.compareTo(candidate) >= 0}. */
    GREATER_THAN_OR_EQUAL,
    /** Match when {@code property.compareTo(candidate) <= 0}. */
    LESS_THAN_OR_EQUAL
}

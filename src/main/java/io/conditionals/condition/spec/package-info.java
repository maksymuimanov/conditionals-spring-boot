/**
  * Specification and matching infrastructure for property-based conditions.
  *
  * <p>This package contains immutable specification objects ({@link io.conditionals.condition.spec.PropertySpec}
  * and its specializations) that represent annotation configuration in a form suitable for evaluation by
  * {@link io.conditionals.condition.impl.PropertySpringBootCondition} implementations.</p>
  *
  * <p><b>Key types</b></p>
  * <ul>
  *     <li>{@link io.conditionals.condition.spec.PropertySpec}: resolves names/prefix and drives property
  *     collection and diagnostics.</li>
  *     <li>{@link io.conditionals.condition.spec.MatchingPropertySpec}: adds {@code matchType} and {@code not}
  *     semantics common to most annotations.</li>
  *     <li>{@link io.conditionals.condition.spec.PropertySpecMatcher}: strategy interface used to compare a
  *     resolved value to the configured candidate value.</li>
  *     <li>Match enums (for example {@link io.conditionals.condition.spec.ComparableMatchType},
  *     {@link io.conditionals.condition.spec.StringMatchType}): define comparison modes interpreted by conditions.</li>
  * </ul>
  *
  * <p><b>Nullability</b></p>
  * <p>This package is {@link org.jspecify.annotations.NullMarked @NullMarked}. Unless otherwise specified,
  * parameters and return values are non-null by default.</p>
  */
@NullMarked
package io.conditionals.condition.spec;

import org.jspecify.annotations.NullMarked;
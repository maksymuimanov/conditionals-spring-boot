/**
  * Utilities used by condition implementations.
  *
  * <p>This package contains small helper types (for example
  * {@link io.conditionals.condition.utils.ConditionUtils}) used to extract annotation attributes from
  * {@link org.springframework.core.type.AnnotatedTypeMetadata}, to create consistent
  * {@link org.springframework.boot.autoconfigure.condition.ConditionOutcome} instances, and to apply shared
  * negation semantics.</p>
  *
  * <p><b>Nullability</b></p>
  * <p>This package is {@link org.jspecify.annotations.NullMarked @NullMarked}. Unless otherwise specified,
  * parameters and return values are non-null by default.</p>
  */
@NullMarked
package io.conditionals.condition.utils;

import org.jspecify.annotations.NullMarked;
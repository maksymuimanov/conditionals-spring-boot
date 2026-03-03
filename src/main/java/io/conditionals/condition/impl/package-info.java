/**
  * Condition implementations for the {@code conditionals-spring-boot} library.
  *
  * <p>Types in this package implement Spring Boot's
  * {@link org.springframework.boot.autoconfigure.condition.SpringBootCondition} and provide the runtime
  * evaluation logic for the conditional annotations in {@code io.conditionals.condition}.</p>
  *
  * <p><b>Evaluation model</b></p>
  * <ul>
  *     <li>Spring Boot invokes {@link org.springframework.boot.autoconfigure.condition.SpringBootCondition#getMatchOutcome}
  *     during configuration processing.</li>
  *     <li>{@link io.conditionals.condition.impl.MatchingSpringBootCondition} provides common aggregation logic
  *     across repeatable annotation instances.</li>
  *     <li>{@link io.conditionals.condition.impl.PropertySpringBootCondition} provides property resolution and
  *     mismatch reporting for property-based annotations.</li>
  * </ul>
  *
  * <p><b>Thread safety</b></p>
  * <p>Condition instances are expected to be stateless and therefore safe to reuse across evaluations.
  * Per-evaluation state is allocated on the stack or in short-lived helper/spec objects.</p>
  *
  * <p><b>Nullability</b></p>
  * <p>This package is {@link org.jspecify.annotations.NullMarked @NullMarked}. Unless otherwise specified,
  * parameters and return values are non-null by default.</p>
  */
@NullMarked
package io.conditionals.condition.impl;

import org.jspecify.annotations.NullMarked;
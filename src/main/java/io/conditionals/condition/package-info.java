/**
  * Public conditional annotation API for the {@code conditionals-spring-boot} library.
  *
  * <p>This package defines a set of {@link org.springframework.context.annotation.Conditional} annotations that
  * provide type-specific alternatives to Spring Boot's property/expression based conditionals.
  * Each annotation is backed by a corresponding {@link org.springframework.boot.autoconfigure.condition.SpringBootCondition}
  * implementation from {@code io.conditionals.condition.impl}.</p>
  *
  * <p><b>General semantics</b></p>
  * <ul>
  *     <li>Property-based annotations resolve keys as {@code prefix + name} with prefix normalization.</li>
  *     <li>Most annotations support repeatable declarations via a {@code *Properties} container annotation.</li>
  *     <li>Matching is performed using type-specific match strategies (for example numeric comparison,
  *     string matching, and collection/map semantics).</li>
  * </ul>
  *
  * <p><b>Nullability</b></p>
  * <p>This package is {@link org.jspecify.annotations.NullMarked @NullMarked}. Unless otherwise specified,
  * parameters and return values are non-null by default.</p>
  */
@NullMarked
package io.conditionals.condition;

import org.jspecify.annotations.NullMarked;
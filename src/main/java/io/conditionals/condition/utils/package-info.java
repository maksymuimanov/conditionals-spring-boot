/**
 * Utilities supporting Spring Boot condition evaluation.
 *
 * <p>
 * This package contains shared helpers used by {@link org.springframework.boot.autoconfigure.condition.SpringBootCondition}
 * implementations to locate annotation attributes, handle repeatable annotations, evaluate property-based
 * specifications, and construct diagnostic messages.
 * </p>
 *
 * <p><b>Semantics</b></p>
 * <ul>
 *     <li><b>Attribute discovery</b>: attributes are read from {@link org.springframework.core.type.AnnotatedTypeMetadata}
 *     and converted to {@link org.springframework.core.annotation.AnnotationAttributes}.</li>
 *     <li><b>Repeatable handling</b>: direct and container attributes can be merged into a single encounter-ordered
 *     stream.</li>
 *     <li><b>Aggregation</b>: multiple attribute instances are aggregated with AND semantics by default.</li>
 * </ul>
 *
 * <p><b>Thread Safety</b></p>
 * <p>
 * Utilities are stateless and thread-safe.
 * </p>
 *
 * <p><b>Null Handling</b></p>
 * <p>
 * This package is {@link org.jspecify.annotations.NullMarked null-marked}. Where attribute instances may be
 * absent, APIs use {@code @Nullable} explicitly.
 * </p>
 *
 * @author Maksym Uimanov
 * @since 1.0
 * @see io.conditionals.condition.impl
 */
@NullMarked
package io.conditionals.condition.utils;

import org.jspecify.annotations.NullMarked;
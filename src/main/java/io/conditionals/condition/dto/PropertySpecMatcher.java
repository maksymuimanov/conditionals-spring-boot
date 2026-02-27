package io.conditionals.condition.dto;

@FunctionalInterface
public interface PropertySpecMatcher<V, S extends PropertySpec<V, S>> {
    boolean compare(S spec, V property, V candidate);
}

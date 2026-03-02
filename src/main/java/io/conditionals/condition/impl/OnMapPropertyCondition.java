package io.conditionals.condition.impl;

import io.conditionals.condition.ConditionalOnCollectionProperties;
import io.conditionals.condition.ConditionalOnCollectionProperty;
import io.conditionals.condition.dto.MapMatchType;
import io.conditionals.condition.dto.MatchingPropertySpec;
import io.conditionals.condition.dto.PropertySpecMatcher;
import io.conditionals.condition.utils.ConditionUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.core.annotation.AnnotationAttributes;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

public class OnMapPropertyCondition extends PropertySpringBootCondition<Map<String, String>, OnMapPropertyCondition.Spec> {
    @Override
    protected Class<? extends Annotation> getAnnotationClass() {
        return ConditionalOnCollectionProperty.class;
    }

    @Override
    protected Class<? extends Annotation> getAnnotationContainerClass() {
        return ConditionalOnCollectionProperties.class;
    }

    @Override
    protected Spec createSpec(@Nullable Class<? extends Annotation> annotationType, AnnotationAttributes annotationAttributes) {
        return new Spec(annotationType, annotationAttributes);
    }

    @Override
    protected PropertySpecMatcher<Map<String, String>, Spec> createPropertySpecMatcher() {
        return (spec, property, candidate) -> {
            if (property == null) return false;
            if (spec.size != -1 && property.size() != spec.size) return spec.isNot();
            boolean result = switch (spec.getMatchType()) {
                case EQUALS -> equals(property, candidate);
                case CONTAINS_ANY -> containsAny(property, candidate);
                case CONTAINS_ALL -> containsAll(property, candidate);
            };
            return ConditionUtils.revert(result, spec.isNot());
        };
    }

    private static boolean equals(Map<String, String> property, Map<String, String> candidate) {
        int candidateSize = candidate.size();
        if (candidateSize == 0) return true;

        return Map.copyOf(property).equals(Map.copyOf(candidate));
    }

    private static boolean containsAny(Map<String, String> property, Map<String, String> candidate) {
        int candidateSize = candidate.size();
        if (candidateSize == 0) return true;
        
        Iterable<Map.Entry<String, String>> entrySet = candidate.entrySet();
        for (Map.Entry<String, String> entry : entrySet) {
            if (property.containsKey(entry.getKey()) && property.get(entry.getKey()).equals(entry.getValue())) {
                return true;
            }
        }

        return false;
    }

    private static boolean containsAll(Map<String, String> property, Map<String, String> candidate) {
        int candidateSize = candidate.size();
        if (candidateSize == 0) return true;
        int propertySize = property.size();
        if (propertySize < candidateSize) return false;

        for (Map.Entry<String, String> entry : candidate.entrySet()) {
            if (!property.containsKey(entry.getKey()) || !property.get(entry.getKey()).equals(entry.getValue())) {
                return false;
            }
        }

        return true;
    }

    public static class Spec extends MatchingPropertySpec<Map<String, String>, Spec, MapMatchType> {
        private static final String SIZE = "size";
        private final int size;

        private Spec(Class<? extends Annotation> annotationType, AnnotationAttributes annotationAttributes) {
            super(annotationType, annotationAttributes, attribute -> {
                String[] candidateKeys = annotationAttributes.getStringArray("havingKey");
                String[] candidateValues = (String[]) attribute;
                if (candidateKeys.length != candidateValues.length) throw new IllegalArgumentException("Candidate keys and values must have the same length");
                Map<String, String> map = new HashMap<>();
                for (int i = 0; i < candidateKeys.length; i++) {
                    map.put(candidateKeys[i], candidateValues[i]);
                }
                return map;
            });
            this.size = annotationAttributes.getNumber(SIZE).intValue();
        }
    }
}

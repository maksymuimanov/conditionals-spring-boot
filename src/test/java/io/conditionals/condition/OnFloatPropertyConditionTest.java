package io.conditionals.condition;

import io.conditionals.condition.dto.NumericMatchType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class OnFloatPropertyConditionTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    void floatPropertyCondition_shouldCreateBean_whenPropertyEqualsHavingValueWithinPrecision() {
        this.contextRunner.withPropertyValues("app.ratio=1.000001")
                .withUserConfiguration(EqualsConfig.class)
                .run(context -> assertThat(context)
                        .hasSingleBean(Float.class));
    }

    @Test
    void floatPropertyCondition_shouldNotCreateBean_whenPropertyNotEqualsHavingValueOutsidePrecision() {
        this.contextRunner.withPropertyValues("app.ratio=1.1")
                .withUserConfiguration(EqualsConfig.class)
                .run(context -> assertThat(context)
                        .doesNotHaveBean(Float.class));
    }

    @Test
    void floatPropertyCondition_shouldNotCreateBean_whenPropertyIsNaN() {
        this.contextRunner.withPropertyValues("app.ratio=NaN")
                .withUserConfiguration(EqualsConfig.class)
                .run(context -> assertThat(context)
                        .doesNotHaveBean(Float.class));
    }

    @Test
    void floatPropertyCondition_shouldCreateBean_whenGreaterThanMatches() {
        this.contextRunner.withPropertyValues("app.ratio=2.0")
                .withUserConfiguration(GreaterThanConfig.class)
                .run(context -> assertThat(context)
                        .hasSingleBean(Float.class));
    }

    @Test
    void floatPropertyCondition_shouldNotCreateBean_whenNotFlagInvertsMatch() {
        this.contextRunner.withPropertyValues("app.ratio=1.0")
                .withUserConfiguration(NotConfig.class)
                .run(context -> assertThat(context)
                        .doesNotHaveBean(Float.class));
    }

    @Test
    void floatPropertyCondition_shouldCreateBean_whenPropertyMissingAndMatchIfMissingTrue() {
        this.contextRunner.withUserConfiguration(MatchIfMissingConfig.class)
                .run(context -> assertThat(context)
                        .hasSingleBean(Float.class));
    }

    @Configuration(proxyBeanMethods = false)
    static class EqualsConfig {
        @Bean
        @ConditionalOnFloatProperty(name = "app.ratio", havingValue = 1.0F)
        Float conditionalBean() {
            return 1.0F;
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class GreaterThanConfig {
        @Bean
        @ConditionalOnFloatProperty(name = "app.ratio", havingValue = 1.0F, matchType = NumericMatchType.GREATER_THAN)
        Float conditionalBean() {
            return 1.0F;
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class NotConfig {
        @Bean
        @ConditionalOnFloatProperty(name = "app.ratio", havingValue = 1.0F, not = true)
        Float conditionalBean() {
            return 1.0F;
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class MatchIfMissingConfig {
        @Bean
        @ConditionalOnFloatProperty(name = "app.ratio", havingValue = 1.0F, matchIfMissing = true)
        Float conditionalBean() {
            return 1.0F;
        }
    }
}

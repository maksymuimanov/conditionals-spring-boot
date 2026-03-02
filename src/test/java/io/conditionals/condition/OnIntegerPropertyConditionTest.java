package io.conditionals.condition;

import io.conditionals.condition.dto.NumericMatchType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class OnIntegerPropertyConditionTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    void integerPropertyCondition_shouldCreateBean_whenPropertyEqualsHavingValue() {
        this.contextRunner.withPropertyValues("app.count=5")
                .withUserConfiguration(EqualsConfig.class)
                .run(context -> assertThat(context).hasSingleBean(Integer.class));
    }

    @Test
    void integerPropertyCondition_shouldNotCreateBean_whenPropertyPresentButDifferent() {
        this.contextRunner.withPropertyValues("app.count=4")
                .withUserConfiguration(EqualsConfig.class)
                .run(context -> assertThat(context).doesNotHaveBean(Integer.class));
    }

    @Test
    void integerPropertyCondition_shouldNotCreateBean_whenPropertyMissingAndMatchIfMissingFalse() {
        this.contextRunner.withUserConfiguration(EqualsConfig.class)
                .run(context -> assertThat(context).doesNotHaveBean(Integer.class));
    }

    @Test
    void integerPropertyCondition_shouldCreateBean_whenPropertyMissingAndMatchIfMissingTrue() {
        this.contextRunner.withUserConfiguration(MatchIfMissingConfig.class)
                .run(context -> assertThat(context).hasSingleBean(Integer.class));
    }

    @Test
    void integerPropertyCondition_shouldCreateBean_whenGreaterThanMatches() {
        this.contextRunner.withPropertyValues("app.count=10")
                .withUserConfiguration(GreaterThanConfig.class)
                .run(context -> assertThat(context).hasSingleBean(Integer.class));
    }

    @Test
    void integerPropertyCondition_shouldNotCreateBean_whenNotFlagInvertsMatch() {
        this.contextRunner.withPropertyValues("app.count=5")
                .withUserConfiguration(NotConfig.class)
                .run(context -> assertThat(context).doesNotHaveBean(Integer.class));
    }

    @Test
    void integerPropertyCondition_shouldCreateBean_whenContainerAnnotationAllMatches() {
        this.contextRunner.withPropertyValues(
                "app.one=3",
                "app.two=2"
        ).withUserConfiguration(ContainerAnyMatchesConfig.class)
                .run(context -> assertThat(context)
                        .hasSingleBean(Integer.class));
    }

    @Test
    void integerPropertyCondition_shouldNotCreateBean_whenContainerAnnotationNoneMatches() {
        this.contextRunner.withPropertyValues(
                "app.one=1",
                "app.two=2"
        ).withUserConfiguration(ContainerAnyMatchesConfig.class)
                .run(context -> assertThat(context)
                        .doesNotHaveBean(Integer.class));
    }

    @Configuration(proxyBeanMethods = false)
    static class EqualsConfig {
        @Bean
        @ConditionalOnIntegerProperty(name = "app.count", havingValue = 5)
        Integer conditionalBean() {
            return 5;
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class MatchIfMissingConfig {
        @Bean
        @ConditionalOnIntegerProperty(name = "app.count", havingValue = 5, matchIfMissing = true)
        Integer conditionalBean() {
            return 5;
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class GreaterThanConfig {
        @Bean
        @ConditionalOnIntegerProperty(name = "app.count", havingValue = 5, matchType = NumericMatchType.GREATER_THAN)
        Integer conditionalBean() {
            return 5;
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class NotConfig {
        @Bean
        @ConditionalOnIntegerProperty(name = "app.count", havingValue = 5, not = true)
        Integer conditionalBean() {
            return 5;
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnIntegerProperty(name = "app.one", havingValue = 3)
    @ConditionalOnIntegerProperty(name = "app.two", havingValue = 2)
    static class ContainerAnyMatchesConfig {
        @Bean
        Integer conditionalBean() {
            return 5;
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class InvalidNameAndValueConfig {
        @Bean
        @ConditionalOnIntegerProperty(name = "app.count", value = "app.count", havingValue = 5)
        Integer conditionalBean() {
            return 5;
        }
    }
}

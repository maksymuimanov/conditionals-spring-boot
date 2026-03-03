package io.conditionals.condition;

import io.conditionals.condition.spec.ComparableMatchType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class OnCharacterPropertyConditionTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    void characterPropertyCondition_shouldCreateBean_whenPropertyEqualsHavingValue() {
        this.contextRunner.withPropertyValues("app.letter=a")
                .withUserConfiguration(EqualsConfig.class)
                .run(context -> assertThat(context)
                        .hasSingleBean(Character.class));
    }

    @Test
    void characterPropertyCondition_shouldNotCreateBean_whenPropertyPresentButDifferent() {
        this.contextRunner.withPropertyValues("app.letter=b")
                .withUserConfiguration(EqualsConfig.class)
                .run(context -> assertThat(context)
                        .doesNotHaveBean(Character.class));
    }

    @Test
    void characterPropertyCondition_shouldNotCreateBean_whenPropertyMissingAndMatchIfMissingFalse() {
        this.contextRunner.withUserConfiguration(EqualsConfig.class)
                .run(context -> assertThat(context)
                        .doesNotHaveBean(Character.class));
    }

    @Test
    void characterPropertyCondition_shouldCreateBean_whenPropertyMissingAndMatchIfMissingTrue() {
        this.contextRunner.withUserConfiguration(MatchIfMissingConfig.class)
                .run(context -> assertThat(context)
                        .hasSingleBean(Character.class));
    }

    @Test
    void characterPropertyCondition_shouldCreateBean_whenGreaterThanMatches() {
        this.contextRunner.withPropertyValues("app.letter=c")
                .withUserConfiguration(GreaterThanConfig.class)
                .run(context -> assertThat(context)
                        .hasSingleBean(Character.class));
    }

    @Test
    void characterPropertyCondition_shouldNotCreateBean_whenNotFlagInvertsMatch() {
        this.contextRunner.withPropertyValues("app.letter=a")
                .withUserConfiguration(NotConfig.class)
                .run(context -> assertThat(context)
                        .doesNotHaveBean(Character.class));
    }

    @Test
    void characterPropertyCondition_shouldCreateBean_whenContainerAnnotationAllMatches() {
        this.contextRunner.withPropertyValues("app.one=a", "app.two=b")
                .withUserConfiguration(ContainerAnyMatchesConfig.class)
                .run(context -> assertThat(context)
                        .hasSingleBean(Character.class));
    }

    @Test
    void characterPropertyCondition_shouldNotCreateBean_whenContainerAnnotationNoneMatches() {
        this.contextRunner.withPropertyValues(
                        "app.one=a",
                        "app.two=67"
                ).withUserConfiguration(ContainerAnyMatchesConfig.class)
                .run(context -> assertThat(context)
                        .doesNotHaveBean(Character.class));
    }

    @Configuration(proxyBeanMethods = false)
    static class EqualsConfig {
        @Bean
        @ConditionalOnCharacterProperty(name = "app.letter", havingValue = 'a')
        Character conditionalBean() {
            return 'A';
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class MatchIfMissingConfig {
        @Bean
        @ConditionalOnCharacterProperty(name = "app.letter", havingValue = 'a', matchIfMissing = true)
        Character conditionalBean() {
            return 'A';
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class GreaterThanConfig {
        @Bean
        @ConditionalOnCharacterProperty(name = "app.letter", havingValue = 'a', matchType = ComparableMatchType.GREATER_THAN)
        Character conditionalBean() {
            return 'A';
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class NotConfig {
        @Bean
        @ConditionalOnCharacterProperty(name = "app.letter", havingValue = 'a', not = true)
        Character conditionalBean() {
            return 'A';
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnCharacterProperty(name = "app.one", havingValue = 'a')
    @ConditionalOnCharacterProperty(name = "app.two", havingValue = 'b')
    static class ContainerAnyMatchesConfig {
        @Bean
        Character conditionalBean() {
            return 'A';
        }
    }
}

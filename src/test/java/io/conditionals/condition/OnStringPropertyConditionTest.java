package io.conditionals.condition;

import io.conditionals.condition.dto.StringMatchType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class OnStringPropertyConditionTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    void stringPropertyCondition_shouldCreateBean_whenPropertyEqualsHavingValue() {
        this.contextRunner.withPropertyValues("app.name=demo")
                .withUserConfiguration(EqualsConfig.class)
                .run(context -> {
                    assertThat(context)
                            .hasSingleBean(String.class);
                    assertThat(context.getBean(String.class))
                            .isEqualTo("OK");
                });
    }

    @Test
    void stringPropertyCondition_shouldNotCreateBean_whenPropertyMissingAndMatchIfMissingFalse() {
        this.contextRunner.withUserConfiguration(EqualsConfig.class)
                .run(context -> assertThat(context)
                        .doesNotHaveBean(String.class));
    }

    @Test
    void stringPropertyCondition_shouldCreateBean_whenPropertyMissingAndMatchIfMissingTrue() {
        this.contextRunner.withUserConfiguration(MatchIfMissingConfig.class)
                .run(context -> assertThat(context)
                        .hasSingleBean(String.class));
    }

    @Test
    void stringPropertyCondition_shouldCreateBean_whenIgnoreCaseAndTrimEnabled() {
        this.contextRunner.withPropertyValues("app.name=  DeMo  ")
                .withUserConfiguration(IgnoreCaseTrimConfig.class)
                .run(context -> assertThat(context)
                        .hasSingleBean(String.class));
    }

    @Test
    void stringPropertyCondition_shouldNotCreateBean_whenNotFlagInvertsMatch() {
        this.contextRunner.withPropertyValues("app.name=demo")
                .withUserConfiguration(NotConfig.class)
                .run(context -> assertThat(context)
                        .doesNotHaveBean(String.class));
    }

    @Test
    void stringPropertyCondition_shouldCreateBean_whenMatchTypeContainsMatches() {
        this.contextRunner.withPropertyValues("app.name=hello-demo-world")
                .withUserConfiguration(ContainsConfig.class)
                .run(context -> assertThat(context)
                        .hasSingleBean(String.class));
    }

    @Test
    void stringPropertyCondition_shouldCreateBean_whenMatchTypeStartsWithMatches() {
        this.contextRunner.withPropertyValues("app.name=demo-123")
                .withUserConfiguration(StartsWithConfig.class)
                .run(context -> assertThat(context)
                        .hasSingleBean(String.class));
    }

    @Test
    void stringPropertyCondition_shouldCreateBean_whenMatchTypeEndsWithMatches() {
        this.contextRunner.withPropertyValues("app.name=123-demo")
                .withUserConfiguration(EndsWithConfig.class)
                .run(context -> assertThat(context)
                        .hasSingleBean(String.class));
    }

    @Test
    void stringPropertyCondition_shouldCreateBean_whenMatchTypeMatchesRegexMatches() {
        this.contextRunner.withPropertyValues("app.name=demo-123")
                .withUserConfiguration(MatchesConfig.class)
                .run(context -> assertThat(context)
                        .hasSingleBean(String.class));
    }

    @Test
    void stringPropertyCondition_shouldNotCreateBean_whenMatchTypeMatchesRegexDoesNotMatch() {
        this.contextRunner.withPropertyValues("app.name=demo-XYZ")
                .withUserConfiguration(MatchesConfig.class)
                .run(context -> assertThat(context)
                        .doesNotHaveBean(String.class));
    }

    @Test
    void stringPropertyCondition_shouldCreateBean_whenContainerAnnotationAllMatches() {
        this.contextRunner.withPropertyValues(
                "app.one=yes",
                "app.two=yes"
        ).withUserConfiguration(ContainerAnyMatchesConfig.class)
                .run(context -> assertThat(context)
                        .hasSingleBean(String.class));
    }

    @Test
    void stringPropertyCondition_shouldNotCreateBean_whenContainerAnnotationNoneMatches() {
        this.contextRunner.withPropertyValues(
                "app.one=nope",
                "app.two=yes"
        ).withUserConfiguration(ContainerAnyMatchesConfig.class)
                .run(context -> assertThat(context)
                        .doesNotHaveBean(String.class));
    }

    @Configuration(proxyBeanMethods = false)
    static class EqualsConfig {
        @Bean
        @ConditionalOnStringProperty(name = "app.name", havingValue = "demo")
        String conditionalBean() {
            return "OK";
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class MatchIfMissingConfig {
        @Bean
        @ConditionalOnStringProperty(name = "app.name", havingValue = "demo", matchIfMissing = true)
        String conditionalBean() {
            return "OK";
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class IgnoreCaseTrimConfig {
        @Bean
        @ConditionalOnStringProperty(name = "app.name", havingValue = "demo", ignoreCase = true, trim = true)
        String conditionalBean() {
            return "OK";
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class NotConfig {
        @Bean
        @ConditionalOnStringProperty(name = "app.name", havingValue = "demo", not = true)
        String conditionalBean() {
            return "OK";
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class ContainsConfig {
        @Bean
        @ConditionalOnStringProperty(name = "app.name", havingValue = "demo", matchType = StringMatchType.CONTAINS)
        String conditionalBean() {
            return "OK";
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class StartsWithConfig {
        @Bean
        @ConditionalOnStringProperty(name = "app.name", havingValue = "demo", matchType = StringMatchType.STARTS_WITH)
        String conditionalBean() {
            return "OK";
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class EndsWithConfig {
        @Bean
        @ConditionalOnStringProperty(name = "app.name", havingValue = "demo", matchType = StringMatchType.ENDS_WITH)
        String conditionalBean() {
            return "OK";
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class MatchesConfig {
        @Bean
        @ConditionalOnStringProperty(name = "app.name", havingValue = "demo-\\d+", matchType = StringMatchType.MATCHES)
        String conditionalBean() {
            return "OK";
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnStringProperty(name = "app.one", havingValue = "yes")
    @ConditionalOnStringProperty(name = "app.two", havingValue = "yes")
    static class ContainerAnyMatchesConfig {
        @Bean
        String conditionalBean() {
            return "OK";
        }
    }
}

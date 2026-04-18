package com.clanofcrows.instagram.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.validation.autoconfigure.ValidationAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

class InstagramPropertiesValidationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    ConfigurationPropertiesAutoConfiguration.class,
                    ValidationAutoConfiguration.class
            ))
            .withUserConfiguration(TestConfiguration.class);

    @Test
    void shouldFailContextStartupWhenRequiredPropertiesAreMissing() {
        contextRunner.run(context -> {
            assertThat(context.getStartupFailure()).isNotNull();
        });
    }

    @Test
    void shouldStartContextWhenRequiredPropertiesArePresent() {
        contextRunner
                .withPropertyValues(
                        "instagram.access-token=token",
                        "instagram.ig-user-id=ig-user"
                )
                .run(context -> {
                    assertThat(context.getStartupFailure()).isNull();
                    assertThat(context).hasSingleBean(InstagramProperties.class);
                });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(InstagramProperties.class)
    static class TestConfiguration {

        @Bean
        String marker(InstagramProperties instagramProperties) {
            return instagramProperties.getIgUserId();
        }
    }
}

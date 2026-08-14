package com.cpa.yusin.quiz.version.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AppVersionPolicyPropertiesTest {
    private static final String PREFIX = "app.version-policy.qualification-exams.APPRAISER.ANDROID.";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
            .withUserConfiguration(AppVersionPolicyProperties.class)
            .withPropertyValues(
                    PREFIX + "latest-version=2.1.0",
                    PREFIX + "minimum-version=2.0.0",
                    PREFIX + "store-url=https://play.google.com/store/apps/details?id=com.yusin.quiz"
            );

    @ParameterizedTest
    @ValueSource(strings = {
            "latest-version=2.0",
            "latest-version=02.0.0",
            "latest-version=2.0.0-beta.01",
            "minimum-version=9007199254740992.0.0"
    })
    void failsStartupForInvalidOrAppIncompatibleVersions(String property) {
        contextRunner.withPropertyValues(PREFIX + property).run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure()).hasRootCauseInstanceOf(IllegalStateException.class);
        });
    }

    @Test
    void failsStartupWhenLatestVersionIsBelowMinimumVersion() {
        contextRunner.withPropertyValues(
                PREFIX + "latest-version=2.0.0-rc.1",
                PREFIX + "minimum-version=2.0.0"
        ).run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure()).hasRootCauseMessage(
                    "Invalid app version policy for APPRAISER/ANDROID: " +
                            "latest-version must be greater than or equal to minimum-version"
            );
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "http://play.google.com/store/apps/details?id=com.yusin.quiz",
            "https://example.com/store/apps/details?id=com.yusin.quiz",
            "https://apps.apple.com/kr/app/yusin/id123456789"
    })
    void failsStartupForEmptyUnsafeOrPlatformMismatchedStoreUrl(String storeUrl) {
        contextRunner.withPropertyValues(PREFIX + "store-url=" + storeUrl).run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure()).hasRootCauseMessage(
                    "Invalid app version policy for APPRAISER/ANDROID: " +
                            "store-url must be a safe URL for the configured platform"
            );
        });
    }

    @Test
    void allowsUnreleasedIosPolicyWithZeroMinimumAndEmptyStoreUrl() {
        iosContextRunner()
                .withPropertyValues(
                        "app.version-policy.qualification-exams.APPRAISER.IOS.latest-version=2.0.0",
                        "app.version-policy.qualification-exams.APPRAISER.IOS.minimum-version=0.0.0",
                        "app.version-policy.qualification-exams.APPRAISER.IOS.store-url="
                )
                .run(context -> assertThat(context).hasNotFailed());
    }

    @Test
    void allowsForcedIosPolicyWithAnAppleStoreUrl() {
        iosContextRunner()
                .withPropertyValues(
                        "app.version-policy.qualification-exams.APPRAISER.IOS.latest-version=2.1.0",
                        "app.version-policy.qualification-exams.APPRAISER.IOS.minimum-version=2.0.0",
                        "app.version-policy.qualification-exams.APPRAISER.IOS.store-url=" +
                                "https://apps.apple.com/kr/app/yusin/id123456789"
                )
                .run(context -> assertThat(context).hasNotFailed());
    }

    private ApplicationContextRunner iosContextRunner() {
        return new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
                .withUserConfiguration(AppVersionPolicyProperties.class);
    }
}

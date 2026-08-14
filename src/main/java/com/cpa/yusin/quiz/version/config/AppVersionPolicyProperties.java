package com.cpa.yusin.quiz.version.config;

import com.cpa.yusin.quiz.qualification.domain.QualificationExamCode;
import com.cpa.yusin.quiz.version.domain.AppPlatform;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.version-policy")
public class AppVersionPolicyProperties {
    private static final BigInteger MAX_SAFE_INTEGER = BigInteger.valueOf(9_007_199_254_740_991L);
    private static final Pattern SEMANTIC_VERSION_PATTERN = Pattern.compile(
            "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)" +
                    "(?:-([0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?" +
                    "(?:\\+[0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*)?$"
    );
    private static final SemanticVersion ZERO_VERSION = SemanticVersion.parse("0.0.0");
    private static final Pattern ANDROID_PACKAGE_PATTERN = Pattern.compile("[A-Za-z0-9_.]+");
    private static final Pattern IOS_APP_PATH_PATTERN = Pattern.compile(".*/id\\d+/?$");

    private Map<QualificationExamCode, Map<AppPlatform, Policy>> qualificationExams =
            new EnumMap<>(QualificationExamCode.class);

    @PostConstruct
    void validate() {
        qualificationExams.forEach((code, policies) -> {
            if (policies == null || policies.isEmpty()) {
                throw invalid(code, null, "platform policies are required");
            }
            policies.forEach((platform, policy) -> validatePolicy(code, platform, policy));
        });
    }

    public Optional<Policy> find(QualificationExamCode code, AppPlatform platform) {
        return Optional.ofNullable(qualificationExams.get(code)).map(policies -> policies.get(platform));
    }

    private void validatePolicy(QualificationExamCode code, AppPlatform platform, Policy policy) {
        if (platform == null || policy == null) {
            throw invalid(code, platform, "platform and policy are required");
        }

        SemanticVersion latest = parseVersion(code, platform, "latest-version", policy.latestVersion);
        SemanticVersion minimum = parseVersion(code, platform, "minimum-version", policy.minimumVersion);
        if (latest.compareTo(minimum) < 0) {
            throw invalid(code, platform, "latest-version must be greater than or equal to minimum-version");
        }
        if (minimum.compareTo(ZERO_VERSION) > 0 && !isSafeStoreUrl(platform, policy.storeUrl)) {
            throw invalid(code, platform, "store-url must be a safe URL for the configured platform");
        }
    }

    private SemanticVersion parseVersion(QualificationExamCode code, AppPlatform platform,
                                         String property, String value) {
        try {
            return SemanticVersion.parse(value);
        } catch (IllegalArgumentException exception) {
            throw invalid(code, platform, property + " must be a strict, app-compatible SemVer");
        }
    }

    private boolean isSafeStoreUrl(AppPlatform platform, String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            URI uri = new URI(value);
            if (uri.getUserInfo() != null || uri.getFragment() != null || uri.getPort() != -1) {
                return false;
            }
            return switch (platform) {
                case ANDROID -> isAndroidStoreUrl(uri);
                case IOS -> isIosStoreUrl(uri);
            };
        } catch (URISyntaxException exception) {
            return false;
        }
    }

    private boolean isAndroidStoreUrl(URI uri) {
        if ("https".equalsIgnoreCase(uri.getScheme())
                && "play.google.com".equalsIgnoreCase(uri.getHost())
                && "/store/apps/details".equals(uri.getPath())) {
            return hasValidAndroidPackage(uri.getRawQuery());
        }
        return "market".equalsIgnoreCase(uri.getScheme())
                && "details".equalsIgnoreCase(uri.getHost())
                && (uri.getPath() == null || uri.getPath().isEmpty())
                && hasValidAndroidPackage(uri.getRawQuery());
    }

    private boolean isIosStoreUrl(URI uri) {
        return ("https".equalsIgnoreCase(uri.getScheme()) || "itms-apps".equalsIgnoreCase(uri.getScheme()))
                && "apps.apple.com".equalsIgnoreCase(uri.getHost())
                && uri.getPath() != null
                && IOS_APP_PATH_PATTERN.matcher(uri.getPath()).matches();
    }

    private boolean hasValidAndroidPackage(String rawQuery) {
        if (rawQuery == null) {
            return false;
        }
        return Arrays.stream(rawQuery.split("&"))
                .filter(parameter -> parameter.startsWith("id="))
                .map(parameter -> parameter.substring(3))
                .anyMatch(packageName -> ANDROID_PACKAGE_PATTERN.matcher(packageName).matches());
    }

    private IllegalStateException invalid(QualificationExamCode code, AppPlatform platform, String reason) {
        return new IllegalStateException("Invalid app version policy for " + code + "/" + platform + ": " + reason);
    }

    private record SemanticVersion(BigInteger major, BigInteger minor, BigInteger patch,
                                   List<PrereleaseIdentifier> prerelease) implements Comparable<SemanticVersion> {
        private static SemanticVersion parse(String value) {
            if (value == null) {
                throw new IllegalArgumentException("version is required");
            }
            Matcher matcher = SEMANTIC_VERSION_PATTERN.matcher(value);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("invalid SemVer");
            }

            BigInteger major = safeCoreNumber(matcher.group(1));
            BigInteger minor = safeCoreNumber(matcher.group(2));
            BigInteger patch = safeCoreNumber(matcher.group(3));
            List<PrereleaseIdentifier> prerelease = new ArrayList<>();
            if (matcher.group(4) != null) {
                for (String identifier : matcher.group(4).split("\\.")) {
                    prerelease.add(PrereleaseIdentifier.parse(identifier));
                }
            }
            return new SemanticVersion(major, minor, patch, List.copyOf(prerelease));
        }

        private static BigInteger safeCoreNumber(String value) {
            BigInteger number = new BigInteger(value);
            if (number.compareTo(MAX_SAFE_INTEGER) > 0) {
                throw new IllegalArgumentException("version component exceeds JavaScript safe integer range");
            }
            return number;
        }

        @Override
        public int compareTo(SemanticVersion other) {
            int coreComparison = compareCore(other);
            if (coreComparison != 0) {
                return coreComparison;
            }
            if (prerelease.isEmpty() || other.prerelease.isEmpty()) {
                if (prerelease.isEmpty() == other.prerelease.isEmpty()) return 0;
                return prerelease.isEmpty() ? 1 : -1;
            }
            for (int index = 0; index < Math.max(prerelease.size(), other.prerelease.size()); index++) {
                if (index >= prerelease.size()) return -1;
                if (index >= other.prerelease.size()) return 1;
                int comparison = prerelease.get(index).compareTo(other.prerelease.get(index));
                if (comparison != 0) return comparison;
            }
            return 0;
        }

        private int compareCore(SemanticVersion other) {
            int majorComparison = major.compareTo(other.major);
            if (majorComparison != 0) return majorComparison;
            int minorComparison = minor.compareTo(other.minor);
            return minorComparison != 0 ? minorComparison : patch.compareTo(other.patch);
        }
    }

    private record PrereleaseIdentifier(BigInteger number, String text) implements Comparable<PrereleaseIdentifier> {
        private static PrereleaseIdentifier parse(String value) {
            if (!value.chars().allMatch(Character::isDigit)) {
                return new PrereleaseIdentifier(null, value);
            }
            if (value.length() > 1 && value.startsWith("0")) {
                throw new IllegalArgumentException("numeric prerelease identifier has a leading zero");
            }
            BigInteger number = new BigInteger(value);
            if (number.compareTo(MAX_SAFE_INTEGER) > 0) {
                throw new IllegalArgumentException("prerelease identifier exceeds JavaScript safe integer range");
            }
            return new PrereleaseIdentifier(number, null);
        }

        @Override
        public int compareTo(PrereleaseIdentifier other) {
            if (number != null && other.number == null) return -1;
            if (number == null && other.number != null) return 1;
            return number != null ? number.compareTo(other.number) : text.compareTo(other.text);
        }
    }

    @Getter
    @Setter
    public static class Policy {
        private String latestVersion;
        private String minimumVersion;
        private String storeUrl;
    }
}

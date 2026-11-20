package demo.supermarket.e2e.harness;

import java.util.Optional;

final class Configuration {

    static boolean configuredBoolean(final String property, final String environment, final boolean defaultValue) {
        return configured(property, environment)
                .map(Boolean::parseBoolean)
                .orElse(defaultValue);
    }

    static Optional<String> configured(final String property, final String environment) {
        String value = System.getProperty(property);
        if (value == null || value.isBlank()) {
            value = System.getenv(environment);
        }

        return value == null || value.isBlank()
                ? Optional.empty()
                : Optional.of(value);
    }

    private Configuration() {
    }
}

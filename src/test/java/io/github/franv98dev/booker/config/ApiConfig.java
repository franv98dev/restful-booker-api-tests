package io.github.franv98dev.booker.config;

/**
 * Central configuration. Every value can be overridden with a JVM system property ({@code
 * -Dbase.url=...}) or an environment variable ({@code BASE_URL=...}), so the same suite runs
 * against any environment without code changes.
 */
public final class ApiConfig {

  public static final String BASE_URL = get("base.url", "https://restful-booker.herokuapp.com");
  public static final String USERNAME = get("api.username", "admin");
  public static final String PASSWORD = get("api.password", "password123");

  private ApiConfig() {}

  private static String get(String property, String defaultValue) {
    String fromProperty = System.getProperty(property);
    if (fromProperty != null && !fromProperty.isBlank()) {
      return fromProperty;
    }
    String envName = property.toUpperCase().replace('.', '_');
    String fromEnv = System.getenv(envName);
    return fromEnv != null && !fromEnv.isBlank() ? fromEnv : defaultValue;
  }
}

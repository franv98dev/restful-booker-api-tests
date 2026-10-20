package io.github.franv98dev.booker.support;

/** Tag names, used with {@code @Tag} and selectable with {@code mvn test -Dgroups=...}. */
public final class TestTags {

  /** Critical paths, fast feedback after every change. */
  public static final String SMOKE = "smoke";

  /** Full functional coverage. */
  public static final String REGRESSION = "regression";

  /** Response structure validated against JSON Schema. */
  public static final String CONTRACT = "contract";

  /** Authentication and authorization checks. */
  public static final String SECURITY = "security";

  /** Multi-step flows across several endpoints. */
  public static final String E2E = "e2e";

  /** Documented defects, see docs/BUG_REPORTS.md. */
  public static final String KNOWN_ISSUE = "known-issue";

  private TestTags() {}
}

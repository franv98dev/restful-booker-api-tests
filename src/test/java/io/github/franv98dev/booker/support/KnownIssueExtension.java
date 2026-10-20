package io.github.franv98dev.booker.support;

import io.qameta.allure.Allure;
import java.lang.reflect.Method;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.opentest4j.AssertionFailedError;

/**
 * Inverts the result of {@link KnownIssue} tests: an assertion failure is the expected outcome, a
 * pass means the bug was fixed. Any other exception (network error, bad test code) is still
 * reported as a real failure.
 */
public class KnownIssueExtension implements InvocationInterceptor {

  private static final String BUG_REPORTS_URL =
      "https://github.com/franv98dev/restful-booker-api-tests/blob/main/docs/BUG_REPORTS.md#";

  @Override
  public void interceptTestMethod(
      Invocation<Void> invocation,
      ReflectiveInvocationContext<Method> invocationContext,
      ExtensionContext extensionContext)
      throws Throwable {

    KnownIssue issue = invocationContext.getExecutable().getAnnotation(KnownIssue.class);
    String label = issue.value() + ": " + issue.description();
    Allure.issue(issue.value(), BUG_REPORTS_URL + issue.value().toLowerCase());

    try {
      invocation.proceed();
    } catch (AssertionError expected) {
      Allure.addAttachment(
          "Known issue still present",
          "text/plain",
          label + System.lineSeparator() + System.lineSeparator() + expected.getMessage());
      return; // Expected failure: the defect is still there.
    }

    throw new AssertionFailedError(
        label
            + " -> the expected behaviour now works. The defect seems fixed: remove @KnownIssue"
            + " and move this test to the regression suite.");
  }
}

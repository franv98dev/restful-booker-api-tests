package io.github.franv98dev.booker.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Marks a test that asserts the CORRECT behaviour of a feature affected by a known defect.
 *
 * <ul>
 *   <li>While the defect exists, the assertion fails and the test is reported as passed ("expected
 *       failure"), so the suite stays green.
 *   <li>When the defect is fixed, the assertion passes and the test FAILS with a message asking to
 *       remove this annotation, so the fix never goes unnoticed.
 * </ul>
 *
 * <p>Equivalent to Playwright's {@code test.fail()} or pytest's {@code xfail(strict=True)}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Tag(TestTags.KNOWN_ISSUE)
@ExtendWith(KnownIssueExtension.class)
public @interface KnownIssue {

  /** Bug id from docs/BUG_REPORTS.md, e.g. {@code "BUG-004"}. */
  String value();

  /** One-line summary of the defect. */
  String description();
}

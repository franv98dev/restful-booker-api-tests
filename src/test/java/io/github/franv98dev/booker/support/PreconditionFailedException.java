package io.github.franv98dev.booker.support;

/**
 * Test setup failed (e.g. the API could not create the test data). Deliberately not an {@link
 * AssertionError}: a broken precondition must fail the test, never count as the expected failure of
 * a {@link KnownIssue} test.
 */
public class PreconditionFailedException extends RuntimeException {

  public PreconditionFailedException(String message) {
    super(message);
  }

  public PreconditionFailedException(String message, Throwable cause) {
    super(message, cause);
  }
}

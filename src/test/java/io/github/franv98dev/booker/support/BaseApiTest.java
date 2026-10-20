package io.github.franv98dev.booker.support;

import io.github.franv98dev.booker.client.AuthClient;
import io.github.franv98dev.booker.client.BookingClient;
import io.github.franv98dev.booker.model.Booking;
import io.github.franv98dev.booker.model.CreatedBooking;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

/**
 * Base class for the API tests: shared clients, test-data setup through the API and cleanup of
 * every booking a test creates, so the public sandbox is left as it was found.
 */
public abstract class BaseApiTest {

  protected static final AuthClient AUTH = new AuthClient();
  protected static final BookingClient BOOKINGS = new BookingClient();

  private final List<Integer> createdIds = new ArrayList<>();

  @BeforeAll
  static void configureRestAssured() {
    // Print request and response to the console only when an assertion fails.
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
  }

  /**
   * Creates a booking as a precondition and registers it for cleanup. A failure here is a {@link
   * PreconditionFailedException}, not an assertion error, so it is never mistaken for the behaviour
   * under test (this matters for {@link KnownIssue} tests).
   */
  protected CreatedBooking givenBooking(Booking booking) {
    Response response = BOOKINGS.create(booking);
    if (response.statusCode() != 200) {
      throw new PreconditionFailedException(
          "Could not create test booking: HTTP "
              + response.statusCode()
              + " "
              + response.asString());
    }
    CreatedBooking created = response.as(CreatedBooking.class);
    createdIds.add(created.bookingid());
    return created;
  }

  /**
   * Registers a booking created inside the test itself (e.g. by a negative test that succeeded).
   */
  protected void registerForCleanup(Response createResponse) {
    if (createResponse.statusCode() == 200) {
      Integer id = createResponse.path("bookingid");
      if (id != null) {
        createdIds.add(id);
      }
    }
  }

  protected String token() {
    try {
      return AUTH.adminToken();
    } catch (AssertionError e) {
      throw new PreconditionFailedException("Could not get an admin token", e);
    }
  }

  @AfterEach
  void deleteCreatedBookings() {
    for (Integer id : createdIds) {
      try {
        BOOKINGS.delete(id, token());
      } catch (RuntimeException ignored) {
        // Best effort: the sandbox also resets itself periodically.
      }
    }
    createdIds.clear();
  }
}

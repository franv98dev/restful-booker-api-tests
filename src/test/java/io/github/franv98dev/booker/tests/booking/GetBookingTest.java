package io.github.franv98dev.booker.tests.booking;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import io.github.franv98dev.booker.data.BookingFactory;
import io.github.franv98dev.booker.model.Booking;
import io.github.franv98dev.booker.support.BaseApiTest;
import io.github.franv98dev.booker.support.TestTags;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Feature("Get booking")
@DisplayName("Get booking - GET /booking/{id}")
class GetBookingTest extends BaseApiTest {

  @Test
  @Tag(TestTags.SMOKE)
  @Tag(TestTags.CONTRACT)
  @DisplayName("Existing booking matches the contract")
  void existing_booking_matches_the_contract() {
    int id = givenBooking(BookingFactory.validBooking()).bookingid();

    BOOKINGS
        .get(id)
        .then()
        .statusCode(200)
        .contentType(containsString("application/json"))
        .body(matchesJsonSchemaInClasspath("schemas/booking.json"));
  }

  @ParameterizedTest(name = "id {0}")
  @ValueSource(ints = {0, -1, 999_999_999})
  @Tag(TestTags.REGRESSION)
  @DisplayName("Non-existent id returns 404")
  void non_existent_id_returns_404(int id) {
    BOOKINGS.get(id).then().statusCode(404);
  }

  @Test
  @Tag(TestTags.REGRESSION)
  @DisplayName("Non-numeric id returns 404")
  void non_numeric_id_returns_404() {
    BOOKINGS.getByRawId("abc").then().statusCode(404);
  }

  @Test
  @Tag(TestTags.REGRESSION)
  @DisplayName("Booking can be requested as XML")
  void booking_can_be_requested_as_xml() {
    Booking booking = BookingFactory.validBooking();
    int id = givenBooking(booking).bookingid();

    BOOKINGS
        .getAsXml(id)
        .then()
        .statusCode(200)
        .body(containsString("<booking>"))
        .body(containsString("<firstname>" + booking.firstname() + "</firstname>"));
  }

  @Test
  @Tag(TestTags.REGRESSION)
  @DisplayName("Reading a booking needs no authentication")
  void reading_needs_no_authentication() {
    Booking booking = BookingFactory.validBooking();
    int id = givenBooking(booking).bookingid();

    BOOKINGS.get(id).then().statusCode(200).body("lastname", equalTo(booking.lastname()));
  }
}

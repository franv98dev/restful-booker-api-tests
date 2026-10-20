package io.github.franv98dev.booker.tests.booking;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.franv98dev.booker.data.BookingFactory;
import io.github.franv98dev.booker.model.Booking;
import io.github.franv98dev.booker.model.CreatedBooking;
import io.github.franv98dev.booker.support.BaseApiTest;
import io.github.franv98dev.booker.support.TestTags;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Feature("Create booking")
@DisplayName("Create booking - POST /booking")
class CreateBookingTest extends BaseApiTest {

  @Test
  @Tag(TestTags.SMOKE)
  @Tag(TestTags.CONTRACT)
  @DisplayName("Valid booking is created and echoed back (documented as 200 OK)")
  void valid_booking_is_created() {
    Booking booking = BookingFactory.validBooking();

    Response response = BOOKINGS.create(booking);
    registerForCleanup(response);

    response
        .then()
        .statusCode(200)
        .body(matchesJsonSchemaInClasspath("schemas/created-booking.json"));
    CreatedBooking created = response.as(CreatedBooking.class);
    assertThat(created.bookingid()).isPositive();
    assertThat(created.booking()).isEqualTo(booking);
  }

  @Test
  @Tag(TestTags.REGRESSION)
  @DisplayName("Created booking can be read back with the same data")
  void created_booking_is_persisted() {
    Booking booking = BookingFactory.validBooking();
    int id = givenBooking(booking).bookingid();

    Booking stored = BOOKINGS.get(id).then().statusCode(200).extract().as(Booking.class);

    assertThat(stored).isEqualTo(booking);
  }

  @Test
  @Tag(TestTags.REGRESSION)
  @DisplayName("Additional needs is optional")
  void additional_needs_is_optional() {
    Booking withoutNeeds =
        new Booking("Ana", "Diaz", 120, false, BookingFactory.validBooking().bookingdates(), null);

    Response response = BOOKINGS.create(withoutNeeds);
    registerForCleanup(response);

    response.then().statusCode(200);
    assertThat(response.as(CreatedBooking.class).booking()).isEqualTo(withoutNeeds);
  }

  @Test
  @Tag(TestTags.REGRESSION)
  @DisplayName("Each booking gets a different id")
  void each_booking_gets_a_different_id() {
    int first = givenBooking(BookingFactory.validBooking()).bookingid();
    int second = givenBooking(BookingFactory.validBooking()).bookingid();

    assertThat(second).isNotEqualTo(first);
  }

  @ParameterizedTest(name = "\"{0}\"")
  @ValueSource(strings = {"María José", "O'Connor-Núñez", "李", "Zoë"})
  @Tag(TestTags.REGRESSION)
  @DisplayName("Names with accents, symbols and non-Latin scripts are stored unchanged")
  void unicode_names_are_stored_unchanged(String name) {
    Booking booking = BookingFactory.validBooking().withFirstname(name);
    int id = givenBooking(booking).bookingid();

    assertThat(BOOKINGS.get(id).as(Booking.class).firstname()).isEqualTo(name);
  }

  @Test
  @Tag(TestTags.REGRESSION)
  @DisplayName("Malformed JSON is rejected with 400")
  void malformed_json_is_rejected() {
    Response response = BOOKINGS.createRaw("{\"firstname\": ");
    registerForCleanup(response);

    response.then().statusCode(400);
  }
}

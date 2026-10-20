package io.github.franv98dev.booker.tests.booking;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.franv98dev.booker.data.BookingFactory;
import io.github.franv98dev.booker.model.Booking;
import io.github.franv98dev.booker.support.BaseApiTest;
import io.github.franv98dev.booker.support.TestTags;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Feature("Update booking")
@DisplayName("Update booking - PUT /booking/{id}")
class UpdateBookingTest extends BaseApiTest {

  @Test
  @Tag(TestTags.SMOKE)
  @Tag(TestTags.CONTRACT)
  @DisplayName("Full update replaces every field")
  void full_update_replaces_every_field() {
    int id = givenBooking(BookingFactory.validBooking()).bookingid();
    Booking change = BookingFactory.validBooking();

    Booking returned =
        BOOKINGS
            .update(id, change, token())
            .then()
            .statusCode(200)
            .body(matchesJsonSchemaInClasspath("schemas/booking.json"))
            .extract()
            .as(Booking.class);

    assertThat(returned).isEqualTo(change);
    assertThat(BOOKINGS.get(id).as(Booking.class)).isEqualTo(change);
  }

  @Test
  @Tag(TestTags.REGRESSION)
  @DisplayName("Update with missing fields is rejected with 400 and changes nothing")
  void update_with_missing_fields_is_rejected() {
    Booking original = BookingFactory.validBooking();
    int id = givenBooking(original).bookingid();
    Booking incomplete = new Booking("OnlyName", null, null, null, null, null);

    BOOKINGS.update(id, incomplete, token()).then().statusCode(400);

    assertThat(BOOKINGS.get(id).as(Booking.class)).isEqualTo(original);
  }

  @Test
  @Tag(TestTags.REGRESSION)
  @DisplayName("Rejected update without token leaves the booking unchanged")
  void rejected_update_changes_nothing() {
    Booking original = BookingFactory.validBooking();
    int id = givenBooking(original).bookingid();

    BOOKINGS.update(id, BookingFactory.validBooking(), null).then().statusCode(403);

    assertThat(BOOKINGS.get(id).as(Booking.class)).isEqualTo(original);
  }
}

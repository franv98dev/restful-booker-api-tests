package io.github.franv98dev.booker.tests.booking;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.franv98dev.booker.data.BookingFactory;
import io.github.franv98dev.booker.model.Booking;
import io.github.franv98dev.booker.model.BookingDates;
import io.github.franv98dev.booker.support.BaseApiTest;
import io.github.franv98dev.booker.support.TestTags;
import io.qameta.allure.Feature;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Feature("Partial update")
@DisplayName("Partial update - PATCH /booking/{id}")
class PartialUpdateBookingTest extends BaseApiTest {

  @Test
  @Tag(TestTags.SMOKE)
  @DisplayName("Patch changes only the fields sent")
  void patch_changes_only_the_fields_sent() {
    Booking original = BookingFactory.validBooking();
    int id = givenBooking(original).bookingid();

    Booking returned =
        BOOKINGS
            .partialUpdate(id, Map.of("firstname", "Patched", "totalprice", 999), token())
            .then()
            .statusCode(200)
            .extract()
            .as(Booking.class);

    Booking expected = original.withFirstname("Patched").withTotalprice(999);
    assertThat(returned).isEqualTo(expected);
    assertThat(BOOKINGS.get(id).as(Booking.class)).isEqualTo(expected);
  }

  @Test
  @Tag(TestTags.REGRESSION)
  @DisplayName("Patch can replace the nested booking dates")
  void patch_can_replace_nested_dates() {
    Booking original = BookingFactory.validBooking();
    int id = givenBooking(original).bookingid();
    BookingDates newDates = new BookingDates("2031-01-10", "2031-01-12");

    Booking returned =
        BOOKINGS
            .partialUpdate(id, Map.of("bookingdates", newDates), token())
            .then()
            .statusCode(200)
            .extract()
            .as(Booking.class);

    assertThat(returned).isEqualTo(original.withBookingdates(newDates));
  }

  @Test
  @Tag(TestTags.REGRESSION)
  @DisplayName("Empty patch leaves the booking unchanged")
  void empty_patch_changes_nothing() {
    Booking original = BookingFactory.validBooking();
    int id = givenBooking(original).bookingid();

    BOOKINGS.partialUpdate(id, Map.of(), token()).then().statusCode(200);

    assertThat(BOOKINGS.get(id).as(Booking.class)).isEqualTo(original);
  }
}

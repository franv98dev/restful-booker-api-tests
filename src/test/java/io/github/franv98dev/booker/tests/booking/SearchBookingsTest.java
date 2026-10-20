package io.github.franv98dev.booker.tests.booking;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.assertj.core.api.Assertions.assertThat;

import io.github.franv98dev.booker.data.BookingFactory;
import io.github.franv98dev.booker.model.Booking;
import io.github.franv98dev.booker.model.BookingId;
import io.github.franv98dev.booker.support.BaseApiTest;
import io.github.franv98dev.booker.support.TestTags;
import io.qameta.allure.Feature;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Feature("Search bookings")
@DisplayName("Search bookings - GET /booking")
class SearchBookingsTest extends BaseApiTest {

  private List<Integer> search(Map<String, ?> filters) {
    return BOOKINGS
        .getIds(filters)
        .then()
        .statusCode(200)
        .extract()
        .jsonPath()
        .getList("", BookingId.class)
        .stream()
        .map(BookingId::bookingid)
        .toList();
  }

  @Test
  @Tag(TestTags.SMOKE)
  @Tag(TestTags.CONTRACT)
  @DisplayName("Listing all bookings matches the contract")
  void listing_matches_the_contract() {
    givenBooking(BookingFactory.validBooking());

    BOOKINGS
        .getIds()
        .then()
        .statusCode(200)
        .body(matchesJsonSchemaInClasspath("schemas/booking-ids.json"));
  }

  @Test
  @Tag(TestTags.REGRESSION)
  @DisplayName("Search by first name returns only the matching booking")
  void search_by_first_name() {
    Booking booking = BookingFactory.uniqueBooking();
    int id = givenBooking(booking).bookingid();

    assertThat(search(Map.of("firstname", booking.firstname()))).containsExactly(id);
  }

  @Test
  @Tag(TestTags.REGRESSION)
  @DisplayName("Search by first and last name combines both filters")
  void search_by_first_and_last_name() {
    Booking booking = BookingFactory.uniqueBooking();
    int id = givenBooking(booking).bookingid();

    assertThat(search(Map.of("firstname", booking.firstname(), "lastname", booking.lastname())))
        .containsExactly(id);
    assertThat(search(Map.of("firstname", booking.firstname(), "lastname", "Other"))).isEmpty();
  }

  @Test
  @Tag(TestTags.REGRESSION)
  @DisplayName("Search with no matches returns an empty list, not an error")
  void search_with_no_matches_returns_empty_list() {
    assertThat(search(Map.of("firstname", "NoOneHasThisName" + System.nanoTime()))).isEmpty();
  }

  @Test
  @Tag(TestTags.REGRESSION)
  @DisplayName("Check-in filter includes bookings that start after the given date")
  void checkin_filter_includes_later_bookings() {
    Booking booking = BookingFactory.uniqueBooking();
    int id = givenBooking(booking).bookingid();
    LocalDate dayBefore = LocalDate.parse(booking.bookingdates().checkin()).minusDays(1);

    assertThat(search(Map.of("firstname", booking.firstname(), "checkin", dayBefore.toString())))
        .containsExactly(id);
  }

  @Test
  @Tag(TestTags.REGRESSION)
  @DisplayName("Check-in filter excludes bookings that start before the given date")
  void checkin_filter_excludes_earlier_bookings() {
    Booking booking = BookingFactory.uniqueBooking();
    givenBooking(booking);
    LocalDate dayAfter = LocalDate.parse(booking.bookingdates().checkin()).plusDays(1);

    assertThat(search(Map.of("firstname", booking.firstname(), "checkin", dayAfter.toString())))
        .isEmpty();
  }
}

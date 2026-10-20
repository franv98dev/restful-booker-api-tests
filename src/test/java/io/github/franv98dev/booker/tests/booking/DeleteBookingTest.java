package io.github.franv98dev.booker.tests.booking;

import io.github.franv98dev.booker.data.BookingFactory;
import io.github.franv98dev.booker.support.BaseApiTest;
import io.github.franv98dev.booker.support.TestTags;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Feature("Delete booking")
@DisplayName("Delete booking - DELETE /booking/{id}")
class DeleteBookingTest extends BaseApiTest {

  @Test
  @Tag(TestTags.SMOKE)
  @DisplayName("Deleted booking can no longer be read (delete documented as 201 Created)")
  void deleted_booking_is_gone() {
    int id = givenBooking(BookingFactory.validBooking()).bookingid();

    BOOKINGS.delete(id, token()).then().statusCode(201);

    BOOKINGS.get(id).then().statusCode(404);
  }

  @Test
  @Tag(TestTags.REGRESSION)
  @DisplayName("Rejected delete without token keeps the booking")
  void rejected_delete_keeps_the_booking() {
    int id = givenBooking(BookingFactory.validBooking()).bookingid();

    BOOKINGS.delete(id, null).then().statusCode(403);

    BOOKINGS.get(id).then().statusCode(200);
  }

  @Test
  @Tag(TestTags.REGRESSION)
  @DisplayName("Deleting a booking does not affect other bookings")
  void delete_affects_only_that_booking() {
    int toDelete = givenBooking(BookingFactory.validBooking()).bookingid();
    int toKeep = givenBooking(BookingFactory.validBooking()).bookingid();

    BOOKINGS.delete(toDelete, token()).then().statusCode(201);

    BOOKINGS.get(toKeep).then().statusCode(200);
  }
}

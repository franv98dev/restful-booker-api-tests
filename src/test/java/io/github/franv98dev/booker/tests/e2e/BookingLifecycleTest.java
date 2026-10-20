package io.github.franv98dev.booker.tests.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.franv98dev.booker.data.BookingFactory;
import io.github.franv98dev.booker.model.Booking;
import io.github.franv98dev.booker.model.BookingId;
import io.github.franv98dev.booker.model.CreatedBooking;
import io.github.franv98dev.booker.support.BaseApiTest;
import io.github.franv98dev.booker.support.TestTags;
import io.qameta.allure.Allure;
import io.qameta.allure.Feature;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Feature("Booking lifecycle")
@DisplayName("Booking lifecycle (end to end)")
class BookingLifecycleTest extends BaseApiTest {

  @Test
  @Tag(TestTags.SMOKE)
  @Tag(TestTags.E2E)
  @DisplayName("A guest books, changes the booking and cancels it")
  void full_booking_lifecycle() {
    Booking booking = BookingFactory.uniqueBooking();
    String token = token();

    CreatedBooking created =
        Allure.step(
            "Create the booking",
            () ->
                BOOKINGS.create(booking).then().statusCode(200).extract().as(CreatedBooking.class));
    int id = created.bookingid();

    Allure.step(
        "Find it by the guest's name",
        () ->
            assertThat(
                    BOOKINGS
                        .getIds(Map.of("firstname", booking.firstname()))
                        .then()
                        .statusCode(200)
                        .extract()
                        .jsonPath()
                        .getList("", BookingId.class))
                .extracting(BookingId::bookingid)
                .containsExactly(id));

    Booking changed = BookingFactory.validBooking().withFirstname(booking.firstname());
    Allure.step(
        "Replace the whole booking",
        () -> BOOKINGS.update(id, changed, token).then().statusCode(200));

    Allure.step(
        "Add breakfast with a partial update",
        () ->
            BOOKINGS
                .partialUpdate(id, Map.of("additionalneeds", "Breakfast"), token)
                .then()
                .statusCode(200));

    Allure.step(
        "Read it back with every change applied",
        () ->
            assertThat(BOOKINGS.get(id).then().statusCode(200).extract().as(Booking.class))
                .usingRecursiveComparison()
                .ignoringFields("additionalneeds")
                .isEqualTo(changed));

    Allure.step("Cancel the booking", () -> BOOKINGS.delete(id, token).then().statusCode(201));

    Allure.step("It no longer exists", () -> BOOKINGS.get(id).then().statusCode(404));
  }
}

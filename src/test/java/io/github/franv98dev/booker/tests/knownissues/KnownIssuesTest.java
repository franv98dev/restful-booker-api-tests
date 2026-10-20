package io.github.franv98dev.booker.tests.knownissues;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;

import io.github.franv98dev.booker.config.ApiConfig;
import io.github.franv98dev.booker.data.BookingFactory;
import io.github.franv98dev.booker.model.Booking;
import io.github.franv98dev.booker.model.BookingDates;
import io.github.franv98dev.booker.model.BookingId;
import io.github.franv98dev.booker.model.Credentials;
import io.github.franv98dev.booker.support.BaseApiTest;
import io.github.franv98dev.booker.support.KnownIssue;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Defects found during exploratory testing, documented in docs/BUG_REPORTS.md. Each test asserts
 * the CORRECT behaviour; {@link KnownIssue} keeps the suite green while the bug exists and fails as
 * soon as it is fixed.
 */
@Feature("Known issues")
@DisplayName("Known issues")
class KnownIssuesTest extends BaseApiTest {

  private Response create(Booking booking) {
    Response response = BOOKINGS.create(booking);
    registerForCleanup(response);
    return response;
  }

  private Response createRaw(String json) {
    Response response = BOOKINGS.createRaw(json);
    registerForCleanup(response);
    return response;
  }

  private List<Integer> search(Map<String, ?> filters) {
    return BOOKINGS.getIds(filters).jsonPath().getList("", BookingId.class).stream()
        .map(BookingId::bookingid)
        .toList();
  }

  // --- Authentication -------------------------------------------------------------------------

  @Test
  @KnownIssue(value = "BUG-001", description = "Failed login returns 200 OK instead of 401")
  @DisplayName("Failed login returns 401 Unauthorized")
  void failed_login_returns_401() {
    AUTH.createToken(new Credentials(ApiConfig.USERNAME, "wrong")).then().statusCode(401);
  }

  // --- Input validation -------------------------------------------------------------------------

  @Test
  @KnownIssue(value = "BUG-002", description = "Missing required fields cause a 500 server error")
  @DisplayName("Booking without first name is rejected with 400")
  void missing_required_field_returns_400() {
    createRaw(
            """
            {"lastname":"Diaz","totalprice":100,"depositpaid":true,
             "bookingdates":{"checkin":"2030-01-01","checkout":"2030-01-05"}}
            """)
        .then()
        .statusCode(400);
  }

  @Test
  @KnownIssue(value = "BUG-003", description = "A field with the wrong type causes a 500 error")
  @DisplayName("Numeric first name is rejected with 400")
  void wrong_type_returns_400() {
    createRaw(
            """
            {"firstname":123,"lastname":"Diaz","totalprice":100,"depositpaid":true,
             "bookingdates":{"checkin":"2030-01-01","checkout":"2030-01-05"}}
            """)
        .then()
        .statusCode(400);
  }

  @Test
  @KnownIssue(value = "BUG-004", description = "Negative total price is accepted")
  @DisplayName("Negative total price is rejected")
  void negative_price_is_rejected() {
    create(BookingFactory.validBooking().withTotalprice(-50)).then().statusCode(400);
  }

  @Test
  @KnownIssue(value = "BUG-005", description = "Check-out before check-in is accepted")
  @DisplayName("Check-out date before check-in date is rejected")
  void checkout_before_checkin_is_rejected() {
    Booking booking =
        BookingFactory.validBooking()
            .withBookingdates(new BookingDates("2030-05-10", "2030-05-01"));

    create(booking).then().statusCode(400);
  }

  @Test
  @KnownIssue(value = "BUG-006", description = "Invalid date is stored as \"0NaN-aN-aN\"")
  @DisplayName("Invalid date is rejected")
  void invalid_date_is_rejected() {
    Booking booking =
        BookingFactory.validBooking()
            .withBookingdates(new BookingDates("not-a-date", "2030-05-01"));

    create(booking).then().statusCode(400);
  }

  @Test
  @KnownIssue(value = "BUG-007", description = "Non-numeric price is saved as null")
  @DisplayName("Non-numeric total price is rejected")
  void non_numeric_price_is_rejected() {
    createRaw(
            """
            {"firstname":"Ana","lastname":"Diaz","totalprice":"abc","depositpaid":true,
             "bookingdates":{"checkin":"2030-01-01","checkout":"2030-01-05"}}
            """)
        .then()
        .statusCode(400);
  }

  @Test
  @KnownIssue(value = "BUG-008", description = "Decimal prices are silently truncated")
  @DisplayName("Decimal total price is stored without losing cents")
  void decimal_price_is_not_truncated() {
    Response response =
        createRaw(
            """
            {"firstname":"Ana","lastname":"Diaz","totalprice":99.99,"depositpaid":true,
             "bookingdates":{"checkin":"2030-01-01","checkout":"2030-01-05"}}
            """);

    Number stored = response.then().statusCode(200).extract().path("booking.totalprice");
    assertThat(stored.doubleValue()).isEqualTo(99.99);
  }

  @Test
  @KnownIssue(value = "BUG-009", description = "Empty first and last names are accepted")
  @DisplayName("Empty names are rejected")
  void empty_names_are_rejected() {
    create(BookingFactory.validBooking().withFirstname("").withLastname("")).then().statusCode(400);
  }

  // --- Error handling ---------------------------------------------------------------------------

  @Test
  @KnownIssue(value = "BUG-010", description = "Update or delete of a missing id returns 405")
  @DisplayName("Updating or deleting a non-existent booking returns 404")
  void missing_booking_returns_404_on_write() {
    int missingId = 999_999_999;

    BOOKINGS.update(missingId, BookingFactory.validBooking(), token()).then().statusCode(404);
    BOOKINGS.delete(missingId, token()).then().statusCode(404);
  }

  // --- Search (documented contract) -------------------------------------------------------------

  @Test
  @KnownIssue(value = "BUG-011", description = "Check-in filter excludes the exact check-in date")
  @DisplayName("Check-in filter includes bookings starting on that exact date (per API docs)")
  void checkin_filter_is_inclusive() {
    Booking booking = BookingFactory.uniqueBooking();
    int id = givenBooking(booking).bookingid();

    assertThat(
            search(
                Map.of(
                    "firstname", booking.firstname(),
                    "checkin", booking.bookingdates().checkin())))
        .containsExactly(id);
  }

  @Test
  @KnownIssue(value = "BUG-012", description = "Check-out filter returns earlier check-outs")
  @DisplayName("Check-out filter excludes bookings ending before that date (per API docs)")
  void checkout_filter_excludes_earlier_checkouts() {
    Booking booking = BookingFactory.uniqueBooking();
    givenBooking(booking);
    String weekAfterCheckout =
        LocalDate.parse(booking.bookingdates().checkout()).plusDays(7).toString();

    assertThat(search(Map.of("firstname", booking.firstname(), "checkout", weekAfterCheckout)))
        .isEmpty();
  }

  // --- Content negotiation ----------------------------------------------------------------------

  @Test
  @KnownIssue(value = "BUG-014", description = "Accept header with several types returns 418")
  @DisplayName("Accept header listing several media types gets a JSON response")
  void accept_header_with_several_types_is_supported() {
    int id = givenBooking(BookingFactory.validBooking()).bookingid();

    BOOKINGS
        .getWithAccept(id, "application/json, application/javascript, text/javascript, text/json")
        .then()
        .statusCode(200)
        .contentType(containsString("json"));
  }

  @Test
  @KnownIssue(value = "BUG-013", description = "XML responses are labeled text/html")
  @DisplayName("XML response has an XML content type")
  void xml_response_has_xml_content_type() {
    int id = givenBooking(BookingFactory.validBooking()).bookingid();

    BOOKINGS.getAsXml(id).then().statusCode(200).contentType(containsString("xml"));
  }
}

package io.github.franv98dev.booker.tests.auth;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import io.github.franv98dev.booker.config.ApiConfig;
import io.github.franv98dev.booker.data.BookingFactory;
import io.github.franv98dev.booker.model.Booking;
import io.github.franv98dev.booker.model.Credentials;
import io.github.franv98dev.booker.support.BaseApiTest;
import io.github.franv98dev.booker.support.TestTags;
import io.qameta.allure.Feature;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Feature("Authentication")
@DisplayName("Authentication")
class AuthTest extends BaseApiTest {

  @Test
  @Tag(TestTags.SMOKE)
  @Tag(TestTags.CONTRACT)
  @DisplayName("Valid credentials return a token")
  void valid_credentials_return_a_token() {
    AUTH.createToken(new Credentials(ApiConfig.USERNAME, ApiConfig.PASSWORD))
        .then()
        .statusCode(200)
        .body(matchesJsonSchemaInClasspath("schemas/auth-token.json"));
  }

  static Stream<Arguments> invalidCredentials() {
    return Stream.of(
        Arguments.of("wrong password", new Credentials(ApiConfig.USERNAME, "wrong")),
        Arguments.of("unknown user", new Credentials("nobody", ApiConfig.PASSWORD)),
        Arguments.of("empty fields", new Credentials("", "")),
        Arguments.of("username with different case", new Credentials("ADMIN", ApiConfig.PASSWORD)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("invalidCredentials")
  @Tag(TestTags.SECURITY)
  @Tag(TestTags.REGRESSION)
  @DisplayName("Invalid credentials never return a token")
  void invalid_credentials_never_return_a_token(String scenario, Credentials credentials) {
    AUTH.createToken(credentials)
        .then()
        .body("token", nullValue())
        .body("reason", equalTo("Bad credentials"));
  }

  @Test
  @Tag(TestTags.SECURITY)
  @Tag(TestTags.REGRESSION)
  @DisplayName("Each login returns a different token")
  void each_login_returns_a_different_token() {
    Credentials admin = new Credentials(ApiConfig.USERNAME, ApiConfig.PASSWORD);
    String first = AUTH.createToken(admin).then().extract().path("token");

    AUTH.createToken(admin).then().body("token", not(equalTo(first)));
  }

  @Test
  @Tag(TestTags.SECURITY)
  @Tag(TestTags.REGRESSION)
  @DisplayName("Write operations are rejected without a token")
  void write_operations_are_rejected_without_a_token() {
    int id = givenBooking(BookingFactory.validBooking()).bookingid();
    Booking change = BookingFactory.validBooking();

    BOOKINGS.update(id, change, null).then().statusCode(403);
    BOOKINGS.partialUpdate(id, Map.of("firstname", "Hacker"), null).then().statusCode(403);
    BOOKINGS.delete(id, null).then().statusCode(403);
  }

  @Test
  @Tag(TestTags.SECURITY)
  @Tag(TestTags.REGRESSION)
  @DisplayName("Write operations are rejected with an invalid token")
  void write_operations_are_rejected_with_an_invalid_token() {
    int id = givenBooking(BookingFactory.validBooking()).bookingid();

    BOOKINGS.update(id, BookingFactory.validBooking(), "invalid-token").then().statusCode(403);
    BOOKINGS.delete(id, "invalid-token").then().statusCode(403);
  }

  @Test
  @Tag(TestTags.SECURITY)
  @Tag(TestTags.REGRESSION)
  @DisplayName("Basic auth is accepted as an alternative to the token")
  void basic_auth_is_accepted() {
    int id = givenBooking(BookingFactory.validBooking()).bookingid();
    Booking change = BookingFactory.validBooking();

    BOOKINGS
        .updateWithBasicAuth(id, change, ApiConfig.USERNAME, ApiConfig.PASSWORD)
        .then()
        .statusCode(200)
        .body("firstname", equalTo(change.firstname()));
  }

  @Test
  @Tag(TestTags.SECURITY)
  @Tag(TestTags.REGRESSION)
  @DisplayName("Basic auth with a wrong password is rejected")
  void basic_auth_with_wrong_password_is_rejected() {
    int id = givenBooking(BookingFactory.validBooking()).bookingid();

    BOOKINGS
        .updateWithBasicAuth(id, BookingFactory.validBooking(), ApiConfig.USERNAME, "wrong")
        .then()
        .statusCode(403);
  }
}

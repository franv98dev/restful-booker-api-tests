package io.github.franv98dev.booker.client;

import static io.restassured.RestAssured.given;

import io.github.franv98dev.booker.model.Booking;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.Map;

/**
 * Client for the {@code /booking} endpoints. Methods return the raw {@link Response} so tests can
 * assert status codes, headers and bodies, including the negative cases.
 */
public final class BookingClient {

  public Response getIds() {
    return getIds(Map.of());
  }

  public Response getIds(Map<String, ?> filters) {
    return given().spec(Specs.base()).queryParams(filters).when().get("/booking");
  }

  public Response get(int id) {
    return given().spec(Specs.base()).when().get("/booking/{id}", id);
  }

  /** Sends a custom Accept header, to test content negotiation. */
  public Response getWithAccept(int id, String accept) {
    return given().spec(Specs.base()).accept(accept).when().get("/booking/{id}", id);
  }

  /** Sends the id as-is, to test ids that are not numbers. */
  public Response getByRawId(String rawId) {
    return given().spec(Specs.base()).when().get("/booking/" + rawId);
  }

  public Response getAsXml(int id) {
    return given().spec(Specs.base()).accept("application/xml").when().get("/booking/{id}", id);
  }

  public Response create(Object body) {
    return given().spec(Specs.base()).body(body).when().post("/booking");
  }

  /** Sends a raw string body, to test malformed or wrongly typed JSON. */
  public Response createRaw(String json) {
    return given().spec(Specs.base()).body(json).when().post("/booking");
  }

  public Response update(int id, Booking booking, String token) {
    return withToken(token).body(booking).when().put("/booking/{id}", id);
  }

  public Response updateWithBasicAuth(int id, Booking booking, String user, String password) {
    return given()
        .spec(Specs.base())
        .auth()
        .preemptive()
        .basic(user, password)
        .body(booking)
        .when()
        .put("/booking/{id}", id);
  }

  public Response partialUpdate(int id, Map<String, ?> fields, String token) {
    return withToken(token).body(fields).when().patch("/booking/{id}", id);
  }

  public Response delete(int id, String token) {
    return withToken(token).when().delete("/booking/{id}", id);
  }

  /** {@code token == null} sends the request without authentication. */
  private RequestSpecification withToken(String token) {
    RequestSpecification spec = given().spec(Specs.base());
    return token == null ? spec : spec.cookie("token", token);
  }
}

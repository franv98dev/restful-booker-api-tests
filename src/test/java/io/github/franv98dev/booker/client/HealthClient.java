package io.github.franv98dev.booker.client;

import static io.restassured.RestAssured.given;

import io.restassured.response.Response;

/** Client for {@code GET /ping}. */
public final class HealthClient {

  public Response ping() {
    return given().spec(Specs.base()).when().get("/ping");
  }
}

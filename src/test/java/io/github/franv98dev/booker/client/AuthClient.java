package io.github.franv98dev.booker.client;

import static io.restassured.RestAssured.given;

import io.github.franv98dev.booker.config.ApiConfig;
import io.github.franv98dev.booker.model.Credentials;
import io.restassured.response.Response;

/** Client for {@code POST /auth}. */
public final class AuthClient {

  private static volatile String cachedToken;

  public Response createToken(Credentials credentials) {
    return given().spec(Specs.base()).body(credentials).when().post("/auth");
  }

  /** Token for the configured admin user, requested once and reused by the whole run. */
  public String adminToken() {
    if (cachedToken == null) {
      synchronized (AuthClient.class) {
        if (cachedToken == null) {
          cachedToken =
              createToken(new Credentials(ApiConfig.USERNAME, ApiConfig.PASSWORD))
                  .then()
                  .statusCode(200)
                  .extract()
                  .path("token");
        }
      }
    }
    return cachedToken;
  }
}

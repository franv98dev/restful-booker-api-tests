package io.github.franv98dev.booker.client;

import io.github.franv98dev.booker.config.ApiConfig;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

/** Shared request specification: base URL, JSON headers and the Allure request/response log. */
public final class Specs {

  private Specs() {}

  public static RequestSpecification base() {
    return new RequestSpecBuilder()
        .setBaseUri(ApiConfig.BASE_URL)
        .setContentType(ContentType.JSON)
        // A single media type on purpose: REST Assured's default JSON Accept header lists four
        // types, and the API answers that with "418 I'm a Teapot" (see BUG-014).
        .setAccept("application/json")
        // Every request and response is attached to the Allure report.
        .addFilter(new AllureRestAssured())
        .build();
  }
}

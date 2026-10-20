package io.github.franv98dev.booker.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * A booking as sent to and returned by the API. {@code totalprice} is an {@link Integer} so the
 * tests can detect a {@code null} coming back from the API.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Booking(
    String firstname,
    String lastname,
    Integer totalprice,
    Boolean depositpaid,
    BookingDates bookingdates,
    String additionalneeds) {

  public Booking withFirstname(String value) {
    return new Booking(value, lastname, totalprice, depositpaid, bookingdates, additionalneeds);
  }

  public Booking withLastname(String value) {
    return new Booking(firstname, value, totalprice, depositpaid, bookingdates, additionalneeds);
  }

  public Booking withTotalprice(Integer value) {
    return new Booking(firstname, lastname, value, depositpaid, bookingdates, additionalneeds);
  }

  public Booking withBookingdates(BookingDates value) {
    return new Booking(firstname, lastname, totalprice, depositpaid, value, additionalneeds);
  }
}

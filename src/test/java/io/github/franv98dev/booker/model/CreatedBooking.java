package io.github.franv98dev.booker.model;

/** Response body of {@code POST /booking}. */
public record CreatedBooking(int bookingid, Booking booking) {}

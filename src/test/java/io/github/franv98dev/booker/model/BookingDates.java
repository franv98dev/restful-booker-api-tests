package io.github.franv98dev.booker.model;

/** Check-in and check-out dates, in {@code yyyy-MM-dd} format as the API expects. */
public record BookingDates(String checkin, String checkout) {}

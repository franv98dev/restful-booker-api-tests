package io.github.franv98dev.booker.data;

import io.github.franv98dev.booker.model.Booking;
import io.github.franv98dev.booker.model.BookingDates;
import java.time.LocalDate;
import java.util.List;
import net.datafaker.Faker;

/**
 * Builds valid, random bookings. Tests start from a valid booking and change only the field that
 * matters to them (e.g. {@code validBooking().withTotalprice(-50)}), so each test states exactly
 * what makes it different.
 */
public final class BookingFactory {

  private static final Faker FAKER = new Faker();
  private static final List<String> NEEDS =
      List.of("Breakfast", "Late checkout", "Parking", "Crib");

  private BookingFactory() {}

  public static Booking validBooking() {
    LocalDate checkin = LocalDate.now().plusDays(FAKER.number().numberBetween(10, 300));
    LocalDate checkout = checkin.plusDays(FAKER.number().numberBetween(1, 14));
    return new Booking(
        FAKER.name().firstName(),
        FAKER.name().lastName(),
        FAKER.number().numberBetween(50, 2000),
        FAKER.bool().bool(),
        new BookingDates(checkin.toString(), checkout.toString()),
        FAKER.options().nextElement(NEEDS));
  }

  /** A booking whose first name is unique, so searches only match this test's data. */
  public static Booking uniqueBooking() {
    String uniqueName = "Qa" + FAKER.regexify("[A-Z][a-z]{9}");
    return validBooking().withFirstname(uniqueName);
  }
}

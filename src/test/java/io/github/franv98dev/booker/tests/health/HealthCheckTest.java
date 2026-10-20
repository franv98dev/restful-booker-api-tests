package io.github.franv98dev.booker.tests.health;

import static org.hamcrest.Matchers.lessThan;

import io.github.franv98dev.booker.client.HealthClient;
import io.github.franv98dev.booker.support.TestTags;
import io.qameta.allure.Feature;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Feature("Health check")
@DisplayName("Health check")
class HealthCheckTest {

  private final HealthClient health = new HealthClient();

  @Test
  @Tag(TestTags.SMOKE)
  @DisplayName("API is up and answers the ping (documented as 201 Created)")
  void api_is_up() {
    health.ping().then().statusCode(201).time(lessThan(5L), TimeUnit.SECONDS);
  }
}

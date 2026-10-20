# Restful-Booker · REST Assured API Test Framework

[![API Tests](https://github.com/franv98dev/restful-booker-api-tests/actions/workflows/api-tests.yml/badge.svg)](https://github.com/franv98dev/restful-booker-api-tests/actions/workflows/api-tests.yml)
[![Allure Report](https://img.shields.io/badge/Allure-report-orange)](https://franv98dev.github.io/restful-booker-api-tests/)
![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![REST Assured](https://img.shields.io/badge/REST%20Assured-6.0-6DB33F)
![JUnit](https://img.shields.io/badge/JUnit-6-25A162?logo=junit5&logoColor=white)
![License](https://img.shields.io/badge/license-MIT-blue)

API test automation framework for [Restful-Booker](https://restful-booker.herokuapp.com/apidoc/index.html),
a hotel booking REST API, built with **Java 21, REST Assured, JUnit 6 and Allure**.

It covers the full booking lifecycle (auth, create, read, search, update, partial update, delete)
with contract validation through JSON Schema, and documents [**14 defects**](docs/BUG_REPORTS.md)
found in the API, each one tracked by an automated test.

📊 **[Latest Allure report](https://franv98dev.github.io/restful-booker-api-tests/)**, published automatically from `main`.

> Companion project: [saucedemo-playwright-framework](https://github.com/franv98dev/saucedemo-playwright-framework)
> covers the UI layer with Playwright + TypeScript.

---

## Highlights

| Area                          | What it shows                                                                                                                              |
| ----------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------ |
| **Layered design**            | Tests → API clients → shared request spec. Tests never build HTTP requests by hand.                                                        |
| **Contract testing**          | Every response shape is validated against JSON Schemas (types, required fields, date format, no unexpected fields).                        |
| **Typed models**              | Java records mapped with Jackson; assertions compare whole objects (`isEqualTo(expected)`) instead of field by field.                      |
| **Self-contained test data**  | Each test creates the data it needs through the API with random values (Datafaker) and deletes it afterwards.                              |
| **Negative & security tests** | Invalid credentials, missing/invalid tokens, basic auth, malformed JSON, non-existent ids.                                                 |
| **Custom JUnit extension**    | `@KnownIssue` inverts the result of tests for known defects: green while the bug exists, red when it gets fixed.                            |
| **Safe preconditions**        | Setup failures throw `PreconditionFailedException`, so a broken setup is never mistaken for the expected failure of a known issue.          |
| **Allure reporting**          | Every request/response attached, grouped by feature, custom failure categories, history and trends between runs.                           |
| **CI/CD**                     | GitHub Actions: format check → tests with retries for network blips → Allure report deployed to GitHub Pages. Nightly run and manual run by tag. |

## Tech stack

- **Java 21** · **Maven** (with Maven Wrapper: no local Maven install needed)
- **[REST Assured 6](https://rest-assured.io/)** for HTTP calls and assertions
- **JUnit 6** (Jupiter): tags, parameterized tests, parallel execution, custom extensions
- **AssertJ** for readable object assertions · **Jackson** for JSON mapping
- **JSON Schema Validator** for contract checks · **Datafaker** for test data
- **Allure** for reporting · **Spotless** (google-java-format) for code style
- **GitHub Actions** + **GitHub Pages** · **Dependabot**

## Project structure

```
.
├── .github/
│   ├── workflows/api-tests.yml          # CI pipeline
│   └── dependabot.yml
├── allure/categories.json               # Groups failures in the report (product vs setup)
├── docs/BUG_REPORTS.md                  # 14 defects with requests, expected and actual results
├── pom.xml
└── src/test/
    ├── java/io/github/franv98dev/booker/
    │   ├── config/ApiConfig.java        # Base URL and credentials (overridable by env vars)
    │   ├── client/                      # AuthClient, BookingClient, HealthClient, shared Specs
    │   ├── model/                       # Booking, BookingDates, CreatedBooking... (records)
    │   ├── data/BookingFactory.java     # Random valid bookings with Datafaker
    │   ├── support/                     # BaseApiTest, @KnownIssue + extension, tags
    │   └── tests/
    │       ├── auth/                    # Tokens, invalid credentials, authorization
    │       ├── booking/                 # Create, get, search, update, patch, delete
    │       ├── e2e/                     # Full booking lifecycle
    │       ├── health/                  # Ping
    │       └── knownissues/             # Documented defects (@KnownIssue)
    └── resources/
        ├── schemas/                     # JSON Schemas (contract)
        ├── junit-platform.properties    # Parallel execution, display names
        └── allure.properties
```

## Getting started

### Prerequisites

- **Java 21** (e.g. [Temurin](https://adoptium.net/)). Maven is **not** required: the wrapper downloads it.

### Run the tests

```bash
git clone https://github.com/franv98dev/restful-booker-api-tests.git
cd restful-booker-api-tests

./mvnw test                       # all tests (Windows: mvnw.cmd test)
./mvnw test -Dgroups=smoke        # only the critical paths
./mvnw test -Dgroups=known-issue  # only the documented defects
./mvnw test -Dtest=AuthTest       # a single test class
```

### See the Allure report locally

```bash
./mvnw allure:serve               # generates the report and opens it in the browser
```

### Configuration

| Setting  | System property | Environment variable | Default                                  |
| -------- | --------------- | -------------------- | ---------------------------------------- |
| Base URL | `base.url`      | `BASE_URL`           | `https://restful-booker.herokuapp.com`   |
| Username | `api.username`  | `API_USERNAME`       | `admin`                                  |
| Password | `api.password`  | `API_PASSWORD`       | `password123` (public demo credentials)  |

Example: `./mvnw test -Dbase.url=http://localhost:3001` to run against a local copy of the API.

## Test coverage

**57 tests** in 10 classes, selectable by tag (a test can have more than one):

| Tag           | Tests | Covers                                                                                   |
| ------------- | ----: | ---------------------------------------------------------------------------------------- |
| `smoke`       |     9 | Health check, token, create, get, list, update, patch, delete, full lifecycle            |
| `regression`  |    34 | Validation, persistence, unicode names, search filters, rejected writes leave data intact |
| `security`    |     9 | Invalid credentials, missing/invalid token, basic auth, token uniqueness                  |
| `contract`    |     5 | JSON Schema validation of every response type                                            |
| `e2e`         |     1 | Book → find → replace → patch → read → cancel → verify gone                              |
| `known-issue` |    14 | Defects documented in [BUG_REPORTS.md](docs/BUG_REPORTS.md)                              |

### Test design techniques

- **Equivalence partitioning:** one test per class of invalid credentials (wrong password, unknown
  user, empty fields, different case) instead of many similar ones.
- **Boundary and edge cases:** ids `0`, `-1`, huge and non-numeric; negative, decimal and
  non-numeric prices; empty names; check-out before check-in; unicode names (`María José`, `李`).
- **State verification after rejected operations:** a failed update or delete must leave the
  booking exactly as it was, verified with a follow-up `GET`.
- **Contract testing:** JSON Schemas catch type changes or new fields that functional
  assertions would miss.
- **Documentation-based testing:** search filters are checked against what the API docs promise,
  which found two defects (BUG-011, BUG-012).

## Bugs found

14 defects are documented with the exact request, expected and actual result, severity and
priority in **[docs/BUG_REPORTS.md](docs/BUG_REPORTS.md)**. Highlights:

- **Critical:** decimal prices are silently truncated (`99.99` → `99`): money is lost without an error.
- **High:** invalid input causes `500` errors instead of `400`, and invalid data (negative prices,
  check-out before check-in, `"0NaN-aN-aN"` dates) is stored.
- **Medium:** both date filters contradict the API documentation.
- **Found while building the framework:** the default `Accept` header of REST Assured makes the API
  answer `418 I'm a Teapot` (BUG-014).

The same file lists **design observations**: documented behaviours that differ from REST
conventions (e.g. `DELETE` returns `201 Created`). The tests assert the documented contract and
the observations are reported separately, because not every surprise is a bug.

### How `@KnownIssue` works

```java
@Test
@KnownIssue(value = "BUG-004", description = "Negative total price is accepted")
void negative_price_is_rejected() {
  create(validBooking().withTotalprice(-50)).then().statusCode(400);
}
```

| API behaviour                | Test result                                              |
| ---------------------------- | -------------------------------------------------------- |
| Bug still present (200)      | ✅ Passes, with the failure attached to the report        |
| Bug fixed (400)              | ❌ Fails: "defect seems fixed, remove @KnownIssue"         |
| Setup broken / API down      | ❌ Fails as a real error (never hidden as "expected")      |

## CI/CD pipeline

```
push / PR / nightly / manual (by tag)
        │
        ▼
┌─────────────────┐     ┌──────────────────────────────────┐     ┌─────────────────────────┐
│ quality         │ ──▶ │ test                             │ ──▶ │ deploy (main only)      │
│ spotless:check  │     │ ./mvnw test (retries: 2)         │     │ Allure report           │
│ test-compile    │     │ Allure report + history + trends │     │ → GitHub Pages          │
└─────────────────┘     └──────────────────────────────────┘     └─────────────────────────┘
```

- **Retries only in CI:** the API is a shared public sandbox; retried tests show up as flaky in the
  report instead of hiding the problem.
- **History between runs:** the job downloads the history of the published report, so Allure shows
  trends of pass rate and duration over time.
- **Manual runs by tag:** *Actions → API Tests → Run workflow* accepts a tag (`smoke`, `regression`...).

## Design decisions

- **Every test owns its data.** The API is shared by everyone who practises with it and resets
  itself periodically, so no test depends on existing bookings. Tests create what they need and
  `BaseApiTest` deletes it after each test.
- **Clients return the raw `Response`.** Tests decide what to assert, including status codes of
  negative cases, instead of the client hiding errors.
- **Whole-object assertions.** Comparing the full `Booking` record catches any field that changed
  unexpectedly, not only the ones the test thought about.
- **Parallel by class, sequential inside a class.** Faster runs without tests of the same class
  competing with each other.
- **Configuration from the environment.** The same suite runs against another environment by
  changing one variable, and secrets can come from GitHub Secrets.

## Possible next steps

- Run the API locally with Docker (`mwinteringham/restfulbooker`) to test without depending on the
  public sandbox.
- Performance smoke tests with k6 or Gatling.
- Consumer-driven contract tests with Pact.

## Author

**Francisco Varela**, QA Automation Engineer

- LinkedIn: [linkedin.com/in/francisco-varela-qa](https://www.linkedin.com/in/francisco-varela-qa)
- GitHub: [@franv98dev](https://github.com/franv98dev)

## License

[MIT](LICENSE)

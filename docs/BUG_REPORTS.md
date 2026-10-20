# Bug Reports

Defects found in [Restful-Booker](https://restful-booker.herokuapp.com/apidoc/index.html) during
exploratory and automated testing. Each one is covered by a test in
[`KnownIssuesTest.java`](../src/test/java/io/github/franv98dev/booker/tests/knownissues/KnownIssuesTest.java)
annotated with `@KnownIssue`: the test asserts the **expected** behaviour, stays green while the
defect exists and fails as soon as it is fixed.

> Restful-Booker is a practice API and several of these defects are intentional. They are
> reported here the same way they would be in a real project.

**Environment:** `https://restful-booker.herokuapp.com` · `Content-Type: application/json` ·
`Accept: application/json` · credentials `admin` / `password123` (public).

| ID                  | Title                                                      | Area           | Severity | Priority |
| ------------------- | ---------------------------------------------------------- | -------------- | -------- | -------- |
| [BUG-001](#bug-001) | Failed login returns 200 OK instead of 401                 | Authentication | Medium   | Medium   |
| [BUG-002](#bug-002) | Missing required fields cause a 500 server error           | Validation     | High     | High     |
| [BUG-003](#bug-003) | A field with the wrong type causes a 500 server error      | Validation     | High     | High     |
| [BUG-004](#bug-004) | Negative total price is accepted                           | Validation     | High     | High     |
| [BUG-005](#bug-005) | Check-out date before check-in date is accepted            | Validation     | High     | High     |
| [BUG-006](#bug-006) | Invalid date is stored as `"0NaN-aN-aN"`                   | Data integrity | High     | High     |
| [BUG-007](#bug-007) | Non-numeric price is saved as `null`                       | Data integrity | High     | Medium   |
| [BUG-008](#bug-008) | Decimal prices are silently truncated                      | Data integrity | Critical | High     |
| [BUG-009](#bug-009) | Empty first and last names are accepted                    | Validation     | Medium   | Medium   |
| [BUG-010](#bug-010) | Update or delete of a missing booking returns 405, not 404 | Error handling | Low      | Low      |
| [BUG-011](#bug-011) | Check-in filter excludes bookings on the exact date        | Search         | Medium   | Medium   |
| [BUG-012](#bug-012) | Check-out filter returns bookings that end earlier         | Search         | Medium   | Medium   |
| [BUG-013](#bug-013) | XML responses are labeled `text/html`                      | Content type   | Low      | Low      |
| [BUG-014](#bug-014) | `Accept` header with several media types returns 418       | Content type   | Medium   | Medium   |

---

## BUG-001

**Failed login returns 200 OK instead of 401** · Severity: Medium

```http
POST /auth
{"username": "admin", "password": "wrong"}
```

- **Expected:** `401 Unauthorized`.
- **Actual:** `200 OK` with body `{"reason": "Bad credentials"}`. Clients that only check the
  status code treat a failed login as a success.

---

## BUG-002

**Missing required fields cause a 500 server error** · Severity: High

```http
POST /booking
{"lastname": "Diaz", "totalprice": 100, "depositpaid": true,
 "bookingdates": {"checkin": "2030-01-01", "checkout": "2030-01-05"}}
```

- **Expected:** `400 Bad Request` naming the missing field.
- **Actual:** `500 Internal Server Error`. The input error is reported as a server crash, and an
  empty body `{}` does the same.

---

## BUG-003

**A field with the wrong type causes a 500 server error** · Severity: High

```http
POST /booking
{"firstname": 123, "lastname": "Diaz", ...}
```

- **Expected:** `400 Bad Request`.
- **Actual:** `500 Internal Server Error`.

---

## BUG-004

**Negative total price is accepted** · Severity: High

```http
POST /booking
{"firstname": "Ana", "lastname": "Diaz", "totalprice": -50, ...}
```

- **Expected:** `400 Bad Request`.
- **Actual:** `200 OK`, and the booking is stored with `totalprice: -50`.

---

## BUG-005

**Check-out date before check-in date is accepted** · Severity: High

```http
POST /booking
{..., "bookingdates": {"checkin": "2030-05-10", "checkout": "2030-05-01"}}
```

- **Expected:** `400 Bad Request`.
- **Actual:** `200 OK`. The API stores a stay that ends before it starts.

---

## BUG-006

**Invalid date is stored as `"0NaN-aN-aN"`** · Severity: High

```http
POST /booking
{..., "bookingdates": {"checkin": "not-a-date", "checkout": "2030-05-01"}}
```

- **Expected:** `400 Bad Request`.
- **Actual:** `200 OK` and the stored check-in is `"0NaN-aN-aN"`, a corrupted value that breaks
  any client parsing dates.

---

## BUG-007

**Non-numeric price is saved as `null`** · Severity: High

```http
POST /booking
{..., "totalprice": "abc", ...}
```

- **Expected:** `400 Bad Request`.
- **Actual:** `200 OK`; the booking is stored with `totalprice: null`.

---

## BUG-008

**Decimal prices are silently truncated** · Severity: Critical

```http
POST /booking
{..., "totalprice": 99.99, ...}
```

- **Expected:** the price is stored as `99.99`, or the request is rejected if only whole numbers
  are allowed.
- **Actual:** `200 OK` and the price is stored as `99`. Money is lost without any error.

---

## BUG-009

**Empty first and last names are accepted** · Severity: Medium

```http
POST /booking
{"firstname": "", "lastname": "", ...}
```

- **Expected:** `400 Bad Request`.
- **Actual:** `200 OK`; a booking without a guest name is stored.

---

## BUG-010

**Update or delete of a missing booking returns 405, not 404** · Severity: Low

```http
PUT /booking/999999999      (valid token and body)
DELETE /booking/999999999   (valid token)
```

- **Expected:** `404 Not Found`, like `GET /booking/999999999`.
- **Actual:** `405 Method Not Allowed`, which suggests the method is not supported at all.

---

## BUG-011

**Check-in filter excludes bookings on the exact date** · Severity: Medium

The [API documentation](https://restful-booker.herokuapp.com/apidoc/index.html)
says the `checkin` filter returns bookings with a check-in date *greater than or equal to* the
given date.

1. Create a booking with `checkin: 2031-03-10`.
2. `GET /booking?firstname=<name>&checkin=2031-03-10`

- **Expected:** the booking is returned (equal date).
- **Actual:** `[]`. It only appears when filtering by an earlier date.

---

## BUG-012

**Check-out filter returns bookings that end earlier** · Severity: Medium

The documentation says the `checkout` filter returns bookings with a check-out date *greater than
or equal to* the given date.

1. Create a booking with `checkout: 2031-03-15`.
2. `GET /booking?firstname=<name>&checkout=2031-03-22`

- **Expected:** `[]`, because the booking ends before 2031-03-22.
- **Actual:** the booking is returned. The filter behaves as "less than or equal", the opposite of
  the documentation.

---

## BUG-013

**XML responses are labeled `text/html`** · Severity: Low

```http
GET /booking/{id}
Accept: application/xml
```

- **Expected:** `Content-Type: application/xml`.
- **Actual:** the body is XML but the header says `text/html; charset=utf-8`, so clients may parse
  or render it wrongly.

---

## BUG-014

**`Accept` header with several media types returns 418** · Severity: Medium

```http
GET /booking/{id}
Accept: application/json, application/javascript, text/javascript, text/json
```

- **Expected:** `200 OK` with JSON (the first acceptable type).
- **Actual:** `418 I'm a Teapot`. The same happens on `POST /booking`. This is the default header
  sent by REST Assured and other HTTP clients, so they fail out of the box. Found while building
  this framework; the tests work around it by sending `Accept: application/json` only.

---

## Design observations (documented, not counted as bugs)

These behaviours are what the documentation says, so the tests assert them as the contract. They
differ from common REST conventions and are worth raising with the API owners:

| Behaviour                               | Documented / actual | Common convention      |
| --------------------------------------- | ------------------- | ---------------------- |
| `POST /booking` success                 | `200 OK`            | `201 Created`          |
| `DELETE /booking/{id}` success          | `201 Created`       | `204 No Content` / 200 |
| `GET /ping` success                     | `201 Created`       | `200 OK`               |
| Write request with no or invalid token  | `403 Forbidden`     | `401 Unauthorized`     |
| Name search (`?firstname=`)             | Case-sensitive      | Usually case-insensitive |

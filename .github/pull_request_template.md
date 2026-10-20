## What does this PR change?

<!-- Short description of the tests or framework changes. -->

## Type of change

- [ ] New tests
- [ ] Framework (clients, models, schemas, support)
- [ ] Fix in existing tests
- [ ] CI / tooling

## Checklist

- [ ] `./mvnw spotless:apply test` passes locally
- [ ] New tests are tagged (`smoke`, `regression`, `contract`, `security`, `e2e`)
- [ ] Tests create their own data and clean it up (`givenBooking(...)`)
- [ ] New defects are documented in `docs/BUG_REPORTS.md` and covered with `@KnownIssue`

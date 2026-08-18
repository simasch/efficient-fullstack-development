# Task Management — Efficient Full-Stack Development

The example application for the book **Efficient Full-Stack Development** by Simon Martinelli.

The application is a task management system built with the stack the book teaches:

- [Spring Boot](https://spring.io/projects/spring-boot) as the foundation
- [Vaadin Flow](https://vaadin.com) for the user interface — the whole UI is written in Java
- [jOOQ](https://www.jooq.org) for type-safe database access, generated from the real schema
- [PostgreSQL](https://www.postgresql.org) with [Flyway](https://flywaydb.org) migrations
- [Testcontainers](https://testcontainers.com) for code generation and integration testing
- Spring Security with stateless JWT authentication

The project is based on the [vaadin-jooq-template](https://github.com/martinellich/vaadin-jooq-template).
Chapter 5 of the book builds this application step by step; the other chapters use parts of it as their examples.

## Prerequisites

- Java 25 (Java 21 or newer works)
- [Docker](https://www.docker.com) — Testcontainers starts throwaway PostgreSQL containers for the jOOQ code
  generation and the integration tests

## Running the Application

The jOOQ metamodel has to be generated once before the first run (and after every schema change):

    ./mvnw compile

There is no database to install or start: the application runs against a PostgreSQL container started by
Spring Boot's Testcontainers support. Run `TestApplication` from your IDE, or from the command line:

    ./mvnw spring-boot:test-run

Log in with `admin`/`admin`, `alice`/`alice` or `bob`/`bob`.

If you want to run against your own PostgreSQL instead, point the `spring.datasource.*` properties at it and use
`./mvnw spring-boot:run`.

## Testing the Application

    ./mvnw verify

There are two base classes for UI tests:

- `AbstractBrowserlessTest` for fast [browserless testing](https://vaadin.com/docs/latest/flow/testing/browserless)
  (UI unit tests without a browser) — see Chapter 6 of the book
- `PlaywrightIT` for end-to-end tests with [Playwright](https://playwright.dev) and
  [Drama Finder](https://github.com/parttio/dramafinder) — see Chapter 7 of the book

## Building for Production

    ./mvnw clean package -Pproduction

Deployment is covered in Chapter 9 of the book.

## Code Quality

The build enforces [Spring Java Format](https://github.com/spring-io/spring-javaformat) (run
`./mvnw spring-javaformat:apply` before committing), ErrorProne with NullAway, and an ArchUnit test that guards
the architecture.

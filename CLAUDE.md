# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is the companion application for the book "Efficient Full-Stack Development": a task management system
that integrates Vaadin Flow with jOOQ on Spring Boot. It is based on the
[vaadin-jooq-template](https://github.com/martinellich/vaadin-jooq-template). Chapter 5 of the book builds it
step by step and extracts its code listings from this repository — keep listings and code in sync when changing
either.

## Technology Stack

- **Backend**: Java 25, Spring Boot 4.1, jOOQ 3.21, PostgreSQL
- **Frontend**: Vaadin 25 (Flow)
- **Database**: PostgreSQL with Flyway migrations (`src/main/resources/db/migration`, `V1__…` naming)
- **Security**: Spring Security with stateless JWT (`VaadinSecurityConfigurer` + `VaadinStatelessSecurityConfigurer`)
- **Testing**: JUnit 5, Vaadin Browserless Testing, Playwright + Mopo, Testcontainers
- **Code Quality**: ErrorProne, NullAway, Spring Java Format, ArchUnit, JaCoCo

## Development Commands

```bash
# Generate the jOOQ metamodel (required before first build and after schema changes; needs Docker)
./mvnw compile

# Run the application (starts PostgreSQL via Testcontainers - no local database needed)
./mvnw spring-boot:test-run
# ...or run TestApplication from the IDE

# All tests
./mvnw verify

# Format code (the build fails on format violations)
./mvnw spring-javaformat:apply
```

## Architecture

### Package Structure
```
ch.martinelli.tm/
├── domain/                # Shared domain types: enums, records (TaskStatus, Task, TaskFilter, …)
├── db/converter/          # Hand-written jOOQ converters; generated code lands in ch.martinelli.tm.db
├── core/
│   ├── configuration/     # TaskManagementJooqConfiguration (optimistic locking etc.)
│   ├── domain/            # User management: UserRepository, UserService, UserDetailsServiceImpl
│   ├── security/          # SecurityConfiguration (JWT), SecurityContext
│   └── ui/                # MainLayout, LoginView, NotFoundView, Notifier, i18n
├── dashboard/ui/          # DashboardView
├── task/
│   ├── domain/            # TaskRepository (plain DSLContext), TaskService (business rules)
│   └── ui/                # TaskListView, TaskGrid, TaskFilterBar, TaskForm, TaskEditorDialog, TaskDetailView
├── project/
│   ├── domain/            # ProjectRepository (MULTISET overview), ProjectService
│   └── ui/                # ProjectListView, ProjectLayout (route scope), ProjectTasksView
└── user/ui/               # UserView (admin only)
```

### Rules the ArchUnit test enforces
- Only `..ui..` and `..security..` packages may access Vaadin classes.
- `..domain..` may only be accessed by the UI, security, and generated database layers.
- `core` may not access the feature modules (task, project, dashboard, user).

### Conventions
- Repositories are hand-written classes around `DSLContext` — no DAO framework. Dynamic filters use the
  `noCondition()` pattern; grid projections are records mapped with `Records.mapping(...)`.
- The `task` table uses optimistic locking (`version` column). UI round trips check the version explicitly in
  the UPDATE; `record.store()` relies on jOOQ's `executeWithOptimisticLocking`.
- Views expose package-private component fields for browserless tests, and forms are usable standalone
  (outside dialogs).
- Schema changes: new Flyway migration + `./mvnw compile` to regenerate jOOQ classes. The `V1` schema is
  printed in Chapter 4 of the book and must not be edited — append migrations instead.
- Null safety: main packages are `@NullMarked` (JSpecify); NullAway runs in the build.

## Book Cross-References

- Chapter 3: Vaadin — view/component shapes (`TaskListView` orchestration, `TaskForm`, events)
- Chapter 4: jOOQ — schema, codegen setup, `TaskRepository`, Testcontainers tests
- Chapter 5: builds this application (security is new material there)
- Chapter 6: browserless UI testing (`AbstractBrowserlessTest`)
- Chapter 7: Playwright E2E (`PlaywrightIT`)
- Chapter 9: deployment (production build, Fly.io, Neon)

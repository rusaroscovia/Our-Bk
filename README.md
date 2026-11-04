# BK Breakfast Management System

BK is a small web application for managing employee breakfast contributions,
recording actual breakfast expenses, and collecting a daily breakfast vote.
The backend uses Java 21 and Spring Boot, with Thymeleaf templates and Bootstrap
for the web interface. PostgreSQL stores the application's data.

## Step 1: Project foundation

The Maven project, Spring Boot entry point, and responsive Thymeleaf welcome
page are in place.

## Step 2: PostgreSQL configuration

Spring Boot connects to PostgreSQL using `DB_URL`, `DB_USERNAME`, and
`DB_PASSWORD`. The optional `.env` file keeps local settings out of source
control. Hibernate updates the development schema as entities are added; use a
reviewed schema migration strategy before deploying to production.

To start PostgreSQL with Docker Compose, copy `.env.example` to `.env`, set a
local database password, and run:

```shell
docker compose up -d db
```

Then start BK with Java 21 and Maven:

```shell
mvn spring-boot:run
```

Open [http://localhost:8080](http://localhost:8080). If PostgreSQL is already
installed locally, configure the same environment variables or `.env` file and
skip Docker Compose. Do not commit `.env` or use development credentials in
production.

## Step 3: Entities and repositories

The JPA model includes the five main tables: `products`, `employees`,
`payments`, `expenses`, and `votes`. A small `app_settings` key/value table is
also included for settings such as public report visibility.

- Product names are restricted to Bread, Chapati, and Andazi.
- RWF prices, contributions, and expenses are stored as whole-number amounts.
- Employees have a normalized name for case-insensitive duplicate checks.
- Votes are unique by normalized name and voting date at the database level.
- Payment and expense repositories support date-range history queries.

## Step 4: Business services

The service layer validates employee names, product prices, payments, expenses,
and voting inputs. On startup it creates the three default product records
(Bread 1,700 RWF; Chapati 200 RWF; Andazi 200 RWF) only when missing, so later
admin price changes are preserved.

Financial summaries are calculated from recorded payments and expenses;
votes never affect the balance. Daily voting is closed by default, blocks
duplicate normalized names per date, and returns all tied winners or no winner
when no votes were cast. Report publication is off by default. Names are not
identity verification: anyone can vote using another person's name, and the
daily database constraint only limits repeated name entries.

## Step 5: Admin controllers and login

Admin pages and form actions are under `/admin/**` and require the `ADMIN`
role. Spring Security provides a form login at `/admin/login`, hashes the
configured password with BCrypt, and keeps CSRF protection enabled for forms.
Configure `ADMIN_USERNAME` and a strong `ADMIN_PASSWORD` in the ignored `.env`
file before starting the application. The app deliberately refuses to start
without both credentials rather than enabling a default admin password.

The dashboard supports employee registration and renaming, monthly payment
and expense recording, product price updates, voting open/close, report
publishing, and monthly financial history. The overview, settings, and
financial records are all behind admin authentication. The employee voting
page and published-report view are available at `/vote`.

## Step 6: Public voting controller

Employees can open `/vote` without creating an account, submit a name and one of
the three fixed options, and view today's counts and winning option. Voting is
available only while the administrator has opened it. The page explains that
name-based voting cannot verify identity.

The public controller checks the report-publishing setting before loading any
financial summary, payment, or expense records. Unpublished financial data is
not added to the public page model; published reports show the current month's
totals and records.

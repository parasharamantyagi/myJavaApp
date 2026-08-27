# myJavaApp

My first Spring Boot project — a Java web application built with Gradle.

| What | Version |
|------|---------|
| Project | Gradle - Groovy |
| Language | Java |
| Java | 21 |
| Spring Boot | 4.0.8 |
| Packaging | Jar |
| Configuration | YAML |
| Gradle | 9.7.1 (via `./gradlew`, no install needed) |
| Database | PostgreSQL 14 (Spring Data JPA) |
| Web server | Embedded Tomcat 11 (port 8080) |


## Gradle command reference

Everything you run day to day. All commands are from the project root.

### Running
```bash
./gradlew bootRun  # Run the app in development mode |
./gradlew classes --continuous # Recompile on every file save (drives DevTools live reload) |
./gradlew clean bootRun # Same, but wipes `build/` first — **use this after editing `build.gradle`** |
./gradlew classes # Recompile once, manually |
./gradlew --stop # The Gradle **build daemon** only — **does not stop your app** |
```
### Stopping

These are three different things and people mix them up:

| Command | Stops |
|---------|-------|
| `Ctrl + C` | Your app — the normal way, in the terminal running `bootRun` |
| `pkill -f 'myJavaApp.*bootRun'` | Your app, when the terminal is gone or the process detached |
| `lsof -ti:8080 \| xargs kill` | Whatever holds port 8080 (same result, by port instead of name) |

> forks the app into a separate JVM, so the app keeps serving on 8080 afterwards.
> Use `Ctrl + C` or `pkill` for the app itself.

Confirm the app is actually down:

```bash
lsof -i:8080                             # no output = port free
curl http://localhost:8080/hello         # expect: Connection refused
```

If the browser still shows the page after this, that's browser cache — hard-refresh
with `Ctrl + Shift + R`. Trust `curl`, not the browser.

### Building and inspecting

| Command | What it does |
|---------|--------------|
| `./gradlew test` | Run the tests |
| `./gradlew clean bootJar` | Build the runnable jar into `build/libs/` |
| `./gradlew build` | Compile + test + build the jar |
| `./gradlew bootBuildImage` | Build a Docker image without a Dockerfile |
| `./gradlew build --refresh-dependencies` | Re-resolve all dependencies |
| `./gradlew dependencies --configuration runtimeClasspath` | Show what's actually on the runtime classpath |
| `./gradlew tasks` | List every available task |
| `java -jar build/libs/myJavaApp-0.0.1-SNAPSHOT.jar` | Run the built jar |

Dependencies — click ADD DEPENDENCIES (Ctrl+B) 8 times

┌──────────────────────┬─────────────────┬────────────────────────────────────────────┐
│      Search for      │  Group in list  │                  Produces                  │
├──────────────────────┼─────────────────┼────────────────────────────────────────────┤
│ Spring Web           │ Web             │ spring-boot-starter-webmvc                 │
├──────────────────────┼─────────────────┼────────────────────────────────────────────┤
│ HTTP Client          │ Web             │ spring-boot-starter-restclient             │
├──────────────────────┼─────────────────┼────────────────────────────────────────────┤
│ Spring Data JPA      │ SQL             │ spring-boot-starter-data-jpa               │
├──────────────────────┼─────────────────┼────────────────────────────────────────────┤
│ PostgreSQL Driver    │ SQL             │ postgresql (runtimeOnly)                   │
├──────────────────────┼─────────────────┼────────────────────────────────────────────┤
│ Flyway Migration     │ SQL             │ spring-boot-starter-flyway                 │
├──────────────────────┼─────────────────┼────────────────────────────────────────────┤
│ Validation           │ I/O             │ spring-boot-starter-validation             │
├──────────────────────┼─────────────────┼────────────────────────────────────────────┤
│ Lombok               │ Developer Tools │ lombok (compileOnly + annotationProcessor) │
├──────────────────────┼─────────────────┼────────────────────────────────────────────┤
│ Spring Boot DevTools │ Developer Tools │ spring-boot-devtools (developmentOnly)     │
└──────────────────────┴─────────────────┴────────────────────────────────────────────┘

## Project structure

```
myJavaApp/
├── build.gradle                 # dependencies and build config
├── settings.gradle              # project name
├── gradlew / gradlew.bat        # Gradle wrapper (use this, never a global gradle)
├── src/
│   ├── main/
│   │   ├── java/demo/com/example/myJavaApp/
│   │   │   ├── MyJavaAppApplication.java   # entry point
│   │   │   └── web/HelloController.java    # REST endpoints
│   │   └── resources/
│   │       ├── application.yaml            # all app settings
│   │       └── db/migration/               # Flyway SQL migrations
│   └── test/java/demo/com/example/myJavaApp/
├── build/                       # generated output (not in git)
└── bin/                         # VS Code Java output — ignore, not used by Gradle
```


## Table of contents

1. [Prerequisites](#prerequisites)
2. [Project structure](#project-structure)
3. [Gradle command reference](#gradle-command-reference)
4. [Managing dependencies](#managing-dependencies)
5. [Step 1 — Database](#step-1--database)
6. [Step 2 — Configuration](#step-2--configuration)
7. [Step 3 — The endpoint](#step-3--the-endpoint)
8. [Step 4 — Run the app](#step-4--run-the-app)
9. [Step 5 — Live reload](#step-5--live-reload)
10. [Step 6 — Tests](#step-6--tests)
11. [Step 7 — Build and deploy](#step-7--build-and-deploy)
12. [Troubleshooting](#troubleshooting)


## Prerequisites

```bash
java -version      # must show 21 or newer
docker --version   # only needed if you don't already have PostgreSQL
git --version
```

Gradle does **not** need installing — `./gradlew` downloads the right version itself.


## Managing dependencies

There is no `npm i` equivalent in Java. Dependencies are declared by hand-editing
`build.gradle` — that file is this project's `package.json`.

`node_modules/` has no equivalent — jars live in a shared `~/.gradle/caches/`.
To add something, put a line inside the `dependencies { }` block:

```groovy
dependencies {
	implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
	implementation 'org.springframework.boot:spring-boot-starter-webmvc'
	implementation 'org.springframework.boot:spring-boot-starter-validation'
	implementation 'org.springframework.boot:spring-boot-starter-restclient'
	implementation 'org.flywaydb:flyway-core'
	implementation 'org.flywaydb:flyway-database-postgresql'
	compileOnly 'org.projectlombok:lombok'
	annotationProcessor 'org.projectlombok:lombok'
	developmentOnly 'org.springframework.boot:spring-boot-devtools'
	runtimeOnly 'org.postgresql:postgresql'
	// test dependencies below…
}
```

No version numbers — the `io.spring.dependency-management` plugin pins them to
whatever Spring Boot 4.0.8 declares. Only write an explicit version for libraries
Boot doesn't manage.

Then **fully stop the app** and run `./gradlew clean bootRun`. DevTools cannot
hot-reload a newly added jar.

Verify a dependency actually resolved:

```bash
./gradlew dependencies --configuration runtimeClasspath | grep -i flyway
./gradlew bootJar && unzip -l build/libs/*.jar | grep -i flyway
```

What the scopes mean:

| Scope | Meaning |
|-------|---------|
| `implementation` | needed to compile and run |
| `runtimeOnly` | needed at runtime only (JDBC drivers) |
| `compileOnly` | needed to compile only (Lombok annotations) |
| `annotationProcessor` | code generators that run at compile time |
| `developmentOnly` | excluded from the built jar (DevTools) |

---

## Step 1 — Database

Spring Boot **refuses to start without a database** because `spring-boot-starter-data-jpa`
and the PostgreSQL driver are on the classpath.

This project uses an existing local PostgreSQL on **port 5432**, database **`localJava`**,
managed through pgAdmin (server `local`). If that's your setup, nothing to do here.

If you need a fresh one instead, Docker is one command:

```bash
docker run -d --name myjavaapp-postgres \
  -e POSTGRES_DB=localJava \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:17
```

Pick a different host port (`-p 5434:5432`) if 5432 is taken, and update
`application.yaml` to match. Multiple PostgreSQL instances on one machine is a common
trap — a *successful* connection isn't proof of a *correct* one. Cross-check the JDBC
URL in the startup log against pgAdmin (right-click server → Properties → Connection).

```bash
docker ps                                            # container should be "Up"
docker exec myjavaapp-postgres pg_isready -U postgres
docker stop  myjavaapp-postgres    # stop, data kept
docker start myjavaapp-postgres    # start again
docker rm -f myjavaapp-postgres    # delete completely, data lost
```

---

## Step 2 — Configuration

`src/main/resources/application.yaml`:

```yaml
spring:
  application:
    name: myJavaApp

  datasource:
    url: jdbc:postgresql://localhost:5432/localJava
    username: postgres
    password: postgres

  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false

  flyway:
    enabled: true
    locations: classpath:db/migration

  devtools:
    livereload:
      enabled: true    # OFF by default in Spring Boot 4

server:
  port: 8080
```

| Setting | Meaning |
|---------|---------|
| `datasource.url` | Must match the host port your database listens on |
| `ddl-auto: validate` | Hibernate checks entities against the real schema and fails loudly on a mismatch. Schema changes belong in Flyway migrations. Use `update` only for throwaway experiments — never in production |
| `open-in-view: false` | Avoids holding database sessions open during view rendering |
| `flyway.locations` | Where migration SQL lives — the default, listed here for clarity |

### Flyway migrations

Filenames are strict: `V<number>__<description>.sql` with **two** underscores.
`V1_create.sql`, `v1__create.sql`, and `V1__create.SQL` are all ignored silently.

```
src/main/resources/db/migration/
└── V1__create_question.sql
```

Every schema change is a **new file** — never edit an applied one. Flyway stores a
checksum per migration in `flyway_schema_history` and fails validation if an old file
changes, which guarantees every environment ran identical SQL.

---

## Step 3 — The endpoint

`src/main/java/demo/com/example/myJavaApp/web/HelloController.java`

```java
package demo.com.example.myJavaApp.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class HelloController {

	@GetMapping("/hello")
	public String hello() {
		return "Hello from myJavaApp!";
	}

	@PostMapping("/items")
	public ResponseEntity<Item> create(@Valid @RequestBody CreateItemRequest req) {
		Item saved = new Item(1L, req.name(), req.price());
		return ResponseEntity.status(HttpStatus.CREATED).body(saved);
	}
}
```

- `@RequestBody` — Jackson deserializes JSON into the record before your method
  runs; there's no `req.body` step like Express. Read fields with `req.name()`, not
  `req.getName()` — records name accessors after their components
- `@Valid` — runs constraints *before* the method body; on failure Spring returns
  400 and your code never executes
- Returning an object or record produces JSON; returning a `String` produces
  `text/plain`, which is why `/hello` isn't JSON

Request DTOs are records with constraints:

```java
public record CreateItemRequest(
		@NotBlank(message = "name is required") String name,
		@Positive(message = "price must be greater than 0") BigDecimal price
) {}
```

First run takes a few minutes while dependencies download. Wait for:

```
Tomcat started on port 8080 (http) with context path '/'
Started MyJavaAppApplication in 1.927 seconds
```

Test it:

```bash
curl http://localhost:8080/hello
# Hello from myJavaApp!

curl -X POST http://localhost:8080/items \
  -H "Content-Type: application/json" \
  -d '{"name":"Wheelchair","price":125.50}'
# {"id":1,"name":"Wheelchair","price":125.50}
```

The `Content-Type` header is mandatory on POST — omit it and you get 415.

Stopping the app is covered in the [Gradle command reference](#stopping).

---

## Step 5 — Live reload

`spring-boot-devtools` restarts the app when compiled classes change. It watches
`build/classes`, so restarts trigger on **recompile**, not on save.


**Option B — IntelliJ.** Enable once:

1. `Settings → Build, Execution, Deployment → Compiler` → **Build project automatically**
2. `Settings → Advanced Settings` → **Allow auto-make to start even if the developed application is currently running**

**Option C — manual.** Run `./gradlew classes` when you want changes picked up.

Restarts take ~0.3s versus ~2s, because DevTools reloads only your classes.

### Browser auto-refresh

DevTools runs a LiveReload server that refreshes the browser after each restart.

1. Set `spring.devtools.livereload.enabled: true` (Step 2) — off by default in Boot 4
2. Verify it's up: `curl -s -o /dev/null -w "%{http_code}\n" http://localhost:35729/livereload.js` → expect `200`
3. Install the [LiveReload extension](https://chromewebstore.google.com/detail/livereload/jnihajbhpnppcggbcgedagnkighmdlei)
   (Chrome or Firefox) and click its icon on your page — solid centre dot means connected

> LiveReload refreshes the **page**; it does not hot-swap Java code. Changes to
> `build.gradle` or significant `application.yaml` edits need a full stop and
> `./gradlew clean bootRun`.

---

## Step 6 — Tests

```bash
./gradlew test
```

`MyJavaAppApplicationTests.contextLoads()` boots the whole Spring context, so the
database must be running or it fails with the same `DataSource` error. HTML report
lands in `build/reports/tests/test/index.html`.

---

## Step 7 — Build and deploy

### Runnable jar

```bash
./gradlew clean bootJar
ls -lh build/libs/       # myJavaApp-0.0.1-SNAPSHOT.jar (~54 MB, includes Tomcat)
```

Never hard-code production credentials in `application.yaml` — environment
variables override the file:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/localJava \
SPRING_DATASOURCE_USERNAME=postgres \
SPRING_DATASOURCE_PASSWORD=secret \
SERVER_PORT=9090 \
java -jar build/libs/myJavaApp-0.0.1-SNAPSHOT.jar
```

Mapping rule: `spring.datasource.url` → `SPRING_DATASOURCE_URL` (uppercase, dots and
dashes to underscores).

### Docker image

`Dockerfile` in the project root:

```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY build/libs/myJavaApp-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
```

```bash
./gradlew clean bootJar
docker build -t myjavaapp:latest .

docker run -d --name myjavaapp \
  --add-host=host.docker.internal:host-gateway \
  -p 8081:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/localJava \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgres \
  myjavaapp:latest

docker logs -f myjavaapp
docker rm -f myjavaapp
```

Inside a container `localhost` means the container itself — hence
`host.docker.internal` for a database on your machine. Or skip the Dockerfile
entirely with `./gradlew bootBuildImage`.

### As a service

On Linux, run the jar under `systemd` so it restarts on crash and starts on boot.
Create `/etc/systemd/system/myjavaapp.service` with `ExecStart=/usr/bin/java -jar
/opt/myjavaapp/app.jar`, the `SPRING_DATASOURCE_*` values as `Environment=` lines,
`SuccessExitStatus=143` and `Restart=always`. Then:

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now myjavaapp
sudo journalctl -u myjavaapp -f
```

> DevTools is `developmentOnly` in `build.gradle`, so it's excluded from the jar —
> no live reload in production, which is what you want.

---

## Troubleshooting

| Symptom | Cause | Fix |
|---------|-------|-----|
| `Failed to configure a DataSource: 'url' attribute is not specified` | No database configured | Steps 1 and 2 |
| `Connection to localhost:5432 refused` | Database not running | Start PostgreSQL / `docker start` |
| `FATAL: database "localJava" does not exist` | Wrong port or database name | Cross-check `application.yaml` against pgAdmin's connection properties |
| `Port 8080 was already in use` | App already running | See [Stopping](#stopping) |
| `Whitelabel Error Page` / 404 | No endpoint for that URL | The path is `/hello`, not `/` |
| 415 Unsupported Media Type on POST | Missing header | Add `-H "Content-Type: application/json"` |
| Code changes don't appear | Classes not recompiled | `./gradlew classes`, or Option A/B in Step 5 |
| Browser doesn't auto-refresh | LiveReload off or extension not connected | Step 5 |
| New dependency has no effect | DevTools restart doesn't load new jars | Fully stop, then `./gradlew clean bootRun` |
| `Schema-validation: missing column [...]` | Entity and migration disagree | Align the SQL with the `@Entity`, or add a new `V<n>__` migration |

### No Flyway output in the startup log

Flyway auto-configuration is gated on `@ConditionalOnClass(Flyway.class)`. If Flyway
isn't on the classpath the whole `spring.flyway.*` block is **ignored silently** —
config looks right, does nothing. Check all three:

```bash
find ~/.gradle/caches -name "flyway-core*.jar" | head -1
./gradlew dependencies --configuration runtimeClasspath | grep -i flyway
./gradlew bootJar && unzip -l build/libs/*.jar | grep -i flyway
```

If Flyway *is* present but still silent, make the auto-configuration explain itself —
look for `FlywayAutoConfiguration` under `Negative matches`:

Boot 4 restructured its starters, so `spring-boot-starter-flyway` may be the intended
artifact rather than bare `flyway-core`. To rule out your SQL and connection entirely,
run migrations without Spring via the `org.flywaydb.flyway` Gradle plugin
(`./gradlew flywayMigrate flywayInfo`).

### `V1` fails with `relation "question" already exists`

A table left over from a previous `ddl-auto: update` run. Either drop it and let
Flyway own the schema:

```sql
DROP TABLE IF EXISTS question;
```

…or keep the data and have Flyway adopt the current state as its baseline:

```yaml
spring:
  flyway:
    baseline-on-migrate: true
```

### pgAdmin doesn't show a new table

Right-click **Schemas → public → Tables → Refresh** specifically — refreshing the
database node isn't enough. And confirm the query tab is on the right database;
pgAdmin keeps a separate connection per tab:

```sql
SELECT current_database();
SELECT * FROM flyway_schema_history;
```

---

## Where to go next

- `@Entity` + `JpaRepository` to persist data
- `@RestControllerAdvice` global exception handler for consistent `ProblemDetail` responses
- Layered packages: `controller` / `service` / `repository` / `dto` / `entity`
- `spring-boot-starter-actuator` for `/actuator/health`
- springdoc-openapi for Swagger UI; Testcontainers so tests don't need a live database
- `application-dev.yaml` / `application-prod.yaml` profiles, secrets from env vars only
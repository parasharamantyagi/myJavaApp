# myJavaApp

My first Spring Boot project — a Java web application built with Gradle.

| What | Version |
|------|---------|
| Java | 21 |
| Spring Boot | 4.0.8 |
| Gradle | 9.7.1 (via `./gradlew`, no install needed) |
| Database | PostgreSQL (Spring Data JPA) |
| Web server | Embedded Tomcat 11 (port 8080) |

---

## Table of contents

1. [Prerequisites](#1-prerequisites)
2. [Project structure](#2-project-structure)
3. [Step 1 — Start a PostgreSQL database](#step-1--start-a-postgresql-database)
4. [Step 2 — Configure the database in the app](#step-2--configure-the-database-in-the-app)
5. [Step 3 — Add a page to see in the browser](#step-3--add-a-page-to-see-in-the-browser)
6. [Step 4 — Run the app](#step-4--run-the-app)
7. [Step 5 — Live reload: see code changes instantly in the browser](#step-5--live-reload-see-code-changes-instantly-in-the-browser)
8. [Step 6 — Run the tests](#step-6--run-the-tests)
9. [Step 7 — Build and deploy](#step-7--build-and-deploy)
10. [Common errors and fixes](#common-errors-and-fixes)
11. [Command cheat sheet](#command-cheat-sheet)

---

## 1. Prerequisites

Install these once, then check each one:

```bash
java -version      # must show 21 (or newer)
docker --version   # used to run PostgreSQL
git --version
```

You do **not** need to install Gradle. The project ships with the Gradle wrapper
(`./gradlew`), which downloads the correct Gradle version by itself on first use.

---

## 2. Project structure

```
myJavaApp/
├── build.gradle                 # dependencies and build config
├── settings.gradle              # project name
├── gradlew / gradlew.bat        # Gradle wrapper (use this, not a global gradle)
├── src/
│   ├── main/
│   │   ├── java/demo/com/example/myJavaApp/
│   │   │   └── MyJavaAppApplication.java   # entry point (main method)
│   │   └── resources/
│   │       └── application.yaml            # all app settings live here
│   └── test/
│       └── java/demo/com/example/myJavaApp/
│           └── MyJavaAppApplicationTests.java
└── build/                       # generated output (not in git)
```

---

## Step 1 — Start a PostgreSQL database

This project includes `spring-boot-starter-data-jpa` and the PostgreSQL driver.
Because of that, **Spring Boot refuses to start without a database**. If you skip
this step you will see:

```
APPLICATION FAILED TO START
Failed to configure a DataSource: 'url' attribute is not specified
```

The easiest way to get a database is Docker — one command, nothing to install:

```bash
docker run -d --name myjavaapp-postgres \
  -e POSTGRES_DB=myjavaapp \
  -e POSTGRES_USER=myjavaapp \
  -e POSTGRES_PASSWORD=secret \
  -p 5434:5432 \
  postgres:17
```

Check it is up and accepting connections:

```bash
docker ps                                        # container should be "Up"
docker exec myjavaapp-postgres pg_isready -U myjavaapp
# expected: /var/run/postgresql:5432 - accepting connections
```

> **Why port 5434 and not the usual 5432?** On this machine both `5432` and
> `5433` are already taken by other PostgreSQL instances (`5432` by the
> `local-env-database-1` container). The `-p 5434:5432` above means "reach it on
> 5434 from my machine, it still listens on 5432 inside the container", and
> `application.yaml` is already set to `5434` to match. On a clean machine you can
> use `-p 5432:5432` and change the URL in `application.yaml` back to `5432`.

Useful database commands:

```bash
docker stop myjavaapp-postgres     # stop it (data is kept)
docker start myjavaapp-postgres    # start it again
docker rm -f myjavaapp-postgres    # delete it completely (data is lost)
```

---

## Step 2 — Configure the database in the app

This is **already done** in the repo — `src/main/resources/application.yaml` looks
like this. Change the username, password and port here if you used different ones
in Step 1:

```yaml
spring:
  application:
    name: myJavaApp

  datasource:
    url: jdbc:postgresql://localhost:5434/myjavaapp
    username: myjavaapp
    password: secret

  jpa:
    hibernate:
      ddl-auto: update      # auto-create/update tables from your @Entity classes
    open-in-view: false

  # Needed for browser auto-refresh in Step 5.
  # In Spring Boot 4 this is OFF by default.
  devtools:
    livereload:
      enabled: true

server:
  port: 8080
```

What each part means:

| Setting | Meaning |
|---------|---------|
| `datasource.url` | Where the database is. Must match the host port you published in Step 1 (`5434` here). |
| `ddl-auto: update` | Hibernate creates/updates tables for you. Great for learning; **do not use in production** — use a migration tool such as Flyway or Liquibase there. |
| `open-in-view: false` | Recommended default; avoids keeping database sessions open during view rendering. |
| `server.port` | The port your app serves on — `http://localhost:8080`. |

---

## Step 3 — The page you'll see in the browser

Without a web endpoint a browser would only show a 404 error page, so the project
includes one **already**:

`src/main/java/demo/com/example/myJavaApp/web/HelloController.java`

```java
package demo.com.example.myJavaApp.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

	@GetMapping("/hello")
	public String hello() {
		return "Hello from myJavaApp!";
	}
}
```

- `@RestController` — tells Spring this class handles web requests and returns data directly.
- `@GetMapping("/hello")` — this method answers `GET http://localhost:8080/hello`.

---

## Step 4 — Run the app

```bash
./gradlew bootRun
```

First run takes a few minutes (Gradle and all dependencies are downloaded).
Wait for this line:

```
Tomcat started on port 8080 (http) with context path '/'
Started MyJavaAppApplication in 2.365 seconds
```

Now open in your browser:

**http://localhost:8080/hello**

You should see: `Hello from myJavaApp!`

Or test from the terminal:

```bash
curl http://localhost:8080/hello
```

**To stop the app:** press `Ctrl + C` in the terminal running it.

---

## Step 5 — Live reload: see code changes instantly in the browser

The project already includes `spring-boot-devtools`, which restarts the app
automatically whenever the compiled classes change. There are two levels:

### 5a. Automatic app restart (code changes take effect without a manual restart)

DevTools watches the `build/classes` folder — so a restart happens when your code
is **recompiled**, not when you merely save the file. Set up one of these:

**Option A — terminal only (works everywhere).** Keep two terminals open:

```bash
# Terminal 1 — runs the app, leave it running
./gradlew bootRun

# Terminal 2 — recompiles automatically every time you save a file
./gradlew classes --continuous
```


# Terminal 2 — recompiles automatically every time you save a file
./gradlew --stop
pkill -f 'myJavaApp.*bootRun'

**Option B — inside IntelliJ IDEA.** Enable auto-compile once, then just save:

1. `Settings → Build, Execution, Deployment → Compiler` → check **Build project automatically**
2. `Settings → Advanced Settings` → check **Allow auto-make to start even if the developed application is currently running**
3. Run the app, then edit and save — IntelliJ compiles and DevTools restarts.

**Option C — manual trigger.** In a second terminal run `./gradlew classes`
whenever you want your changes picked up.

**Try it:**

1. With the app running, change the message in `HelloController.java` to
   `"Hello again, live reloaded!"` and save.
2. Recompile (automatic with Option A/B, or run `./gradlew classes`).
3. In the app's terminal you will see a fast restart:
   ```
   Started MyJavaAppApplication in 0.284 seconds
   ```
4. Refresh **http://localhost:8080/hello** — the new text is there.

Restarts are much faster than a full startup (~0.3s vs ~2.5s) because DevTools
reloads only your classes, not the libraries.

### 5b. Automatic browser refresh (you don't even press F5)

DevTools also runs a **LiveReload** server that tells the browser to refresh
itself after each restart.

1. Make sure `spring.devtools.livereload.enabled: true` is in your
   `application.yaml` (Step 2) — **in Spring Boot 4 this is disabled by default**.
2. Restart the app, then verify the LiveReload server is up:
   ```bash
   curl -s -o /dev/null -w "%{http_code}\n" http://localhost:35729/livereload.js
   # expected: 200
   ```
3. Install the **LiveReload** browser extension
   ([Chrome](https://chromewebstore.google.com/detail/livereload/jnihajbhpnppcggbcgedagnkighmdlei),
   also available for Firefox) and click its toolbar icon on your page so the
   centre dot turns solid — that means it is connected.
4. Now edit code → save → recompile. The browser tab refreshes on its own.

> LiveReload refreshes the **page**; it does not hot-swap Java code. Bigger
> changes (adding dependencies in `build.gradle`, changing `application.yaml`
> heavily) still need a full stop and `./gradlew bootRun`.

---

## Step 6 — Run the tests

```bash
./gradlew test
```

`MyJavaAppApplicationTests.contextLoads()` starts the whole Spring context, so
**the database from Step 1 must be running** or the test will fail with the same
`DataSource` error. Expected output:

```
BUILD SUCCESSFUL
```

The HTML report is written to `build/reports/tests/test/index.html`.

---

## Step 7 — Build and deploy

### 7a. Build a runnable jar

```bash
./gradlew clean bootJar
ls -lh build/libs/
# myJavaApp-0.0.1-SNAPSHOT.jar   (~54 MB — app + all libraries + Tomcat inside)
```

`./gradlew build` does the same plus running the tests.

### 7b. Run the jar anywhere Java 21 is installed

Never hard-code production passwords in `application.yaml` — pass them as
environment variables, which override the file:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5434/myjavaapp \
SPRING_DATASOURCE_USERNAME=myjavaapp \
SPRING_DATASOURCE_PASSWORD=secret \
SERVER_PORT=9090 \
java -jar build/libs/myJavaApp-0.0.1-SNAPSHOT.jar
```

Then open **http://localhost:9090/hello**.

The rule is simple: `spring.datasource.url` in YAML becomes
`SPRING_DATASOURCE_URL` as an environment variable (uppercase, dots and dashes
become underscores).

### 7c. Deploy as a Docker image

Create a `Dockerfile` in the project root:

```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY build/libs/myJavaApp-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
```

Build and run it:

```bash
./gradlew clean bootJar
docker build -t myjavaapp:latest .

docker run -d --name myjavaapp \
  --add-host=host.docker.internal:host-gateway \
  -p 8081:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5434/myjavaapp \
  -e SPRING_DATASOURCE_USERNAME=myjavaapp \
  -e SPRING_DATASOURCE_PASSWORD=secret \
  myjavaapp:latest

curl http://localhost:8081/hello
docker logs -f myjavaapp     # watch the logs
docker rm -f myjavaapp       # stop and remove
```

Inside a container, `localhost` means the container itself — that is why the
database URL uses `host.docker.internal` (mapped to your machine by
`--add-host=...:host-gateway`) instead of `localhost`.

### 7d. Alternative: let Spring Boot build the image for you

No Dockerfile needed — Spring Boot builds an optimised image with Cloud Native
Buildpacks (needs Docker running; the first build downloads a large builder image):

```bash
./gradlew bootBuildImage
docker images | grep myjavaapp
```

### 7e. Running it as a real service

On a Linux server, run the jar under `systemd` so it starts on boot and restarts
on crash. Create `/etc/systemd/system/myjavaapp.service`:

```ini
[Unit]
Description=myJavaApp
After=network.target

[Service]
User=myjavaapp
ExecStart=/usr/bin/java -jar /opt/myjavaapp/app.jar
Environment=SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5434/myjavaapp
Environment=SPRING_DATASOURCE_USERNAME=myjavaapp
Environment=SPRING_DATASOURCE_PASSWORD=changeme
SuccessExitStatus=143
Restart=always

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now myjavaapp
sudo systemctl status myjavaapp
sudo journalctl -u myjavaapp -f      # live logs
```

> DevTools is a **development-only** tool. It is declared as `developmentOnly` in
> `build.gradle`, so it is automatically excluded from the jar — no live reload in
> production, which is exactly what you want.

---

## Common errors and fixes

| Error message | Cause | Fix |
|---------------|-------|-----|
| `Failed to configure a DataSource: 'url' attribute is not specified` | No database configured | Do Step 1 and Step 2 |
| `Connection to localhost:5434 refused` | Database not running | `docker start myjavaapp-postgres` |
| `Bind for 0.0.0.0:5434 failed: port is already allocated` | Something else grabbed that port | Pick a free one (`-p 5435:5432`) and update `datasource.url` |
| `Web server failed to start. Port 8080 was already in use` | App already running | Stop it (`Ctrl + C`), or set `SERVER_PORT=8081` |
| `Whitelabel Error Page` / 404 | No endpoint for that URL | Check the path — Step 3 creates `/hello`, not `/` |
| Code changes don't appear | Classes not recompiled | Run `./gradlew classes` or set up Option A/B in Step 5a |
| Browser doesn't auto-refresh | LiveReload off or extension not connected | Set `spring.devtools.livereload.enabled: true`; click the extension icon |

---

## Command cheat sheet

| Command | What it does |
|---------|--------------|
| `./gradlew bootRun` | Run the app in development |
| `./gradlew classes --continuous` | Recompile on every save (drives live reload) |
| `./gradlew test` | Run the tests |
| `./gradlew clean bootJar` | Build the runnable jar |
| `./gradlew build` | Compile + test + build the jar |
| `./gradlew bootBuildImage` | Build a Docker image without a Dockerfile |
| `./gradlew dependencies` | Show the dependency tree |
| `./gradlew tasks` | List every available task |
| `java -jar build/libs/myJavaApp-0.0.1-SNAPSHOT.jar` | Run the built jar |
| `docker start myjavaapp-postgres` | Start the database |

---

## Where to go next

- Add an `@Entity` class and a `JpaRepository` to save data in PostgreSQL.
- Return JSON instead of text by returning an object or a `record` from the controller.
- Add `spring-boot-starter-actuator` for health checks at `/actuator/health`.
- See `HELP.md` for the official Spring Boot reference links for this exact version.

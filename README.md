# Hello World MVC

JavaFX application in which users sign in and see their data. It follows a two-layer
architecture (user interface and data access), and the users are read from a properties
file or a MySQL database, as selected in `config.properties`.

Challenge 0 of the user interface development module (DIN), second year of DAM.

## Authors

- Aritz Navarro
- Brayan Romero
- Ekaitz Rivero

## Features

- Sign-in window with input validation: the button stays disabled until both fields have
  text, and the login format is checked before the data store is contacted.
- The sign-in runs in a background task, so the window never freezes.
- User data window with sign-out, and a confirmation before exiting.
- Two data access implementations, selected in `config.properties` on each run: a
  properties file and a MySQL 8.0 database.
- Passwords stored as salted PBKDF2 hashes, never in plain text.
- Logging to the console and to rotating files in `logs/`.
- Complete Javadoc, private members included.
- Distribution zip with its own Java runtime and launch scripts.

## Requirements

| Tool | Version | Needed for |
| --- | --- | --- |
| JDK | 25 (for example Eclipse Temurin) | Building and running; `JAVA_HOME` must point to it |
| Maven | 3.9 or later (for example `choco install maven`) | Building |
| Git | Any recent version | Version control |
| MySQL Server | 8.0, with MySQL Workbench | Only the `DATABASE` implementation |
| Scene Builder | 26 | Only editing the views |

## Getting started

1. Clone the repository and open its folder.
2. Create your own configuration from the tracked example:

   ```shell
   copy config.properties.example config.properties    # Windows
   cp config.properties.example config.properties      # Linux and macOS
   ```

   `config.properties` is ignored by Git because it holds your database credentials:
   never commit it.
3. Run the application:

   ```shell
   mvn javafx:run
   ```

4. Sign in with one of the demo users:

   | Login | Password | Name |
   | --- | --- | --- |
   | `demo` | `Demo1234` | Demo User |
   | `jdoe` | `JohnDoe1234` | John Doe |
   | `asmith` | `AliceSmith1234` | Alice Smith |

The example configuration selects the `FILE` implementation, which reads
`data/users.properties`, so no database is needed to get started.

## Using the MySQL database

1. Open `database/hello_world_mvc.sql`, replace both `CHANGE_ME` values with a password of
   your choice and run the script in MySQL Workbench as `root`. It creates the
   `hello_world_mvc` database, the `app_user` table with the demo users and the read-only
   account `hwmvc_app`.
2. In your `config.properties`, set:

   ```properties
   dao.implementation=DATABASE
   db.password=the password you chose
   ```

3. Run the application again: the user data window now shows "Data source: MySQL database".

## Configuration reference

| Key | Used by | Description |
| --- | --- | --- |
| `dao.implementation` | Always | `FILE` or `DATABASE` |
| `db.url` | `DATABASE` | JDBC URL; the example points to `localhost:3306/hello_world_mvc` |
| `db.user` | `DATABASE` | Database account (`hwmvc_app`) |
| `db.password` | `DATABASE` | Password of that account; it may be empty |
| `file.path` | `FILE` | Users file, relative to the folder of `config.properties` |

The file is read from the working directory. To use another one, start the application
with `-Dapp.config=<path>`.

## Architecture

The application has two layers: the user interface (FXML views and their controllers) and
the data access layer (DAO). The model is shared by both. The user interface only knows the
`UserDao` interface, and `DaoFactory` creates the implementation selected in
`config.properties`.

| Design element | Implementation (in `tartanga.dami2.din.helloworldmvc`) |
| --- | --- |
| Sign-in view | `view/LoginView.fxml` and `controller.LoginController` |
| User data view | `view/UserDataView.fxml` and `controller.UserDataController` |
| Model | `model.User` |
| Data access interface | `dao.UserDao` |
| Database implementation | `dao.UserDaoDbImpl` |
| File implementation | `dao.UserDaoFileImpl` |
| Implementation selection | `dao.DaoFactory`, `dao.DaoConfig` and `dao.DaoType` |

```text
src/main/java/tartanga/dami2/din/helloworldmvc/
├── App.java       entry point of the application
├── controller/    controllers of the views (user interface layer)
├── dao/           data access layer and its configuration
├── exception/     exceptions shared by the layers
├── model/         the User model
└── util/          password hashing and input validation
src/main/resources/tartanga/dami2/din/helloworldmvc/view/   FXML views and stylesheet
```

## Building, testing and documentation

| Command | Result |
| --- | --- |
| `mvn javafx:run` | Runs the application |
| `mvn test` | Runs the unit tests |
| `mvn javadoc:javadoc` | Generates the API documentation in `target/reports/apidocs` |
| `mvn clean package -Pdist` | Builds the distribution zip (see below) |

The database tests (`UserDaoDbImplTest`) use your `config.properties`. They are skipped
when it selects the `FILE` implementation or when the database cannot be reached.

The Javadoc build fails if any package, class, method or attribute, private ones included,
has no documentation comment.

## Distribution

`mvn clean package -Pdist` builds `target/hello-world-mvc-1.0.0-win.zip`, which contains:

- `hello-world-mvc.jar`: the application.
- `lib/`: the MySQL driver.
- `jre/`: a Java runtime created with `jlink` that only has the JDK and JavaFX modules the
  application needs.
- `hello-world-mvc.bat` and `hello-world-mvc.sh`: the launch scripts.
- `config.properties` (ready to use, in `FILE` mode) and `config.properties.example`.
- `data/`, `database/` and `docs/api/` (the Javadoc).

Unzip it and run `hello-world-mvc.bat` (Windows) or `./hello-world-mvc.sh` (Linux, macOS
or Git Bash). No Java installation is needed. The runtime is built for the operating system
that runs Maven, so build the zip on Linux to get a Linux version (`-linux.zip`).

## Adding a user

Passwords are stored as PBKDF2 hashes. To add a user:

1. Get the hash of the new password:

   ```shell
   mvn -q compile exec:java "-Dexec.args=TheNewPassword"
   ```

2. Add the user to `data/users.properties` (five keys that start with the login, in
   lowercase) and insert it into the `app_user` table with the same hash.

## Editing the views with Scene Builder

Open the FXML files of `src/main/resources/tartanga/dami2/din/helloworldmvc/view` with
Scene Builder. After saving:

- Set the `xmlns` attribute back to `http://javafx.com/javafx/25`: Scene Builder 26 writes
  version 26, and JavaFX 25 warns about it when it loads the view.
- Keep the header comment and the order of the sign-in fields: each field is declared
  before the label that references it with `labelFor`.

## Logging

Messages go to the console and to `logs/hello-world-mvc-0.log` (rotating, up to five files
of 1 MB). The levels are set in `src/main/resources/logging.properties`. Passwords and
password hashes are never logged.

## Git

- `config.properties` must never be committed. If it happens, run
  `git rm --cached config.properties`, commit, and change the database password.
- To publish the repository, create an empty remote repository and run:

  ```shell
  git remote add origin <repository URL>
  git push -u origin main --tags
  ```

## Troubleshooting

| Problem | Solution |
| --- | --- |
| `java -version` shows 1.8 | An old Java comes first on the `PATH`. Maven uses `JAVA_HOME`, but put `%JAVA_HOME%\bin` first on the `PATH` to run Java tools by hand. |
| "Configuration file not found" at start-up | Copy `config.properties.example` to `config.properties`. |
| "Access denied for user 'hwmvc_app'@'localhost'" | Run `database/hello_world_mvc.sql` and use the same password in `db.password`. |
| "Communications link failure" | The MySQL service (MySQL80 on Windows) is not running. |
| "Public Key Retrieval is not allowed" | Keep `allowPublicKeyRetrieval=true` in `db.url`. |
| An upgrade to Connector/J 26.x is suggested | Do not upgrade: it requires MySQL 8.4 or later. Version 9.7.0 is the last one that supports MySQL 8.0. |
| VS Code shows errors after `pom.xml` changes | Run "Java: Reload Projects" or "Java: Clean Java Language Server Workspace". |

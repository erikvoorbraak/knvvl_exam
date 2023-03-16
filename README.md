# knvvl_exam
Online tool for maintaining questions and exams for the Dutch paragliding theory exam.  
# Components
* Back-end: **Java 19**
  * **Spring Boot** for configuration and dependency injection
  * **Hibernate** for Object-to-Relational mapping
  * **Gson** for mapping to/from json
  * **Liquibase** for database schema migration
  * **opencsv** for reading database exports
  * **itextpdf + pdfbox** for creating PDF documents
  * **Thymeleaf + Spring Security** for user authentication
  * **JUnit** for unit testing
* Front-end: **Vue 3** (https://vuejs.org)
  * **Vite** based
  * Quick-started using https://cli.vuejs.org/guide/creating-a-project.html#vue-create
  * **Router** for request routing: https://router.vuejs.org/guide/#html
  * **Axios** for HTTP requests: https://axios-http.com/docs/example
  * **EasyDataTable** for tables: https://vuejsexamples.com/a-easy-to-use-data-table-component-made-with-vue-js-3-x/

# Prerequisites
For developing, I have the following components installed on my local development environment. 
Some of these are explained in more detail below, the rest are assumed to be installed.
* **Git** for version control, backed by Githuib (https://gitforwindows.org/)
* **IntelliJ Community Edition** or your favorite IDE for back-end development
* **Visual Studio Code** or your favorite IDE for front-end development
* **maven** for Java dependency management (https://maven.apache.org/)
* **npm** as javascript package manager (https://www.npmjs.com/)
* **nginx for Windows** for redirecting back-end and front-end requests to separate services (https://nginx.org)
* **PostgreSQL** for storing everything, including files (https://www.postgresql.org/)

# Setup
To get started, first pull all sources from Github: https://github.com/erikvoorbraak/knvvl_exam.
Let's assume you will pull into `C:/github/knvvl_exam/`.

### Start the frontend
Open a command prompt and go to `C:/github/knvvl_exam/vue`. If you want to see or edit the code from there,
simply type `code` to open VS Code on that location.<br/>
Start the Vite frontend:<br/>
`npm run dev`<br/>
You should see a message that the frontend is running on http://localhost:5173/.
Try this in your browser, you should see the Vue application but no content.

### Setup the backend
In IntelliJ, use "Module" > "Open from existing sources" and navigate to
`C:/github/knvvl_exam/java/`. Open the Java code as Maven project using the `pom.xml` file.
Make sure your Maven configuration can connect to an artifact repository such as Maven Central.<br/>
If all dependencies are resolved, you can set up the connection to the database.
Find `application.properties` and correct the database setup when needed.<br/>

### Start the backend
Now you should be able to start the Spring Boot application. Right-click `ExamApplication` and
choose "Run" or "Debug". It should start the back-end on http://localhost:8080/.
If you try it, you will see a logon dialog. Use the predefined account "admin" with password "Welcome01".
Try if the REST endpoints work, for example: http://localhost:8080/api/users.

### Setup nginx
Although not strictly required, it is very convenient to use nginx as a proxy. Install it
and go to the installation folder, let's say `C:/servers/nginx-1.23.3`.
Inside the configuration folder, find and edit the proxy config: `C:/servers/nginx-1.23.3/conf/nginx.conf`:
```
events {}
http {
  server {
    location /api {
        proxy_pass http://localhost:8080/api;
    }
    location /login {
        proxy_pass http://localhost:8080/login;
    }
    location / {
        proxy_pass http://localhost:5173;
    }
  }
}
```
As you can see, we will redirect all /login and /api requests to the backend, and the rest to the frontend.
By default, nginx listens to port 80.

### Start nginx
Open a command prompt and go to the installation folder (`C:/servers/nginx-1.23.3`). Start nginx:<br/>
`start nginx`<br/>
Note: just closing the command prompt won't stop nginx; you have to stop it manually:<br/>
`nginx -s stop`<br/>
After nginx starts successfully, you should be able to point your browser at http://localhost/ and see the login screen.<br/>

NOTE: the login screen from the backend doesn't automatically redirect to the frontend. You may have to 
click or manually type http://localhost/ again.

# Build
When development is done, follow the steps below to create a single JAR file.

### Building the frontend
Open a command prompt and go to `C:/github/knvvl_exam/vue`. Build the frontend:<br/>
`npm run build`<br/>
This will build by default into `C:/github/knvvl_exam/vue/dist`.<br/>

### Building the backend
To make Java aware of the frontend build, first move everything 
from `C:/github/knvvl_exam/vue/dist`
into `C:/github/knvvl_exam/java/src/main/resources/static`.

Now we can create the JAR file. Open a command prompt on `C:/github/knvvl_exam/java` and run `mvn package`.
Alternatively, use your IDE to run Maven's package command.

If successful, then a JAR file should appear in `C:/github/knvvl_exam/java/target`.

### Running the server
Finally we can set up an environment to test the jar file. Let's collect everything in a folder
`C:/servers/exam`, having the following content:
* A folder `Fonts` for additional `ttf` files for use by the generated PDFs.
* Optional folder `Imports` for `txt` files containing CSV exports from the original database.
* A file `application.properties` to capture the actual settings to connect to the database.
* The `exam.jar` file as it was built.
* If on Windows, a file `run.bat` to launch the server with a single click. File contents: `java -jar exam.jar`.
* Alternatively, open a command prompt and type `java -jar exam.jar`.

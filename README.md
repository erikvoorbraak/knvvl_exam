# knvvl_exam
Online tool for maintaining questions and exams for the Dutch paragliding theory exam.  
# Components
* Back-end: **Java 19**
  * **Spring Boot** for configuration and dependency injection
  * **Hibernate** for Object-to-Relational mapping
  * **EhCache** for second-level caching
  * **Gson** for mapping to/from json
  * **Liquibase** for database schema migration
  * **opencsv** for reading database exports
  * **itextpdf + pdfbox** for creating PDF documents
  * **Thymeleaf + Spring Security** for user authentication
  * **JUnit** for unit testing
  * **Dropbox** for sending backups to
* Front-end: **Vue 3** (https://vuejs.org)
  * **Vite** based
  * Quick-started using https://cli.vuejs.org/guide/creating-a-project.html#vue-create
  * **Router** for request routing: https://router.vuejs.org/guide/#html
  * **Axios** for HTTP requests: https://axios-http.com/docs/example
  * **EasyDataTable** for tables: https://vuejsexamples.com/a-easy-to-use-data-table-component-made-with-vue-js-3-x/

# Prerequisites
For developing, I have the following components installed on my local development environment. 
Some of these are explained in more detail below, the rest are assumed to be installed.
* **Git** for version control, backed by Github (https://gitforwindows.org/)
* **IntelliJ Community Edition** or your favorite IDE for back-end development
* **Visual Studio Code** or your favorite IDE for front-end development
* **maven** for Java dependency management (https://maven.apache.org/)
* **npm** as javascript package manager (https://www.npmjs.com/)
* **nginx for Windows** for redirecting back-end and front-end requests to separate services (https://nginx.org)
* **PostgreSQL** for storing everything, including files (https://www.postgresql.org/)
* **Docker Desktop** for building and running Docker images (https://www.docker.com/products/docker-desktop/)
* **Dropbox** account, no local install of Dropbox executable required (https://www.dropbox.com/)

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

### Setting up Dropbox
To write backups to Dropbox, you need to create a Dropbox App with sufficient permissions.
https://github.com/dropbox/dropbox-sdk-java describes in detail how to do that.
https://www.dropbox.com/developers/apps allows you to set up an App; make sure that on the Permissions tab,
you enable `files.content.write` and `files.content.read`. After that, generate an `access token` to use here.


# Build
When development is done, follow the steps below. The complete flow from development to deployment is:
* Build the frontend
* Build the backend
* Build a Docker image
* Upload it to Docker hub
* Restart cloud provider

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

### Building a Docker image
To be able to create a Docker image on a Windows machine, use "Docker Desktop" (https://www.docker.com/products/docker-desktop/).
Open a command prompt and go to `C:/github/knvvl_exam/java`. Build the Docker image (note: the single '.' at the end is
intended to be copied!):<br/>
`docker build -t erikvoorbraak/knvvl_exam .`<br/>

### Uploading the image to docker.io
Open a command prompt and go to `C:/github/knvvl_exam/java`. First, log on to Docker Hub:<br/>
`docker login -u "erikvoorbraak" -p "[secret]" docker.io`<br/>

Now you can upload the Docker image:<br/>
`docker push erikvoorbraak/knvvl_exam`<br/>

### Running the server for Testing
For testing purposes we can set up an environment to test the jar file. Collect everything in a folder
`C:/servers/exam`, having the following content:
* Optional folder `Imports` for `txt` files containing CSV exports from the original database.
* A file `application.properties` to capture the actual settings to connect to the database.
* The `exam.jar` file as it was built.
* If on Windows, a file `run.bat` to launch the server with a single click. File contents: `java -jar exam.jar`.
* Alternatively, open a command prompt and type `java -jar exam.jar`.

### Running the Docker image for Testing

To run a Postgres Docker image with a default password of "postgres":<br/>
`docker run -e=POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:11.7`<br/>

To run the Spring Boot application in Docker Desktop locally:<br/>
`docker run -p 80:8080 -e spring.datasource.url="jdbc:postgresql://host.docker.internal:5432/exam" -e spring.datasource.password="postgres" erikvoorbraak/knvvl_exam`<br/>


# Deploy

Below are the steps I took to get the application running on Google Cloud with Postgres.

### Cloud SQL
To store any data, you need a database. Choose the smallest offering: 1 vCPU, 3.75 GB, SSD storage: 10 GB.
* Go to https://console.cloud.google.com/ and choose "Cloud SQL".
* Click "Create service", select PostgreSQL and click "Create Instance".
* Type a name for the database and a password for superuser "postgres".
* Choose a configuration. I chose "Development".
* Choose "Single zone" for zonal availability.
* Create the service.
* Write down the "Public IP address" and "Connection name" for later use.
* To be able to connect to this Postgres instance from your local machine:
  * Go to menu "Connections".
  * Go to tab "Networking" and find "Authorized networks".
  * Choose "Add network" and type the IP address of your home network (try https://whatismyipaddress.com/)

### Cloud Run
To run the Java Spring Boot Docker image, you need a Cloud Run instance.
* Go to https://console.cloud.google.com/ and choose "Cloud Run".
* Click "Create service", select "Deploy one revision from an existing container image".
* Type the Container image url: "erikvoorbraak/knvvl_exam".
* Type a service name, for example "knvvl_exam".
* Choose "CPU is only allocated during request processing".
* Set "Maximum number of instances" to 1.
* For Authentication, choose "Allow unauthenticated invocations" as we do our own authentication.
* Choose memory as 512MB and CPU as 1.
* Set up environment variables that Java will pick up to connect to Cloud SQL (substitute your own "Connection name"):
  * `spring.datasource.url` = `jdbc:postgresql:///exam?cloudSqlInstance=daring-atrium-382409:us-central1:pdb&socketFactory=com.google.cloud.sql.postgres.SocketFactory`
  * `spring.datasource.password` = `[secret]`
  * To configure this I found some useful tips on https://cloud.google.com/sql/docs/postgres/connect-run#java.
* Under "Cloud SQL connections", select your Postgres instance.

### Security
During the process, I had to enable some services and add some roles.
* Enable service: compute.googleapis.com
* Enable service: sqladmin.googleapis.com
* Enable services: sqladmin
* Find Google's identity management "IAM & Admin", you may need to add the role "Cloud SQL Access" to your service account(s).

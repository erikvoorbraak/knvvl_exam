# knvvl_exam
Online tool for maintaining questions and exams for the Dutch paragliding theory exam.  
# Components
* Back-end: **Java**
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
* Build:
  * **Maven** for Java dependency management
  * 

# Prerequisites
For developing, I have the following components installed on my local development environment:
* **IntelliJ Community Edition** or your favorite IDE for back-end development
* **Visual Studio Code** or your favorite IDE for front-end development
* **maven** for Java dependency management (https://maven.apache.org/)
* **npm** as javascript package manager (https://www.npmjs.com/)
* **nginx for Windows** for redirecting back-end and front-end requests to separate services (https://nginx.org)
* 
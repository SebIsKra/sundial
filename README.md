Sundial

Sundial is a Spring Boot web application for user authentication, favorite café management, and weather-based café checking. The project uses Spring MVC, Thymeleaf, Spring Data JPA, and PostgreSQL for persistence.

Overview

This application allows users to:

- register and log in
- browse a map
- retrieve weather data for coordinates
- check cafés and mark them as sunny or cloudy
- save favourite café locations per user

Tech Stack

- Java 21
- Spring Boot 4.1.0 & Maven
- Spring Web MVC
- Spring Data JPA
- Thymeleaf
- PostgreSQL
- Docker Compose
- PGAdmin4


Prerequisites

Before running the project, make sure you have:

- Java 21 installed
- Maven installed or use the provided Maven wrapper (`mvnw`)
- Docker Desktop / Docker Engine available for PostgreSQL and open and running
- An OpenWeatherMap API key if you want live weather requests either add it to your application.properties, in a .env file or for demo purposes directly in the WeatherController-class

Database
Take a look at the added DATABASESETUP.md to set up a database which is containerised. Important: Always run Docker before starting the application. To visualise the database we recommend PGAdmin4

Configuration
The application configuration is stored in:

- `src/main/resources/application.properties`

Property Settings
spring.datasource.url=jdbc:postgresql://localhost:5432/sundial
spring.datasource.username=sundial
spring.datasource.password=sundial
spring.jpa.hibernate.ddl-auto=update
weather.api.key=testkey
weather.api.base-url=http://localhost:8089


Run the Application

Using Maven Wrapper:

In the terminal type (or run the 'Sundial.java' class via the IDE):
.\mvnw spring-boot:run

Use your local browser to access the application at:
http://localhost:8080

Main Endpoints

The main web and API entry points are:

- `GET /` — login page
- `POST /` — user login
- `GET /register` — registration page
- `POST /register` — register a new user
- `GET /location` — location page
- `GET /weather` — weather view page <- historically redundant and will be deleted in further releases
- `GET /weather-data` — weather data JSON endpoint
- `POST /cafes/check` — check cafés and enrich them with weather/cloudiness data
- `POST /cafes/favourite` — toggle favourite café status
- `GET /favorites` — list favourite cafés for the logged-in user

Testing

Run the test suite with:
.\mvnw clean test (Depending on the OS you might need to change the slashes)

Tests are stored under:

- `src/test/java` All the Java classes integrate a testclass using Mockito, Spring boot testing, Wiremock and webmvc. Check out the pom.xml for all the added dependencies 

Rest-API Integration

The weather feature communicates with the OpenWeatherMap REST API through the controller layer and maps the response into the `WeatherResponse` model.
The Response model currently expects fields such as:
- `name`
- `weather[]`
- `main.temp`
- `clouds.all`

What is which class doing?

- The app uses Spring Boot auto-configuration and component scanning.
- `UserRepository` and `CafeLocationRepository` are JPA repositories responsible for persistence.
- `UserService` handles user authentication and registration logic.
- `WeatherController` is the main component for weather-related HTTP endpoints. As of now it also contains the favorites-functions which will be re-ordered in future releases


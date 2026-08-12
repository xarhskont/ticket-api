# Ticket API - High Concurrency Flash Sale System

A full-stack web application designed to handle huge spikes in traffic during event flash sales without crashing or overselling tickets. 

This project focuses heavily on backend development, demonstrating message queues, fast data caching, and building a clean, efficient API.

## Key Backend Features

* **Background Processing**: Used **RabbitMQ** to handle database saves in the background. This lets the API respond to the user instantly instead of making them wait for the database to finish.
* **Fast Ticket Caching**: Used **Redis** to keep track of available tickets in memory. This is much faster than checking the database and prevents accidentally selling the same ticket twice when many people buy at the exact same time.
* **Load Testing**: Used **k6** to stress test the API. The app successfully handled over **7,100 requests per second** from 1,000 simulated users at once without crashing.
* **Automated Testing**: Used **Testcontainers** to automatically start fresh Docker containers (for Postgres, Redis, and RabbitMQ) every time the tests run, making sure the integration tests are always accurate.
* **Database Design**: Created a relational **PostgreSQL** database to connect Users, Events, and Orders, using proper foreign keys and table relationships.
* **Error Handling**: Added custom Spring Boot error handlers to catch bad requests (like trying to buy too many tickets) and return clean error messages to the user.

## Technology Stack

* **Backend Environment**: Java 21
* **Web Framework**: Spring Boot 4.1 (WebMVC)
* **Database ORM**: Hibernate / Spring Data JPA
* **Relational Database**: PostgreSQL
* **In-Memory Cache**: Redis
* **Message Broker**: RabbitMQ
* **Testing Frameworks**: JUnit 5, Mockito, Testcontainers
* **Load Testing**: k6
* **API Documentation**: Swagger / SpringDoc OpenAPI

## Environment Configuration

This application uses Docker to run the database and background services. 

There is a `docker-compose.yml` file included in the root folder that will set up PostgreSQL, Redis, and RabbitMQ for you. The Spring Boot `application.properties` file is already set up to connect to them automatically.

## How to Run the Application

### Option 1: Running Locally (API)

1. Clone this repository to your local machine.
2. Ensure you have **Docker** and **Docker Desktop** installed and running.
3. Open a terminal in the root directory of the project.
4. Run the following command to start PostgreSQL, Redis, and RabbitMQ in the background:
   ```bash
   docker compose up -d
   ```
5. Run the following command to start the Spring Boot API:
   ```bash
   ./mvnw spring-boot:run
   ```
6. Open your web browser and navigate to the interactive API documentation at: `http://localhost:8080/swagger-ui.html`

*Note: On initial startup, a `DataLoader` class will automatically populate the PostgreSQL database and Redis cache with mock users, events, and ticket inventory.*

### Option 2: Running the Load Test (k6)

To verify the architecture's performance capabilities:
1. Ensure the API is running locally (Follow Option 1).
2. Install [k6](https://k6.io/).
3. Open a separate terminal in the project root.
4. Run the stress test to simulate 1,000 concurrent users:
   ```bash
   k6 run load_test.js
   ```

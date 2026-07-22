# High-Concurrency Ticketing & Flash Sale API

## Project Overview
An enterprise-level RESTful API engineered to handle flash sales and high-traffic ticket purchases under extreme concurrency. The system is designed to process thousands of concurrent requests with low latency while strictly preventing data corruption, race conditions, and double-booking (overselling).

Instead of relying on basic synchronous database operations, the application combines Clean Layered Architecture with an asynchronous, event-driven processing pipeline to decouple high-volume user traffic from relational database commits.

## Engineering Value & Architecture Highlights

### 1. Race Condition & Overselling Prevention
When thousands of users simultaneously attempt to purchase the final remaining tickets, standard database updates can cause race conditions resulting in negative inventory. This system prevents overselling through a multi-tier concurrency strategy:
* **Atomic Pre-checks:** In-memory atomic operations and distributed locks verify and reserve ticket stock in sub-millisecond times before database transactions are initiated.
* **Database Row-Level Locking:** Transactional workers apply pessimistic row locking (`SELECT ... FOR UPDATE`) and optimistic locking (`@Version`) during database updates to ensure complete ACID compliance.

### 2. High-Throughput Read & Write Optimization
* **In-Memory Caching:** High-frequency read endpoints (such as checking event details and live stock) are served directly from cache using a cache-aside pattern, significantly reducing database load during traffic spikes.
* **Asynchronous Order Fulfillment:** To prevent HTTP thread pool exhaustion under heavy traffic, the purchase endpoint acts as a message producer. It validates the request, reserves stock in memory, publishes an order event to a message queue, and immediately returns a `202 Accepted` response.

### 3. Background Processing & Reliability
* **Dedicated Consumer Workers:** Background workers consume order events at a controlled rate to perform the heavy relational database transactions without overloading the persistence layer.
* **Compensation & Dead-Letter Handling:** If a transaction fails during database fulfillment (e.g., payment decline or stock mismatch), compensating transactions restore the reserved stock in cache, and unprocessable messages are routed to a dead-letter queue for auditing.

## Tech Stack & Infrastructure

* **Language & Framework:** Java 21, Spring Boot 3 (REST API, Spring Data JPA, Spring Cache, Spring AMQP)
* **Relational Database:** PostgreSQL (ACID transactions, persistent entity storage, row-level locking)
* **In-Memory Data Store:** Redis (Fast read caching, atomic stock tracking, distributed locking)
* **Message Broker:** RabbitMQ (Asynchronous order queueing, backpressure management, dead-letter exchanges)
* **Testing & Verification:** JUnit 5, Mockito, Testcontainers (Containerized end-to-end integration testing)
* **Documentation:** OpenAPI 3 / Swagger UI
* **Containerization:** Docker & Docker Compose (Local infrastructure orchestration)

## Domain Model & Database Schema

### User
* `id` (Primary Key, BIGINT)
* `username` (VARCHAR)
* `email` (VARCHAR, Unique)
* `created_at` (TIMESTAMP)

### Event
* `id` (Primary Key, BIGINT)
* `title` (VARCHAR)
* `description` (TEXT)
* `event_date` (TIMESTAMP)
* `total_tickets` (INTEGER)
* `available_tickets` (INTEGER)
* `price` (DECIMAL)
* `status` (VARCHAR: DRAFT, ACTIVE, SOLD_OUT, CANCELLED)
* `version` (BIGINT, Optimistic Lock Version)

### Order
* `id` (Primary Key, UUID)
* `event_id` (Foreign Key -> Event)
* `user_id` (Foreign Key -> User)
* `ticket_count` (INTEGER)
* `total_amount` (DECIMAL)
* `status` (VARCHAR: PENDING, CONFIRMED, FAILED)
* `failure_reason` (VARCHAR)
* `created_at` (TIMESTAMP)

## REST API Specification Summary

### Events API
* `GET /api/v1/events`
  Retrieves a paginated list of active events (cached).
* `GET /api/v1/events/{id}`
  Retrieves detailed information for a specific event including real-time available ticket stock (cached).
* `POST /api/v1/events`
  Administrative endpoint to create a new event and initialize inventory across database and cache layers.

### Orders & Flash Purchase API
* `POST /api/v1/orders/flash-purchase`
  Submits a high-concurrency ticket purchase request.
  * **Request Body:** `eventId`, `userId`, `ticketCount`
  * **Response:** `202 Accepted` containing the generated `orderId` and initial `PENDING` status.
* `GET /api/v1/orders/{orderId}`
  Retrieves the current fulfillment status (`PENDING`, `CONFIRMED`, or `FAILED`) of an order.

## Implementation Milestones & Development Checklist

### 1. Infrastructure & Environment Setup
- [ ] Configure `docker-compose.yml` defining containerized PostgreSQL, Redis, and RabbitMQ services.
- [ ] Configure application environment variables and database connection pooling (HikariCP).
- [ ] Verify startup connectivity across all persistent and in-memory services.

### 2. Domain Layer & Persistence
- [ ] Implement JPA entities (`User`, `Event`, `Order`) with appropriate schema constraints and version attributes.
- [ ] Create Spring Data JPA repositories with custom query methods for row-level locking.
- [ ] Configure database migrations or automated schema initialization.

### 3. Caching & Atomic Pre-check Layer
- [ ] Integrate Spring Data Redis and configure `RedisTemplate` serialization.
- [ ] Implement cache-aside logic for event catalog and details endpoints (`@Cacheable`).
- [ ] Build atomic stock initialization and decrement operations in Redis (`event:stock:{eventId}`) to validate availability before database interaction.

### 4. Asynchronous Messaging Infrastructure
- [ ] Configure RabbitMQ exchanges, order queues, routing keys, and dead-letter queues.
- [ ] Implement robust message serialization (JSON) and error handling configuration.
- [ ] Build the order message producer responsible for publishing purchase requests immediately after cache reservation.

### 5. Transactional Background Processing
- [ ] Implement the asynchronous RabbitMQ message consumer (`@RabbitListener`).
- [ ] Create the core transactional service (`TicketReservationService`) executing row locks, stock deduction, and order confirmation inside an ACID boundary.
- [ ] Add compensation logic to restore cache stock if a background database transaction fails.

### 6. REST Controllers, Validation & Documentation
- [ ] Build REST controllers with Jakarta Validation (`@NotNull`, `@Min`) for all request payloads.
- [ ] Implement global exception handling (`@ControllerAdvice`) to return standardized error responses (`ProblemDetail`).
- [ ] Integrate OpenAPI/Swagger UI configuration for automated API documentation.

### 7. Automated Testing & Concurrency Benchmarking
- [ ] Write unit tests for core domain services and concurrency validation rules using JUnit 5 and Mockito.
- [ ] Write end-to-end integration tests using Testcontainers running real PostgreSQL, Redis, and RabbitMQ containers.
- [ ] Conduct concurrency load benchmarking (`k6` / `JMeter`) simulating high-density concurrent purchase requests to verify zero double-bookings under load.

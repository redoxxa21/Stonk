# Audit Log Service Walkthrough

This document explains the `audit-log-service` in simple terms. The service is the platform's central event history recorder. It listens to Kafka topics from the other backend services, stores each event in a database table, and exposes a REST endpoint to read the stored events later.

It is mostly passive:

- It consumes Kafka messages.
- It writes audit rows to its own database.
- It exposes read-only HTTP access to those rows.
- It does not publish Kafka messages.
- It does not call other services directly.

## 1. Service Purpose

The audit service answers questions like:

- "Did a user registration event happen?"
- "Which trade saga events happened for a trade?"
- "Did the wallet fail before the portfolio step?"
- "Did market simulation orders reach the exchange?"
- "What payload was sent on a Kafka topic?"

It is useful for debugging, support, tracing system behavior, and checking business flows after they happen.

## 2. Location

Service folder:

```text
backend/audit-log-service
```

Main package:

```text
io.stonk.audit
```

## 3. Runtime Shape

Default local runtime:

- HTTP port: `2780`
- Kafka broker: `localhost:9092`
- Database: in-memory H2 database named `audit_db`
- Eureka registration: off by default

Docker Compose runtime:

- Container name: `stonks-audit-log`
- Kafka broker: `kafka:9092`
- Database: PostgreSQL database `audit_db` on container `stonks-postgres`
- Eureka registration: enabled
- Routed through API Gateway at `/audit/**`

## 4. Packages

### `io.stonk.audit`

Contains the Spring Boot application entry point.

### `io.stonk.audit.config`

Contains startup and Kafka consumer configuration.

### `io.stonk.audit.controller`

Contains the REST controller used to query audit records.

### `io.stonk.audit.entity`

Contains the JPA entity that maps to the database table.

### `io.stonk.audit.kafka`

Contains the Kafka listener and topic constants.

### `io.stonk.audit.repository`

Contains the Spring Data JPA repository for audit rows.

### `io.stonk.audit.service`

Contains the ingest logic that turns a Kafka message into a database row.

## 5. Files In The Service

### `pom.xml`

Defines the Maven project.

Important values:

- Parent: `spring-boot-starter-parent` version `3.2.5`
- Java version: `21`
- Spring Cloud version: `2023.0.1`
- Artifact: `audit-log-service`

Main dependencies:

- `spring-boot-starter-web`: exposes REST endpoints.
- `spring-boot-starter-data-jpa`: stores audit records through JPA.
- `spring-boot-starter-actuator`: exposes health/info endpoints.
- `spring-kafka`: consumes Kafka events.
- `spring-cloud-starter-netflix-eureka-client`: lets the service register with Eureka in Docker/prod.
- `h2`: local/test runtime database.
- `postgresql`: production/Docker database driver.
- `lombok`: reduces boilerplate getters, setters, builders, constructors, and logging fields.
- `spring-boot-starter-test`: basic test support.

Build plugins:

- `spring-boot-maven-plugin`: packages the app as a runnable Spring Boot jar.
- `maven-compiler-plugin`: configures Lombok annotation processing.

### `Dockerfile`

Builds and runs the service in two stages.

Build stage:

- Uses `maven:3.9-eclipse-temurin-21-alpine`.
- Copies `pom.xml`.
- Downloads dependencies using `mvn dependency:go-offline -B`.
- Copies `src`.
- Builds the jar with tests skipped.

Run stage:

- Uses `eclipse-temurin:21-jre-alpine`.
- Copies the built jar as `app.jar`.
- Starts it with `java -jar app.jar`.

### `mvnw`, `mvnw.cmd`, `wrapper/maven-wrapper.properties`

Maven wrapper files. They allow the service to be built without requiring Maven to be installed globally.

### `src/main/java/io/stonk/audit/AuditLogApplication.java`

The application entry point.

What it does:

- Starts Spring Boot.
- Enables Kafka listener support with `@EnableKafka`.

### `src/main/java/io/stonk/audit/config/AuditStartupLogger.java`

Runs at startup and logs that the audit service will persist Kafka events.

Why it exists:

- It gives a quick startup confirmation that the service is alive and bound to the expected topic set.

### `src/main/java/io/stonk/audit/config/KafkaConsumerConfig.java`

Creates the Kafka consumer configuration used by the listener.

Important behavior:

- Reads Kafka bootstrap servers from `spring.kafka.bootstrap-servers`.
- Reads group id from `spring.kafka.consumer.group-id`.
- Uses `StringDeserializer` for keys.
- Uses `StringDeserializer` for values.
- Uses `auto.offset.reset=earliest`.
- Creates a `ConcurrentKafkaListenerContainerFactory` named `kafkaListenerContainerFactory`.

Simple meaning:

- The audit service treats every consumed payload as text.
- If the service joins a consumer group with no previous offset, it starts from the earliest available message.

### `src/main/java/io/stonk/audit/controller/AuditQueryController.java`

Exposes the query API.

Base path:

```text
/audit
```

Endpoint:

```text
GET /audit/events
```

Query parameters:

- `topic`: optional Kafka topic filter.
- `page`: optional page number, default `0`.
- `size`: optional page size, default `50`, maximum forced to `200`.

Response:

- A Spring `Page<AuditEventRecord>`.
- Rows are sorted by `receivedAt` descending, so newest records come first.

Examples:

```text
GET /audit/events
GET /audit/events?page=0&size=20
GET /audit/events?topic=trade-initiated
```

### `src/main/java/io/stonk/audit/entity/AuditEventRecord.java`

Represents one stored Kafka message.

Database table:

```text
audit_events
```

Columns:

- `id`: generated primary key.
- `receivedAt`: timestamp when the audit service received and saved the event.
- `topic`: Kafka topic name. Required. Max length `256`.
- `messageKey`: Kafka message key. Optional. Max length `256`.
- `partitionIndex`: Kafka partition number.
- `offsetValue`: Kafka offset.
- `payload`: message body stored as text.

Important note:

- `receivedAt` is the audit service receive time, not necessarily the original business event time.

### `src/main/java/io/stonk/audit/kafka/AuditKafkaListener.java`

The Kafka listener.

It subscribes to these topics:

- `user-registration`
- `trade-initiated`
- `trade-completed`
- `wallet-debited`
- `wallet-failed`
- `wallet-credited`
- `wallet-refund-requested`
- `wallet-credit-requested`
- `portfolio-add-requested`
- `portfolio-added`
- `portfolio-failed`
- `portfolio-deducted`
- `order-request`
- `trade-executed`
- `market-price-updated`

For each consumed Kafka record, it passes these fields to `AuditIngestService`:

- topic
- key
- partition
- offset
- value

Failure behavior:

- If saving the audit row throws an exception, the listener logs an error.
- It does not rethrow the exception.
- Because it catches the exception, the Kafka listener will usually continue processing later messages instead of crashing the consumer.

### `src/main/java/io/stonk/audit/kafka/AuditSubscribedTopics.java`

Contains topic constants for:

- `USER_REGISTRATION = "user-registration"`
- `TRADE_INITIATED = "trade-initiated"`
- `TRADE_COMPLETED = "trade-completed"`

Important limitation:

- Only three topics are constants here.
- The remaining listener topics are hardcoded directly in `AuditKafkaListener`.

### `src/main/java/io/stonk/audit/repository/AuditEventRepository.java`

Spring Data JPA repository for `AuditEventRecord`.

Methods:

- `findByTopicOrderByReceivedAtDesc(String topic, Pageable pageable)`
- `findAllByOrderByReceivedAtDesc(Pageable pageable)`

The controller uses these methods to list events with or without a topic filter.

### `src/main/java/io/stonk/audit/service/AuditIngestService.java`

The core ingest service.

What it does:

1. Receives Kafka metadata and payload.
2. Truncates payloads longer than `32,000` characters.
3. Creates an `AuditEventRecord`.
4. Saves it with `auditEventRepository.save(row)`.
5. Logs important topics at `info` level and other topics at `debug` level.

Payload truncation:

- Max stored payload length: `32,000`.
- If longer, it stores the first `32,000` characters plus `...(truncated)`.

Transaction behavior:

- The `record` method is annotated with `@Transactional`.
- Each audit save happens inside a database transaction.

### `src/main/resources/application.yaml`

Default configuration.

Important values:

- Server port: `${SERVER_PORT:2780}`
- Service name: `audit-log-service`
- Kafka URL: `${KAFKA_URL:localhost:9092}`
- Kafka group id: `audit-log-service-group`
- Kafka auto offset reset: `earliest`
- Kafka auto commit: `true`
- Kafka missing topics fatal: `false`
- Default database: H2 in-memory `jdbc:h2:mem:audit_db`
- JPA schema mode: `${JPA_DDL_AUTO:update}`
- Eureka registration: disabled by default
- Actuator exposed endpoints: `health`, `info`

Important note:

- `missing-topics-fatal: false` means the service can start even if some subscribed topics do not exist yet.

### `src/main/resources/application-prod.yaml`

Production/Docker-like overrides.

Important values:

- Database URL default: `jdbc:postgresql://localhost:5444/audit_db`
- Username default: `stonks`
- Password default: `stonks123`
- Driver: `org.postgresql.Driver`
- Hibernate dialect: `PostgreSQLDialect`
- Eureka registration: enabled
- Eureka registry fetch: enabled

### `src/test/java/io/stonk/audit/AuditLogApplicationTests.java`

Basic Spring context test.

It only verifies that the application context can load.

### `src/test/resources/application.yaml`

Test configuration.

Important value:

- `spring.kafka.listener.auto-startup: false`

Simple meaning:

- Tests do not start real Kafka listeners.

### `target/`

Generated build output.

This includes compiled classes and copied resources. It should not be treated as source code.

## 6. External Backend Files That Matter

### `backend/Docker-compose.yml`

Defines the `audit-log-service` container.

Important environment variables:

- `DB_URL=jdbc:postgresql://stonks-postgres:5432/audit_db`
- `DB_USERNAME=stonks`
- `DB_PASSWORD=stonks123`
- `DB_DRIVER_CLASS_NAME=org.postgresql.Driver`
- `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka`
- `SERVICE_REGISTRY_AUTO_REGISTRATION_ENABLED=true`
- `EUREKA_FETCH_REGISTRY=true`
- `EUREKA_REGISTER_WITH_EUREKA=true`
- `KAFKA_URL=kafka:9092`

Dependencies:

- waits for healthy `postgres`
- waits for healthy `eureka-server`
- waits for started `kafka`

### `backend/init-databases.sql`

Creates the PostgreSQL database:

```sql
CREATE DATABASE audit_db;
```

### `backend/api-gateway/src/main/resources/application.yaml`

Routes gateway traffic:

```text
Path=/audit/** -> lb://audit-log-service
```

Gateway security behavior:

- `/audit/**` is not listed in `gateway.open-paths`.
- Through the gateway, audit endpoints require a valid `Authorization: Bearer <JWT>` header.
- Direct calls to the audit service port are not protected by a service-local Spring Security configuration.

## 7. Communication Type

The service uses two communication styles.

### Inbound Kafka

This is the main communication type.

Other services publish events to Kafka. The audit service consumes those events and stores them.

Pattern:

```text
producer service -> Kafka topic -> audit-log-service -> audit_events table
```

### Inbound REST

This is for reading stored audit records.

Pattern:

```text
frontend/admin/client -> API Gateway -> audit-log-service -> audit_events table
```

There is no outbound REST communication from `audit-log-service`.

There is no outbound Kafka publishing from `audit-log-service`.

## 8. Services That Interact With It

The audit service does not actively call these services. They interact with it indirectly by publishing Kafka events to topics that the audit service consumes.

| Service | Interaction | Topics |
| --- | --- | --- |
| `auth-service` | Publishes successful user registration events | `user-registration` |
| `trading-service` | Publishes trade saga events and compensation requests | `trade-initiated`, `trade-completed`, `portfolio-add-requested`, `wallet-refund-requested`, `wallet-credit-requested` |
| `wallet-service` | Publishes wallet saga outcomes | `wallet-debited`, `wallet-failed`, `wallet-credited` |
| `portfolio-service` | Publishes portfolio saga outcomes | `portfolio-added`, `portfolio-deducted`, `portfolio-failed` |
| `market-simulation-service` | Publishes simulated bot orders | `order-request` |
| `order-service` | Publishes matched exchange trades | `trade-executed` |
| `market-data-service` | Publishes price updates after trades | `market-price-updated` |
| `api-gateway` | Routes HTTP reads to audit service | `/audit/**` |
| `postgres` | Stores audit rows in Docker/prod | `audit_db.audit_events` |
| `kafka` | Supplies consumed events | all subscribed topics |
| `eureka-server` | Service discovery in Docker/prod | registers as `audit-log-service` |

## 9. Kafka Topic Walkthrough

### `user-registration`

Producer:

- `auth-service`

When it happens:

- After a user successfully registers and the user transaction commits.

Audit outcome:

- A row is stored with the registered user event payload.

Failure cases:

- If registration fails, no event is produced.
- If auth cannot serialize or publish the event, registration still succeeds but audit will not receive that event.
- If Kafka is down, the auth service logs a warning and audit has no row for that registration.

### `trade-initiated`

Producer:

- `trading-service`

When it happens:

- After a buy or sell trade request is validated, current price is fetched, and a pending trade is saved.

Consumers:

- `wallet-service`
- `portfolio-service`
- `audit-log-service`

Audit outcome:

- A row marks the start of a trade saga.

Failure cases:

- If the trade request fails before the event is published, there is no audit row.
- If the trading service saves the trade but Kafka publish fails after commit, the trade may stay pending and audit will not show the start event.

### `wallet-debited`

Producer:

- `wallet-service`

When it happens:

- For a BUY trade, after the wallet successfully debits the total trade amount.

Audit outcome:

- A row proves that the wallet step succeeded.

Downstream effect:

- `trading-service` receives it and requests portfolio add.

### `wallet-failed`

Producer:

- `wallet-service`

When it happens:

- BUY wallet debit fails because of insufficient funds or another wallet error.

Audit outcome:

- A row records the wallet failure reason.

Downstream effect:

- `trading-service` marks the trade as failed.

### `portfolio-add-requested`

Producer:

- `trading-service`

When it happens:

- After a BUY trade wallet debit succeeds.

Audit outcome:

- A row shows that the trade saga moved from wallet debit to portfolio add.

### `portfolio-added`

Producer:

- `portfolio-service`

When it happens:

- Portfolio successfully adds shares for a BUY trade.

Audit outcome:

- A row proves the portfolio step succeeded.

Downstream effect:

- `trading-service` marks the trade as completed and publishes `trade-completed`.

### `portfolio-failed`

Producer:

- `portfolio-service`

When it happens:

- BUY portfolio add fails, or SELL portfolio deduction fails.

Audit outcome:

- A row stores the portfolio failure reason.

Downstream effect:

- For failed BUY, `trading-service` requests a wallet refund.
- The trade is marked failed.

### `wallet-refund-requested`

Producer:

- `trading-service`

When it happens:

- BUY trade wallet debit succeeded, but portfolio add failed.

Audit outcome:

- A row shows compensation was requested.

Important edge case:

- `wallet-service` credits the refund but the current code does not publish a separate refund-success topic. The audit log can show the refund request, but not a dedicated refund completion event.

### `portfolio-deducted`

Producer:

- `portfolio-service`

When it happens:

- SELL trade successfully reduces the user's holding.

Audit outcome:

- A row proves shares were deducted.

Downstream effect:

- `trading-service` requests wallet credit.

### `wallet-credit-requested`

Producer:

- `trading-service`

When it happens:

- SELL trade portfolio deduction succeeds.

Audit outcome:

- A row shows that the user should receive sale proceeds.

### `wallet-credited`

Producer:

- `wallet-service`

When it happens:

- Wallet successfully credits proceeds for a SELL trade.

Audit outcome:

- A row proves the wallet credit step succeeded.

Downstream effect:

- `trading-service` marks the SELL trade completed and publishes `trade-completed`.

### `trade-completed`

Producer:

- `trading-service`

When it happens:

- BUY completes after `portfolio-added`.
- SELL completes after `wallet-credited`.

Audit outcome:

- A row marks the end of a successful trade saga.

### `order-request`

Producer:

- `market-simulation-service`

When it happens:

- Bot engine emits buy/sell market or limit orders.

Consumers:

- `order-service`
- `audit-log-service`

Audit outcome:

- A row records each simulated order request that reached Kafka.

### `trade-executed`

Producer:

- `order-service`

When it happens:

- The exchange matching engine matches an order and creates one or more executions.

Consumers:

- `market-data-service`
- `audit-log-service`

Audit outcome:

- A row records each matched exchange trade.

### `market-price-updated`

Producer:

- `market-data-service`

When it happens:

- A `trade-executed` event updates a known stock's price and derived market data.

Consumers:

- `market-simulation-service`
- `audit-log-service`

Audit outcome:

- A row records the published market price update.

Failure cases:

- If `trade-executed` references an unknown symbol, market-data-service ignores it and no `market-price-updated` event is produced.
- If market-data-service fails to publish the update, audit has no row for the price update.

## 10. REST API Walkthrough

### `GET /audit/events`

Purpose:

- Read recent audit records.

Through gateway:

```text
GET http://localhost:8081/audit/events
Authorization: Bearer <JWT>
```

Direct local service:

```text
GET http://localhost:2780/audit/events
```

Success:

- Returns HTTP `200 OK`.
- Returns a paginated response.
- Records are sorted newest first.

Common response fields:

- `content`: list of audit rows.
- `pageable`: page metadata.
- `totalElements`: total matching rows.
- `totalPages`: total page count.
- `size`: page size.
- `number`: current page number.

Audit row shape:

```json
{
  "id": 1,
  "receivedAt": "2026-05-13T10:00:00Z",
  "topic": "trade-initiated",
  "messageKey": "42",
  "partitionIndex": 0,
  "offsetValue": 123,
  "payload": "{\"tradeId\":42}"
}
```

### Topic filter

```text
GET /audit/events?topic=wallet-failed
```

Success:

- Returns only rows where `topic` exactly equals `wallet-failed`.

Edge cases:

- Unknown topic filter returns an empty page.
- Blank topic behaves like no topic filter.
- Topic matching is exact. `wallet` will not match `wallet-failed`.

### Pagination

```text
GET /audit/events?page=0&size=50
```

Behavior:

- `page` defaults to `0`.
- `size` defaults to `50`.
- `size` is capped at `200`.

Edge cases:

- `size=1000` becomes `200`.
- A page beyond the available records returns an empty `content` list.
- Negative `page` or invalid negative `size` can cause Spring/Data pagination errors because the controller does not validate them before creating `PageRequest`.

## 11. Successful Interaction Cases

### Case A: User registration audit succeeds

Flow:

```text
client -> auth-service register -> user saved -> user-registration event -> audit-log-service -> audit_events row
```

Expected result:

- Registration returns success to the client.
- Audit row appears under topic `user-registration`.

### Case B: BUY trade completes

Flow:

```text
trading-service -> trade-initiated
wallet-service -> wallet-debited
trading-service -> portfolio-add-requested
portfolio-service -> portfolio-added
trading-service -> trade-completed
audit-log-service stores each topic event
```

Expected result:

- Audit rows show the full successful BUY chain.
- Trade status becomes `COMPLETED`.

### Case C: BUY trade fails because of insufficient funds

Flow:

```text
trading-service -> trade-initiated
wallet-service -> wallet-failed
trading-service marks trade failed
audit-log-service stores trade-initiated and wallet-failed
```

Expected result:

- Audit log shows the failure reason in `wallet-failed`.
- There is no `portfolio-add-requested`.
- There is no `trade-completed`.

### Case D: BUY trade wallet debit succeeds but portfolio add fails

Flow:

```text
trade-initiated
wallet-debited
portfolio-add-requested
portfolio-failed
wallet-refund-requested
```

Expected result:

- Audit log shows the failed portfolio step.
- Audit log shows that a refund was requested.
- Trade is marked `FAILED`.

### Case E: SELL trade completes

Flow:

```text
trading-service -> trade-initiated
portfolio-service -> portfolio-deducted
trading-service -> wallet-credit-requested
wallet-service -> wallet-credited
trading-service -> trade-completed
audit-log-service stores each topic event
```

Expected result:

- Audit rows show the full successful SELL chain.
- Trade status becomes `COMPLETED`.

### Case F: SELL trade fails because user lacks shares

Flow:

```text
trade-initiated
portfolio-failed
```

Expected result:

- Audit log shows `portfolio-failed` with reason `Insufficient shares`.
- There is no `wallet-credit-requested`.
- There is no `trade-completed`.

### Case G: Market simulation order is matched

Flow:

```text
market-simulation-service -> order-request
order-service -> trade-executed
market-data-service -> market-price-updated
audit-log-service stores each topic event
```

Expected result:

- Audit rows show the order request, execution, and price update.

## 12. Failure Cases Inside Audit Service

### Kafka is unavailable at startup

Expected behavior:

- The application may start, but Kafka listener containers will not be able to consume until Kafka is reachable.
- No new audit rows are written while Kafka is unavailable.

Outcome:

- Audit history has gaps for messages that are not retained in Kafka long enough or are never published.

### Kafka topic does not exist

Expected behavior:

- The service does not fail startup because `missing-topics-fatal` is `false`.
- When the topic appears later, consumption can begin.

Outcome:

- No rows for that topic until Kafka has messages on it and the consumer receives them.

### Database is unavailable

Expected behavior:

- Startup can fail if the datasource cannot initialize.
- If the database goes down after startup, saves fail.
- The listener catches the exception and logs `Failed to persist audit for topic=...`.

Outcome:

- The service may keep consuming but fail to store records.
- Because the listener catches the exception and Kafka auto commit is enabled, some failed audit records may be lost from the audit table.

### Payload is too large

Expected behavior:

- Payload is truncated to `32,000` characters plus `...(truncated)`.

Outcome:

- The audit row is saved, but the payload is incomplete by design.

### Payload is null

Expected behavior:

- The row is saved with a null payload.

Outcome:

- Metadata still exists: topic, key, partition, offset, receive time.

### Message key is null

Expected behavior:

- The row is saved with null `messageKey`.

Outcome:

- You can still trace by topic, partition, offset, and payload.

### Database row validation fails

Possible causes:

- `topic` is null.
- `topic` is longer than the column allows.
- Database schema is unavailable or incompatible.

Expected behavior:

- Save throws an exception.
- Listener logs the failure.
- Row is not stored.

### REST query has bad pagination values

Possible causes:

- Negative `page`.
- Negative or zero `size`.
- Non-number value for `page` or `size`.

Expected behavior:

- Spring returns an error response, usually `400` or `500` depending on where the exception is raised.

Outcome:

- No data is returned.

### Unauthorized gateway request

When calling through API Gateway:

- Missing token returns `401 Unauthorized`.
- Invalid token returns `401 Unauthorized`.

When calling the service directly:

- The service has no local security config, so direct `/audit/events` calls are not blocked by JWT validation.

## 13. Edge Cases And Limitations

### Audit stores text, not typed objects

The payload is stored as a string. The audit service does not validate the business meaning of the event.

Outcome:

- Invalid JSON can still be stored if Kafka delivers it as a string.
- The service is useful for evidence and debugging, not business rule enforcement.

### Object-producing services rely on JSON serialization

Several services publish Java objects using Spring Kafka JSON serialization. Audit consumes values as strings.

Outcome:

- This works when the Kafka value bytes are JSON text.
- If a producer changes to binary serialization, audit will store unreadable text or fail deserialization.

### No duplicate protection

The audit table has no uniqueness constraint on topic, partition, and offset.

Outcome:

- If Kafka redelivers a message or offsets are reset, duplicate audit rows can be stored.

### No correlation-id column

The table stores topic, key, partition, offset, and payload, but there is no dedicated `correlationId` or `tradeId` column.

Outcome:

- To trace a full trade, you may need to search/filter by topic and inspect payloads manually.

### No retention or cleanup policy

There is no code that deletes old audit rows.

Outcome:

- The table grows forever unless the database is manually cleaned or a retention job is added later.

### No service-local authentication

The gateway protects `/audit/**`, but the audit service itself does not.

Outcome:

- In Docker, normal frontend/API traffic should go through the gateway.
- Direct network access to the audit service should be restricted by deployment/network rules if audit data is sensitive.

### `receivedAt` is not event-created time

The service sets `receivedAt` when it stores the event.

Outcome:

- If Kafka has backlog, `receivedAt` can be later than the real business event time.

### Consumer uses auto commit

Configuration has auto commit enabled.

Outcome:

- If persistence fails around the same time offsets are committed, the audit table can miss events.
- For stronger audit guarantees, manual offset commit after database save would be safer.

### Listener catches persistence exceptions

The listener logs errors and continues.

Outcome:

- A bad save does not stop the service.
- But failed audit records are not retried by this code.

### Some subscribed topics may not be produced by current code paths often

Example:

- `wallet-refund-requested` is only produced when a BUY trade needs compensation.
- `wallet-credit-requested` is only produced during successful SELL flow after portfolio deduction.

Outcome:

- Empty topic results can be normal.

### Refund completion is not audited as a dedicated event

The wallet handles `wallet-refund-requested` by crediting the wallet, but the current code does not publish a `wallet-refunded` event.

Outcome:

- Audit can prove the refund request happened.
- Audit cannot prove refund completion from a dedicated Kafka event.

### Topic constants are incomplete

Only three topics are centralized in `AuditSubscribedTopics`; the rest are string literals in `AuditKafkaListener`.

Outcome:

- Renaming topics is easier to get wrong because not all topic names are in one place.

### No custom error response for audit controller

The service does not define a global exception handler.

Outcome:

- Framework default error responses are returned for invalid query parameters and unexpected errors.

## 14. Overall Outcomes

### When everything works

The audit service creates a searchable database history of important platform events.

You can:

- See successful user registrations.
- Trace trade saga progress.
- Identify where a trade failed.
- Confirm wallet and portfolio events.
- Confirm market simulation, exchange execution, and price update events.
- Query recent events over HTTP.

### When producers fail before Kafka publish

The audit service sees nothing.

Simple rule:

```text
No Kafka message means no audit row.
```

### When Kafka receives the message but audit database save fails

The audit service logs an error, but the row is not stored.

Because auto commit is enabled and exceptions are swallowed, this can create an audit gap.

### When audit service is down

Kafka may retain messages while the service is offline.

When the audit service comes back:

- If offsets and retention allow it, it can consume pending messages.
- If messages expired or offsets already moved, those audit rows are lost.

### When queried successfully

The caller gets paginated audit rows newest first.

### When queried through gateway without JWT

The caller gets `401 Unauthorized`.

## 15. Quick Operational Checks

Health check:

```text
GET /actuator/health
```

Recent events:

```text
GET /audit/events?page=0&size=50
```

Filter a topic:

```text
GET /audit/events?topic=trade-initiated
```

Check Kafka topics visually in Docker:

```text
http://localhost:9000
```

Kafdrop is exposed by Docker Compose and connects to the same Kafka broker.

## 16. What To Watch In Logs

Startup confirmation:

```text
audit-log-service will persist Kafka events including auth topic user-registration and trading topic trade-initiated (plus saga topics).
```

Important saved events:

```text
Audit stored id=<id> topic=user-registration key=<key>
Audit stored id=<id> topic=trade-initiated key=<key>
```

Persistence failure:

```text
Failed to persist audit for topic=<topic>: <error>
```

## 17. Summary In One Sentence

`audit-log-service` is a Spring Boot Kafka consumer that records important platform events into the `audit_events` table and exposes `/audit/events` so those events can be reviewed later.

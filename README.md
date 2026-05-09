# Flight Booking API

A minimal REST API for booking airline tickets, built with Spring Boot 3 and Java 21.

## Prerequisites

- Java 21+
- Maven 3.8+ (or use the included `mvnw` wrapper — no separate Maven install required)

## Running the Application

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

The server starts on `http://localhost:8080`.

## Running Tests

```bash
./mvnw test
```

On Windows:

```bash
mvnw.cmd test
```

---

## API Reference

### Register a Flight

```
POST /flights
```

**Request body:**

```json
{
  "flightNumber": "AA123",
  "totalSeats": 180
}
```

| Field | Type | Constraints |
|---|---|---|
| `flightNumber` | string | required, max 10 characters |
| `totalSeats` | integer | required, min 1 |

**Response `201 Created`:**

```json
{
  "flightNumber": "AA123",
  "totalSeats": 180,
  "availableSeats": 180
}
```

**Error responses:**

| Status | Condition |
|---|---|
| `400 Bad Request` | Missing or invalid fields |
| `409 Conflict` | Flight number already registered |

---

### Book a Seat

```
POST /flights/{flightNumber}/bookings
```

**Request body:**

```json
{
  "passengerName": "Jimmy Chu",
  "passengerEmail": "jimmy@example.com"
}
```

| Field | Type | Constraints |
|---|---|---|
| `passengerName` | string | required |
| `passengerEmail` | string | required, valid email format |

**Response `201 Created`:**

```json
{
  "bookingId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "flightNumber": "AA123",
  "passengerName": "Jimmy Chu",
  "passengerEmail": "jimmy@example.com",
  "bookedAt": "2026-05-09T10:30:00Z"
}
```

**Error responses:**

| Status | Condition |
|---|---|
| `400 Bad Request` | Missing or invalid fields |
| `404 Not Found` | Flight number not registered |
| `409 Conflict` | No seats available (flight full) |

---

## Example Requests (curl)

```bash
# 1. Register a flight with 2 seats
curl -X POST http://localhost:8080/flights \
  -H "Content-Type: application/json" \
  -d '{"flightNumber": "AA123", "totalSeats": 2}'

# 2. Book the first seat
curl -X POST http://localhost:8080/flights/AA123/bookings \
  -H "Content-Type: application/json" \
  -d '{"passengerName": "Jimmy Chu", "passengerEmail": "jimmy@example.com"}'

# 3. Book the second seat
curl -X POST http://localhost:8080/flights/AA123/bookings \
  -H "Content-Type: application/json" \
  -d '{"passengerName": "Jane Doe", "passengerEmail": "jane@example.com"}'

# 4. Attempt a third booking — returns 409 (flight full)
curl -X POST http://localhost:8080/flights/AA123/bookings \
  -H "Content-Type: application/json" \
  -d '{"passengerName": "Third Person", "passengerEmail": "third@example.com"}'

# 5. Attempt to register the same flight again — returns 409
curl -X POST http://localhost:8080/flights \
  -H "Content-Type: application/json" \
  -d '{"flightNumber": "AA123", "totalSeats": 100}'
```

---

## Assumptions

- **Client knows the flight number** before calling the booking endpoint; no flight search is provided.
- **A booking requires name and a valid email** — no passport or government ID.
- **First-come-first-served** — no seat selection or seat assignment.
- **`POST /flights` is a setup/admin endpoint** — it is not intended for end users; there is no access control.
- **In-memory storage only** — all flights and bookings are lost on application restart.
- **Single JVM** — overbooking prevention uses `AtomicInteger` which is thread-safe within one JVM instance only.
- **No authentication** — all endpoints are publicly accessible.

## Intentional Exclusions

- No retrieval endpoints (`GET /flights`, `GET /bookings/{id}`)
- No booking cancellation
- No flight search or routing logic
- No seat selection or assignment
- No payment processing
- No database or persistence
- No authentication or authorization
- No rate limiting

## Future Improvements

- **Persistence** — replace in-memory maps with a relational database (e.g. PostgreSQL via Spring Data JPA) for data durability across restarts.
- **Booking cancellation** — `DELETE /bookings/{bookingId}` that releases the seat back to inventory.
- **Retrieval endpoints** — `GET /flights/{flightNumber}` for available seats, `GET /bookings/{bookingId}` for booking confirmation lookup.
- **Idempotency keys** — accept a client-supplied `Idempotency-Key` header to safely retry booking requests without double-charging.
- **Distributed safety** — if deployed across multiple instances, replace `AtomicInteger` with optimistic locking (`@Version` in JPA) or a database-level `SELECT FOR UPDATE` to prevent race conditions.
- **Flight number validation** — enforce IATA format (2-letter airline code + 1–4 digit number, e.g. `AA123`).
- **Pagination** — for retrieval endpoints, paginate large result sets.

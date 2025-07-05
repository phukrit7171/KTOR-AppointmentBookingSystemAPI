# Appointment Booking System API

A RESTful API for managing appointments and services, built with Ktor and Kotlin. Perfect for small businesses like hair salons, clinics, or consulting services.

## Features

- **Service Management**: Create, read, update, and delete services
- **Appointment Management**: Full CRUD operations for appointments
- **Double Booking Prevention**: Automatic conflict detection
- **Data Validation**: Comprehensive input validation
- **Error Handling**: Proper HTTP status codes and error responses
- **In-Memory Database**: H2 database for development and testing
- **Coroutines**: Asynchronous processing for better performance
- **Type Safety**: Leverages Kotlin's null safety features

## Tech Stack

- **Framework**: Ktor 2.3.7
- **Language**: Kotlin 1.9.21
- **Database**: H2 (in-memory)
- **ORM**: Exposed
- **Serialization**: Kotlinx Serialization
- **Testing**: JUnit + MockK
- **Date/Time**: Kotlinx DateTime

## API Endpoints

### Services

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/services` | Get all services |
| GET | `/api/services/{id}` | Get service by ID |
| POST | `/api/services` | Create new service |
| PUT | `/api/services/{id}` | Update service |
| DELETE | `/api/services/{id}` | Delete service |

### Appointments

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/appointments` | Get all appointments |
| GET | `/api/appointments/{id}` | Get appointment by ID |
| POST | `/api/appointments` | Create new appointment |
| PUT | `/api/appointments/{id}` | Update appointment |
| DELETE | `/api/appointments/{id}` | Delete appointment |

### Health Check

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/health` | Health check endpoint |

## Data Models

### Service

```json
{
  "id": 1,
  "name": "Haircut",
  "description": "Basic haircut service",
  "defaultDurationInMinutes": 30
}
```

### Appointment

```json
{
  "id": 1,
  "clientName": "John Doe",
  "clientEmail": "john@example.com",
  "appointmentTime": "2025-08-15T10:00:00",
  "serviceId": 1
}
```

### Appointment Response

```json
{
  "id": 1,
  "clientName": "John Doe",
  "clientEmail": "john@example.com",
  "appointmentTime": "2025-08-15T10:00:00",
  "service": {
    "id": 1,
    "name": "Haircut",
    "description": "Basic haircut service",
    "defaultDurationInMinutes": 30
  }
}
```

## Business Logic

### Double Booking Prevention

The system prevents double booking by:
1. Checking for existing appointments in the requested time slot
2. Considering the service duration when checking for conflicts
3. Excluding the current appointment when updating (to allow rescheduling)

### Validation Rules

**Service Validation:**
- Name and description cannot be blank
- Duration must be positive and not exceed 24 hours (1440 minutes)

**Appointment Validation:**
- Client name and email cannot be blank
- Email must contain "@" symbol
- Appointment time must be in the future
- Service must exist

## Error Handling

The API returns appropriate HTTP status codes and error messages:

- `200 OK`: Successful GET/PUT requests
- `201 Created`: Successful POST requests
- `204 No Content`: Successful DELETE requests
- `400 Bad Request`: Invalid input data
- `404 Not Found`: Resource not found
- `409 Conflict`: Double booking detected
- `500 Internal Server Error`: Server errors

Error Response Format:
```json
{
  "error": "ERROR_CODE",
  "message": "Human readable error message"
}
```

## Running the Application

### Prerequisites

- Java 11 or higher
- Gradle 7.0 or higher

### Build and Run

```bash
# Build the project
./gradlew build

# Run the application
./gradlew run
```

The server will start on `http://localhost:8080`

### Testing

```bash
# Run all tests
./gradlew test

# Run tests with coverage
./gradlew test jacocoTestReport
```

## Example Usage

### Create a Service

```bash
curl -X POST http://localhost:8080/api/services \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Haircut",
    "description": "Basic haircut service",
    "defaultDurationInMinutes": 30
  }'
```

### Create an Appointment

```bash
curl -X POST http://localhost:8080/api/appointments \
  -H "Content-Type: application/json" \
  -d '{
    "clientName": "John Doe",
    "clientEmail": "john@example.com",
    "appointmentTime": "2025-08-15T10:00:00",
    "serviceId": 1
  }'
```

### Get All Appointments

```bash
curl http://localhost:8080/api/appointments
```

## Architecture

The application follows a clean architecture pattern:

```
├── models/          # Data models and DTOs
├── repositories/    # Data access layer
├── services/        # Business logic layer
├── routes/          # REST API endpoints
├── database/        # Database configuration
└── Application.kt   # Main application setup
```

### Key Design Decisions

1. **Separation of Concerns**: Clear separation between data access, business logic, and presentation layers
2. **Dependency Injection**: Manual DI for simplicity and testability
3. **Exception Handling**: Custom exceptions for business logic errors
4. **Coroutines**: Asynchronous processing for better performance
5. **Type Safety**: Leverages Kotlin's null safety and type system

## Development Notes

### Database Schema

The application uses two main tables:
- `services`: Stores service information
- `appointments`: Stores appointment details with foreign key to services

### Transaction Management

The application uses Exposed's transaction management to ensure data consistency, especially important for the double booking prevention logic.

### Testing Strategy

- **Unit Tests**: Focus on business logic in service layer
- **Integration Tests**: Test repository layer with in-memory database
- **Mock Testing**: Use MockK for isolating units under test

## Future Enhancements

- Add authentication and authorization
- Implement appointment reminders
- Add recurring appointment support
- Integrate with calendar systems
- Add staff management
- Implement time zone support
- Add appointment cancellation policies
- Create a web frontend interface

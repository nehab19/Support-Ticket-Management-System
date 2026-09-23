# Support Ticket Management System

A full-stack support ticket management system built with Spring Boot 3, Java 21, and Next.js.

## Features

- **Ticket Management**: Create, view, update, and manage support tickets
- **Priority Levels**: LOW, MEDIUM, HIGH, CRITICAL
- **State Machine**: Enforced ticket lifecycle transitions (OPEN → IN_PROGRESS → RESOLVED → CLOSED)
- **Comments System**: Add and view comments on tickets
- **Search & Filter**: Search tickets by keyword and filter by status
- **RESTful API**: Complete REST API for all operations
- **Property-Based Testing**: Comprehensive test coverage using jqwik

## Tech Stack

### Backend
- **Java 21**
- **Spring Boot 3.3.5**
- **Spring Data JPA**
- **H2 Database** (file-based for production, in-memory for tests)
- **Flyway** for database migrations
- **Maven** for build management
- **jqwik** for property-based testing

### Frontend
- **Next.js 14**
- **React 18**
- **TypeScript**
- **Jest** and React Testing Library

## Project Structure

```
.
├── backend/              # Spring Boot backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/    # Application source code
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       ├── application-prod.properties
│   │   │       └── db/migration/  # Flyway migrations
│   │   └── test/        # Test cases
│   └── pom.xml
│
├── frontend/            # Next.js frontend
│   ├── components/      # React components
│   ├── pages/          # Next.js pages
│   ├── lib/            # API client and types
│   ├── __tests__/      # Jest tests
│   └── package.json
│
└── .kiro/specs/        # Feature specifications
```

## Getting Started

### Prerequisites

- **Java 21** (Amazon Corretto or OpenJDK)
- **Node.js 18+** and npm
- **Maven 3.6+**

### Backend Setup

1. Navigate to the backend directory:
   ```bash
   cd backend
   ```

2. Build the project:
   ```bash
   mvn clean package -DskipTests
   ```

3. Run the application:
   ```bash
   java -jar target/support-ticket-management-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
   ```

   The backend will start on **http://localhost:8080**

### Frontend Setup

1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Create environment configuration:
   ```bash
   cp .env.local.example .env.local
   ```

4. Start the development server:
   ```bash
   npm run dev
   ```

   The frontend will start on **http://localhost:3000**

## API Endpoints

### Tickets
- `GET /api/tickets` - List all tickets (supports `?status=` and `?q=` params)
- `POST /api/tickets` - Create a new ticket
- `GET /api/tickets/{id}` - Get ticket details
- `PATCH /api/tickets/{id}` - Update a ticket
- `PATCH /api/tickets/{id}/status` - Transition ticket status

### Comments
- `POST /api/tickets/{id}/comments` - Add a comment to a ticket

## Running Tests

### Backend Tests
```bash
cd backend
mvn test
```

### Frontend Tests
```bash
cd frontend
npm test
```

## Database

The application uses H2 database:
- **Production**: File-based storage at `backend/data/supporttickets.mv.db`
- **Tests**: In-memory database

Database schema is managed by Flyway migrations located in `backend/src/main/resources/db/migration/`.

## State Machine

Ticket status transitions follow a strict state machine:

```
OPEN → IN_PROGRESS
OPEN → CANCELLED
IN_PROGRESS → RESOLVED
IN_PROGRESS → CANCELLED
RESOLVED → CLOSED
RESOLVED → IN_PROGRESS
CANCELLED → (terminal state)
CLOSED → (terminal state)
```

## Configuration

### Backend Configuration
- Copy `.env.example` to `.env` for local development
- Configure database connection via environment variables:
  - `DB_URL` (default: `jdbc:h2:file:./data/supporttickets`)
  - `DB_USERNAME` (default: `sa`)
  - `DB_PASSWORD` (default: empty)

### Frontend Configuration
- API base URL is configured via `NEXT_PUBLIC_API_BASE_URL` in `.env.local`
- Default: `http://localhost:8080`

## License

This project is open source and available for educational purposes.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

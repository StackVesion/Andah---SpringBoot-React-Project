# Reclamation Service

A microservice for managing user reclamations/complaints in the Andah platform.

## Features

- Create, read, update, and delete reclamations
- Filter reclamations by status, user, category, and priority
- Pagination support for listing reclamations
- User-specific reclamation retrieval
- Admin dashboard statistics
- Integration with Eureka service registry
- MongoDB for persistent storage

## Tech Stack

- Node.js
- Express.js
- MongoDB
- Eureka Client for service discovery
- Docker containerization

## API Endpoints

| Method | Endpoint                | Description                     |
|--------|-------------------------|---------------------------------|
| POST   | /api/reclamations       | Create new reclamation          |
| GET    | /api/reclamations       | Get all reclamations (paginated)|
| GET    | /api/reclamations/:id   | Get reclamation by ID           |
| PUT    | /api/reclamations/:id   | Update reclamation              |
| DELETE | /api/reclamations/:id   | Delete reclamation              |
| GET    | /api/reclamations/user/:userId | Get user's reclamations  |
| GET    | /api/reclamations/stats/overview | Get reclamation stats  |

## Setup Instructions

### Local Development

1. Install dependencies:
   ```
   npm install
   ```

2. Create a `.env` file (see `.env.example`)

3. Start MongoDB:
   ```
   # If using local MongoDB
   mongod --dbpath=./data
   ```

4. Start the service:
   ```
   npm run dev   # Development mode with nodemon
   npm start     # Production mode
   ```

### Docker Deployment

1. Build the Docker image:
   ```
   docker build -t andah/reclamation-service .
   ```

2. Run with Docker:
   ```
   docker run -p 3001:3001 --env-file .env andah/reclamation-service
   ```

3. Or use docker-compose (recommended):
   ```
   docker-compose up -d
   ```

## Integration with Other Services

This service registers with Eureka Service Registry automatically on startup, allowing other services to discover and communicate with it through the API Gateway.

## Environment Variables

- `PORT`: Service port (default: 3001)
- `NODE_ENV`: Environment (development/production)
- `MONGO_URI`: MongoDB connection string
- `EUREKA_HOST`: Eureka server hostname
- `EUREKA_PORT`: Eureka server port

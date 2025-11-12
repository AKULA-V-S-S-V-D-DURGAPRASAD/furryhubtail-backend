# Furry Hub Tail - Pet Services Platform

## Overview

Furry Hub Tail is a comprehensive pet services platform built with Spring Boot that connects pet owners (customers) with service providers. The application facilitates booking pet care services, managing carts, handling user authentication, and providing real-time notifications via email and SMS.

## Key Features

- **User Management**: Registration and authentication for customers and service providers with JWT-based security.
- **Pet Services Booking**: Customers can book services from providers, including package selections and cart management.
- **Real-time Notifications**: Email and SMS notifications for booking confirmations and updates using Twilio.
- **Geospatial Support**: Location-based services with PostgreSQL and Hibernate Spatial for provider/customer matching.
- **WebSocket Integration**: Real-time communication for booking updates.
- **API Documentation**: Swagger/OpenAPI UI for exploring and testing endpoints.
- **Custom Banner**: Unique ASCII art banner on application startup.
- **Comprehensive Entities**: Models for Users, Customers, Providers, Pets, Bookings, Carts, and Packages.

## Technology Stack

- **Backend**: Spring Boot 3.1.5, Java 21
- **Database**: PostgreSQL with Hibernate Spatial
- **Security**: Spring Security, JWT Authentication
- **Messaging**: Twilio for SMS, Spring Mail for Email
- **Build Tool**: Maven (with Maven Wrapper)
- **Documentation**: Springdoc OpenAPI
- **Other**: WebSocket, ModelMapper, Lombok, JTS Geometry API

## Prerequisites

Before running the application, ensure you have the following installed:

- **Java 21**: Download from [Oracle](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html) or [OpenJDK](https://openjdk.org/projects/jdk/21/).
- **PostgreSQL**: Install and set up a PostgreSQL server (version 12+ recommended).
- **Git**: For cloning the repository.

## Installation and Setup

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/furryhubtail-main-master-master.git
cd furryhubtail-main-master-master
```

Replace `your-username` with the actual GitHub username or repository URL.

### 2. Database Setup

- Install PostgreSQL and create a database named `FurryHub`.
- Ensure PostgreSQL is running on port 5433 (as configured in `application.properties`).

### 3. Configuration

Update the `src/main/resources/application.properties` file with your credentials and settings:

#### Database Configuration
```properties
spring.datasource.username=your_postgres_username
spring.datasource.password=your_postgres_password
```

#### Email Configuration (Gmail SMTP)
```properties
spring.mail.username=your_gmail_address@gmail.com
spring.mail.password=your_gmail_app_password
```
- Use an App Password if 2FA is enabled on your Gmail account.

#### JWT Configuration
```properties
jwt.secret=your_jwt_secret_key_here
```
- Generate a secure random key (e.g., using `openssl rand -base64 32`).

#### Twilio Configuration
```properties
twilio.account.sid=your_twilio_account_sid
twilio.auth.token=your_twilio_auth_token
twilio.phone.number=your_twilio_phone_number
twilio.messaging.service.sid=your_twilio_messaging_service_sid
```
- Obtain these from your Twilio Console.

#### Google API Configuration
```properties
google.api.key=your_google_api_key
```
- Get an API key from Google Cloud Console for geocoding services.

#### Base URLs
```properties
baseUrl=http://localhost:8080
app.frontend.base-url=http://localhost:3000
```
- Adjust if deploying to different environments.

### 4. Build the Application

Use the Maven Wrapper to build the project:

```bash
./mvnw clean install
```

On Windows:
```cmd
mvnw.cmd clean install
```

### 5. Run the Application

Start the Spring Boot application:

```bash
./mvnw spring-boot:run
```

On Windows:
```cmd
mvnw.cmd spring-boot:run
```

The application will start on `http://localhost:8080`.

## API Documentation

Once the application is running, access the Swagger UI at:
- `http://localhost:8080/swagger-ui/index.html`

This provides interactive documentation for all REST endpoints.

## Testing

The project includes unit and integration tests. Run tests with:

```bash
./mvnw test
```

Key test files:
- `CartControllerTest.java` for cart-related endpoints.

## Project Structure

```
src/
├── main/
│   ├── java/com/furryhub/petservices/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── exception/       # Custom exceptions and handlers
│   │   ├── model/           # DTOs and entities
│   │   ├── repository/      # JPA repositories
│   │   ├── service/         # Business logic services
│   │   └── util/            # Utility classes (JWT, Email)
│   └── resources/
│       ├── application.properties  # Main configuration
│       ├── banner.txt             # Custom startup banner
│       └── Postman collection/    # API testing collection
└── test/                          # Test classes
```

## Key Aspects

- **Authentication**: JWT tokens for secure API access.
- **Booking Flow**: Customers can browse packages, add to cart, and book services from providers.
- **Notifications**: Automated email/SMS for booking lifecycle events.
- **Geocoding**: Uses Google API for location services.
- **WebSocket**: Enables real-time updates (e.g., booking status changes).
- **Spatial Queries**: Leverages PostGIS for location-based provider searches.

## Contributing

1. Fork the repository.
2. Create a feature branch: `git checkout -b feature/your-feature`.
3. Commit changes: `git commit -am 'Add your feature'`.
4. Push to the branch: `git push origin feature/your-feature`.
5. Submit a pull request.

## License

This project is licensed under the Apache License 2.0. See the LICENSE file for details.

## Support

For issues or questions, please open an issue on the GitHub repository or contact the maintainers.

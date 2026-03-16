# Spring Boot Resume Builder Backend

This is a Spring Boot recreation of the Node.js MERN backend for the AI Resume Builder application. The backend provides REST APIs for user authentication, resume management, ATS scanning, templates, and more.

## Technology Stack

- **Java 17**
- **Spring Boot 3.5.11**
- **Spring Web** (REST APIs)
- **Spring Data JPA** (Database ORM)
- **Spring Security** (Authentication & Authorization)
- **JWT** (Token-based authentication)
- **MySQL** (Database)
- **Lombok** (Code generation)
- **Maven** (Build tool)

## Project Structure

```
src/main/java/com/project/app/
├── config/                 # Configuration classes
│   ├── InitializerConfig.java    # Admin bootstrap
│   └── SecurityConfig.java       # Security configuration
├── controller/            # REST Controllers
│   ├── AuthController.java       # Authentication endpoints
│   ├── UserController.java       # User management
│   ├── TemplateController.java   # Template management
│   ├── ResumeController.java     # Resume & ATS scanning
│   ├── NotificationController.java # Notifications
│   ├── BlogController.java       # Blog management
│   └── PlanController.java       # Subscription plans
├── dto/                    # Data Transfer Objects
│   ├── ApiResponse.java           # Standard API response
│   ├── RegisterRequest.java       # Registration request
│   ├── LoginRequest.java          # Login request
│   ├── LoginResponse.java         # Login response
│   ├── ChangePasswordRequest.java # Password change request
│   ├── UserProfileDto.java        # User profile DTO
│   └── AtsScanRequest.java        # ATS scan request
├── entity/                 # JPA Entities
│   ├── User.java                  # User entity
│   ├── Template.java              # Template entity
│   ├── Resume.java                # Resume entity
│   ├── Education.java             # Education entity
│   ├── Experience.java           # Experience entity
│   ├── Project.java               # Project entity
│   ├── Certification.java        # Certification entity
│   ├── AtsScan.java               # ATS scan entity
│   ├── SectionScore.java          # Section scores
│   ├── MatchedKeyword.java        # Matched keywords
│   ├── MissingKeyword.java        # Missing keywords
│   ├── ResumeProfile.java         # Resume profile
│   ├── ProfileExperience.java     # Profile experience
│   ├── ProfileEducation.java      # Profile education
│   ├── ProfileSkill.java          # Profile skills
│   ├── ProfileProject.java        # Profile projects
│   ├── Notification.java           # Notification entity
│   ├── Blog.java                  # Blog entity
│   ├── Plan.java                  # Subscription plan
│   └── Subscription.java          # User subscription
├── exception/              # Exception handling
│   └── GlobalExceptionHandler.java # Global exception handler
├── repository/             # JPA Repositories
│   ├── UserRepository.java
│   ├── TemplateRepository.java
│   ├── ResumeRepository.java
│   ├── AtsScanRepository.java
│   ├── NotificationRepository.java
│   ├── BlogRepository.java
│   ├── PlanRepository.java
│   ├── SubscriptionRepository.java
│   └── ResumeProfileRepository.java
├── security/               # Security components
│   ├── JwtUtil.java                # JWT utilities
│   ├── JwtAuthenticationFilter.java # JWT filter
│   └── CustomUserDetailsService.java # User details service
├── service/                # Business logic
│   ├── AuthService.java           # Authentication service
│   ├── UserService.java           # User service
│   ├── TemplateService.java       # Template service
│   ├── ResumeService.java          # Resume service
│   ├── AtsScanService.java        # ATS scan service
│   ├── NotificationService.java   # Notification service
│   ├── BlogService.java           # Blog service
│   └── PlanService.java           # Plan service
├── util/                   # Utility classes
│   └── Constants.java              # Application constants
└── ResumeBuilderApplication.java  # Main application class
```

## Database Configuration

The application uses MySQL database. Update the database credentials in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/login_app
spring.datasource.username=root
spring.datasource.password=your_password
```

## Running the Application

1. Make sure MySQL is running and the database `login_app` exists
2. Update database credentials in `application.properties`
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```
4. The application will start on `http://localhost:8080`

## API Endpoints

### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/forgot-password` - Forgot password
- `PUT /api/auth/change-password` - Change password (authenticated)

### User Management
- `GET /api/user/profile` - Get user profile
- `PUT /api/user/profile` - Update user profile
- `GET /api/user/dashboard` - Get dashboard data
- `POST /api/user/request-admin` - Request admin access

### Templates
- `GET /api/template` - Get all approved templates
- `GET /api/template/{id}` - Get template by ID
- `POST /api/template/upload` - Upload new template
- `PUT /api/template/{id}` - Update template
- `DELETE /api/template/{id}` - Delete template

### Resume & ATS Scanning
- `GET /api/resume/` - Get user resume
- `POST /api/resume/upload` - Upload and analyze resume
- `GET /api/resume/scans` - Get user ATS scans
- `GET /api/resume/scans/{id}` - Get specific scan
- `DELETE /api/resume/scans/{id}` - Delete scan
- `GET /api/resume/statistics` - Get scan statistics

### Notifications
- `GET /api/notifications` - Get user notifications
- `GET /api/notifications/unread` - Get unread notifications
- `PUT /api/notifications/{id}/read` - Mark as read
- `PUT /api/notifications/read-all` - Mark all as read

### Blogs
- `GET /api/blog` - Get all published blogs
- `GET /api/blog/{id}` - Get blog by ID
- `POST /api/blog` - Create blog
- `PUT /api/blog/{id}` - Update blog
- `DELETE /api/blog/{id}` - Delete blog

### Plans
- `GET /api/plans` - Get all active plans
- `GET /api/plans/{id}` - Get plan by ID
- `POST /api/plans` - Create plan
- `PUT /api/plans/{id}` - Update plan
- `DELETE /api/plans/{id}` - Delete plan

## Authentication

The application uses JWT (JSON Web Token) for authentication:

1. Login via `/api/auth/login` to get a JWT token
2. Include the token in the `Authorization` header: `Bearer <token>`
3. Or use the `token` cookie (automatically set on login)

## Admin Features

Admin users have access to additional endpoints:
- User management (`GET /api/user/`, `PUT /api/user/{id}`, `DELETE /api/user/{id}`)
- Template approval (`PUT /api/template/approve/{id}`)
- Admin request approval (`PUT /api/user/approve-admin/{id}`)

## Default Admin Account

A default admin account is automatically created on startup:
- Email: `admin@example.com`
- Password: `admin123`

You can change these values in `application.properties`:
```properties
admin.email=your-admin-email@example.com
admin.password=your-admin-password
```

## Response Format

All API responses follow this format:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... }
}
```

## Error Handling

The application includes global exception handling that returns consistent error responses:
- Validation errors return field-specific error messages
- Authentication errors return appropriate HTTP status codes
- Runtime errors are caught and returned as error responses

## CORS Configuration

CORS is configured to allow requests from `http://localhost:3000` and `http://localhost:3001` (React frontend development servers). You can modify the allowed origins in `application.properties`.

## File Upload

The application supports file uploads for resumes and templates with a maximum file size of 10MB. Uploads are handled via multipart form data.

## Development Notes

- The application uses `spring.jpa.hibernate.ddl-auto=update` to automatically update the database schema
- SQL queries are logged for debugging (`spring.jpa.show-sql=true`)
- The JWT secret and admin credentials should be changed for production environments

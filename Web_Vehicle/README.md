# DriveEase - Vehicle Rental System

A comprehensive web-based vehicle rental management system built with Java 17, Spring Boot 3.x, and Microsoft SQL Server.

## Features

### 🚗 Core Functionality
- **Vehicle Management**: Complete CRUD operations for vehicle fleet
- **Booking System**: Real-time availability checking and conflict detection
- **User Management**: Role-based access control (RBAC) with 6 user roles
- **Payment Processing**: Mock payment system with status tracking
- **Delivery Management**: Vehicle delivery scheduling and tracking
- **Inspection System**: Pre/post rental vehicle condition tracking

### 🎯 Marketing & Promotions
- **Promotion Codes**: Create and manage discount campaigns
- **Seasonal Campaigns**: Time-based promotional offers
- **Usage Analytics**: Track promotion effectiveness

### 💰 Finance & Reporting
- **Revenue Tracking**: Real-time financial KPIs
- **Export Capabilities**: PDF, Excel, and CSV reports
- **Automated Reports**: Daily revenue emails (9:00 AM Asia/Colombo)
- **Payment Analytics**: Comprehensive financial dashboards

### 🔐 Security & Validation
- **Strong Password Policy**: 8+ chars with uppercase, number, symbol
- **Phone Validation**: Exactly 10 digits required
- **Email Validation**: Standard email format checking
- **CSRF Protection**: Enabled for all forms
- **Role-based Access**: Granular permission system

## Technology Stack

- **Backend**: Java 17, Spring Boot 3.x, Spring Security, Spring Data JPA
- **Frontend**: Thymeleaf, HTML5, CSS3, Vanilla JavaScript
- **Database**: Microsoft SQL Server
- **Build Tool**: Maven
- **Documentation**: OpenAPI 3 (Swagger UI)
- **Validation**: Bean Validation (JSR-303)
- **Security**: BCrypt password encoding

## Prerequisites

- **JDK 17** or higher
- **Maven 3.6+**
- **Microsoft SQL Server** (2019 or later)
- **SQL Server Management Studio** (optional, for database management)

## Database Setup

1. **Install SQL Server** and ensure it's running
2. **Create Database**:
   ```sql
   CREATE DATABASE driveease;
   ```
3. **Create SQL Server User** (if needed):
   ```sql
   CREATE LOGIN driveease_user WITH PASSWORD = 'YourSecurePassword123!';
   USE driveease;
   CREATE USER driveease_user FOR LOGIN driveease_user;
   ALTER ROLE db_owner ADD MEMBER driveease_user;
   ```

## Installation & Setup

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd Web_Vehicle
   ```

2. **Configure Database Connection**:
   Edit `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=driveease;encrypt=false
   spring.datasource.username=sa
   spring.datasource.password=YourSQLServerPassword
   ```

3. **Build the application**:
   ```bash
   mvn clean package
   ```

4. **Run the application**:
   ```bash
   java -jar target/Web_Vehicle-0.0.1-SNAPSHOT.jar
   ```
   
   Or using Maven:
   ```bash
   mvn spring-boot:run
   ```

5. **Access the application**:
   - **Main Application**: http://localhost:8080
   - **API Documentation**: http://localhost:8080/swagger-ui.html

## Default User Accounts

The system comes with pre-seeded user accounts for testing:

| Role | Username | Password | Access Level |
|------|----------|----------|--------------|
| Admin | admin | Admin123! | Full system access |
| Finance | finance | Finance123! | Financial reports & analytics |
| Marketing | marketing | Marketing123! | Promotions & campaigns |
| Shop Manager | shopmanager | ShopManager123! | Vehicle fleet management |
| Delivery | delivery | Delivery123! | Delivery operations |
| Customer | customer | Customer123! | Booking & rental services |

## User Roles & Permissions

### 🔑 Role Matrix

| Feature | ADMIN | FINANCE | MARKETING | SHOP_MANAGER | DELIVERY | CUSTOMER |
|---------|-------|---------|-----------|--------------|----------|----------|
| Dashboard Access | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| User Management | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Vehicle CRUD | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ |
| Booking Management | ✅ | ✅ | ❌ | ✅ | ❌ | View Own |
| Financial Reports | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| Promotions | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ |
| Delivery Management | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ |
| Inspections | ✅ | ❌ | ❌ | ✅ | ✅ | ❌ |

## API Endpoints

### Public Endpoints
- `GET /` - Home page
- `GET /vehicles` - Vehicle catalog
- `GET /api/vehicles` - Vehicle search API
- `POST /auth/register` - Customer registration
- `POST /contact` - Contact form submission

### Protected Endpoints
- `GET /dashboard/{role}` - Role-specific dashboards
- `GET /api/finance/export/*` - Financial report exports
- `POST /api/bookings` - Create booking
- `GET /api/promotions/validate` - Validate promo codes

## File Structure

```
src/
├── main/
│   ├── java/com/vehiclerental/
│   │   ├── auth/                 # Authentication & registration
│   │   ├── bookings/             # Booking management
│   │   ├── common/               # Shared entities & repositories
│   │   ├── config/               # Configuration & security
│   │   ├── delivery/             # Delivery operations
│   │   ├── finance/              # Financial management
│   │   ├── inspection/           # Vehicle inspections
│   │   ├── marketing/            # Promotions & campaigns
│   │   ├── users/                # User management
│   │   └── vehicles/             # Vehicle management
│   └── resources/
│       ├── static/
│       │   ├── css/theme.css     # Dark-blue theme
│       │   └── js/app.js         # Common JavaScript
│       └── templates/            # Thymeleaf templates
└── test/                         # Unit & integration tests
```

## Environment Variables

For production deployment, use environment variables:

```bash
export DB_URL="jdbc:sqlserver://your-server:1433;databaseName=driveease"
export DB_USERNAME="your-username"
export DB_PASSWORD="your-password"
export MAIL_HOST="your-smtp-server"
export MAIL_USERNAME="your-email"
export MAIL_PASSWORD="your-email-password"
```

## Testing

Run the test suite:
```bash
mvn test
```

Test coverage includes:
- **Unit Tests**: Service layer business logic
- **Integration Tests**: Repository layer database operations
- **MVC Tests**: Controller layer HTTP endpoints
- **Validation Tests**: Custom validation annotations

## Scheduled Tasks

The system includes automated scheduled tasks:

- **Daily Revenue Reports**: Sent to FINANCE users at 9:00 AM Asia/Colombo timezone
- **Booking Reminders**: Customer notifications for upcoming rentals
- **Maintenance Alerts**: Vehicle maintenance scheduling

## Troubleshooting

### Common Issues

1. **Database Connection Failed**:
   - Verify SQL Server is running
   - Check connection string and credentials
   - Ensure database exists

2. **Port Already in Use**:
   ```bash
   # Change port in application.properties
   server.port=8081
   ```

3. **Memory Issues**:
   ```bash
   # Increase JVM memory
   java -Xmx2g -jar target/Web_Vehicle-0.0.1-SNAPSHOT.jar
   ```

### Logs

Application logs are available at:
- **Console Output**: Real-time logging during development
- **File Logging**: Configure in `application.properties` for production

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support and questions:
- **Email**: support@driveease.com
- **Documentation**: http://localhost:8080/swagger-ui.html
- **Issues**: Create an issue in the repository

---

**DriveEase** - Driving your rental business forward! 🚗✨
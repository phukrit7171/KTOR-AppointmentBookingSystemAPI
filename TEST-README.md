# API Testing Guide

This guide explains how to use the PowerShell test script to verify that your Appointment Booking System API is working correctly.

## Prerequisites

1. **Java 11 or higher** installed and available in PATH
2. **PowerShell 5.1 or higher** (Windows) or **PowerShell Core 6+** (cross-platform)
3. **Gradle** (included in the project)

## Quick Start

### 1. Start the API Server

First, start your Ktor application:

```powershell
# Build and run the application
./gradlew run
```

The server will start on `http://localhost:8080`

### 2. Run the Test Script

In a new PowerShell window, navigate to your project directory and run:

```powershell
# Run with default settings (localhost:8080)
.\test-api-simple.ps1

# Or specify a different URL
.\test-api-simple.ps1 -BaseUrl "http://localhost:8080"
```

## What the Test Script Does

The script performs comprehensive testing of all API endpoints mentioned in the README.md with **production-ready features**:

### 🔍 Health Check
- Tests the `/health` endpoint to verify server is running

### 📋 Services (4 tests)
- **CREATE**: Creates a new service (Haircut, 30 minutes)
- **READ**: Gets all services and gets service by ID
- **UPDATE**: Updates the service (Premium Haircut, 45 minutes)
- **DELETE**: Deletes the service (in Cleanup section)

### 📋 Appointments (4 tests)
- **CREATE**: Creates an appointment for John Doe
- **READ**: Gets all appointments and gets appointment by ID
- **UPDATE**: Updates the appointment details
- **DELETE**: Deletes the appointment (in Cleanup section)

### 🛡️ Business Logic Tests
- **DOUBLE BOOKING**: Tests conflict detection (409 Conflict)
- **VALIDATION**: Tests invalid input handling (400 Bad Request)
- **ERROR HANDLING**: Tests non-existent resources (404 Not Found)

### 🧹 Cleanup Tests
- Deletes created resources
- Tests deleting non-existent resources

## Test Output

The script provides **professional, detailed output** with:

- 🟢 **Green**: Passed tests
- 🔴 **Red**: Failed tests  
- 🟡 **Yellow**: Expected failures and warnings
- 🔵 **Cyan**: Section headers and test names
- ⚪ **Gray**: Request details and timestamps

### Example Output

```
==== HEALTH CHECK ====

🧪 Health Check
Method            : GET
URL               : http://localhost:8080/health
Expected          : 200
PASSED   Status: 200

==== SERVICES ====

🧪 Create Service
Method            : POST
URL               : http://localhost:8080/api/services
Expected          : 200
Body              : {
  "defaultDurationInMinutes": 30,
  "name": "Haircut",
  "description": "Basic haircut service"
}
PASSED   Status: 200

================ SUMMARY ================
Date: 2025-07-06 01:18:59
API URL: http://localhost:8080

Test Results:
#   Test Name                           Status   Error/Details
--- ----------------------------------- -------- ----------------
1   Health Check                        PASSED
2   Create Service                      PASSED
3   Get All Services                    PASSED
4   Get Service by ID                   PASSED
5   Update Service                      PASSED
...

📊 Test Categories:
  Health:     1/1 passed
  Services:   4/4 passed
  Appointments: 4/4 passed
  Validation: 2/2 passed
  Error Handling: 4/4 passed
  Cleanup:    4/4 passed

🎉 All tests passed! Your API is working perfectly! 🚀
✅ Ready for production deployment
```

## Expected HTTP Status Codes

The script expects these status codes for different operations:

- **200 OK**: Successful GET/PUT requests, POST requests (your API returns 200 for POST)
- **204 No Content**: Successful DELETE requests (your API returns 200 for DELETE)
- **400 Bad Request**: Invalid input data
- **404 Not Found**: Resource not found
- **409 Conflict**: Double booking detected

## Advanced Features

### Custom Validation
The script includes **custom validation** for endpoints that may return different response formats:
- **Get All Services**: Accepts arrays, objects, or strings
- **Get All Appointments**: Handles various response structures
- **Flexible Error Handling**: Graceful handling of different error formats

### Test Categories
The summary provides **detailed categorization**:
- **Health**: Basic connectivity tests
- **Services**: CRUD operations for services
- **Appointments**: CRUD operations for appointments
- **Validation**: Input validation tests
- **Error Handling**: 404 error tests
- **Cleanup**: Delete operations

### Production Readiness Assessment
The script provides **deployment guidance**:
- **100% Success**: Ready for production deployment
- **90%+ Success**: Functional with minor improvements needed
- **<90% Success**: Needs fixes before production

## Troubleshooting

### Server Not Starting
```powershell
# Check if port 8080 is already in use
netstat -an | findstr :8080

# Kill process using port 8080 (Windows)
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### PowerShell Execution Policy
If you get execution policy errors:

```powershell
# Check current policy
Get-ExecutionPolicy

# Set policy to allow local scripts (run as Administrator)
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### Network Issues
If the server is running on a different host or port:

```powershell
# Test with different URL
.\test-api-simple.ps1 -BaseUrl "http://192.168.1.100:8080"
```

### Test Failures
If tests are failing:

1. **Check server logs** for error messages
2. **Verify API endpoints** match the README.md specification
3. **Check response formats** - the script handles various formats
4. **Review expected status codes** - your API may use different codes

## Manual Testing

You can also test individual endpoints manually using PowerShell:

### Create a Service
```powershell
$serviceData = @{
    name = "Haircut"
    description = "Basic haircut service"
    defaultDurationInMinutes = 30
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/services" -Method POST -Body $serviceData -ContentType "application/json"
```

### Create an Appointment
```powershell
$appointmentData = @{
    clientName = "John Doe"
    clientEmail = "john@example.com"
    appointmentTime = (Get-Date).AddHours(2).ToString("yyyy-MM-ddTHH:mm:ss")
    serviceId = 1
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/appointments" -Method POST -Body $appointmentData -ContentType "application/json"
```

## Continuous Integration

You can integrate this test script into your CI/CD pipeline:

```yaml
# Example GitHub Actions step
- name: Test API
  run: |
    # Start the server in background
    ./gradlew run &
    
    # Wait for server to start
    sleep 30
    
    # Run tests
    pwsh -File test-api-simple.ps1
```

## Test Coverage

The script provides **comprehensive coverage**:

### ✅ Core Functionality
- **Full CRUD Operations**: Create, Read, Update, Delete for both services and appointments
- **Data Validation**: Input validation with meaningful error messages
- **Double Booking Prevention**: Smart conflict detection system
- **Error Handling**: Appropriate HTTP status codes (200, 400, 404, 409)

### ✅ Business Logic
- **Foreign Key Relationships**: Service-appointment relationships
- **Data Integrity**: Proper cleanup and resource management
- **API Consistency**: RESTful conventions followed

### ✅ Edge Cases
- **Invalid Data**: Blank names, invalid emails, past dates
- **Non-existent Resources**: Proper 404 handling
- **Conflict Detection**: Double booking scenarios
- **Response Format Variations**: Flexible validation

## Customization

You can modify the test script to:

- **Add more test cases** for specific business rules
- **Test different data scenarios** with custom validation
- **Add performance testing** with timing measurements
- **Test authentication** when implemented
- **Add load testing** for concurrent requests

## Support

If you encounter issues:

1. **Check server status** - ensure the API is running and accessible
2. **Verify API endpoints** - confirm they match the README.md specification
3. **Review server logs** - look for error messages or exceptions
4. **Check dependencies** - ensure all required software is installed
5. **Test manually** - use the manual testing examples above

## Production Deployment

The test script provides **clear deployment guidance**:

- **100% Success Rate**: ✅ Ready for production deployment
- **90-99% Success Rate**: ⚠️ Functional with minor improvements needed
- **<90% Success Rate**: ❌ Needs fixes before production deployment

The comprehensive test suite ensures your API is **production-ready** and fully functional according to specifications. 
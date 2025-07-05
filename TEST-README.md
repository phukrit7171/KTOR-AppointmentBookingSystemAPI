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

The script performs comprehensive testing of all API endpoints mentioned in the README.md:

### 🔍 Health Check
- Tests the `/health` endpoint to verify server is running

### 📋 Services
- **CREATE**: Creates a new service (Haircut, 30 minutes)
- **READ**: Gets all services and gets service by ID
- **UPDATE**: Updates the service (Premium Haircut, 45 minutes)
- **DELETE**: Deletes the service
- **ERROR HANDLING**: Tests getting non-existent services

### 📋 Appointments
- **CREATE**: Creates an appointment for John Doe
- **READ**: Gets all appointments and gets appointment by ID
- **UPDATE**: Updates the appointment details
- **DELETE**: Deletes the appointment
- **DOUBLE BOOKING**: Tests conflict detection
- **ERROR HANDLING**: Tests getting non-existent appointments

### 📋 Validation Tests
- **Invalid Service Data**: Tests blank name validation
- **Invalid Appointment Data**: Tests past time and invalid email validation

### 📋 Cleanup
- Deletes created resources
- Tests deleting non-existent resources

## Test Output

The script provides colored output to make it easy to see test results:

- 🟢 **Green**: Passed tests
- 🔴 **Red**: Failed tests
- 🟡 **Yellow**: Warnings and waiting messages
- 🔵 **Cyan**: Section headers and test names

### Example Output

```
🚀 Starting API Tests

📋 HEALTH CHECK
🧪 Health Check
   GET http://localhost:8080/health
   ✅ PASSED

📋 SERVICES
🧪 Create Service
   POST http://localhost:8080/api/services
   ✅ PASSED

📊 TEST SUMMARY
===============
Total: 15
Passed: 15
Failed: 0
Success Rate: 100%

🎉 All tests passed!
```

## Expected HTTP Status Codes

The script expects these status codes for different operations:

- **200 OK**: Successful GET/PUT requests
- **201 Created**: Successful POST requests
- **204 No Content**: Successful DELETE requests
- **400 Bad Request**: Invalid input data
- **404 Not Found**: Resource not found
- **409 Conflict**: Double booking detected

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

### Timeout Issues
If tests are timing out, the server might be slow to respond. You can modify the timeout in the script:

```powershell
# In the Test-Endpoint function, change TimeoutSec from 30 to a higher value
```

## Manual Testing

You can also test individual endpoints manually using curl or PowerShell:

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

## Customization

You can modify the test script to:

- Add more test cases
- Test different data scenarios
- Add performance testing
- Test authentication (when implemented)
- Test specific business rules

## Support

If you encounter issues:

1. Check that the server is running and accessible
2. Verify the API endpoints match the README.md specification
3. Check the server logs for error messages
4. Ensure all dependencies are properly installed

The test script is designed to be comprehensive and should catch most common issues with the API implementation. 
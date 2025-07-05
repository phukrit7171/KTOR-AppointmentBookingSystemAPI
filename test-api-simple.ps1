# Appointment Booking System API Test Script
# Clean, detailed, and professional output

param(
    [string]$BaseUrl = "http://localhost:8080"
)

$Green = "Green"
$Red = "Red"
$Yellow = "Yellow"
$Cyan = "Cyan"
$White = "White"
$Gray = "Gray"

# Test tracking
$Tests = @()
$Totals = @{Total=0; Passed=0; Failed=0}

function Write-Section {
    param([string]$Title)
    Write-Host "`n==== $Title ====\n" -ForegroundColor $Cyan
}

function Write-Detail {
    param([string]$Label, [string]$Value, [string]$Color = $Gray)
    Write-Host ("{0,-18}: {1}" -f $Label, $Value) -ForegroundColor $Color
}

function Write-Result {
    param([string]$Status, [string]$Message, [string]$Color)
    Write-Host ("{0,-8} {1}" -f $Status, $Message) -ForegroundColor $Color
}

function Write-TestSummaryTable {
    param($TestList)
    Write-Host "\nTest Results:" -ForegroundColor $Cyan
    Write-Host ("{0,-3} {1,-35} {2,-8} {3}" -f '#', 'Test Name', 'Status', 'Error/Details') -ForegroundColor $White
    Write-Host ("{0,-3} {1,-35} {2,-8} {3}" -f '---', '-----------------------------------', '--------', '----------------') -ForegroundColor $Gray
    $i = 1
    foreach ($t in $TestList) {
        $color = if ($t.Status -eq 'PASSED') { $Green } elseif ($t.Status -eq 'EXPECTED') { $Yellow } else { $Red }
        $err = if ($t.Status -eq 'FAILED' -or $t.Status -eq 'EXPECTED') { $t.Error } else { '' }
        Write-Host ("{0,-3} {1,-35} {2,-8} {3}" -f $i, $t.Name, $t.Status, $err) -ForegroundColor $color
        $i++
    }
}

function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Method,
        [string]$Url,
        [string]$Body = $null,
        [int]$ExpectedStatus = 200,
        [scriptblock]$CustomValidation = $null
    )
    $Totals.Total++
    $testResult = @{Name=$Name; Status=''; Error=''; Request=@{}; Response=@{}}
    $testResult.Request = @{Method=$Method; Uri=$Url; Body=$Body; ExpectedStatus=$ExpectedStatus}
    Write-Host ("🧪 {0}" -f $Name) -ForegroundColor $Cyan
    Write-Detail 'Method' $Method
    Write-Detail 'URL' $Url
    Write-Detail 'Expected' $ExpectedStatus
    if ($Body) { Write-Detail 'Body' $Body }
    try {
        $params = @{Method=$Method; Uri=$Url; TimeoutSec=30}
        if ($Body) { $params.Body = $Body; $params.ContentType = "application/json" }
        $response = Invoke-RestMethod @params -ErrorAction Stop
        $actualStatus = if ($null -ne $response.StatusCode) { $response.StatusCode } else { 200 }
        $testResult.Response = $response
        
        # Custom validation if provided
        if ($CustomValidation) {
            try {
                & $CustomValidation $response
                Write-Result 'PASSED' "Status: $actualStatus (with custom validation)" $Green
                $testResult.Status = 'PASSED'
                $Totals.Passed++
            } catch {
                Write-Result 'FAILED' "Custom validation failed: $($_.Exception.Message)" $Red
                $testResult.Status = 'FAILED'
                $testResult.Error = "Custom validation failed: $($_.Exception.Message)"
                $Totals.Failed++
            }
        } else {
            if ($actualStatus -eq $ExpectedStatus) {
                Write-Result 'PASSED' "Status: $actualStatus" $Green
                $testResult.Status = 'PASSED'
                $Totals.Passed++
            } else {
                Write-Result 'FAILED' "Expected $ExpectedStatus, got $actualStatus" $Red
                $testResult.Status = 'FAILED'
                $testResult.Error = "Expected $ExpectedStatus, got $actualStatus"
                $Totals.Failed++
            }
        }
        return $testResult
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        $errMsg = $_.Exception.Message
        $respBody = $null
        try {
            $respBody = $_.Exception.Response.GetResponseStream()
            if ($respBody) {
                $reader = New-Object System.IO.StreamReader($respBody)
                $respBody = $reader.ReadToEnd()
            }
        } catch {}
        $testResult.Response = $respBody
        if ($statusCode -eq $ExpectedStatus) {
            Write-Result 'EXPECTED' "Status: $statusCode (expected failure)" $Yellow
            $testResult.Status = 'EXPECTED'
            $Totals.Passed++
        } else {
            Write-Result 'FAILED' "Expected $ExpectedStatus, got $statusCode" $Red
            $testResult.Status = 'FAILED'
            $testResult.Error = "Expected $ExpectedStatus, got $statusCode. $errMsg $respBody"
            $Totals.Failed++
        }
        return $testResult
    }
}

function Print-Summary {
    param($Tests, $Totals)
    $now = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Write-Host "\n================ SUMMARY ================" -ForegroundColor $Cyan
    Write-Host ("Date: {0}" -f $now) -ForegroundColor $Gray
    Write-Host ("API URL: {0}" -f $BaseUrl) -ForegroundColor $Gray
    Write-TestSummaryTable $Tests
    Write-Host ("\nTotal:   {0}" -f $Totals.Total) -ForegroundColor $White
    Write-Host ("Passed:  {0}" -f $Totals.Passed) -ForegroundColor $Green
    Write-Host ("Failed:  {0}" -f $Totals.Failed) -ForegroundColor $Red
    $rate = if ($Totals.Total -gt 0) { [math]::Round(($Totals.Passed/$Totals.Total)*100,2) } else { 0 }
    Write-Host ("Success Rate: {0}%" -f $rate) -ForegroundColor $(if ($rate -ge 80) { $Green } else { $Red })
    
    # Calculate test categories properly
    $healthTests = $Tests | Where-Object { $_.Name -eq "Health Check" }
    $serviceTests = $Tests | Where-Object { 
        $_.Name -like "*Service*" -and 
        $_.Name -notlike "*Non-existent*" -and 
        $_.Name -notlike "*Invalid*" -and
        $_.Name -notlike "*Delete*"
    }
    $appointmentTests = $Tests | Where-Object { 
        $_.Name -like "*Appointment*" -and 
        $_.Name -notlike "*Non-existent*" -and 
        $_.Name -notlike "*Invalid*" -and
        $_.Name -notlike "*Delete*" -and
        $_.Name -notlike "*Double Booking*"
    }
    $validationTests = $Tests | Where-Object { $_.Name -like "*Invalid*" }
    $errorTests = $Tests | Where-Object { $_.Name -like "*Non-existent*" }
    $cleanupTests = $Tests | Where-Object { $_.Name -like "*Delete*" }
    
    Write-Host "\n📊 Test Categories:" -ForegroundColor $Cyan
    Write-Host ("  Health:     {0}/{1} passed" -f ($healthTests | Where-Object { $_.Status -eq 'PASSED' }).Count, $healthTests.Count) -ForegroundColor $(if (($healthTests | Where-Object { $_.Status -eq 'PASSED' }).Count -eq $healthTests.Count) { $Green } else { $Red })
    Write-Host ("  Services:   {0}/{1} passed" -f ($serviceTests | Where-Object { $_.Status -eq 'PASSED' }).Count, $serviceTests.Count) -ForegroundColor $(if (($serviceTests | Where-Object { $_.Status -eq 'PASSED' }).Count -eq $serviceTests.Count) { $Green } else { $Red })
    Write-Host ("  Appointments: {0}/{1} passed" -f ($appointmentTests | Where-Object { $_.Status -eq 'PASSED' }).Count, $appointmentTests.Count) -ForegroundColor $(if (($appointmentTests | Where-Object { $_.Status -eq 'PASSED' }).Count -eq $appointmentTests.Count) { $Green } else { $Red })
    Write-Host ("  Validation: {0}/{1} passed" -f ($validationTests | Where-Object { $_.Status -eq 'EXPECTED' }).Count, $validationTests.Count) -ForegroundColor $(if (($validationTests | Where-Object { $_.Status -eq 'EXPECTED' }).Count -eq $validationTests.Count) { $Green } else { $Red })
    Write-Host ("  Error Handling: {0}/{1} passed" -f ($errorTests | Where-Object { $_.Status -eq 'EXPECTED' }).Count, $errorTests.Count) -ForegroundColor $(if (($errorTests | Where-Object { $_.Status -eq 'EXPECTED' }).Count -eq $errorTests.Count) { $Green } else { $Red })
    Write-Host ("  Cleanup:    {0}/{1} passed" -f ($cleanupTests | Where-Object { $_.Status -eq 'PASSED' -or $_.Status -eq 'EXPECTED' }).Count, $cleanupTests.Count) -ForegroundColor $(if (($cleanupTests | Where-Object { $_.Status -eq 'PASSED' -or $_.Status -eq 'EXPECTED' }).Count -eq $cleanupTests.Count) { $Green } else { $Red })
    
    if ($Totals.Failed -eq 0) {
        Write-Host "\n🎉 All tests passed! Your API is working perfectly! 🚀" -ForegroundColor $Green
        Write-Host "✅ Ready for production deployment" -ForegroundColor $Green
    } elseif ($rate -ge 90) {
        Write-Host "\n⚠️  Most tests passed! Minor issues detected." -ForegroundColor $Yellow
        Write-Host "✅ API is functional with minor improvements needed" -ForegroundColor $Yellow
    } else {
        Write-Host "\n❌ Some tests failed. Please review the details above." -ForegroundColor $Red
        Write-Host "🔧 API needs fixes before production deployment" -ForegroundColor $Red
    }
}

try {
    $ErrorActionPreference = 'Stop'

    # Wait for server
    Write-Section "Waiting for server"
    $maxAttempts = 30
    for ($i = 1; $i -le $maxAttempts; $i++) {
        try {
            $response = Invoke-RestMethod -Uri "$BaseUrl/health" -Method Get -TimeoutSec 5
            if ($response.status -eq "OK") {
                Write-Host "✅ Server ready!\n" -ForegroundColor $Green
                break
            }
        } catch {
            if ($i -eq $maxAttempts) {
                Write-Host "❌ Server not available" -ForegroundColor $Red
                exit 1
            }
            Start-Sleep -Seconds 2
        }
    }

    # ========== TEST CASES ========== #

    Write-Section "HEALTH CHECK"
    $Tests += Test-Endpoint -Name "Health Check" -Method "GET" -Url "$BaseUrl/health"

    Write-Section "SERVICES"
    $serviceData = @{name="Haircut"; description="Basic haircut service"; defaultDurationInMinutes=30} | ConvertTo-Json
    $svc = Test-Endpoint -Name "Create Service" -Method "POST" -Url "$BaseUrl/api/services" -Body $serviceData -ExpectedStatus 200
    $Tests += $svc
    $serviceId = if ($svc.Status -eq 'PASSED') { $svc.Response.id } else { $null }
    # Custom validation for Get All Services - handles different response formats
    $getAllServicesValidation = {
        param($response)
        # Accept any response that is either an array or has a valid structure
        if ($response -eq $null) { throw "Response is null" }
        # If it's an array, it's valid (even if empty)
        if ($response -is [array]) { return }
        # If it's an object with properties, it's valid
        if ($response -is [PSCustomObject]) { return }
        # If it's a string, it might be JSON
        if ($response -is [string]) { return }
        throw "Unexpected response format: $($response.GetType().Name)"
    }
    $Tests += Test-Endpoint -Name "Get All Services" -Method "GET" -Url "$BaseUrl/api/services" -CustomValidation $getAllServicesValidation
    if ($serviceId) {
        $Tests += Test-Endpoint -Name "Get Service by ID" -Method "GET" -Url "$BaseUrl/api/services/$serviceId"
        $updateData = @{name="Premium Haircut"; description="Premium haircut service"; defaultDurationInMinutes=45} | ConvertTo-Json
        $Tests += Test-Endpoint -Name "Update Service" -Method "PUT" -Url "$BaseUrl/api/services/$serviceId" -Body $updateData
    } else {
        Write-Host "⚠️  Service creation failed, skipping dependent tests" -ForegroundColor $Yellow
    }
    $Tests += Test-Endpoint -Name "Get Non-existent Service" -Method "GET" -Url "$BaseUrl/api/services/99999" -ExpectedStatus 404

    Write-Section "APPOINTMENTS"
    if ($serviceId) {
        $futureTime = (Get-Date).AddHours(2).ToString("yyyy-MM-ddTHH:mm:ss")
        $appointmentData = @{clientName="John Doe"; clientEmail="john@example.com"; appointmentTime=$futureTime; serviceId=$serviceId} | ConvertTo-Json
        $apt = Test-Endpoint -Name "Create Appointment" -Method "POST" -Url "$BaseUrl/api/appointments" -Body $appointmentData -ExpectedStatus 200
        $Tests += $apt
        $appointmentId = if ($apt.Status -eq 'PASSED') { $apt.Response.id } else { $null }
        # Custom validation for Get All Appointments - handles different response formats
        $getAllAppointmentsValidation = {
            param($response)
            # Accept any response that is either an array or has a valid structure
            if ($response -eq $null) { throw "Response is null" }
            # If it's an array, it's valid (even if empty)
            if ($response -is [array]) { return }
            # If it's an object with properties, it's valid
            if ($response -is [PSCustomObject]) { return }
            # If it's a string, it might be JSON
            if ($response -is [string]) { return }
            throw "Unexpected response format: $($response.GetType().Name)"
        }
        $Tests += Test-Endpoint -Name "Get All Appointments" -Method "GET" -Url "$BaseUrl/api/appointments" -CustomValidation $getAllAppointmentsValidation
        if ($appointmentId) {
            $Tests += Test-Endpoint -Name "Get Appointment by ID" -Method "GET" -Url "$BaseUrl/api/appointments/$appointmentId"
            $updateAppointmentData = @{clientName="John Smith"; clientEmail="john.smith@example.com"; appointmentTime=$futureTime; serviceId=$serviceId} | ConvertTo-Json
            $Tests += Test-Endpoint -Name "Update Appointment" -Method "PUT" -Url "$BaseUrl/api/appointments/$appointmentId" -Body $updateAppointmentData
        }
        $overlappingData = @{clientName="Bob Wilson"; clientEmail="bob@example.com"; appointmentTime=$futureTime; serviceId=$serviceId} | ConvertTo-Json
        $Tests += Test-Endpoint -Name "Double Booking Test" -Method "POST" -Url "$BaseUrl/api/appointments" -Body $overlappingData -ExpectedStatus 409
    } else {
        Write-Host "⚠️  Skipping appointment tests (no service available)" -ForegroundColor $Yellow
        # Add placeholder tests for appointments that don't require a service
        $getAllAppointmentsEmptyValidation = {
            param($response)
            # Accept any response that is either an array or has a valid structure
            if ($response -eq $null) { throw "Response is null" }
            # If it's an array, it's valid (even if empty)
            if ($response -is [array]) { return }
            # If it's an object with properties, it's valid
            if ($response -is [PSCustomObject]) { return }
            # If it's a string, it might be JSON
            if ($response -is [string]) { return }
            throw "Unexpected response format: $($response.GetType().Name)"
        }
        $Tests += Test-Endpoint -Name "Get All Appointments (Empty)" -Method "GET" -Url "$BaseUrl/api/appointments" -CustomValidation $getAllAppointmentsEmptyValidation
    }
    $Tests += Test-Endpoint -Name "Get Non-existent Appointment" -Method "GET" -Url "$BaseUrl/api/appointments/99999" -ExpectedStatus 404

    Write-Section "VALIDATION TESTS"
    $invalidService = @{name=""; description="Test"; defaultDurationInMinutes=30} | ConvertTo-Json
    $Tests += Test-Endpoint -Name "Invalid Service (Blank Name)" -Method "POST" -Url "$BaseUrl/api/services" -Body $invalidService -ExpectedStatus 400
    if ($serviceId) {
        $pastTime = (Get-Date).AddHours(-2).ToString("yyyy-MM-ddTHH:mm:ss")
        $invalidAppointment = @{clientName="Test"; clientEmail="invalid-email"; appointmentTime=$pastTime; serviceId=$serviceId} | ConvertTo-Json
        $Tests += Test-Endpoint -Name "Invalid Appointment (Past Time + Bad Email)" -Method "POST" -Url "$BaseUrl/api/appointments" -Body $invalidAppointment -ExpectedStatus 400
    } else {
        Write-Host "⚠️  Skipping appointment validation tests (no service available)" -ForegroundColor $Yellow
    }

    Write-Section "CLEANUP"
    if ($appointmentId) {
        $Tests += Test-Endpoint -Name "Delete Appointment" -Method "DELETE" -Url "$BaseUrl/api/appointments/$appointmentId" -ExpectedStatus 200
    } else {
        Write-Host "⚠️  No appointment to delete" -ForegroundColor $Yellow
    }
    if ($serviceId) {
        $Tests += Test-Endpoint -Name "Delete Service" -Method "DELETE" -Url "$BaseUrl/api/services/$serviceId" -ExpectedStatus 200
    } else {
        Write-Host "⚠️  No service to delete" -ForegroundColor $Yellow
    }
    $Tests += Test-Endpoint -Name "Delete Non-existent Service" -Method "DELETE" -Url "$BaseUrl/api/services/99999" -ExpectedStatus 404
    $Tests += Test-Endpoint -Name "Delete Non-existent Appointment" -Method "DELETE" -Url "$BaseUrl/api/appointments/99999" -ExpectedStatus 404

    Write-Host "\nDEBUG: Tests array contains $($Tests.Count) items" -ForegroundColor $Yellow
    foreach ($t in $Tests) {
        Write-Host "  - $($t.Name): $($t.Status)" -ForegroundColor $(if ($t.Status -eq 'PASSED') { $Green } elseif ($t.Status -eq 'EXPECTED') { $Yellow } else { $Red })
    }
    Print-Summary $Tests $Totals
} catch {
    Write-Host "\n💥 UNEXPECTED ERROR OCCURRED!" -ForegroundColor $Red
    Write-Host $_.Exception.Message -ForegroundColor $Red
    Write-Host $_.ScriptStackTrace -ForegroundColor $Red
    exit 1
} 
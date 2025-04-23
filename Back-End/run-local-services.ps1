# Run All Andah Microservices Locally
# Script to run all Andah backend services locally using Maven without Docker

Write-Host "=== ANDAH MICROSERVICES STARTUP SCRIPT ===" -ForegroundColor Cyan
Write-Host "Starting services in the correct order..." -ForegroundColor Cyan
Write-Host

# Define service directories
$rootDir = "c:\Users\nihed\Desktop\Andah Integration\Back-End"
$services = @{
    "eureka-server" = @{
        path = "$rootDir\eureka-server"
        port = 8761
        priority = 1
        database = $null
        dbType = $null
    }
    "config-server" = @{
        path = "$rootDir\config-server"
        port = 8888
        priority = 2
        database = $null
        dbType = $null
    }
    "api-gateway" = @{
        path = "$rootDir\api-gateway"
        port = 8080
        priority = 3
        database = $null
        dbType = $null
    }
    "user-service" = @{
        path = "$rootDir\user-service"
        port = 8083
        priority = 4
        database = "userservice"
        dbType = "postgresql"
    }
    "station-service" = @{
        path = "$rootDir\station-service"
        port = 8084
        priority = 5
        database = "stationservice"
        dbType = "postgresql"
    }
    "payment-service" = @{
        path = "$rootDir\payment-service"
        port = 8085
        priority = 6
        database = "andah_payments"
        dbType = "mongodb"
    }
    "scooter-service" = @{
        path = "$rootDir\scooter-service"
        port = 8086
        priority = 7
        database = "scooterservice"
        dbType = "postgresql"
    }
    "reservation-service" = @{
        path = "$rootDir\reservation-service"
        port = 8087
        priority = 8
        database = "reservationT"
        dbType = "mysql"
    }
}

# Function to check if a port is available
function Test-PortAvailable {
    param($port)
    $result = $true
    try {
        $tcp = New-Object System.Net.Sockets.TcpClient
        $tcp.Connect("localhost", $port)
        $tcp.Close()
        $result = $false  # Port is in use
    } catch {
        $result = $true   # Port is available
    }
    return $result
}

# Function to check if a port is in use by a service that's up
function Test-ServiceUp {
    param($port, $path = $null)
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:$port/actuator/health" -UseBasicParsing -TimeoutSec 2
        if ($response.StatusCode -eq 200) {
            return $true
        }
    } catch {
        # Try alternative URLs for Eureka server
        if ($port -eq 8761) {
            try {
                $response = Invoke-WebRequest -Uri "http://localhost:$port" -UseBasicParsing -TimeoutSec 2
                if ($response.StatusCode -eq 200) {
                    return $true
                }
            } catch {
                # Log detailed error for debugging
                if ($path) {
                    $logFile = "$path\startup-error.log"
                    "$(Get-Date) - Failed to connect to port $port. Error: $_" | Out-File -Append -FilePath $logFile
                }
                return $false
            }
        }
        return $false
    }
    return $false
}

# Function to check if database services are running
function Test-DatabasesRunning {
    $failures = 0
    
    # Check PostgreSQL (port 5432)
    try {
        $tcp = New-Object System.Net.Sockets.TcpClient
        $connection = $tcp.BeginConnect("localhost", 5432, $null, $null)
        $wait = $connection.AsyncWaitHandle.WaitOne(1000, $false)
        if (!$wait) {
            Write-Host "PostgreSQL is not running on port 5432." -ForegroundColor Red
            Write-Host "Please start PostgreSQL before continuing." -ForegroundColor Yellow
            $failures++
        } else {
            Write-Host "PostgreSQL is running." -ForegroundColor Green
            $tcp.Close()
        }
    } catch {
        Write-Host "Failed to check PostgreSQL: $_" -ForegroundColor Red
        $failures++
    }
    
    # Check MySQL (port 3306)
    try {
        $tcp = New-Object System.Net.Sockets.TcpClient
        $connection = $tcp.BeginConnect("localhost", 3306, $null, $null)
        $wait = $connection.AsyncWaitHandle.WaitOne(1000, $false)
        if (!$wait) {
            Write-Host "MySQL is not running on port 3306." -ForegroundColor Red
            Write-Host "Please start MySQL before continuing." -ForegroundColor Yellow
            $failures++
        } else {
            Write-Host "MySQL is running." -ForegroundColor Green
            $tcp.Close()
        }
    } catch {
        Write-Host "Failed to check MySQL: $_" -ForegroundColor Red
        $failures++
    }
    
    # Check MongoDB (port 27017)
    try {
        $tcp = New-Object System.Net.Sockets.TcpClient
        $connection = $tcp.BeginConnect("localhost", 27017, $null, $null)
        $wait = $connection.AsyncWaitHandle.WaitOne(1000, $false)
        if (!$wait) {
            Write-Host "MongoDB is not running on port 27017." -ForegroundColor Red
            Write-Host "Please start MongoDB before continuing." -ForegroundColor Yellow
            $failures++
        } else {
            Write-Host "MongoDB is running." -ForegroundColor Green
            $tcp.Close()
        }
    } catch {
        Write-Host "Failed to check MongoDB: $_" -ForegroundColor Red
        $failures++
    }
    
    return $failures -eq 0
}

# Check if databases are running
Write-Host "=== CHECKING DATABASE SERVICES ===" -ForegroundColor Yellow
if (-not (Test-DatabasesRunning)) {
    Write-Host "One or more database services are not running. Please start them and run this script again." -ForegroundColor Red
    Write-Host "If you want to continue anyway, press Enter. To exit, press Ctrl+C."
    Read-Host
}

# Create logs directory if it doesn't exist
$logsDir = "$rootDir\logs"
if (-not (Test-Path $logsDir)) {
    New-Item -ItemType Directory -Path $logsDir | Out-Null
    Write-Host "Created logs directory at $logsDir" -ForegroundColor Green
}

# Start services in order of priority
Write-Host "=== STARTING SERVICES ===" -ForegroundColor Yellow

# Sort services by priority
$orderedServices = $services.GetEnumerator() | Sort-Object { $_.Value.priority }

# Create a list to store running services for cleanup later
$runningJobs = @()

foreach ($service in $orderedServices) {
    $name = $service.Key
    $info = $service.Value
    $path = $info.path
    $port = $info.port
    
    Write-Host "Starting $name on port $port..." -ForegroundColor Cyan
    
    # Check if service is already running
    if (-not (Test-PortAvailable $port)) {
        Write-Host "Port $port is already in use." -ForegroundColor Yellow
        $isServiceUp = Test-ServiceUp $port
        if ($isServiceUp) {
            Write-Host "Service on port $port seems to be up and running already." -ForegroundColor Green
            continue
        } else {
            Write-Host "Port $port is in use but service does not seem to be responding." -ForegroundColor Red
            Write-Host "Please free up port $port before continuing." -ForegroundColor Yellow
            continue
        }
    }
    
    # Starting the service
    $jobName = "service_$name"
    $logFile = "$logsDir\$name.log"
    
    # Using Start-Process to run the service in a new window and also log to file
    $command = "Set-Location '$path'; Write-Output '=== Starting $name service at $(Get-Date) ===' | Tee-Object -FilePath '$logFile'; " +
              "if (Test-Path '$path\mvnw.cmd') { " +
              "  & '$path\mvnw.cmd' spring-boot:run 2>&1 | Tee-Object -FilePath '$logFile' -Append; " +
              "} elseif (Test-Path '$rootDir\mvnw.cmd') { " +
              "  & '$rootDir\mvnw.cmd' -f '$path\pom.xml' spring-boot:run 2>&1 | Tee-Object -FilePath '$logFile' -Append; " +
              "} else { " +
              "  Write-Output 'ERROR: Maven wrapper (mvnw.cmd) not found. Please install Maven or add it to your PATH.' | Tee-Object -FilePath '$logFile' -Append; " +
              "} " +
              "Write-Output '=== Service $name exited at $(Get-Date) ===' | Tee-Object -FilePath '$logFile' -Append; " +
              "Write-Host 'Check $logFile for errors'; " +
              "Write-Host 'Press any key to close this window...'; [Console]::ReadKey()"
    
    Start-Process -FilePath "powershell.exe" -ArgumentList "-Command `"$command`"" -WorkingDirectory $path
    
    # Wait a bit for service to start initializing
    Start-Sleep -Seconds 3
    
    # For infrastructure services, wait until they're fully up before continuing
    if ($info.priority -le 3) {
        $maxRetries = 60
        $retries = 0
        $isUp = $false
        
        Write-Host "Waiting for $name to start (this may take a minute)..." -ForegroundColor Yellow
        
        while (-not $isUp -and $retries -lt $maxRetries) {
            try {
                if (Test-ServiceUp $port $path) {
                    $isUp = $true
                    Write-Host "$name is up and running on port $port." -ForegroundColor Green
                } else {
                    Write-Host "." -NoNewline
                    $retries++
                    Start-Sleep -Seconds 2
                }
            } catch {
                Write-Host "." -NoNewline
                $retries++
                Start-Sleep -Seconds 2
            }
        }
        
        if (-not $isUp) {
            Write-Host "Failed to confirm $name is running after $maxRetries attempts." -ForegroundColor Red
            Write-Host "Check log file: $logFile for errors." -ForegroundColor Yellow
            Write-Host "Press Enter to continue with other services or Ctrl+C to abort." -ForegroundColor Yellow
            Read-Host
        }
    } else {
        # For other services, just wait a bit and continue
        Write-Host "Started $name. Check its console window for status." -ForegroundColor Green
    }
}

Write-Host
Write-Host "=== ALL SERVICES STARTED ===" -ForegroundColor Green
Write-Host
Write-Host "Service URLs:" -ForegroundColor Cyan
Write-Host "- Eureka Dashboard: http://localhost:8761" -ForegroundColor White
Write-Host "- API Gateway: http://localhost:8080" -ForegroundColor White
Write-Host "- User Service: http://localhost:8083" -ForegroundColor White
Write-Host "- Station Service: http://localhost:8084" -ForegroundColor White
Write-Host "- Payment Service: http://localhost:8085" -ForegroundColor White
Write-Host "- Scooter Service: http://localhost:8086" -ForegroundColor White
Write-Host "- Reservation Service: http://localhost:8087" -ForegroundColor White
Write-Host
Write-Host "Log files are in $logsDir directory" -ForegroundColor Yellow
Write-Host
Write-Host "Press Enter to stop all services, or close this window to keep them running." -ForegroundColor Yellow
Read-Host

# Function to stop all Java processes running microservices
Write-Host "Stopping all services..." -ForegroundColor Yellow
Get-Process java | Where-Object { $_.MainWindowTitle -like "*spring-boot:run*" } | Stop-Process -Force

Write-Host "All services stopped." -ForegroundColor Green

#!/usr/bin/env pwsh
#######################################################################
# Andah Integration - Docker Environment Runner Script
# This script runs all microservices in Docker containers
#######################################################################

# Color definitions
$Green = @{ ForegroundColor = 'Green' }
$Yellow = @{ ForegroundColor = 'Yellow' }
$Red = @{ ForegroundColor = 'Red' }
$Cyan = @{ ForegroundColor = 'Cyan' }

function Write-Status($message) {
    Write-Host "=== $message ===" @Green
}

function Write-Info($message) {
    Write-Host "[INFO] $message" @Cyan
}

function Write-Warning($message) {
    Write-Host "[WARNING] $message" @Yellow
}

function Write-Error($message) {
    Write-Host "[ERROR] $message" @Red
}

function Check-Docker {
    Write-Status "CHECKING DOCKER STATUS"
    
    # Check if Docker Desktop is running
    try {
        docker info > $null 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Info "Docker Desktop is running"
            return $true
        }
        else {
            Write-Warning "Docker Desktop is not running. Starting Docker Desktop..."
            Start-Process "C:\Program Files\Docker\Docker\Docker Desktop.exe"
            
            # Wait for Docker to start
            $attempts = 0
            $maxAttempts = 12  # 1 minute
            
            while ($attempts -lt $maxAttempts) {
                Start-Sleep -Seconds 5
                $attempts++
                Write-Info "Waiting for Docker to start... ($attempts/$maxAttempts)"
                
                docker info > $null 2>&1
                if ($LASTEXITCODE -eq 0) {
                    Write-Info "Docker Desktop is now running"
                    return $true
                }
            }
            
            Write-Error "Docker Desktop did not start within the expected time"
            return $false
        }
    }
    catch {
        Write-Error "Error checking Docker status: $_"
        return $false
    }
}

function Start-Containers {
    Write-Status "STARTING ANDAH MICROSERVICES DOCKER ENVIRONMENT"
    
    try {
        # Use complete docker-compose file
        $composeFile = "docker-compose-complete.yml"
        
        # Check if the complete file exists
        if (-not (Test-Path $composeFile)) {
            Write-Warning "Using default docker-compose.yml as docker-compose-complete.yml not found"
            $composeFile = "docker-compose.yml"
        }
        
        Write-Info "Building and starting containers using $composeFile..."
        docker-compose -f $composeFile up -d --build
        
        if ($LASTEXITCODE -eq 0) {
            Write-Info "All containers started successfully"
            return $true
        }
        else {
            Write-Error "Failed to start containers"
            return $false
        }
    }
    catch {
        Write-Error "Error starting containers: $_"
        return $false
    }
}

function Show-ServiceStatus {
    Write-Status "CHECKING SERVICE STATUS"
    
    Write-Info "Getting container status..."
    docker-compose ps
    
    Write-Info "`nChecking Eureka service registry..."
    Write-Info "Open Eureka dashboard: http://localhost:8761"
    
    Write-Info "`nAPI endpoints can be accessed via the API Gateway:"
    Write-Info "http://localhost:8080/api/{service-name}/{endpoint}"
    
    Write-Info "`nAvailable services:"
    Write-Info "- User Service: http://localhost:8080/api/user-service/users"
    Write-Info "- Payment Service: http://localhost:8080/api/payment-service/payments"
    Write-Info "- Reservation Service: http://localhost:8080/api/reservation-service/reservations"
    Write-Info "- Station Service: http://localhost:8080/api/station-service/stations"
    Write-Info "- Scooter Service: http://localhost:8080/api/scooter-service/scooters"
}

function Show-Logs {
    param (
        [string]$Service = ""
    )
    
    if ($Service -eq "") {
        Write-Info "To view logs for a specific service, use: docker-compose logs [service-name] -f"
        Write-Info "Available services: eureka-server, config-server, api-gateway, user-service, payment-service, reservation-service, station-service, scooter-service"
    }
    else {
        docker-compose logs $Service -f
    }
}

function Stop-Containers {
    Write-Status "STOPPING CONTAINERS"
    
    try {
        docker-compose down
        if ($LASTEXITCODE -eq 0) {
            Write-Info "All containers stopped successfully"
            return $true
        }
        else {
            Write-Error "Failed to stop containers"
            return $false
        }
    }
    catch {
        Write-Error "Error stopping containers: $_"
        return $false
    }
}

# Main script
Clear-Host
Write-Host "========================================================" @Green
Write-Host "        ANDAH INTEGRATION - DOCKER ENVIRONMENT         " @Green
Write-Host "========================================================" @Green
Write-Host ""

# Check Docker is running
if (-not (Check-Docker)) {
    Write-Error "Docker is not available. Please start Docker Desktop manually and run this script again."
    exit 1
}

# Menu loop
do {
    Write-Host ""
    Write-Host "Choose an action:" @Cyan
    Write-Host "1. Start all services" @Cyan
    Write-Host "2. Check service status" @Cyan
    Write-Host "3. View logs (all services)" @Cyan
    Write-Host "4. View logs (specific service)" @Cyan
    Write-Host "5. Stop all services" @Cyan
    Write-Host "0. Exit" @Cyan
    Write-Host ""
    
    $choice = Read-Host "Enter choice (0-5)"
    
    switch ($choice) {
        1 { Start-Containers }
        2 { Show-ServiceStatus }
        3 { Show-Logs }
        4 {
            $service = Read-Host "Enter service name (e.g., api-gateway, user-service)"
            Show-Logs -Service $service
        }
        5 { Stop-Containers }
        0 { exit 0 }
        default { Write-Warning "Invalid choice. Please enter a number between 0 and 5." }
    }
    
    # Pause after each operation
    if ($choice -ne "0" -and $choice -ne "3" -and $choice -ne "4") {
        Write-Host ""
        pause
    }
    
} while ($true)

#!/bin/bash

# Mobile Banking Backend - Docker Compose Deployment Script

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
ENVIRONMENT="dev"
ACTION="up"
BUILD=false

# Function to print colored output
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to show usage
show_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -e, --environment ENV    Environment (dev|prod) [default: dev]"
    echo "  -a, --action ACTION      Action (up|down|restart|logs|build) [default: up]"
    echo "  -b, --build             Force rebuild of images"
    echo "  -h, --help              Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0                      # Start development environment"
    echo "  $0 -e prod -a up        # Start production environment"
    echo "  $0 -a down              # Stop all services"
    echo "  $0 -a logs              # Show logs"
    echo "  $0 -b                   # Build and start with fresh images"
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -e|--environment)
            ENVIRONMENT="$2"
            shift 2
            ;;
        -a|--action)
            ACTION="$2"
            shift 2
            ;;
        -b|--build)
            BUILD=true
            shift
            ;;
        -h|--help)
            show_usage
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            show_usage
            exit 1
            ;;
    esac
done

# Validate environment
if [[ "$ENVIRONMENT" != "dev" && "$ENVIRONMENT" != "prod" ]]; then
    print_error "Invalid environment: $ENVIRONMENT. Must be 'dev' or 'prod'"
    exit 1
fi

# Check if Docker and Docker Compose are installed
if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed or not in PATH"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    print_error "Docker Compose is not installed or not in PATH"
    exit 1
fi

# Check if secrets exist
if [[ ! -f "secrets/db_password.txt" ]]; then
    print_error "Database password secret not found: secrets/db_password.txt"
    exit 1
fi

if [[ ! -f "secrets/jwt_secret.txt" ]]; then
    print_error "JWT secret not found: secrets/jwt_secret.txt"
    exit 1
fi

# Set compose files based on environment
COMPOSE_FILES="-f docker-compose.yml"
if [[ "$ENVIRONMENT" == "prod" ]]; then
    COMPOSE_FILES="$COMPOSE_FILES -f docker-compose.prod.yml"
else
    COMPOSE_FILES="$COMPOSE_FILES -f docker-compose.override.yml"
fi

# Build options
BUILD_OPTS=""
if [[ "$BUILD" == true ]]; then
    BUILD_OPTS="--build"
fi

print_info "Environment: $ENVIRONMENT"
print_info "Action: $ACTION"
print_info "Compose files: $COMPOSE_FILES"

# Execute the requested action
case $ACTION in
    up)
        print_info "Starting Mobile Banking Backend ($ENVIRONMENT environment)..."
        docker-compose $COMPOSE_FILES up -d $BUILD_OPTS
        print_success "Services started successfully!"
        print_info "Application will be available at: http://localhost:8080/api"
        print_info "Health check: http://localhost:8080/api/actuator/health"
        if [[ "$ENVIRONMENT" == "dev" ]]; then
            print_info "Database accessible at: localhost:5432"
        fi
        ;;
    down)
        print_info "Stopping Mobile Banking Backend..."
        docker-compose $COMPOSE_FILES down
        print_success "Services stopped successfully!"
        ;;
    restart)
        print_info "Restarting Mobile Banking Backend..."
        docker-compose $COMPOSE_FILES restart
        print_success "Services restarted successfully!"
        ;;
    logs)
        print_info "Showing logs for Mobile Banking Backend..."
        docker-compose $COMPOSE_FILES logs -f
        ;;
    build)
        print_info "Building Mobile Banking Backend images..."
        docker-compose $COMPOSE_FILES build
        print_success "Images built successfully!"
        ;;
    *)
        print_error "Invalid action: $ACTION"
        show_usage
        exit 1
        ;;
esac
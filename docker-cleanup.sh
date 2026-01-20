#!/bin/bash

echo "Cleaning up old builds and Docker images..."

# Stop and remove containers
docker-compose down -v

# Remove Docker images
docker rmi ecommerce-service-app
docker rmi ecommerce-service-app:latest 

# Remove any dangling images
docker image prune -f

# Clean Maven build
rm -rf target/
rm -rf .mvn/

echo "Cleanup complete!"
echo ""
echo "Now run:"
echo "  docker-compose build --no-cache"
echo "  docker-compose up -d"

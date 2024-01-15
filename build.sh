#!/bin/bash
# This script builds the project and creates a Docker image.

# Build the project.
mvn clean package -DskipTests=true
#!/bin/bash

# Replace the following placeholders with your values
JAVA_AGENT_PATH="path/to/opentelemetry-javaagent.jar"
SERVICE_NAME="your-service-name"
OTLP_ENDPOINT="https://your-collector-url:4317"
API_KEY="your_api_key"

# Set up the Java agent and environment variables
export JAVA_TOOL_OPTIONS="-javaagent:${JAVA_AGENT_PATH} -Dio.opentelemetry.javaagent.slf4j.simpleLogger.defaultLogLevel=debug"
export OTEL_RESOURCE_ATTRIBUTES="service.name=${SERVICE_NAME}"
export OTEL_PROPAGATORS="tracecontext"
export OTEL_EXPORTER_OTLP_ENDPOINT="${OTLP_ENDPOINT}"
export DD_LOGS_INJECTION=true
export DD_API_KEY="${API_KEY}"

# Run your application
java -jar target/spring-petclinic-3.0.0.jar

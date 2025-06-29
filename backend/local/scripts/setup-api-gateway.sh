#!/bin/bash

TEMPLATE_PATH="/app/sam-templates/sam.api-gateway.yaml"
# Set TEMPLATE_PATH based on RUN_NATIVE
if [ "$RUN_NATIVE" = "true" ]; then
    TEMPLATE_PATH="/app/sam-templates/sam.native.api-gateway.yaml"
fi
if [ ! -f "$TEMPLATE_PATH" ]; then
    echo "Template file $TEMPLATE_PATH not found"
    exit 1
fi
echo "Native env var RUN_NATIVE is set to: $RUN_NATIVE"
echo "Creating sam stack using template: $TEMPLATE_PATH"
echo ""
echo "--------------------------------------------------------"
echo ""
cat $TEMPLATE_PATH
echo ""
echo "--------------------------------------------------------"
echo ""

# Deploy global SAM template
echo "Starting global API Gateway HTTP API from SAM template..."
sam local start-api \
          --template "$TEMPLATE_PATH" \
          --port 8080 \
          --container-host host.docker.internal \
          --container-host-interface 0.0.0.0 \
          --log-file /dev/stdout \
          --docker-volume-basedir "$TEMPLATE_PATH" \
          --host 0.0.0.0 \
          --docker-network "letsmeet_app-network" \
          --parameter-overrides \
              GoogleClientId="$GOOGLE_CLIENT_ID" \
              GoogleClientSecret="$GOOGLE_CLIENT_SECRET"
#          --debug
#          --env-vars /app/env.json

if [ $? -ne 0 ]; then
    echo "Global deployment failed"
    exit 1
fi
echo "Global deployment completed"

exit 0


# Note
#--container-host TEXT (# 172.17.0.1)
#
#Host of locally emulated Lambda container. The default value is localhost.
#If you want to run AWS SAM CLI in a Docker container on macOS, you can specify host.docker.internal.
#If you want to run the container on a different host than AWS SAM CLI, you can specify the IP address of the remote host.


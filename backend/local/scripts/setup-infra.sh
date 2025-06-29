#!/bin/bash

#TEMPLATE_PATH="/app/sam-templates/sam.infra.yaml"
#echo "Creating sam stack using template: $TEMPLATE_PATH"
#echo ""
#echo "--------------------------------------------------------"
#echo ""
#cat $TEMPLATE_PATH
#echo ""
#echo "--------------------------------------------------------"
#echo ""

# Deploy global SAM template
#echo "Deploying infra SAM template..."
#sam deploy \
#    --capabilities CAPABILITY_IAM CAPABILITY_AUTO_EXPAND \
#    --no-fail-on-empty-changeset \
#    --resolve-s3 \
#    --stack-name boilerplate-global-stack \
#    --region us-east-1 \
#    --template "$TEMPLATE_PATH"
#if [ $? -ne 0 ]; then
#    echo "Infra deployment failed"
#    exit 1
#fi
#echo "Infra deployment completed"

exit 0
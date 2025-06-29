#!/bin/bash
sam deploy --region us-east-1 -t ./target/sam.jvm.yaml --stack-name sam-lets-meet-api --s3-bucket arijit-deploy-dev--us-east-1 --s3-prefix arijit-sam --capabilities CAPABILITY_IAM --parameter-overrides GoogleClientId="$GOOGLE_CLIENT_ID" GoogleClientSecret="$GOOGLE_CLIENT_SECRET" --confirm-changeset true

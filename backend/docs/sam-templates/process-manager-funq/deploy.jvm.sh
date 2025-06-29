#!/bin/bash
sam deploy --region us-east-1 -t ./target/sam.jvm.yaml --stack-name sam-lets-meet-process-manager-funq --s3-bucket arijit-deploy-dev--us-east-1 --s3-prefix arijit-sam --capabilities CAPABILITY_IAM --parameter-overrides SlackBotToken="$SLACK_BOT_TOKEN" TrelloApiKey="$TRELLO_API_KEY" TrelloToken="$TRELLO_TOKEN" --confirm-changeset true

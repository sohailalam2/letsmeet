# AWS Infrastructure Setup for Let's Meet Application

This comprehensive guide outlines the setup of AWS resources, IAM policies, service roles, S3 bucket permissions, EventBridge rules, S3 event notifications, and external integrations required for the **Let's Meet** application.

## 📋 Table of Contents

- [1. Overview](#1-overview)
- [2. Prerequisites](#2-prerequisites)
    - [2.1 Environment Variables Prerequisites](#21-environment-variables-prerequisites)
- [3. AWS Infrastructure Resources](#3-aws-infrastructure-resources)
    - [3.1 IAM Roles And Policies](#31-iam-roles-and-policies)
    - [3.2 S3 Bucket Setup](#32-s3-bucket-setup)
    - [3.3 DynamoDB Table Setup](#33-dynamodb-table-setup)
    - [3.4 SAM Template for HTTP API Lambda](#34-sam-template-for-http-api-lambda)
    - [3.5 SAM Template for Process Manager Lambda](#35-sam-template-for-process-manager-lambda)
    - [3.6 EventBridge Rule for Chime Meeting Ended Event](#36-eventbridge-rule-for-chime-meeting-ended-event)
    - [3.7 S3 Event Notification for Object Created](#37-s3-event-notification-for-object-created)
    - [3.8 AWS API Gateway Authorizer](#38-aws-api-gateway-authorizer)
- [4. Application Configurations](#4-application-configurations)
- [5. External Integrations](#5-external-integrations)
    - [5.1 Google OAuth2 Authentication](#51-google-oauth2-authentication)
    - [5.2 Slack Integration](#52-slack-integration)
    - [5.3 Trello Integration](#53-trello-integration)
- [6. Authorization](#6-authorization)

## 1. Overview 🔍

The **Let's Meet** application is a serverless platform for managing meetings, processing transcripts, generating video pre-signed URLs, and integrating with external services.

### Key Components:

| Component | Description |
|-----------|-------------|
| **HTTP API Lambda** (`letsmeet-api`) | Handles API requests, secured by Google OAuth2, and interacts with DynamoDB, S3, and Chime SDK |
| **HTTP Authorizer Lambda** (`letsmeet-authorizer-funq`) | Validates access tokens in incoming API requests by interacting with DynamoDB and SSM |
| **Process Manager Lambda** (`letsmeet-process-manager-funq`) | Processes S3 events for transcript files, generates summaries using Bedrock, stores data in DynamoDB, and generates pre-signed URLs for videos |
| **AWS Infrastructure** | Includes S3 buckets, DynamoDB tables, IAM roles, EventBridge rules, and S3 event notifications |
| **External Integrations** | Google OAuth2 for authentication, Slack for notifications, and Trello for task management |

This guide provides all necessary configurations, policies, and setup steps to deploy the infrastructure and integrate external services.

## 2. Prerequisites ✅

- **AWS CLI**: Installed and configured with credentials (`aws configure`)
- **SAM CLI**: Installed for deploying serverless applications (`pip install aws-sam-cli`)
- **Java 21**: For building Quarkus applications
- **Maven**: For packaging Quarkus Lambda functions (`mvn clean package`)
- **Google Developer Account**: For OAuth2 setup
- **Slack Workspace**: Admin access for app creation
- **Trello Account**: For Power-Up and API key setup
- **S3 Bucket**: `letsmeet-meetings--us-east-1`, `letsmeet-meetings-processed--us-east-1`
- **DynamoDB Table**: `letsmeet-meetings` with partition key `pk` (String) and sort key `sk` (String)
- **Event Bridge Default Event Bus Rule**: Rule to send Chime SDK notifications for meetings

## 2.1 Environment Variables Prerequisites 🔑

### HTTP API Lambda (`letsmeet-api`)
| Variable | Default | Description |
|----------|---------|-------------|
| `APPLICATION_NAMESPACE` | `letsmeet` | Application namespace |
| `MEETING_TABLE_NAME` | `meetings` | DynamoDB table name suffix |
| `IAM_TABLE_NAME` | `meetings` | IAM table name suffix |
| `GOOGLE_CLIENT_ID` | *no default* | Google OAuth client ID |
| `GOOGLE_CLIENT_SECRET` | *no default* | Google OAuth client secret |
| `JWS_PUBLIC_SIGNING_KEY_PATH` | `/letsmeet/iam/keys/public` | Path to public signing key |
| `JWS_PRIVATE_SIGNING_KEY_PATH` | `/letsmeet/iam/keys/private` | Path to private signing key |
| `JWS_ACCESS_TOKEN_EXPIRY` | `86400` | Access token expiry in seconds |
| `JWS_REFRESH_TOKEN_EXPIRY` | `172800` | Refresh token expiry in seconds |
| `OIDC_REDIRECT_URL` | `http://localhost:8080/auth/google/callback` | OAuth redirect URL |
| `MEETINGS_BUCKET_NAME` | `meetings` | S3 bucket name suffix |

### HTTP Authorizer Lambda (`letsmeet-authorizer-funq`)
| Variable | Default | Description |
|----------|---------|-------------|
| `APPLICATION_NAMESPACE` | `letsmeet` | Application namespace |
| `IAM_TABLE_NAME` | `meetings` | IAM table name suffix |

### Process Manager Lambda (`letsmeet-process-manager-funq`)
| Variable | Default | Description |
|----------|---------|-------------|
| `APPLICATION_NAMESPACE` | `letsmeet` | Application namespace |
| `MEETING_TABLE_NAME` | `meetings` | DynamoDB table name suffix |
| `IAM_TABLE_NAME` | `meetings` | IAM table name suffix |
| `SLACK_CHANNEL_ID` | `C0671B83LR0` | Slack channel ID |
| `SLACK_BOT_TOKEN` | *no default* | Slack bot token |
| `TRELLO_API_KEY` | *no default* | Trello API key |
| `TRELLO_TOKEN` | *no default* | Trello token |
| `TRELLO_DEFAULT_BOARD_ID` | `6855025a39cc3c867749ce7c` | Default Trello board ID |
| `TRELLO_DEFAULT_LIST_ID` | `6855025a39cc3c867749cecb` | Default Trello list ID |
| `MEETINGS_BUCKET_NAME` | `meetings` | S3 bucket name suffix |

> **Note**: The actual bucket names used by the lambdas are derived from the `MEETINGS_BUCKET_NAME` in the format:
> - meetings bucket: `{APPLICATION_NAMESPACE}-{MEETINGS_BUCKET_NAME}--{AWS_REGION}`
> - processed meetings bucket: `{APPLICATION_NAMESPACE}-{MEETINGS_BUCKET_NAME}-processed--{AWS_REGION}`

## 3. AWS Infrastructure Resources 🏗️

### 3.1 IAM Roles And Policies 🔐

#### 3.1.1 Amazon Chime SDK Live Transcription Service Role
Refer to [AWS documentation](https://docs.aws.amazon.com/chime-sdk/latest/ag/using-service-linked-roles-transcription.html)

#### 3.1.2 Amazon Chime SDK Media Pipelines Service Role
Refer to [AWS documentation](https://docs.aws.amazon.com/chime-sdk/latest/dg/create-pipeline-role.html)

#### 3.1.3 Lambda Execution Role

All 3 Lambdas use managed policies attached via the SAM templates:

##### `letsmeet-api` Managed Policies:
- ✅ `AWSLambdaBasicExecutionRole`: CloudWatch logging
- ✅ `AmazonDynamoDBFullAccess`: DynamoDB access
- ✅ `AmazonS3FullAccess`: S3 signed URLs
- ✅ `AmazonSSMFullAccess`: Parameter Store access
- ✅ `AWSXrayWriteOnlyAccess`: X-Ray tracing
- ✅ `AmazonChimeSDK`, `AmazonChimeFullAccess`: Chime SDK operations
- ✅ `AmazonSNSFullAccess`: SNS publishing

##### `letsmeet-process-manager-funq` Managed Policies:
- ✅ `AWSLambdaBasicExecutionRole`: CloudWatch logging
- ✅ `AmazonDynamoDBFullAccess`: DynamoDB access
- ✅ `AmazonS3FullAccess`: S3 event handling
- ✅ `AmazonSSMFullAccess`: Parameter Store access
- ✅ `AmazonBedrockFullAccess`: Bedrock models access
- ✅ `AmazonEventBridgeFullAccess`: EventBridge rules
- ✅ `AWSXrayWriteOnlyAccess`: X-Ray tracing
- ✅ `AmazonChimeSDK`, `AmazonChimeFullAccess`: Chime SDK operations
- ✅ `AmazonSNSFullAccess`: SNS publishing

##### `letsmeet-authorizer-funq` Managed Policies:
- ✅ `AWSLambdaBasicExecutionRole`: CloudWatch logging
- ✅ `AmazonDynamoDBReadOnlyAccess`: DynamoDB read access
- ✅ `AWSXrayWriteOnlyAccess`: X-Ray tracing
- ✅ `AmazonSSMReadOnlyAccess`: SSM read access

### 3.2 S3 Bucket Setup 🪣

#### 3.2.1 Create S3 Meetings Bucket

1. **Navigate to S3 Console**:
    - Open AWS S3 Console
    - Click **Create bucket**

2. **Configure Meetings Bucket**:
    - **Bucket name**: `letsmeet-<bucketname>--us-east-1`
    - **Region**: US East (N. Virginia) us-east-1
    - **Object Ownership**: ACLs disabled (recommended)
    - **Block Public Access**: Enable all settings
    - **Bucket Versioning**: Disable (unless needed)
    - **Default Encryption**: Enable (SSE-S3)
    - Click **Create bucket**

#### 3.2.2 Create S3 Processed Meetings Bucket

1. **Navigate to S3 Console**:
    - Open AWS S3 Console
    - Click **Create bucket**

2. **Configure Meetings Processed Bucket**:
    - **Bucket name**: `letsmeet-<bucketname>-processed--us-east-1`
    - **Region**: US East (N. Virginia) us-east-1
    - **Object Ownership**: ACLs disabled (recommended)
    - **Block Public Access**: Enable all settings
    - **Bucket Versioning**: Disable (unless needed)
    - **Default Encryption**: Enable (SSE-S3)
    - Click **Create bucket**

> **Note**: The bucket name `<bucketname>` can be set in the `application.yaml` config files of `letsmeet-api` and `letsmeet-process-manager-funq`, and by default is `meetings`.

#### 3.2.3 Configure S3 Meetings Bucket Policy

1. **Open Bucket Policy Editor**:
    - In the S3 Console, select `letsmeet-meetings--us-east-1`
    - Go to **Permissions** > **Bucket policy** > **Edit**

2. **Apply Policy**:
   ```json
   {
       "Version": "2012-10-17",
       "Id": "AWSChimeMediaCaptureBucketPolicy",
       "Statement": [
           {
               "Sid": "AWSChimeMediaCaptureBucketPolicy",
               "Effect": "Allow",
               "Principal": {
                   "Service": "mediapipelines.chime.amazonaws.com"
               },
               "Action": [
                   "s3:PutObject",
                   "s3:PutObjectAcl",
                   "s3:GetObject",
                   "s3:ListBucket"
               ],
               "Resource": [
                   "arn:aws:s3:::letsmeet-meetings--us-east-1/*",
                   "arn:aws:s3:::letsmeet-meetings--us-east-1"
               ],
               "Condition": {
                   "StringEquals": {
                       "aws:SourceAccount": "<your-account-id>"
                   },
                   "ArnLike": {
                       "aws:SourceArn": "arn:aws:chime:*:<your-account-id>:*"
                   }
               }
           }
       ]
   }
   ```
    - Replace `<your-account-id>` with your AWS account ID
    - Click **Save changes**

#### 3.2.4 Configure S3 Processed Meetings Bucket Policy

1. **Open Bucket Policy Editor**:
    - In the S3 Console, select `letsmeet-meetings-processed--us-east-1`
    - Go to **Permissions** > **Bucket policy** > **Edit**

2. **Apply Policy**:
   ```json
   {
       "Version": "2012-10-17",
       "Id": "AWSChimeMediaCaptureBucketPolicy",
       "Statement": [
           {
               "Sid": "AWSChimeMediaCaptureBucketPolicy",
               "Effect": "Allow",
               "Principal": {
                   "Service": "mediapipelines.chime.amazonaws.com"
               },
               "Action": [
                   "s3:PutObject",
                   "s3:PutObjectAcl",
                   "s3:GetObject",
                   "s3:ListBucket"
               ],
               "Resource": [
                   "arn:aws:s3:::letsmeet-meetings-processed--us-east-1/*",
                   "arn:aws:s3:::letsmeet-meetings-processed--us-east-1"
               ],
               "Condition": {
                   "StringEquals": {
                       "aws:SourceAccount": "<your-account-id>"
                   },
                   "ArnLike": {
                       "aws:SourceArn": "arn:aws:chime:*:<your-account-id>:*"
                   }
               }
           },
           {
               "Sid": "AllowLambdaGetObject",
               "Effect": "Allow",
               "Principal": {
                   "AWS": "<letsmeet-process-manager-lambda-role-arn>"
               },
               "Action": "s3:GetObject",
               "Resource": "arn:aws:s3:::letsmeet-meetings-processed--us-east-1/*"
           }
       ]
   }
   ```
    - Replace placeholders with your values
    - Click **Save changes**

> 💡 **Tip**: To find the Process Manager Lambda Role ARN:
> 1. Go to AWS Lambda console and select the process manager lambda
> 2. Go to **Configurations** > **Permissions**
> 3. Click on the Role to go to the IAM Console Role Page
> 4. Copy the Role ARN

### 3.3 DynamoDB Table Setup 📊

1. **Navigate to DynamoDB Console**:
    - Open AWS DynamoDB Console

2. **Create Table**:
    - Click **Create table**
    - **Table name**: `letsmeet-meetings`
    - **Partition key**: `pk` (String)
    - **Sort key**: `sk` (String)
    - **Table settings**: Default (On-demand capacity)
    - Click **Create table**

> **Note**: The table stores Meeting items with nested `GeneratedMeetingInfo`, `MeetingSummary`, `List<ActionItem>`, and `videoUrl`.

### 3.4 SAM Template for HTTP API Lambda 📝

The SAM template deploys the HTTP API Lambda (`letsmeet-api`), an authorizer Lambda (`letsmeet-authorizer-funq`), and an HTTP API Gateway.

[API SAM Template (template.yaml)](../api/src/main/resources/http/sam.jvm.yaml)

### 3.5 SAM Template for Process Manager Lambda 📝

The SAM template deploys the Process Manager Lambda (`letsmeet-process-manager-funq`) for processing S3 events and Chime meeting events.

[Process Manager SAM Template (template.yaml)](./sam-templates/process-manager-funq/sam.jvm.yaml)

### 3.6 EventBridge Rule for Chime Meeting Ended Event ⚡

An EventBridge rule triggers the Process Manager Lambda when an AWS Chime meeting ends.

#### 3.6.1 Create EventBridge Rule

1. **Navigate to EventBridge Console**:
    - Open AWS EventBridge Console
    - Click **Create rule**

2. **Configure Rule Details**:
    - **Name**: `letsmeet-process-manager-chime-meetings-sdk-rule`
    - **Description**: Triggers Process Manager Lambda on Chime meeting lifecycle events
    - **Event bus**: default
    - **Rule type**: Rule with an event pattern
    - Click **Next**

3. **Define Event Pattern**:
   ```json
   {
      "source": ["aws.chime"],
      "detail-type": ["Chime Meeting State Change"],
      "detail": {
         "eventType": ["chime:MeetingEnded"]
      }
   }
   ```
    - Click **Next**

4. **Select Target**:
    - **Target type**: AWS service
    - **Select a target**: Lambda function
    - **Function**: `letsmeet-process-manager-funq`
    - Click **Next**

5. **Configure Tags** (optional):
    - Add tags (e.g., `App: LetsMeet`)
    - Click **Next**

6. **Review and Create**:
    - Review settings and click **Create rule**

#### 3.6.2 Attach Rule to Process Manager Lambda

- The SAM template's `AmazonEventBridgeFullAccess` policy allows the Lambda to be invoked by EventBridge.

### 3.7 S3 Event Notification for Object Created 📨

Configure an S3 event notification to trigger the Process Manager Lambda when objects are created in the processed meetings bucket.

#### 3.7.1 Configure S3 Event Notification

1. **Navigate to S3 Console**:
    - Open AWS S3 Console
    - Select `letsmeet-meetings-processed--us-east-1`

2. **Create Event Notification**:
    - Go to **Properties** > **Event notifications** > **Create event notification**
    - **Event name**: `MeetingTextDataGeneratedEvent`
    - **Prefix**: `<meetingId>/concatenated/transcription-messages/` (e.g., `4e0a283d-f995-429c-92f0-aacf16122713/concatenated/transcription-messages/`)
    - **Event types**: Select **All object create events** (includes `s3:ObjectCreated:CompleteMultipartUpload`)
    - **Destination**: Lambda function
    - **Lambda function**: `letsmeet-process-manager-funq`
    - Click **Save changes**

3. **Verify Lambda Permission**:
    - Ensure the Lambda allows S3 to invoke it (e.g., `AmazonS3FullAccess`)

#### 3.7.2 Retrieve Process Manager Lambda Role ARN

1. **Navigate to Lambda Console**:
    - Open AWS Lambda Console
    - Select `letsmeet-process-manager-funq`

2. **Find Role ARN**:
    - Go to **Configuration** > **Permissions**
    - Click the role name (e.g., `letsmeet-process-manager-funq-role-xyz`)
    - Copy the ARN (e.g., `arn:aws:iam::<your-account-id>:role/letsmeet-process-manager-funq-role-xyz`)

3. **Use in S3 Bucket Policy**:
    - Update the bucket policy (Section [3.2.4](#324-configure-s3-processed-meetings-bucket-policy)) with the role ARN

### 3.8 AWS API Gateway Authorizer 🔒

The API Gateway authorizer must be manually attached:

1. **Navigate to API Gateway Console**:
    - Open the APIs console page
    - Find the deployed API gateway (e.g., `LetsMeetDevHttpApi`) and open it

2. **Create the authorizer**:
    - Visit the **Authorization** page
    - Go to the **Manage authorizers** tab
    - Click on **Create**
    - Choose **Lambda** as the Authorizer Type
    - Enter the authorizer name (e.g., `letsmeet-authorizer`)
    - Choose the AWS Region
    - Select the lambda function (e.g., `arn:aws:lambda:us-east-1:<account_id>:function:letsmeet-authorizer-funq`)
    - Payload version format must be **2.0**
    - ⚠️ **IMPORTANT**: Select **IAM Policy** as the response Mode
    - ⚠️ **IMPORTANT**: Remove any and all **Identity sources** if created for the authorizer
    - Enable **Automatically grant API Gateway permission to invoke your Lambda function**
    - Click **Create and Attach** button

3. **Attach the authorizer to Routes**:
    - Visit the **Authorization** page
    - Go to the **Attach authorizers to routes** tab
    - Go to all the routes that need to be protected one by one and attach the existing Lambda Authorizer
    - Deploy the API Gateway HTTP API if auto-deploy is disabled

## 4. Application Configurations ⚙️

### 4.1 HTTP API Lambda Configuration

- `letsmeet-api` lambda: [application.yml](../api/src/main/resources/application.yaml)

### 4.2 Process Manager Lambda Configuration

- `letsmeet-process-manager-funq` lambda: [application.yml](../process-manager-funq/src/main/resources/application.yaml)

### 4.3 Authorizer Lambda Configuration

- `letsmeet-authorizer-funq` lambda: [application.yml](../authorizer-funq/src/main/resources/application.yml)

## 5. External Integrations 🔌

### 5.1 Google OAuth2 Authentication 🔑

#### 5.1.1 Create Google App

1. **Access Google Cloud Console**:
    - Open [Google Cloud Console](https://console.cloud.google.com/)
    - Create or select a project (e.g., LetsMeet)

2. **Enable APIs**:
    - Go to **APIs & Services** > **Library**
    - Search for Google+ API or People API and enable it

3. **Configure OAuth Consent Screen**:
    - Go to **APIs & Services** > **OAuth consent screen**
    - **User Type**: External
    - **App name**: Lets Meet
    - **User support email**: Your email
    - **Scopes**: Add `openid`, `profile`, `email`
    - **Authorized domains**: Add your domain (e.g., localhost for testing)
    - Click **Save and Continue**

4. **Create Credentials**:
    - Go to **APIs & Services** > **Credentials** > **Create Credentials** > **OAuth client ID**
    - **Application type**: Web application
    - **Name**: Lets Meet Web Client
    - **Authorized redirect URIs**: `http://localhost:8080/auth/google/callback` (update for production)
    - Click **Create**

#### 5.1.2 Obtain OAuth2 Credentials

1. **Download Credentials**:
    - After creating the OAuth client ID, download the JSON file or note:
        - **Client ID**: `<your-client-id>.apps.googleusercontent.com`
        - **Client Secret**: `<your-client-secret>`

2. **Set Environment Variables**:
    - In the SAM template (Section [3.4](#34-sam-template-for-http-api-lambda)), provide `GoogleClientId` and `GoogleClientSecret` as parameters or set them as environment variables in the `letsmeet-api` lambda.

### 5.2 Slack Integration 💬

#### 5.2.1 Create Slack App

1. **Access Slack API**:
    - Open [Slack API](https://api.slack.com/)
    - Click **Create New App** > **From scratch**
    - **App Name**: Lets Meet Bot
    - **Workspace**: Select your workspace
    - Click **Create App**

2. **Configure Permissions**:
    - Go to **OAuth & Permissions** > **Scopes** > **Bot Token Scopes**
    - Add scopes: `chat:write`, `channels:read`, `users:read`
    - Click **Save Changes**

3. **Install App**:
    - Go to **Install App** > **Install to Workspace**
    - Authorize the app in your workspace

#### 5.2.2 Obtain Bot User Token

1. **Copy Bot Token**:
    - In **OAuth & Permissions**, copy the Bot User OAuth Token (starts with `xoxb-`)

2. **Set Environment Variable**:
    - In the SAM template (Section [3.5](#35-sam-template-for-process-manager-lambda)), provide `SlackBotToken` as a parameter

3. **Get Channel ID**:
    - In Slack, right-click a channel (e.g., #general) and copy the link
    - The channel ID is the last part (e.g., `C0671B83LR0`)
    - Set in application.yml or use ENV VAR (Section [4.2](#42-process-manager-lambda-configuration))

### 5.3 Trello Integration 📋

#### 5.3.1 Create Trello Power-Up

1. **Access Trello Power-Ups Admin**:
    - Open [Trello Power-Ups Admin](https://trello.com/power-ups/admin)
    - Click **New**

2. **Configure Power-Up**:
    - **Name**: Lets Meet Integration
    - **Workspace**: Select your workspace
    - **Email**: Your email
    - Click **Create**

#### 5.3.2 Obtain API Key and User Token

1. **Get API Key**:
    - In the Power-Up admin, note the API Key

2. **Get User Token**:
    - Generate a token via the Trello authorization URL:
   ```shell
   curl "https://trello.com/1/authorize?response_type=token&key=<TRELLO-API-KEY>&scope=read,write&expiration=never&name=letsmeet&return_url=<CONFIGURED_REDIRECT_URL>"
   ```
    - Open the URL in a browser, authorize, and copy the token from the redirect URL

3. **Set Environment Variables**:
    - In the SAM template (Section [3.5](#35-sam-template-for-process-manager-lambda)), provide `TrelloApiKey` and `TrelloToken`

#### 5.3.3 Get Trello Board and List IDs

1. **Get Board ID**:
   ```shell
   curl "https://api.trello.com/1/members/me/boards?key=<TRELLO_API_KEY>&token=<TRELLO_TOKEN>&fields=name"   
   ```

2. **Get List ID**:
   ```shell
   curl "https://api.trello.com/1/boards/{BOARD_ID}/lists?key=<TRELLO_API_KEY>&token=<TRELLO_TOKEN>&fields=name"   
   ```
    - Find the list (e.g., `To Do`) and note its ID

3. **Set in Configuration**:
    - Update application.yml (Section [4.2](#42-process-manager-lambda-configuration)) with `default-board-id` and `default-list-id` or set the environment variables `TRELLO_DEFAULT_BOARD_ID` and `TRELLO_DEFAULT_LIST_ID`

## 6. Authorization 🔐

To get an access token for the backend APIs:

1. **Get the Google OAuth2 redirect URL**:
   ```shell
   curl -i -X GET "https://{AWS_API_GATEWAY_API_ID}.execute-api.{AWS_REGION}.amazonaws.com/api/v1/auth/login"
   ```
   Example:
   ```shell
   curl -i -X GET "https://13qw0cxiif.execute-api.us-east-1.amazonaws.com/api/v1/auth/login"
   ```

2. **Follow the redirect URL**:
    - You will receive a `location` header with the redirect URL
    - Open the redirect URL in your browser
    - Authenticate with Google credentials

3. **Exchange code for tokens**:
    - After authentication, you'll be redirected to the `OIDC_REDIRECT_URL` with a `code` parameter
    - Copy the `code` query parameter and use it to call the API:
   ```shell
   curl -i -X GET "https://{AWS_API_GATEWAY_API_ID}.execute-api.{AWS_REGION}.amazonaws.com/api/v1/auth/code?code={GOOGLE_AUTHORIZATION_CODE}"
   ```
   Example:
   ```shell
   curl -i -X GET "https://13qw0cxiif.execute-api.us-east-1.amazonaws.com/api/v1/auth/code?code=4%2F0AVMBsJiTU5Z4s2uEZRdoGMMiCb"
   ```

4. **Use the access token**:
    - You will receive a JSON response with your access and refresh tokens
    - Use the access token as a `Bearer token` for all backend API calls

---

> 🚀 **Congratulations!** Your Let's Meet infrastructure is now fully set up and ready to use!

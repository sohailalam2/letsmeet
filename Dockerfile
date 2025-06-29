FROM python:3.9-slim

# Install system dependencies
RUN apt-get update && apt-get install -y \
    curl \
    unzip \
    less \
    jq \
    dos2unix \
    && rm -rf /var/lib/apt/lists/*

# Install AWS CLI v2 (version 2.25.1)
RUN curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64-2.25.1.zip" -o "awscliv2.zip" && \
    unzip awscliv2.zip && \
    ./aws/install --install-dir /usr/local/aws-cli --bin-dir /usr/local/bin --update && \
    rm -rf awscliv2.zip aws

# Install SAM CLI via pip
RUN pip install --no-cache-dir aws-sam-cli

# Set working directory
WORKDIR /app
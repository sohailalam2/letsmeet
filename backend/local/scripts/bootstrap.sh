#!/bin/bash

# Function to store a key file as an SSM parameter
# Args:
#   $1: Path to the key file (e.g., /keys/public.pem)
#   $2: SSM parameter name (e.g., /boilerplate/public.pem)
store_key_to_ssm() {
    local key_file="$1"
    local parameter_name="$2"

    # Check if the key file exists
    if [ ! -f "$key_file" ]; then
        echo "Error: Key file $key_file does not exist"
        return 1
    fi

    # Read the key
    local key_content
    key_content=$(cat "$key_file")
    if [ -z "$key_content" ]; then
        echo "Error: Key file $key_file is empty"
        return 1
    fi

    # Store the key in SSM
    echo "Storing key from $key_file as SSM parameter $parameter_name..."
    aws ssm put-parameter \
        --name "$parameter_name" \
        --value "$key_content" \
        --type "SecureString" \
        --overwrite

    if [ $? -eq 0 ]; then
        echo "Successfully stored SSM parameter $parameter_name"
    else
        echo "Error: Failed to store SSM parameter $parameter_name"
        return 1
    fi
}

put_dynamodb_item() {
  local table_name="$1"
  local item_json="$2"

  if aws dynamodb put-item --table-name "$table_name" --item "$item_json"; then
    echo "✅ Successfully inserted item $item_json into '$table_name'"
  else
    echo "❌ Failed to insert item into '$table_name'"
    echo "Item data: $item_json"
    return 1
  fi
}
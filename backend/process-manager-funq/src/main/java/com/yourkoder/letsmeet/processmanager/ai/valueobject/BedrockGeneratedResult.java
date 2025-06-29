package com.yourkoder.letsmeet.processmanager.ai.valueobject;

public record BedrockGeneratedResult(String generatedContent, String modelID, Long processingTimeMillis) {
}

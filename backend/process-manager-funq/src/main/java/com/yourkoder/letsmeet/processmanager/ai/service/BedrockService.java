package com.yourkoder.letsmeet.processmanager.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourkoder.letsmeet.processmanager.ai.service.exception.AiPromptException;
import com.yourkoder.letsmeet.processmanager.ai.valueobject.BedrockGeneratedResult;
import com.yourkoder.letsmeet.processmanager.config.ApplicationConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;
import software.amazon.awssdk.services.bedrockruntime.model.PerformanceConfigLatency;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@ApplicationScoped
@JBossLog
public class BedrockService {

    @Inject
    ObjectMapper mapper;

    @Inject
    BedrockRuntimeClient bedrockClient;

    private final String modelId;

    public BedrockService(ApplicationConfig config) {
        this.modelId = config.aws().bedrock().modelId();
    }

    public BedrockGeneratedResult prompt(String prompt) throws AiPromptException {

        long startTime = System.currentTimeMillis();
        try {
            String requestBody = buildRequestBody(prompt);

            LOG.debugf("Sending request to Bedrock with model: %s", modelId);

            InvokeModelRequest invokeRequest = InvokeModelRequest.builder()
                    .modelId(modelId)
                    .body(SdkBytes.fromString(requestBody, StandardCharsets.UTF_8))
                    .performanceConfigLatency(PerformanceConfigLatency.STANDARD)
                    .build();

            InvokeModelResponse response = bedrockClient.invokeModel(invokeRequest);
            String responseBody = response.body().asString(StandardCharsets.UTF_8);

            LOG.debugf("Received response:%n%n%s%n%s%n%s%n%n",
                    "-".repeat(30), responseBody, "-".repeat(30));
            String summary = extractContentFromResponse(responseBody);
            long processingTime = System.currentTimeMillis() - startTime;

            return new BedrockGeneratedResult(summary, modelId, processingTime);
        } catch (Exception e) {
            LOG.error("Error during AI prompt", e);
            throw new AiPromptException("Failed to prompt.", e);
        }
    }

    private String buildRequestBody(String prompt) throws JsonProcessingException {
        Map<String, Object> requestBody;

        String lcModel = this.modelId.toLowerCase(Locale.ROOT);
        if (lcModel.contains("claude")) {
            // Use Claude "messages" format
            Map<String, Object> message = Map.of(
                    "role", "user",
                    "content", prompt
            );
            requestBody = Map.of(
                    "anthropic_version", "bedrock-2023-05-31",
                    "messages", List.of(message),
                    "max_tokens", 512,
                    "temperature", 0.5,
                    "top_p", 0.9
            );
        } else if (lcModel.contains("llama")) {
            prompt = """
                <|begin_of_text|><|start_header_id|>user<|end_header_id>
                %s
                <|eot_id|>
                <|start_header_id|>assistant<|end_header_id>""".formatted(prompt);
            // Llama models expect a single prompt field
            requestBody = Map.of(
                    "prompt", prompt,
                    "max_gen_len", 512,
                    "temperature", 0.5,
                    "top_p", 0.9
            );
        } else {
            // Fallback: include prompt only
            requestBody = Map.of("prompt", prompt);
        }

        return mapper.writeValueAsString(requestBody);
    }

    private String extractContentFromResponse(String responseBody) throws JsonProcessingException,
            AiPromptException {
        if (this.modelId.contains("claude")) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(responseBody);

                // Claude 3 structure: { "content": [ { "type": "text", "text": "..." }, ... ] }
                JsonNode contentArray = root.path("content");
                if (!contentArray.isArray()) {
                    return null;
                }

                StringBuilder sb = new StringBuilder();
                for (JsonNode node : contentArray) {
                    if (node.has("type") && "text".equals(node.get("type").asText())
                            && node.has("text")) {
                        sb.append(node.get("text").asText());
                    }
                }

                return sb.toString().trim();
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse Claude response body: " + responseBody, e);
            }
        } else if (this.modelId.contains("llama")) {
            Map<String, Object> responseMap = mapper.readValue(
                    responseBody,
                    new TypeReference<>() {
                        // pass
                    }
            );

            if (responseMap.containsKey("generation")) {
                return (String) responseMap.get("generation");
            } else if (responseMap.containsKey("completions")
                    && responseMap.get("completions") instanceof List) {
                List<?> completions = (List<?>) responseMap.get("completions");
                if (!completions.isEmpty() && completions.get(0) instanceof Map) {
                    Map<?, ?> completion = (Map<?, ?>) completions.get(0);
                    if (completion.containsKey("text")) {
                        return (String) completion.get("text");
                    }
                }
            }
        }

        throw new AiPromptException("Could not extract summary from model response");
    }
}

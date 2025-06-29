package com.yourkoder.letsmeet.processmanager.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.yourkoder.letsmeet.domain.meet.valueobject.ActionItem;
import com.yourkoder.letsmeet.processmanager.ai.valueobject.BedrockGeneratedResult;
import com.yourkoder.letsmeet.domain.meet.valueobject.MeetingSummary;
import com.yourkoder.letsmeet.domain.meet.model.GeneratedMeetingData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ApplicationScoped
@JBossLog
public class AiResponseParser {

    private static final Pattern JSON_BLOCK_PATTERN = Pattern.compile("```json\\n(.*?)\\n```", Pattern.DOTALL);

    @Inject
    ObjectMapper mapper;

    public GeneratedMeetingData parseResponse(BedrockGeneratedResult generatedResult) throws Exception {

        String response = generatedResult.generatedContent();
        // Extract JSON code blocks
        Matcher matcher = JSON_BLOCK_PATTERN.matcher(response);
        List<String> jsonBlocks = new ArrayList<>();
        while (matcher.find()) {
            jsonBlocks.add(matcher.group(1));
        }

        // Validate: At least one block (meeting summary) expected
        if (jsonBlocks.isEmpty()) {
            throw new IllegalArgumentException("No JSON code blocks found in response");
        }

        // Parse meeting summary (first block)
        MeetingSummary meetingSummary = mapper.readValue(jsonBlocks.get(0), MeetingSummary.class);

        List<ActionItem> actionItems = new ArrayList<>();
        try {
            // Parse action items (second block, if present and non-empty)

            if (jsonBlocks.size() > 1 && !jsonBlocks.get(1).trim().equals("[]")) {
                actionItems = mapper.readValue(
                        jsonBlocks.get(1),
                        TypeFactory.defaultInstance().constructCollectionType(List.class, ActionItem.class)
                );
            }

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return GeneratedMeetingData.builder()
                .meetingSummary(meetingSummary)
                .actionItems(actionItems)
                .modelID(generatedResult.modelID())
                .processingTimeMillis(generatedResult.processingTimeMillis())
                .build();
    }
}

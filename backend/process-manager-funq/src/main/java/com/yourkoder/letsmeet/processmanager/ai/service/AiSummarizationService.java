package com.yourkoder.letsmeet.processmanager.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourkoder.letsmeet.processmanager.ai.service.exception.AiPromptException;
import com.yourkoder.letsmeet.processmanager.ai.valueobject.BedrockGeneratedResult;
import com.yourkoder.letsmeet.domain.meet.model.GeneratedMeetingData;
import com.yourkoder.letsmeet.processmanager.meet.valueobject.Transcript;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;

import java.util.Map;

@ApplicationScoped
@JBossLog
public class AiSummarizationService {

    @Inject
    ObjectMapper mapper;

    @Inject
    AiResponseParser aiResponseParser;

    @Inject
    BedrockService bedrockService;

    public GeneratedMeetingData summarizeText(
            Transcript text,
            int maxLength,
            Map<String, String> userNameMap
    ) throws AiPromptException {

        maxLength = maxLength > 0 ? maxLength : 300;

        try {
            String prompt = buildTranscriptSummarizationPrompt(text, maxLength, userNameMap);

            BedrockGeneratedResult response = bedrockService.prompt(prompt);
            return aiResponseParser.parseResponse(response);
        } catch (Exception e) {
            LOG.error("Error during text summarization", e);
            throw new AiPromptException("Failed to summarize text.", e);
        }
    }

    @SuppressWarnings("checkstyle:LineLength")
    private String buildTranscriptSummarizationPrompt(Transcript text, int maxLength, Map<String, String> userNameMap) {
        String userNamesPromptPart = "";
        if (userNameMap != null && !userNameMap.isEmpty()) {
            try {
                String userNames = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(userNameMap);
                userNamesPromptPart = """
                        
                        Also, Here are the names of the user for each user ID:
                        ---
                        %s
                        ---
                        Use this to decipher who is talking to whom in the transcript.
                        
                        """.formatted(userNames);
            } catch (JsonProcessingException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        String instructionTemplate = """
                Here is a transcript of a meeting between multiple participants identified by their user IDs in the format '<user_id>:'.
                
                ---
                <transcript>
                %s
                </transcript>
                ---
                
                %s
                Instructions:
                
                1) Create a summary of this transcript in less than %s words and also create concise meeting notes. Keep each speaker's contributions coherent and do not include any personal or irrelevant discussions between users. Respond in this JSON format:\s
                
                ```json
                { summary: Text, meeting_notes: Text[] }
                ```
                
                2) If the participants discussed any to-do or action item tasks, then create a JSON structure of those tasks. Only create detailed_breakdown of a discussed task if it is complex.  Use the following schema:
                
                ```json
                [{ task: Text, detailed_breakdown?: Text[] due_date?: dd-MM-yyyy, assignee?: UserId }]
                ```
                
                The tasks must always be a list of objects.
                Note: Create separate json code snippets (```json ```) for the two above instructions.
                """;

        Transcript cleanedTranscript = cleanTranscript(text);
        return instructionTemplate.formatted(cleanedTranscript.getValue(), userNamesPromptPart, maxLength);
    }

    Transcript cleanTranscript(Transcript transcript) {
        return transcript
                // Remove common filler words
                // Remove 'um', 'uh', 'ah', etc (case-insensitive) when surrounded by non-alphabetic characters
                .replaceAll("(?i)(^|[^a-zA-Z])(um|uh|ah|umm|uhh|uhuh|hmm|hm|so"
                        + "|hey|oh|oof|ahah|hello|hi|hii|bye)(?=[^a-zA-Z]|$)", "$1")
                // Remove 'like' (case-insensitive) when surrounded by non-alphabetic characters and
                // not followed by a space and an alphabetic character
                .replaceAll(
                        "(?i)(^|[^a-zA-Z])(like)(?:\\s*,\\s*|\\s+)(?=[^a-zA-Z]|$)(?!\\s[a-zA-Z])", "$1"
                )
                // Remove stray commas and normalize spaces
                // Replace multiple spaces/tabs/commas with a single comma
                .replaceAll("[ \t,]+,", ",")
                // Replace comma(s) surrounded by spaces/tabs with a single comma
                .replaceAll(",[ \t]*,", ",")
                // Replace multiple spaces/tabs with a single space
                .replaceAll("[ \t]+", " ")
                // Remove leading/trailing commas with spaces/tabs
                .replaceAll("(^[ \t]*,[ \t]*|[ \t]*,[ \t]*$)", "")
                .replaceAll("([:-])[ \t]*,[ \t]*", "$1 ")
                // Remove lines containing only punctuation after user ID
                .replaceAll("(?m)^<\\d+>:\\s*[^a-zA-Z0-9\\s]+\\s*$\n?", "")
                // Remove invalid trailing punctuation not preceded by non-whitespace
                .replaceAll("(?<=\\s)[\\p{Punct}]+", "")
                .removeDuplicateConsecutives()
                .removeIllegals();
    }
}

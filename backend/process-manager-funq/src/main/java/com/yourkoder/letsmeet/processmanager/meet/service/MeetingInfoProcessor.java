package com.yourkoder.letsmeet.processmanager.meet.service;

import com.yourkoder.letsmeet.domain.auth.model.User;
import com.yourkoder.letsmeet.domain.auth.service.exception.AuthorizationException;
import com.yourkoder.letsmeet.domain.meet.model.GeneratedMeetingData;
import com.yourkoder.letsmeet.domain.meet.model.Meeting;
import com.yourkoder.letsmeet.domain.meet.service.MeetingService;
import com.yourkoder.letsmeet.domain.meet.service.exception.MeetingNotFoundException;
import com.yourkoder.letsmeet.domain.meet.valueobject.ActionItem;
import com.yourkoder.letsmeet.processmanager.ai.service.AiSummarizationService;
import com.yourkoder.letsmeet.processmanager.ai.service.exception.AiPromptException;
import com.yourkoder.letsmeet.processmanager.meet.service.exception.InvalidTranscriptSourceException;
import com.yourkoder.letsmeet.processmanager.meet.valueobject.Transcript;
import com.yourkoder.letsmeet.processmanager.meet.valueobject.exception.InvalidTranscriptException;
import com.yourkoder.letsmeet.processmanager.slack.SlackService;
import com.yourkoder.letsmeet.processmanager.trello.TrelloTicketCreationService;
import com.yourkoder.letsmeet.processmanager.util.keymanagement.exception.ParameterNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
@JBossLog
public class MeetingInfoProcessor {

    @Inject
    AiSummarizationService aiSummarizationService;

    @Inject
    S3Client s3Client;

    @Inject
    MeetingService meetingService;

    @Inject
    SlackService slackService;

    @Inject
    TrelloTicketCreationService trelloTicketCreationService;

    public void process(String bucketName, String key) throws InvalidTranscriptException,
            InvalidTranscriptSourceException, AiPromptException, MeetingNotFoundException {
        // Extract file name (last segment)
        String[] pathSegments = key.split("/");
        if (pathSegments.length < 2) {
            throw new InvalidTranscriptSourceException(
                    "Received Invalid bucket path: [%s] in bucket: [%s]. Contains: [%s] path segments only."
                            .formatted(key, bucketName, pathSegments.length));
        }

        String meetingId = pathSegments[0];
        String fileName = pathSegments[pathSegments.length - 1];
        if (key.contains("transcription-messages") && fileName.endsWith(".txt")) {
            this.processTranscript(bucketName, key, fileName, meetingId);
        } else if (key.contains("composited-video") && fileName.endsWith(".mp4")) {
            meetingService.setMeetingVideoRecording(meetingId, key);
        }
    }

    private void processTranscript(
            String bucket,
            String key,
            String fileName,
            String meetingId
    ) throws InvalidTranscriptException, AiPromptException, MeetingNotFoundException {
        String transcriptContent = s3Client.getObjectAsBytes(GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build()).asUtf8String();
        LOG.infof("Retrieved transcript file: [%s] with content: [%n%s%n]", fileName, transcriptContent);
        Transcript transcript = Transcript.fromString(transcriptContent);
        Meeting meeting = meetingService.getMeeting(meetingId);
        Map<String, User> userMap = meetingService.getUsersByIds(meeting);
        GeneratedMeetingData generatedMeetingData = aiSummarizationService.summarizeText(
                transcript, 300, userMap.entrySet().stream().collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().getName()
                )));

        meetingService.addGeneratedInfo(meeting, generatedMeetingData);

        try {
            slackService.sendMeetingSummary(
                    generatedMeetingData,
                    meeting,
                    Optional.empty(),
                    userMap
            );
        } catch (Exception e) {
            LOG.error("Failed to send meeting summary slack notification.", e);
        }

        LOG.debugf("Saved generated meeting info for meeting: [%s].", meetingId);

        if (generatedMeetingData.getActionItems() != null) {
            for (ActionItem actionItem: generatedMeetingData.getActionItems()) {
                try {
                    trelloTicketCreationService.createTicket(
                            userMap.get(actionItem.getAssignee()),
                            actionItem,
                            meeting
                    );
                } catch (AuthorizationException | ParameterNotFoundException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
    }
}

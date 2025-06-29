package com.yourkoder.letsmeet.processmanager.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourkoder.letsmeet.domain.meet.service.MeetingService;
import com.yourkoder.letsmeet.domain.meet.service.exception.MeetingNotFoundException;
import com.yourkoder.letsmeet.processmanager.ai.service.exception.AiPromptException;
import com.yourkoder.letsmeet.processmanager.dto.ChimeMeetingEventDTO;
import com.yourkoder.letsmeet.processmanager.meet.service.MeetingInfoProcessor;
import com.yourkoder.letsmeet.processmanager.meet.service.exception.InvalidTranscriptSourceException;
import com.yourkoder.letsmeet.processmanager.meet.valueobject.exception.InvalidTranscriptException;
import io.quarkus.funqy.Funq;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;

import java.util.List;
import java.util.Map;

@JBossLog
public class ChimeMeetingsEventHandler {
    private static final String EVENT_NAME = "eventName";

    private static final String OBJECT_CREATED_S3_EVENT = "ObjectCreated:";

    private static final String CHIME_MEETING_STATE_CHANGE = "Chime Meeting State Change";

    private static final String DETAIL_TYPE = "detail-type";

    private static final String DETAIL = "detail";

    private static final String CHIME_MEETING_ENDED = "chime:MeetingEnded";

    private static final String RECORDS = "Records";

    @Inject
    ObjectMapper mapper;

    @Inject
    MeetingService meetingService;

    @Inject
    MeetingInfoProcessor meetingInfoProcessor;

    @Funq
    public void handleRequest(Map<String, Object> rawEVent) {
        LOG.infof("Received raw event: [%s]", rawEVent);

        if (rawEVent.containsKey(DETAIL_TYPE)) {
            processEventBridgeEvent(rawEVent);
        } else if (rawEVent.containsKey(RECORDS)) {
            processS3EventNotification(rawEVent);
        }
    }

    private void processS3EventNotification(Map<String, Object> eventMap) {
        // Extract Records list
        List<Map<String, Object>> records = (List<Map<String, Object>>) eventMap.get("Records");
        if (records == null || records.isEmpty()) {
            LOG.warnf("No s3 records found in raw event: %s", eventMap);
            return;
        }

        // Process first record
        for (Map<String, Object> record: records) {
            if (!record.containsKey(EVENT_NAME)) {
                LOG.warnf("S3 event notification: [%s] does not have a type. Skipping record.", record);
                continue;
            }
            if (!((String) record.get(EVENT_NAME)).startsWith(OBJECT_CREATED_S3_EVENT)) {
                LOG.warnf("S3 event notification is of type: [%s]. Skipping record.", record.get(EVENT_NAME));
                continue;
            }
            if (!record.containsKey("s3")) {
                LOG.warnf("No s3 data found in raw event record: %s", record);
                continue;
            }
            Map<String, Object> s3 = (Map<String, Object>) record.get("s3");
            Map<String, Object> bucket = (Map<String, Object>) s3.get("bucket");
            Map<String, Object> object = (Map<String, Object>) s3.get("object");

            // Extract bucket name and key
            String bucketName = (String) bucket.get("name");
            String key = (String) object.get("key");

            // Check path
            if (!key.contains("/concatenated/")) {
                return;
            }

            try {
                meetingInfoProcessor.process(bucketName, key);
            } catch (InvalidTranscriptSourceException | InvalidTranscriptException | AiPromptException
                     | MeetingNotFoundException e) {
                LOG.error("Failed to process  s3 event notification.");
            }
        }
    }

    private void processEventBridgeEvent(Map<String, Object> rawEVent) {
        if (!rawEVent.get(DETAIL_TYPE).equals(CHIME_MEETING_STATE_CHANGE)) {
            return;
        }
        ChimeMeetingEventDTO meetingEvent = mapper.convertValue(rawEVent.get(DETAIL), ChimeMeetingEventDTO.class);

        LOG.infof("Received event: [%s]", meetingEvent);
        if (CHIME_MEETING_ENDED.equalsIgnoreCase(meetingEvent.getEventType())) {
            String meetingId = meetingEvent.getMeetingId();
            if (meetingId == null || meetingId.isEmpty()) {
                LOG.warn("MeetingId is null or empty in event");
                return;
            }

            LOG.infof("Processing MeetingEnded event for meetingId: %s", meetingId);

            // Call service to end the meeting
            try {
                meetingService.postProcessResourcesForEndedMeeting(meetingId);
            } catch (MeetingNotFoundException e) {
                LOG.error(e.getMessage(), e);
                return;
            }

            LOG.infof("Successfully processed ended meeting for meetingId: %s", meetingId);
        } else {
            LOG.debug("Event is not a MeetingEnded event, ignoring");
        }
    }
}

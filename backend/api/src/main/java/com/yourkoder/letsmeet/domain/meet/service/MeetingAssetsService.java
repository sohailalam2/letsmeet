package com.yourkoder.letsmeet.domain.meet.service;

import com.yourkoder.letsmeet.config.ApplicationConfig;
import com.yourkoder.letsmeet.domain.auth.service.exception.AuthorizationException;
import com.yourkoder.letsmeet.domain.meet.model.Meeting;
import com.yourkoder.letsmeet.domain.meet.repository.MeetingRepository;
import com.yourkoder.letsmeet.domain.meet.service.exception.MeetingNotFoundException;
import com.yourkoder.letsmeet.domain.meet.service.exception.MeetingResourceNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.Getter;
import lombok.extern.jbosslog.JBossLog;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;

@ApplicationScoped
@JBossLog
public class MeetingAssetsService {

    public static final long SIGNED_URL_EXPIRATION_TIMEOUT = 300L;
    public static final String MP_4_SUFFIX = ".mp4";

    @Inject
    S3Presigner presigner;

    @Inject
    S3Client s3Client;

    @Inject
    MeetingRepository meetingRepo;

    @Getter
    private final String processedS3Bucket;

    public MeetingAssetsService(
            ApplicationConfig config
    ) {
        this.processedS3Bucket = "%s-%s-processed--%s".formatted(
                config.applicationNamespace(),
                config.aws().s3().bucket(),
                config.aws().getRegion()
        );
    }

    public String getMeetingRecording(String meetingID, String userID) throws MeetingNotFoundException,
            AuthorizationException, MeetingResourceNotFoundException {
        Meeting meeting = meetingRepo.findById(meetingID)
                .orElseThrow(() -> new MeetingNotFoundException("Meeting: [%s] not found.".formatted(meetingID)));

        if (!meeting.getParticipantIds().contains(userID)) {
            throw new AuthorizationException("user is not authorized to get generated meeting info for meeting: [%s]"
                    .formatted(meetingID));
        }
        if (meeting.isEnded() && meeting.getVideoRecording() == null) {
            String s3VideoRecordingKey = retrieveS3VideoKeyForMeeting(meetingID);
            meeting.setVideoRecording(s3VideoRecordingKey);
            meetingRepo.save(meeting);
        }
        if (meeting.getVideoRecording() == null) {
            throw new MeetingResourceNotFoundException("Meeting video recording key is null for meeting: [%s]"
                    .formatted(meetingID));
        }
        return this.getSignedURLForRecordedVideo(
                meeting.getVideoRecording(),
                SIGNED_URL_EXPIRATION_TIMEOUT
        );
    }

    private String getSignedURLForRecordedVideo(String key, Long expirationDurationSeconds) {
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(expirationDurationSeconds))
                .getObjectRequest(builder -> builder
                        .bucket(processedS3Bucket)
                        .key(key)
                        .build())
                .build();

        PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
        return presignedRequest.url().toString();
    }

    private String retrieveS3VideoKeyForMeeting(String meetingID) {
        String prefix = "%s/concatenated/composited-video/".formatted(meetingID);
        String continuationToken = null;

        do {
            ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                    .bucket(processedS3Bucket)
                    .prefix(prefix)
                    .maxKeys(1000); // adjust as needed

            if (continuationToken != null) {
                requestBuilder.continuationToken(continuationToken);
            }

            ListObjectsV2Response response = s3Client.listObjectsV2(requestBuilder.build());

            for (S3Object s3Object : response.contents()) {
                String key = s3Object.key();
                LOG.debugf("Checking key: [%s] for video recording data for meeting: [%s]",
                        key, meetingID);
                if (key.endsWith(MP_4_SUFFIX)) {
                    String[] pathSegments = key.split("/");
                    if (pathSegments.length < 2) {
                        continue;
                    }
                    String fileName = pathSegments[pathSegments.length - 1];
                    if (fileName.endsWith(MP_4_SUFFIX)) {
                        LOG.debugf("Found key: [%s] for video recording data for meeting: [%s]",
                                key, meetingID);
                        return key;
                    }

                }
            }

            continuationToken = response.isTruncated() ? response.nextContinuationToken() : null;

        } while (continuationToken != null);
        return null;
    }
}

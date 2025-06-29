package com.yourkoder.letsmeet.domain.meet.service;

import com.yourkoder.letsmeet.domain.auth.model.User;
import com.yourkoder.letsmeet.domain.auth.repository.UserRepository;
import com.yourkoder.letsmeet.domain.auth.service.exception.AuthorizationException;
import com.yourkoder.letsmeet.domain.meet.model.Attendee;
import com.yourkoder.letsmeet.domain.meet.model.GeneratedMeetingData;
import com.yourkoder.letsmeet.domain.meet.model.Meeting;
import com.yourkoder.letsmeet.domain.meet.model.MeetingMediaPlacement;
import com.yourkoder.letsmeet.domain.meet.repository.AttendeeRepository;
import com.yourkoder.letsmeet.domain.meet.repository.MeetingGeneratedInfoRepository;
import com.yourkoder.letsmeet.domain.meet.repository.MeetingRepository;
import com.yourkoder.letsmeet.domain.meet.service.exception.FailedToEndMeetingException;
import com.yourkoder.letsmeet.domain.meet.service.exception.MeetingAttendeeNotFoundException;
import com.yourkoder.letsmeet.domain.meet.service.exception.MeetingNotFoundException;
import com.yourkoder.letsmeet.domain.meet.service.exception.MeetingResourceNotFoundException;
import com.yourkoder.letsmeet.domain.meet.valueobject.AttendeeJoinInfo;
import com.yourkoder.letsmeet.domain.meet.valueobject.GeneratedMeetingInfo;
import com.yourkoder.letsmeet.domain.meet.valueobject.MeetingInfo;
import com.yourkoder.letsmeet.domain.meet.valueobject.UserAttendeeInfo;
import jakarta.enterprise.inject.Vetoed;
import lombok.Getter;
import lombok.extern.jbosslog.JBossLog;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.chimesdkmediapipelines.ChimeSdkMediaPipelinesClient;
import software.amazon.awssdk.services.chimesdkmediapipelines.model.ArtifactsConcatenationConfiguration;
import software.amazon.awssdk.services.chimesdkmediapipelines.model.ArtifactsConcatenationState;
import software.amazon.awssdk.services.chimesdkmediapipelines.model.ArtifactsConfiguration;
import software.amazon.awssdk.services.chimesdkmediapipelines.model.ArtifactsState;
import software.amazon.awssdk.services.chimesdkmediapipelines.model.AudioArtifactsConcatenationState;
import software.amazon.awssdk.services.chimesdkmediapipelines.model.AudioArtifactsConfiguration;
import software.amazon.awssdk.services.chimesdkmediapipelines.model.AudioConcatenationConfiguration;
import software.amazon.awssdk.services.chimesdkmediapipelines.model.AudioMuxType;
import software.amazon.awssdk.services.chimesdkmediapipelines.model.ChimeSdkMeetingConcatenationConfiguration;
import software.amazon.awssdk.services.chimesdkmediapipelines.model.ChimeSdkMeetingConfiguration;
import software.amazon.awssdk.services.chimesdkmediapipelines.model.CompositedVideoArtifactsConfiguration;
import software.amazon.awssdk.services.chimesdkmediapipelines.model.CompositedVideoConcatenationConfiguration;
import software.amazon.awssdk.services.chimesdkmediapipelines.model.ConcatenationSink;
import software.amazon.awssdk.services.chimesdkmediapipelines.model.ConcatenationSinkType;
import software.amazon.awssdk.services.chimesdkmediapipelines.model.ConcatenationSource;
import software.amazon.awssdk.services.chimesdkmediapipelines.model.ConcatenationSourceType;
import software.amazon.awssdk.services.chimesdkmediapipelines.model.ContentArtifactsConfiguration;
import software.amazon.awssdk.services.chimesdkmediapipelines.model.ContentConcatenationConfiguration;
import software.amazon.awssdk.services.chimesdkmediapipelines.model.ContentShareLayoutOption;
import software.amazon.awssdk.services.chimesdkmediapipelines.model.CreateMediaCapturePipelineRequest;
import software.amazon.awssdk.services.chimesdkmediapipelines.model.CreateMediaCapturePipelineResponse;
import software.amazon.awssdk.services.chimesdkmediapipelines.model.CreateMediaConcatenationPipelineRequest;
import software.amazon.awssdk.services.chimesdkmediapipelines.model.CreateMediaConcatenationPipelineResponse;
import software.amazon.awssdk.services.chimesdkmediapipelines.model.DataChannelConcatenationConfiguration;
import software.amazon.awssdk.services.chimesdkmediapipelines.model.DeleteMediaCapturePipelineRequest;
import software.amazon.awssdk.services.chimesdkmediapipelines.model.DeleteMediaCapturePipelineResponse;
import software.amazon.awssdk.services.chimesdkmediapipelines.model.GridViewConfiguration;
import software.amazon.awssdk.services.chimesdkmediapipelines.model.LayoutOption;
import software.amazon.awssdk.services.chimesdkmediapipelines.model.MediaCapturePipelineSourceConfiguration;
import software.amazon.awssdk.services.chimesdkmediapipelines.model.MediaPipelineSinkType;
import software.amazon.awssdk.services.chimesdkmediapipelines.model.MediaPipelineSourceType;
import software.amazon.awssdk.services.chimesdkmediapipelines.model.MeetingEventsConcatenationConfiguration;
import software.amazon.awssdk.services.chimesdkmediapipelines.model.ResolutionOption;
import software.amazon.awssdk.services.chimesdkmediapipelines.model.S3BucketSinkConfiguration;
import software.amazon.awssdk.services.chimesdkmediapipelines.model.Tag;
import software.amazon.awssdk.services.chimesdkmediapipelines.model.TranscriptionMessagesConcatenationConfiguration;
import software.amazon.awssdk.services.chimesdkmediapipelines.model.VideoArtifactsConfiguration;
import software.amazon.awssdk.services.chimesdkmediapipelines.model.VideoConcatenationConfiguration;
import software.amazon.awssdk.services.chimesdkmeetings.ChimeSdkMeetingsClient;
import software.amazon.awssdk.services.chimesdkmeetings.model.ChimeSdkMeetingsException;
import software.amazon.awssdk.services.chimesdkmeetings.model.CreateAttendeeRequest;
import software.amazon.awssdk.services.chimesdkmeetings.model.CreateAttendeeResponse;
import software.amazon.awssdk.services.chimesdkmeetings.model.CreateMeetingRequest;
import software.amazon.awssdk.services.chimesdkmeetings.model.CreateMeetingResponse;
import software.amazon.awssdk.services.chimesdkmeetings.model.DeleteAttendeeRequest;
import software.amazon.awssdk.services.chimesdkmeetings.model.DeleteMeetingRequest;
import software.amazon.awssdk.services.chimesdkmeetings.model.DeleteMeetingResponse;
import software.amazon.awssdk.services.chimesdkmeetings.model.EngineTranscribeSettings;
import software.amazon.awssdk.services.chimesdkmeetings.model.NotFoundException;
import software.amazon.awssdk.services.chimesdkmeetings.model.StartMeetingTranscriptionRequest;
import software.amazon.awssdk.services.chimesdkmeetings.model.StopMeetingTranscriptionRequest;
import software.amazon.awssdk.services.chimesdkmeetings.model.StopMeetingTranscriptionResponse;
import software.amazon.awssdk.services.chimesdkmeetings.model.TranscribeLanguageCode;
import software.amazon.awssdk.services.chimesdkmeetings.model.TranscriptionConfiguration;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Vetoed
@JBossLog
public class MeetingService {
    private final Region region;

    private final ChimeSdkMeetingsClient meetingsClient;

    private final ChimeSdkMediaPipelinesClient pipelineClient;

    private final MeetingRepository meetingRepo;

    private final AttendeeRepository attendeeRepo;

    private final UserRepository userRepo;

    private final MeetingGeneratedInfoRepository meetingGeneratedInfoRepo;

    private final String rawS3Bucket;

    private final String processedS3Bucket;

    @Getter
    private final String sinkArnFormatter;

    private final boolean switchHostsOnLeave;

    @SuppressWarnings("checkstyle:ParameterNumber")
    public MeetingService(
            Region region,
            String applicationNamespace,
            String s3Bucket,
            boolean switchHostsOnLeave,
            ChimeSdkMeetingsClient meetingsClient,
            ChimeSdkMediaPipelinesClient pipelineClient,
            MeetingRepository meetingRepo,
            AttendeeRepository attendeeRepo,
            MeetingGeneratedInfoRepository meetingGeneratedInfoRepo,
            UserRepository userRepo
            ) {
        this.region = region;
        this.rawS3Bucket = "%s-%s--%s".formatted(
                applicationNamespace,
                s3Bucket,
                this.region.id()
        );
        this.processedS3Bucket = "%s-%s-processed--%s".formatted(
                applicationNamespace,
                s3Bucket,
                this.region.id()
        );
        sinkArnFormatter = "arn:aws:s3:::%s";
        this.switchHostsOnLeave = switchHostsOnLeave;
        this.meetingsClient = meetingsClient;
        this.pipelineClient = pipelineClient;
        this.meetingRepo = meetingRepo;
        this.attendeeRepo = attendeeRepo;
        this.userRepo = userRepo;
        this.meetingGeneratedInfoRepo = meetingGeneratedInfoRepo;
    }

    public MeetingInfo createMeeting(String hostUserID, String meetingName) {
        LOG.infof("Creating meeting in region: [%s]", region.id());
        CreateMeetingResponse resp = meetingsClient.createMeeting(
                CreateMeetingRequest.builder()
                        .mediaRegion(region.id())
                        .externalMeetingId(meetingName)
                        .clientRequestToken("%s-%s".formatted(hostUserID, UUID.randomUUID().toString()))
                        .meetingHostId(hostUserID)
                        .build());

        String meetingID = resp.meeting().meetingId();
        String meetingArn = resp.meeting().meetingArn();
        String externalMeetingID = resp.meeting().externalMeetingId();
        LOG.infof(
                "Created new meeting with ID: [%s], external ID: [%s] and ARN: [%s]",
                meetingID,
                externalMeetingID,
                meetingArn
        );

        String mediaRegion = resp.meeting().mediaRegion();
        Meeting meeting = new Meeting();

        meeting.initialize(
                hostUserID,
                meetingID,
                meetingArn,
                externalMeetingID,
                mediaRegion,
                MeetingMediaPlacement.from(resp.meeting().mediaPlacement())
        );

        meetingRepo.save(meeting);

        getOrCreateWaitingAttendeeJoinInfo(meeting, meetingID, hostUserID);

        return getMeetingResponse(meeting, Optional.of(hostUserID));
    }

    public MeetingInfo getMeetingInfo(String meetingID, String userID) throws MeetingNotFoundException {
        Meeting meeting = meetingRepo.findById(meetingID)
                .orElseThrow(() -> new MeetingNotFoundException("Meeting: [%s] not found.".formatted(meetingID)));

        return getMeetingResponse(meeting, Optional.of(userID));
    }

    public Meeting getMeeting(String meetingID) throws MeetingNotFoundException {
        return meetingRepo.findById(meetingID)
                .orElseThrow(() -> new MeetingNotFoundException("Meeting: [%s] not found.".formatted(meetingID)));
    }

    public List<UserAttendeeInfo> getMeetingAttendees(
            String meetingID,
            String userID
    ) throws MeetingNotFoundException, AuthorizationException, MeetingResourceNotFoundException {

        Meeting meeting = meetingRepo.findById(meetingID)
                .orElseThrow(() -> new MeetingNotFoundException("Meeting: [%s] not found.".formatted(meetingID)));

        if (!meeting.getParticipantIds().contains(userID)/* || meeting.isEnded()*/) {
            throw new AuthorizationException("user is not authorized to get attendee info for meeting: [%s]"
                    .formatted(meetingID));
        }

        ArrayList<String> attendeeIDs = new ArrayList<>(meeting.getParticipantIds());
        Map<String, String> attendees = attendeeRepo.getAttendeesByIDs(meetingID, attendeeIDs)
                .stream().collect(Collectors.toMap(
                        Attendee::getUserId,
                        Attendee::getAttendeeId
                ));
        List<User> users = userRepo.getUsersByIds(attendeeIDs);
        return users.stream().map(user -> UserAttendeeInfo.builder()
                .userID(user.getUserId())
                .name(user.getName())
                .picture(user.getPicture())
                .attendeeID(attendees.get(user.getUserId()))
                .build()).toList();
    }

    public GeneratedMeetingInfo getGeneratedMeetingInfo(
            String meetingID,
            String userID
    ) throws MeetingNotFoundException, AuthorizationException, MeetingResourceNotFoundException {

        Meeting meeting = meetingRepo.findById(meetingID)
                .orElseThrow(() -> new MeetingNotFoundException("Meeting: [%s] not found.".formatted(meetingID)));

        if (!meeting.getParticipantIds().contains(userID)) {
            throw new AuthorizationException("user is not authorized to get generated meeting info for meeting: [%s]"
                    .formatted(meetingID));
        }
        GeneratedMeetingData generatedMeetingData = meetingGeneratedInfoRepo.findById(meetingID)
                .orElseThrow(
                        () -> new MeetingResourceNotFoundException("Meeting generated info not found for meeting: [%s]."
                                .formatted(meetingID))
                );

        Map<String, User> stringUserMap = getUsersByIds(meeting);
        return GeneratedMeetingInfo.builder()
                .meetingID(meetingID)
                .generatedMeetingData(generatedMeetingData)
                .users(stringUserMap)
                .build();
    }

    public List<MeetingInfo> getAllMeetings(String userID) {

        List<Meeting> meetings = meetingRepo.getAllMeetingsForUser(userID);

        return meetings.stream().map(meeting -> getMeetingResponse(meeting, Optional.of(userID))).toList();
    }

    public List<MeetingInfo> getAllMeetings() {

        List<Meeting> meetings = meetingRepo.getAllMeetings();

        return meetings.stream().map(meeting -> getMeetingResponse(meeting, Optional.empty())).toList();
    }

    public AttendeeJoinInfo joinMeeting(String meetingID, String userId) throws MeetingNotFoundException,
            AuthorizationException {
        Meeting meeting = meetingRepo.findById(meetingID)
                .orElseThrow(() -> new MeetingNotFoundException("Meeting: [%s] not found.".formatted(meetingID)));
        try {

            if (meeting.getBlockedIds().contains(userId)) {
                throw new AuthorizationException("User is not authorized to join the meeting: [%s]"
                        .formatted(meetingID));
            }
            if (meeting.getHostUserId().equals(userId)) {
                return startMeeting(meeting, meetingID, userId);
            }

            if (!meeting.isHostJoined()) {
                meeting.getWaitingUsers().add(userId);

                meetingRepo.save(meeting);
                return getOrCreateWaitingAttendeeJoinInfo(meeting, meetingID, userId);
            }

            AttendeeJoinInfo joinResponse = getOrCreateReadyAttendee(meeting, meetingID, userId);
            meeting.getParticipantIds().add(userId);
            meetingRepo.save(meeting);
            return joinResponse;
        } catch (NotFoundException e) {
            cleanUpEndedMeetingResourcesAndStartPostProcessing(meetingID, meeting);
            throw new MeetingNotFoundException("Meeting: [%s] not found.".formatted(meetingID), e);
        }
    }

    public AttendeeJoinInfo getAttendee(String meetingID, String userId) throws MeetingAttendeeNotFoundException,
            MeetingNotFoundException {
        Meeting meeting = meetingRepo.findById(meetingID)
                .orElseThrow(() -> new MeetingNotFoundException("Meeting: [%s] not found.".formatted(meetingID)));
        Attendee attendee = attendeeRepo.findByID(userId, meetingID)
                .orElseThrow(() -> new MeetingAttendeeNotFoundException("Meeting attendee not found"));
        return getAttendeeJoinInfo(meeting, attendee);
    }

    private AttendeeJoinInfo startMeeting(Meeting meeting, String meetingID, String userId) {
        if (meeting.isHostJoined()) {
            return getOrCreateReadyAttendee(meeting, meetingID, userId);
        }
        meeting.setHostJoined(true);
        meeting.setStartedAt(Instant.now());
        meeting.getParticipantIds().add(userId);
        // Start transcription
        createMeetingTranscription(meeting);

        // Start media capture pipeline to S3
        CreateMediaCapturePipelineResponse pipelineResp = createMediaCapturePipelineResponse(meeting);
        meeting.setCapturePipelineId(pipelineResp.mediaCapturePipeline().mediaPipelineId());
        meeting.setCapturePipelineArn(pipelineResp.mediaCapturePipeline().mediaPipelineArn());
        meetingRepo.save(meeting);

        return getOrCreateReadyAttendee(meeting, meetingID, userId);
    }

    public void leaveMeeting(String meetingID, String userId) throws MeetingNotFoundException {
        Meeting meeting = meetingRepo.findById(meetingID)
                .orElseThrow(() -> new MeetingNotFoundException("Meeting: [%s] not found.".formatted(meetingID)));

        if (meeting.isEnded()) {
            return;
        }

        if (switchHostsOnLeave && meeting.isHost(userId)) {
            Optional<String> newHost = meeting.getParticipantIds().stream().findAny();
            newHost.ifPresent(nh -> {
                meeting.setHostUserId(nh);
                // TODO notify via WebSocket or event
            });
        }
        meeting.getWaitingUsers().remove(userId);
        meetingRepo.save(meeting);
    }

    public void removeAttendee(
            String meetingID,
            String userId,
            String attendeeUserID
    ) throws MeetingNotFoundException, AuthorizationException {
        Meeting meeting = meetingRepo.findById(meetingID)
                .orElseThrow(() -> new MeetingNotFoundException("Meeting: [%s] not found.".formatted(meetingID)));

        if (meeting.isEnded()) {
            return;
        }

        if (!meeting.isHost(userId)) {
            throw new AuthorizationException("User is not authorized to remove attendees from meeting: [%s]"
                    .formatted(meetingID));
        }

        meeting.getParticipantIds().remove(attendeeUserID);
        meeting.getWaitingUsers().remove(userId);
        meeting.getBlockedIds().add(attendeeUserID);
        meetingRepo.save(meeting);

        attendeeRepo.findByID(userId, meetingID).ifPresent(attendee -> {
            this.meetingsClient.deleteAttendee(DeleteAttendeeRequest.builder()
                            .attendeeId(attendee.getAttendeeId())
                            .meetingId(attendee.getMeetingId())
                    .build());
            attendee.setBlocked(true);
            attendeeRepo.save(attendee);
        });
        meetingRepo.save(meeting);
    }

    public void postProcessResourcesForEndedMeeting(String meetingID) throws MeetingNotFoundException {
        Meeting meeting = meetingRepo.findById(meetingID)
                .orElseThrow(() -> new MeetingNotFoundException("Meeting: [%s] not found".formatted(meetingID)));

        if (meeting.isEnded()) {
            LOG.infof("Meeting: [%s] already ended.", meetingID);
        }
        cleanUpEndedMeetingResourcesAndStartPostProcessing(meetingID, meeting);

    }

    public void endMeeting(String meetingID, String hostUserId) throws FailedToEndMeetingException,
            AuthorizationException, MeetingNotFoundException {
        Meeting meeting = meetingRepo.findById(meetingID)
                .orElseThrow(() -> new MeetingNotFoundException("Meeting: [%s] not found".formatted(meetingID)));
        if (!meeting.getHostUserId().equals(hostUserId)) {
            throw new AuthorizationException("Only host can end the meeting");
        }

        try {
            if (meeting.getCapturePipelineId() != null && !meeting.isCapturePipelineComplete()) {
                // Delete media capture pipeline
                try {
                    DeleteMediaCapturePipelineResponse capturePipelineResponse
                            = pipelineClient.deleteMediaCapturePipeline(
                            DeleteMediaCapturePipelineRequest.builder()
                                    .mediaPipelineId(meeting.getCapturePipelineId())
                                    .build());

                    if (capturePipelineResponse.sdkHttpResponse().isSuccessful()) {
                        meeting.setCapturePipelineComplete(true);
                    }
                } catch (software.amazon.awssdk.services.chimesdkmediapipelines.model.NotFoundException e) {
                    LOG.debug("media capture pipeline for ended meeting: [%s] is already stopped"
                            .formatted(meetingID));
                    meeting.setCapturePipelineComplete(true);
                }
            }

            // Stop transcription
            if (meeting.isTranscribing()) {
                StopMeetingTranscriptionResponse transcriptionResponse = meetingsClient.stopMeetingTranscription(
                        StopMeetingTranscriptionRequest.builder()
                                .meetingId(meetingID)
                                .build());
                if (transcriptionResponse.sdkHttpResponse().isSuccessful()) {
                    meeting.setTranscribing(false);
                }
            }
        } catch (NotFoundException e) {
            meeting.setTranscribing(false);
        } catch (Exception e) {
            throw new FailedToEndMeetingException("Failed to transcription for ended meeting: [%s]."
                    .formatted(meeting.getMeetingId()), e);
        } finally {
            meetingRepo.save(meeting);
        }

        try {
            // Delete the meeting
            if (!meeting.isEnded()) {
                DeleteMeetingResponse deleteMeetingResponse = meetingsClient.deleteMeeting(
                        DeleteMeetingRequest.builder()
                                .meetingId(meeting.getMeetingId())
                                .build());
                if (deleteMeetingResponse.sdkHttpResponse().isSuccessful()) {
                    meeting.setEnded(true);
                    meeting.setEndedAt(Instant.now());
                }
            }
            LOG.infof("Deleted meeting: %s", meeting.getMeetingId());
        } catch (NotFoundException e) {
            LOG.debug("Meeting: [%s] has already ended.".formatted(meetingID));
            meeting.setEnded(true);
            meeting.setEndedAt(Instant.now());
        }

        if (meeting.getCapturePipelineArn() == null || meeting.getConcatenationPipelineId() != null) {
            return;
        }
        startMeetingCaptureConcatenation(meetingID, meeting);
        meetingRepo.save(meeting);
    }

    public CreateMediaConcatenationPipelineResponse startConcatenation(
            String capturePipelineArn,
            String meetingID
    ) {
        String formattedSinkArn = sinkArnFormatter.formatted("%s/%s/concatenated".formatted(
                processedS3Bucket,
                meetingID
        ));

        return pipelineClient.createMediaConcatenationPipeline(
                CreateMediaConcatenationPipelineRequest.builder()
                        .clientRequestToken(UUID.randomUUID().toString())
                        .sources(ConcatenationSource.builder()
                                .type(ConcatenationSourceType.MEDIA_CAPTURE_PIPELINE)
                                .mediaCapturePipelineSourceConfiguration(
                                        getMediaCapturePipelineSourceConfiguration(capturePipelineArn)
                                ).build())
                        .sinks(ConcatenationSink.builder()
                                .type(ConcatenationSinkType.S3_BUCKET)
                                .s3BucketSinkConfiguration(
                                        S3BucketSinkConfiguration.builder()
                                                .destination(formattedSinkArn)
                                                .build()
                                ).build())
                        .tags(Tag.builder().key("meetingId").value(meetingID).build())
                        .build()
        );
    }

    public Optional<Meeting> addGeneratedInfo(
            String meetingID,
            GeneratedMeetingData generatedMeetingData
    ) {
        generatedMeetingData.setPrimaryKey(meetingID);
        meetingGeneratedInfoRepo.save(generatedMeetingData);
        return meetingRepo.findById(meetingID)
                .map(meeting -> {
                    meeting.setSummarized(true);
                    meetingRepo.save(meeting);
                    return meeting;
                });
    }

    public Meeting addGeneratedInfo(
            Meeting meeting,
            GeneratedMeetingData generatedMeetingData
    ) {
        generatedMeetingData.setPrimaryKey(meeting.getMeetingId());
        meetingGeneratedInfoRepo.save(generatedMeetingData);
        meeting.setSummarized(true);
        meetingRepo.save(meeting);
        return meeting;
    }

    public void setMeetingVideoRecording(String meetingID, String videoRecordingS3Key) throws MeetingNotFoundException {
        LOG.debugf("Setting meeting video recording storage path: [%s] for meeting: [%s]",
                videoRecordingS3Key, meetingID);
        this.meetingRepo.findById(meetingID).map(meeting -> {
            meeting.setVideoRecording(videoRecordingS3Key);
            meetingRepo.save(meeting);
            LOG.debugf("Saved meeting video recording storage path: [%s] for meeting: [%s]",
                    videoRecordingS3Key, meetingID);
            return meeting;
        }).orElseThrow(() -> new MeetingNotFoundException("Meeting: [%s] not found".formatted(meetingID)));
    }

    public Map<String, User> getUsersByIds(String meetingID) throws MeetingNotFoundException {
        return meetingRepo.findById(meetingID).map(this::getUsersByIds)
                .orElseThrow(() -> new MeetingNotFoundException("Meeting: [%s] not found".formatted(meetingID)));
    }

    public Map<String, User> getUsersByIds(Meeting meeting) {
        return userRepo.getUsersByIds(
                        new ArrayList<>(meeting.getParticipantIds())
                ).stream()
                .collect(Collectors.toMap(
                        User::getUserId,
                        entry -> entry
                ));
    }

    private void cleanUpEndedMeetingResourcesAndStartPostProcessing(String meetingID, Meeting meeting) {
        LOG.infof("Starting post processing for ended meeting: [%s].", meetingID);

        meeting.setEnded(true);
        meeting.setEndedAt(Instant.now());

        try {
            meeting.setCapturePipelineComplete(true);
            if (meeting.getCapturePipelineId() != null) {
                pipelineClient.deleteMediaCapturePipeline(
                        DeleteMediaCapturePipelineRequest.builder()
                                .mediaPipelineId(meeting.getCapturePipelineId())
                                .build());
            }
        } catch (software.amazon.awssdk.services.chimesdkmediapipelines.model.NotFoundException e) {
            LOG.debug("media capture pipeline for ended meeting: [%s] is already stopped"
                    .formatted(meetingID));
        } catch (ChimeSdkMeetingsException e) {
            LOG.warn("Error occurred when stopping media capture pipeline for ended meeting: [%s]"
                    .formatted(meetingID), e);
        }

        try {
            meeting.setTranscribing(false);
            meetingsClient.stopMeetingTranscription(
                    StopMeetingTranscriptionRequest.builder()
                            .meetingId(meetingID)
                            .build());
        } catch (NotFoundException e) {
            LOG.debug("transcription for ended meeting: [%s] is already stopped"
                    .formatted(meetingID));
        } catch (ChimeSdkMeetingsException e) {
            LOG.warn("Error occurred when stopping transcription for ended meeting: [%s]"
                    .formatted(meetingID), e);
        }

        if (meeting.getCapturePipelineArn() == null || meeting.getConcatenationPipelineId() != null) {
            meetingRepo.save(meeting);
            return;
        }
        try {
            startMeetingCaptureConcatenation(meetingID, meeting);
        } finally {
            meetingRepo.save(meeting);
        }
    }

    private void startMeetingCaptureConcatenation(String meetingID, Meeting meeting) {
        CreateMediaConcatenationPipelineResponse concatenationPipelineResponse
                = startConcatenation(meeting.getCapturePipelineArn(), meetingID);
        meeting.setConcatenationPipelineId(
                concatenationPipelineResponse.mediaConcatenationPipeline().mediaPipelineId()
        );
        meeting.setConcatenationPipelineArn(
                concatenationPipelineResponse.mediaConcatenationPipeline().mediaPipelineArn()
        );

        meeting.setConcatenationPipelineStatus(
                concatenationPipelineResponse.mediaConcatenationPipeline().statusAsString()
        );
    }

    private AttendeeJoinInfo getOrCreateWaitingAttendeeJoinInfo(Meeting meeting, String meetingID, String userId) {
        Attendee attendee = attendeeRepo.findByID(userId, meetingID).orElseGet(() -> {
            Attendee attendeeModel = new Attendee();
            attendeeModel.setPrimaryKey(userId, meetingID);
            attendeeRepo.save(attendeeModel);
            return attendeeModel;
        });

        return getAttendeeJoinInfo(meeting, attendee);
    }

    private AttendeeJoinInfo getOrCreateReadyAttendee(Meeting meeting, String meetingID, String userId) {
        Attendee attendee = attendeeRepo.findByID(userId, meetingID).map(existingAttendee -> {
                    if (existingAttendee.getAttendeeId() == null) {
                        createAttendeeJoinInfo(meetingID, userId, existingAttendee);
                        attendeeRepo.save(existingAttendee);
                    }
                    return existingAttendee;
                }).orElseGet(() -> {
                    Attendee attendeeModel = new Attendee();
                    attendeeModel.setPrimaryKey(userId, meetingID);
                    createAttendeeJoinInfo(meetingID, userId, attendeeModel);
                    attendeeRepo.save(attendeeModel);
                    return attendeeModel;
                });
        return getAttendeeJoinInfo(meeting, attendee);
    }

    private void createAttendeeJoinInfo(String meetingID, String userId, Attendee attendeeModel) {
        CreateAttendeeResponse resp = meetingsClient.createAttendee(
                CreateAttendeeRequest.builder()
                        .meetingId(meetingID)
                        .externalUserId(userId)
                        .build());
        attendeeModel.setAttendeeId(resp.attendee().attendeeId());
        attendeeModel.setJoinToken(resp.attendee().joinToken());
    }

    private void createMeetingTranscription(Meeting meeting) {
        String meetingID = meeting.getMeetingId();
        meetingsClient.startMeetingTranscription(
                StartMeetingTranscriptionRequest.builder()
                        .meetingId(meetingID)
                        .transcriptionConfiguration(
                                TranscriptionConfiguration.builder()
                                        .engineTranscribeSettings(
                                                EngineTranscribeSettings.builder()
                                                        .languageCode(TranscribeLanguageCode.EN_US)
                                                        .region(region.id())
                                                        .build())
                                        .build())
                        .build());

        meeting.setTranscribing(true);

        LOG.infof("Started live transcription for meeting: %s", meetingID);
    }

    private CreateMediaCapturePipelineResponse createMediaCapturePipelineResponse(Meeting meeting) {
        String meetingArn = meeting.getMeetingArn();
        String formattedSinkArn = sinkArnFormatter.formatted(rawS3Bucket);
        LOG.infof("Formatted sink ARN for Media Capture pipeline is: [%s]", formattedSinkArn);

        CreateMediaCapturePipelineResponse pipelineResp = pipelineClient.createMediaCapturePipeline(
                CreateMediaCapturePipelineRequest.builder()
                        .chimeSdkMeetingConfiguration(ChimeSdkMeetingConfiguration.builder()
                                .artifactsConfiguration(ArtifactsConfiguration.builder()
                                        .audio(AudioArtifactsConfiguration.builder()
                                                .muxType(AudioMuxType.AUDIO_ONLY)
                                                .build())
                                        .compositedVideo(
                                                CompositedVideoArtifactsConfiguration.builder()
                                                        .layout(LayoutOption.GRID_VIEW)
                                                        .resolution(ResolutionOption.HD)
                                                        .gridViewConfiguration(GridViewConfiguration.builder()
                                                                .contentShareLayout(
                                                                        ContentShareLayoutOption.PRESENTER_ONLY
                                                                )
                                                                .build()).build()
                                        )
                                        .content(ContentArtifactsConfiguration.builder()
                                                .state(ArtifactsState.DISABLED).build())
                                        .video(VideoArtifactsConfiguration.builder()
                                                .state(ArtifactsState.DISABLED).build())
                                        .build())
                                .build())
                        .sourceType(MediaPipelineSourceType.CHIME_SDK_MEETING)
                        .sourceArn(meetingArn)
                        .sinkType(MediaPipelineSinkType.S3_BUCKET)
                        .sinkArn(formattedSinkArn)
                        .tags(Tag.builder()
                                .key("transcription-for-comprehend").value("true").build())
                        .build());
        return pipelineResp;
    }

    private static MediaCapturePipelineSourceConfiguration getMediaCapturePipelineSourceConfiguration(
            String capturePipelineArn
    ) {
        return MediaCapturePipelineSourceConfiguration.builder()
                .mediaPipelineArn(capturePipelineArn)
                .chimeSdkMeetingConfiguration(
                        ChimeSdkMeetingConcatenationConfiguration.builder()
                                .artifactsConfiguration(
                                        ArtifactsConcatenationConfiguration.builder()
                                                .audio(AudioConcatenationConfiguration.builder()
                                                        .state(AudioArtifactsConcatenationState.ENABLED)
                                                        .build())
                                                .video(VideoConcatenationConfiguration.builder()
                                                        .state(ArtifactsConcatenationState.DISABLED)
                                                        .build())
                                                .compositedVideo(CompositedVideoConcatenationConfiguration.builder()
                                                        .state(ArtifactsConcatenationState.ENABLED)
                                                        .build())
                                                .content(ContentConcatenationConfiguration.builder()
                                                        .state(ArtifactsConcatenationState.DISABLED)
                                                        .build())
                                                .dataChannel(DataChannelConcatenationConfiguration.builder()
                                                        .state(ArtifactsConcatenationState.ENABLED)
                                                        .build())
                                                .transcriptionMessages(
                                                        TranscriptionMessagesConcatenationConfiguration.builder()
                                                                .state(ArtifactsConcatenationState.ENABLED)
                                                                .build()
                                                )
                                                .meetingEvents(MeetingEventsConcatenationConfiguration.builder()
                                                        .state(ArtifactsConcatenationState.DISABLED)
                                                        .build())
                                                .build())
                                .build())
                .build();
    }

    private static MeetingInfo getMeetingResponse(Meeting meeting, Optional<String> userID) {
        String meetingId = meeting.getMeetingId();
        return MeetingInfo.builder()
                .meetingID(meetingId)
                .externalMeetingID(meeting.getExternalMeetingId())
                .mediaRegion(meeting.getMediaRegion())
                .mediaPlacement(meeting.getMediaPlacement())
                .hostUserID(meeting.getHostUserId())
                .hostJoined(meeting.isHostJoined())
                .host(userID.map(meeting::isHost).orElse(null))
                .createdAt(meeting.getCreatedAt())
                .startedAt(meeting.getStartedAt())
                .endedAt(meeting.getEndedAt())
                .ended(meeting.isEnded())
                .waitingUsers(meeting.getWaitingUsers())
                .participants(meeting.getParticipantIds())
                .meetingJoinURL(URI.create("/api/v1/meetings/%s/join".formatted(meetingId)))
                .summarized(meeting.isSummarized())
                .build();
    }

    private static AttendeeJoinInfo getAttendeeJoinInfo(Meeting meeting, Attendee attendee) {
        String userId = attendee.getUserId();
        return AttendeeJoinInfo.builder()
                .ready(meeting.isHostJoined() || meeting.isHost(userId))
                .userId(userId)
                .attendeeId(attendee.getAttendeeId())
                .joinToken(attendee.getJoinToken())
                .meetingStartedAt(meeting.getStartedAt())
                .build();
    }
}

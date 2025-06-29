package com.yourkoder.letsmeet.processmanager.util.meet;

import com.yourkoder.letsmeet.domain.auth.repository.UserRepository;
import com.yourkoder.letsmeet.domain.meet.repository.AttendeeRepository;
import com.yourkoder.letsmeet.domain.meet.repository.MeetingGeneratedInfoRepository;
import com.yourkoder.letsmeet.domain.meet.repository.MeetingRepository;
import com.yourkoder.letsmeet.domain.meet.service.MeetingService;
import com.yourkoder.letsmeet.processmanager.config.ApplicationConfig;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import jakarta.ws.rs.Produces;
import software.amazon.awssdk.services.chimesdkmediapipelines.ChimeSdkMediaPipelinesClient;
import software.amazon.awssdk.services.chimesdkmeetings.ChimeSdkMeetingsClient;

@ApplicationScoped
public class MeetingServiceProducer {

    @Inject
    ApplicationConfig config;

    @Inject
    ChimeSdkMeetingsClient meetingsClient;

    @Inject
    ChimeSdkMediaPipelinesClient pipelineClient;

    @Inject
    MeetingRepository meetingRepo;

    @Inject
    AttendeeRepository attendeeRepo;

    @Inject
    MeetingGeneratedInfoRepository meetingGeneratedInfoRepo;

    @Inject
    UserRepository userRepo;

    @Produces
    @ApplicationScoped
    @Alternative
    @Priority(1)
    public MeetingService produce() {
        return new MeetingService(
                config.aws().getRegion(),
                config.applicationNamespace(),
                config.aws().s3().bucket(),
                false,
                meetingsClient,
                pipelineClient,
                meetingRepo,
                attendeeRepo,
                meetingGeneratedInfoRepo,
                userRepo
        );
    }
}

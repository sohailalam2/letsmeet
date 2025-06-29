package com.yourkoder.letsmeet.processmanager.util.database;

import com.yourkoder.letsmeet.domain.auth.repository.UserRepository;
import com.yourkoder.letsmeet.domain.meet.repository.AttendeeRepository;
import com.yourkoder.letsmeet.domain.meet.repository.MeetingGeneratedInfoRepository;
import com.yourkoder.letsmeet.domain.meet.repository.MeetingRepository;
import com.yourkoder.letsmeet.processmanager.config.ApplicationConfig;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.ws.rs.Produces;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

@ApplicationScoped
@Alternative
@Priority(1)
public class DynamoDbRepositoryProducer {

    private final ApplicationConfig config;

    private final DynamoDbEnhancedClient client;

    private final AttendeeRepository attendeeRepository;

    public DynamoDbRepositoryProducer(ApplicationConfig config, DynamoDbEnhancedClient client) {
        this.config = config;
        this.client = client;
        String tableName = "%s-%s"
                .formatted(config.applicationNamespace(), config.meetingTableName());
        this.attendeeRepository = new AttendeeRepository(
                tableName,
                client
        );
    }

    @Produces
    @ApplicationScoped
    @Alternative
    @Priority(1)
    public MeetingRepository produceMeetingRepository() {
        String tableName = "%s-%s"
                .formatted(config.applicationNamespace(), config.meetingTableName());
        return new MeetingRepository(
                tableName,
                client,
                attendeeRepository
        );
    }

    @Produces
    @ApplicationScoped
    @Alternative
    @Priority(1)
    public AttendeeRepository produceAttendeeRepository() {
        return attendeeRepository;
    }

    @Produces
    @ApplicationScoped
    @Alternative
    @Priority(1)
    public MeetingGeneratedInfoRepository produceMeetingGeneratedInfoRepository() {
        String tableName = "%s-%s"
                .formatted(config.applicationNamespace(), config.meetingTableName());
        return new MeetingGeneratedInfoRepository(
                tableName,
                client
        );
    }

    @Produces
    @ApplicationScoped
    @Alternative
    @Priority(1)
    public UserRepository produceUserRepository() {
        String tableName = "%s-%s"
                .formatted(config.applicationNamespace(), config.iamTableName());
        return new UserRepository(
                tableName,
                client
        );
    }
}

package com.yourkoder.letsmeet.util.database;

import com.yourkoder.letsmeet.domain.auth.repository.UserRepository;
import com.yourkoder.letsmeet.domain.meet.repository.AttendeeRepository;
import com.yourkoder.letsmeet.domain.meet.repository.MeetingGeneratedInfoRepository;
import com.yourkoder.letsmeet.domain.meet.repository.MeetingRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.ws.rs.Produces;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

@ApplicationScoped
@Default
public class DefaultDynamoDbRepositoryProducer {

    private final DynamoDbEnhancedClient client;

    private final AttendeeRepository attendeeRepository;

    public DefaultDynamoDbRepositoryProducer(DynamoDbEnhancedClient client) {
        this.client = client;
        String tableName = "default-table";
        this.attendeeRepository = new AttendeeRepository(
                tableName,
                client
        );
    }

    @Produces
    @ApplicationScoped
    @Default
    public MeetingRepository produceMeetingRepository() {
        String tableName = "default-table";
        return new MeetingRepository(
                tableName,
                client,
                attendeeRepository
        );
    }

    @Produces
    @ApplicationScoped
    @Default
    public AttendeeRepository produceAttendeeRepository() {
        return attendeeRepository;
    }

    @Produces
    @ApplicationScoped
    @Default
    public MeetingGeneratedInfoRepository produceMeetingGeneratedInfoRepository() {
        String tableName = "default-table";
        return new MeetingGeneratedInfoRepository(
                tableName,
                client
        );
    }

    @Produces
    @ApplicationScoped
    @Default
    public UserRepository produceUserRepository() {
        String tableName = "default-table";
        return new UserRepository(
                tableName,
                client
        );
    }
}

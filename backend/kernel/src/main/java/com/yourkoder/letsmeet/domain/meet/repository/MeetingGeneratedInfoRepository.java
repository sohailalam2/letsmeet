package com.yourkoder.letsmeet.domain.meet.repository;

import com.yourkoder.letsmeet.domain.meet.model.GeneratedMeetingData;
import jakarta.enterprise.inject.Vetoed;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.Optional;

@Vetoed
public class MeetingGeneratedInfoRepository {
    private final DynamoDbTable<GeneratedMeetingData> table;

    public MeetingGeneratedInfoRepository(
            String tableName,
            DynamoDbEnhancedClient client
    ) {
        table = client.table(tableName, TableSchema.fromBean(GeneratedMeetingData.class));
    }

    public void save(GeneratedMeetingData m) {
        table.putItem(m);
    }

    public Optional<GeneratedMeetingData> findById(String id) {
        GeneratedMeetingData meeting  = new GeneratedMeetingData();
        meeting.setPrimaryKey(id);
        return Optional.ofNullable(table.getItem(
                Key.builder().partitionValue(meeting.getPk()).sortValue(meeting.getSk()).build()
        ));
    }
}

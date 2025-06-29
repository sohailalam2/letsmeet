package com.yourkoder.letsmeet.domain.meet.repository;

import com.yourkoder.letsmeet.domain.meet.model.Attendee;
import com.yourkoder.letsmeet.domain.meet.model.Meeting;
import jakarta.enterprise.inject.Vetoed;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchGetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchGetResultPage;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchGetResultPageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ReadBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Vetoed
public class MeetingRepository {
    private final DynamoDbTable<Meeting> table;
    private final DynamoDbEnhancedClient enhancedClient;

    private final AttendeeRepository attendeeRepository;

    public MeetingRepository(String tableName, DynamoDbEnhancedClient client, AttendeeRepository attendeeRepository) {
        this.enhancedClient = client;
        table = client.table(tableName, TableSchema.fromBean(Meeting.class));
        this.attendeeRepository = attendeeRepository;
    }

    public void save(Meeting m) {
        table.putItem(m);
    }

    public Optional<Meeting> findById(String id) {
        Meeting meeting  = new Meeting();
        meeting.setPrimaryKey(id);
        return Optional.ofNullable(table.getItem(
                Key.builder().partitionValue(meeting.getPk()).sortValue(meeting.getSk()).build()
        ));
    }

    public List<Meeting> getAllMeetings() {
        Meeting meeting = new Meeting();
        return this.table.query(QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(
                        Key.builder().partitionValue(meeting.getPk()).build()
                ))
                .build()).items().stream().toList();
    }

    public List<Meeting> getAllMeetingsForUser(String userID) {
        List<Attendee> attendees = attendeeRepository.findByUserID(userID);
        List<String> meetingIDs = attendees.stream().map(Attendee::getMeetingId).toList();
        return getMeetingsByIds(meetingIDs);
    }

    public List<Meeting> getMeetingsByIds(List<String> meetingIDs) {
        List<Meeting> results = new ArrayList<>();
        int batchSize = 100;
        Meeting meeting = new Meeting();

        for (int i = 0; i < meetingIDs.size(); i += batchSize) {
            List<Key> keys = meetingIDs.subList(i, Math.min(i + batchSize, meetingIDs.size()))
                    .stream()
                    .map(id -> {
                        meeting.setSkFromMeetingId(id);
                        return Key.builder()
                                .partitionValue(meeting.getPk())
                                .sortValue(meeting.getSk())
                                .build();
                    })
                    .toList();

            // Build the ReadBatch properly
            ReadBatch.Builder<Meeting> readBatchBuilder = ReadBatch.builder(Meeting.class)
                    .mappedTableResource(table);

            keys.forEach(readBatchBuilder::addGetItem);

            BatchGetItemEnhancedRequest batchRequest = BatchGetItemEnhancedRequest.builder()
                    .readBatches(readBatchBuilder.build())
                    .build();

            // Execute and collect results
            BatchGetResultPageIterable responsePages = enhancedClient.batchGetItem(batchRequest);
            for (BatchGetResultPage page : responsePages) {
                results.addAll(page.resultsForTable(table));
            }
        }

        return results;
    }
}

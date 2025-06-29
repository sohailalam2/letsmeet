package com.yourkoder.letsmeet.domain.meet.repository;

import com.yourkoder.letsmeet.domain.meet.model.Attendee;
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
public class AttendeeRepository {
    private final DynamoDbTable<Attendee> table;

    private final DynamoDbEnhancedClient client;

    public AttendeeRepository(String tableName, DynamoDbEnhancedClient client) {
        table = client.table(tableName, TableSchema.fromBean(Attendee.class));
        this.client = client;
    }

    public void save(Attendee a) {
        table.putItem(a);
    }

    public Optional<Attendee> findByID(String userID, String meetingID) {
        Attendee attendee = new Attendee();
        attendee.setPrimaryKey(userID, meetingID);
        return Optional.ofNullable(table.getItem(Key.builder().partitionValue(
                attendee.getPk()
        ).sortValue(attendee.getSk()).build()));
    }

    public void delete(String userID, String meetingID) {
        Attendee attendee = new Attendee();
        attendee.setPrimaryKey(userID, meetingID);
        table.deleteItem(Key.builder().partitionValue(attendee.getPk()).sortValue(attendee.getSk()).build());
    }

    public List<Attendee> findByUserID(String userID) {
        Attendee attendee = new Attendee();
        attendee.setSkPrefixFromUserID(userID);
        return this.table.query(QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.sortBeginsWith(
                        Key.builder().partitionValue(attendee.getPk()).sortValue(attendee.getSk()).build()
                ))
                .build()).items().stream().toList();
    }

    public List<Attendee> getAttendeesByIDs(String meetingID, List<String> attendeeIDs) {
        Attendee attendee = new Attendee();

        List<Attendee> results = new ArrayList<>();
        int batchSize = 100;

        for (int i = 0; i < attendeeIDs.size(); i += batchSize) {
            List<Key> keys = attendeeIDs.subList(i, Math.min(i + batchSize, attendeeIDs.size()))
                    .stream()
                    .map(id -> {
                        attendee.setPrimaryKey(id, meetingID);
                        return Key.builder()
                                .partitionValue(attendee.getPk())
                                .sortValue(attendee.getSk())
                                .build();
                    })
                    .toList();

            // Build the ReadBatch properly
            ReadBatch.Builder<Attendee> readBatchBuilder = ReadBatch.builder(Attendee.class)
                    .mappedTableResource(table);

            keys.forEach(readBatchBuilder::addGetItem);

            BatchGetItemEnhancedRequest batchRequest = BatchGetItemEnhancedRequest.builder()
                    .readBatches(readBatchBuilder.build())
                    .build();

            // Execute and collect results
            BatchGetResultPageIterable responsePages = client.batchGetItem(batchRequest);
            for (BatchGetResultPage page : responsePages) {
                results.addAll(page.resultsForTable(table));
            }
        }

        return results;
    }
}
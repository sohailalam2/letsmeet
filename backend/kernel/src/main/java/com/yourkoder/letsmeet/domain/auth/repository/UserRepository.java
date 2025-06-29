package com.yourkoder.letsmeet.domain.auth.repository;

import com.yourkoder.letsmeet.domain.auth.model.User;
import jakarta.enterprise.inject.Vetoed;
import jakarta.inject.Inject;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchGetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchGetResultPage;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchGetResultPageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.ReadBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Vetoed
public class UserRepository {
    private final DynamoDbTable<User> table;

    private final DynamoDbEnhancedClient client;

    @Inject
    public UserRepository(String tableName, DynamoDbEnhancedClient client) {
        this.client = client;
        this.table = client.table(tableName, TableSchema.fromBean(User.class));
    }

    /**
     * Save or update a User.
     * If you’re creating a new user, call user.initialize(userInfo) first.
     */
    public void save(User user) {
        table.putItem(user);
    }

    /**
     * Lookup by the “sub” claim (userID).
     */
    public Optional<User> findById(String userId) {
        User user = new User();
        user.setPrimaryKey(userId);
        Key key = Key.builder()
                .partitionValue(user.getPk())
                .sortValue(user.getSk())
                .build();
        return Optional.ofNullable(table.getItem(key));
    }

    /**
     * Delete a user by userId.
     */
    public void deleteById(String userId) {
        User user = new User();
        user.setPrimaryKey(userId);
        Key key = Key.builder()
                .partitionValue(user.getPk())
                .sortValue(user.getSk())
                .build();
        table.deleteItem(key);
    }

    /**
     * Delete a user by userId.
     */
    public void deleteUser(User user) {
        Key key = Key.builder()
                .partitionValue(user.getPk())
                .sortValue(user.getSk())
                .build();
        table.deleteItem(key);
    }

    public void updateUser(User existingUser) {
        table.updateItem(existingUser);
    }

    public List<User> getUsersByIds(List<String> userIDs) {
        List<User> results = new ArrayList<>();
        int batchSize = 100;
        User meeting = new User();

        for (int i = 0; i < userIDs.size(); i += batchSize) {
            List<Key> keys = userIDs.subList(i, Math.min(i + batchSize, userIDs.size()))
                    .stream()
                    .map(id -> {
                        meeting.setSkFromUserId(id);
                        return Key.builder()
                                .partitionValue(meeting.getPk())
                                .sortValue(meeting.getSk())
                                .build();
                    })
                    .toList();

            // Build the ReadBatch properly
            ReadBatch.Builder<User> readBatchBuilder = ReadBatch.builder(User.class)
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

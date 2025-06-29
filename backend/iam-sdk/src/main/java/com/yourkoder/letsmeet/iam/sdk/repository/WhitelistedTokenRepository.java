package com.yourkoder.letsmeet.iam.sdk.repository;

import com.yourkoder.letsmeet.iam.sdk.model.WhitelistedToken;
import jakarta.enterprise.inject.Vetoed;
import jakarta.inject.Inject;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Vetoed
public class WhitelistedTokenRepository {
    private final DynamoDbTable<WhitelistedToken> table;
    private final DynamoDbEnhancedClient enhancedClient;

    @Inject
    public WhitelistedTokenRepository(String tableName, DynamoDbEnhancedClient client) {
        this.enhancedClient = client;
        this.table = client.table(tableName, TableSchema.fromBean(WhitelistedToken.class));
    }

    /**
     * Save or update a WhiteListedToken.
     * If you’re creating a new whiteListedToken, call whiteListedToken.initialize(whiteListedTokenInfo) first.
     */
    public void save(WhitelistedToken whiteListedToken) {
        table.putItem(whiteListedToken);
    }

    /**
     * Lookup by the “sub” claim (whiteListedTokenID).
     */
    public Optional<WhitelistedToken> findById(String userID, String tokenID) {
        WhitelistedToken whiteListedToken = new WhitelistedToken();
        whiteListedToken.setPrimaryKey(userID, tokenID);
        Key key = Key.builder()
                .partitionValue(whiteListedToken.getPk())
                .sortValue(whiteListedToken.getSk())
                .build();
        return Optional.ofNullable(table.getItem(key));
    }

    public List<WhitelistedToken> getAllTokensBySecurityPrincipalId(String userID) {
        WhitelistedToken whiteListedToken = new WhitelistedToken();
        whiteListedToken.setPkFromUserID(userID);
        return this.table.query(QueryEnhancedRequest.builder()
                        .queryConditional(QueryConditional.keyEqualTo(
                                Key.builder().partitionValue(whiteListedToken.getPk()).build()
                        ))
                .build()).items().stream().toList();
    }

    /**
     * Delete a whiteListedToken by whiteListedTokenId.
     */
    public void deleteById(String userID, String tokenID) {
        WhitelistedToken whiteListedToken = new WhitelistedToken();
        whiteListedToken.setPrimaryKey(userID, tokenID);
        Key key = Key.builder()
                .partitionValue(whiteListedToken.getPk())
                .sortValue(whiteListedToken.getSk())
                .build();
        table.deleteItem(key);
    }

    /**
     * Delete a whiteListedToken by whiteListedTokenId.
     */
    public void deleteWhiteListedToken(WhitelistedToken whiteListedToken) {
        Key key = Key.builder()
                .partitionValue(whiteListedToken.getPk())
                .sortValue(whiteListedToken.getSk())
                .build();
        table.deleteItem(key);
    }

    public void updateWhiteListedToken(WhitelistedToken existingWhitelistedToken) {
        table.updateItem(existingWhitelistedToken);
    }

    public void deleteAllWhitelistedToken(String userID) {
        // 1. Query all items with this PK
        WhitelistedToken user = new WhitelistedToken();
        user.setPkFromUserID(userID);
        List<WhitelistedToken> items = table.query(r ->
                r.queryConditional(QueryConditional.keyEqualTo(
                        Key.builder().partitionValue(user.getPk()).build()
                ))
        ).items().stream().toList();

        // 2. Batch delete in groups of up to 25
        for (List<WhitelistedToken> chunk : partition(items, 25)) {
            enhancedClient.batchWriteItem(b -> {
                WriteBatch.Builder<WhitelistedToken> batch = WriteBatch.builder(WhitelistedToken.class)
                        .mappedTableResource(table);
                chunk.forEach(item -> batch.addDeleteItem(bi -> bi.key(Key.builder()
                        .partitionValue(item.getPk())
                        .sortValue(item.getSk())
                        .build())));
                b.addWriteBatch(batch.build());
            });
        }
    }

    // Utility: split list into sublists of up to 'size'
    private <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> parts = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            parts.add(list.subList(i, Math.min(list.size(), i + size)));
        }
        return parts;
    }
}

package com.yourkoder.letsmeet.iam.sdk.util.database;

import com.yourkoder.letsmeet.iam.sdk.repository.WhitelistedTokenRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;
import jakarta.ws.rs.Produces;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

@ApplicationScoped
public class DefaultDynamoDbRepositoryProducer {

    @Inject
    DynamoDbEnhancedClient client;

    @Produces
    @ApplicationScoped
    @Default
    public WhitelistedTokenRepository produce() {
        String tableName = "default-iam-table";
        return new WhitelistedTokenRepository(
                tableName,
                client
        );
    }
}

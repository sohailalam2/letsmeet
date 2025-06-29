package com.yourkoder.letsmeet.iam.util.database;

import com.yourkoder.letsmeet.iam.config.ApplicationConfig;
import com.yourkoder.letsmeet.iam.sdk.repository.WhitelistedTokenRepository;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import jakarta.ws.rs.Produces;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

@ApplicationScoped
public class DynamoDbRepositoryProducer {

    @Inject
    DynamoDbEnhancedClient client;

    @Inject
    ApplicationConfig config;

    @Produces
    @ApplicationScoped
    @Alternative
    @Priority(1)
    public WhitelistedTokenRepository produce() {
        String tableName = "%s-%s"
                .formatted(config.applicationNamespace(), config.iamTableName());
        return new WhitelistedTokenRepository(
                tableName,
                client
        );
    }
}

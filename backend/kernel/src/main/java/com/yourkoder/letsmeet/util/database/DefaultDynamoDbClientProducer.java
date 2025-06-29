package com.yourkoder.letsmeet.util.database;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.ws.rs.Produces;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@ApplicationScoped
public class DefaultDynamoDbClientProducer {

    @Produces
    @ApplicationScoped
    @Default
    public DynamoDbEnhancedClient produce() {
        DynamoDbClient ddb = DynamoDbClient.builder().region(Region.AP_SOUTH_1).build();
        return DynamoDbEnhancedClient.builder().dynamoDbClient(ddb).build();
    }
}

package com.yourkoder.letsmeet.iam.sdk.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbIgnore;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

import static java.lang.String.format;

@ToString
@EqualsAndHashCode
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamoDbBean
@RegisterForReflection
public class WhitelistedToken implements Serializable {

    private static final String PARTITION_KEY_PREFIX = "WL";

    @Serial
    private static final long serialVersionUID = -5021525643932732990L;

    @Getter(onMethod = @__({@DynamoDbPartitionKey}))
    private String pk;

    @Getter(onMethod = @__({@DynamoDbSortKey}))
    private String sk;

    private Long timestamp = Instant.now().toEpochMilli();

    private Long ttl;

    @DynamoDbIgnore
    public void setPkFromUserID(
            String userID
    ) {
        this.pk = format("%s#%s", PARTITION_KEY_PREFIX, userID);
    }

    @DynamoDbIgnore
    public void setSkFromTokenID(String tokenID) {
        this.sk = tokenID;
    }

    @DynamoDbIgnore
    public String getTokenID() {
        return this.sk;
    }

    @DynamoDbIgnore
    public String getSecurityPrincipalID() {
        return this.pk.split("#")[1];
    }

    @DynamoDbIgnore
    public void setPrimaryKey(String userID, String tokenID) {
        this.setPkFromUserID(userID);
        this.setSkFromTokenID(tokenID);
    }
}


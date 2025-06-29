package com.yourkoder.letsmeet.domain.auth.model;


import com.yourkoder.letsmeet.domain.auth.valueobject.UserInfo;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.UpdateBehavior;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbIgnore;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbUpdateBehavior;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@DynamoDbBean
@RegisterForReflection
public class User {
    private static final String PARTITION_KEY = "USR";

    // constant partition key so all users share the same PK namespace
    @Getter(onMethod_ = {
            @DynamoDbPartitionKey,
            @DynamoDbAttribute("pk"),
            @DynamoDbUpdateBehavior(UpdateBehavior.WRITE_ALWAYS)
    })
    @Builder.Default
    private String pk = PARTITION_KEY;

    // sort key = the user’s “sub” claim
    @Getter(onMethod_ = {
            @DynamoDbSortKey,
            @DynamoDbAttribute("sk"),
            @DynamoDbUpdateBehavior(UpdateBehavior.WRITE_ALWAYS)
    })
    private String sk;

    // user attributes
    private String name;
    private String email;
    private String picture;

    // when this record was created
    @Getter(onMethod_ = @DynamoDbAttribute("created_at"))
    private Long createdAtEpochSec;

    @Getter(onMethod_ = @DynamoDbAttribute("trello_token_path"))
    @Setter(onMethod_ = @DynamoDbAttribute("trello_token_path"))
    private String trelloTokenPath;

    @Getter(onMethod_ = @DynamoDbAttribute("trello_board_id"))
    @Setter(onMethod_ = @DynamoDbAttribute("trello_board_id"))
    private String trelloBoardID;

    @Getter(onMethod_ = @DynamoDbAttribute("trello_board_name"))
    @Setter(onMethod_ = @DynamoDbAttribute("trello_board_name"))
    private String trelloBoardName;

    @Getter(onMethod_ = @DynamoDbAttribute("trello_list_id"))
    @Setter(onMethod_ = @DynamoDbAttribute("trello_list_id"))
    private String trelloListID;

    @Getter(onMethod_ = @DynamoDbAttribute("trello_list_name"))
    @Setter(onMethod_ = @DynamoDbAttribute("trello_list_name"))
    private String trelloListName;

    // convenience setters/getters to mirror your Meeting style:

    @DynamoDbIgnore
    public String getUserId() {
        return this.sk;
    }

    @DynamoDbIgnore
    public void setSkFromUserId(String userId) {
        this.sk = userId;
    }

    @DynamoDbIgnore
    public void setPrimaryKey(String userId) {
        setSkFromUserId(userId);
    }

    @DynamoDbIgnore
    public void initialize(UserInfo info) {
        setPrimaryKey(info.getUserID());
        setName(info.getName());
        setEmail(info.getEmail());
        setPicture(info.getPicture());
        setCreateAt(Instant.now());
    }

    @DynamoDbIgnore
    private void setCreateAt(Instant now) {
        this.createdAtEpochSec = now.getEpochSecond();
    }

    @DynamoDbIgnore
    public Instant getCreateAt() {
         return Instant.ofEpochSecond(this.createdAtEpochSec);
    }
}

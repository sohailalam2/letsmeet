package com.yourkoder.letsmeet.domain.meet.valueobject;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

import java.util.List;

@EqualsAndHashCode
@ToString
@RegisterForReflection
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@DynamoDbBean
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActionItem {
    @JsonProperty("task")
    private String task;

    @JsonProperty("assignee")
    private String assignee;

    @JsonProperty("detailed-breakdown")
    private List<String> detailedBreakdown;

    @JsonProperty("due_date")
    private String dueDate;
}

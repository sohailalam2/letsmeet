package com.yourkoder.letsmeet.api.meet.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yourkoder.letsmeet.domain.auth.model.User;
import com.yourkoder.letsmeet.domain.meet.valueobject.ActionItem;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActionItemDTO {
    @JsonProperty("task")
    private String task;

    @JsonProperty("assignee")
    private String assignee;

    @JsonProperty("assignee_name")
    private String assigneeName;

    @JsonProperty("assignee_email")
    private String assigneeEmail;

    @JsonProperty("detailed_breakdown")
    private List<String> detailedBreakdown;

    @JsonProperty("due_date")
    private String dueDate;

    public static ActionItemDTO from(ActionItem actionItem, User user) {
        return ActionItemDTO.builder()
                .assigneeEmail(user != null ? user.getEmail() : null)
                .assigneeName(user != null ? user.getName() : null)
                .assignee(actionItem.getAssignee())
                .dueDate(actionItem.getDueDate())
                .task(actionItem.getTask())
                .detailedBreakdown(actionItem.getDetailedBreakdown())
                .build();
    }
}

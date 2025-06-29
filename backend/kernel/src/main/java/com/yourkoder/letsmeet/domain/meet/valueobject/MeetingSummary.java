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
@JsonIgnoreProperties(ignoreUnknown = true)
@DynamoDbBean
public class MeetingSummary {

    @JsonProperty("summary")
    private String summary;

    @JsonProperty("meeting_notes")
    private List<String> meetingNotes;
}

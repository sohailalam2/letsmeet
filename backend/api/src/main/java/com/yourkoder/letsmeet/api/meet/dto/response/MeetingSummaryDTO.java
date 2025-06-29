package com.yourkoder.letsmeet.api.meet.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yourkoder.letsmeet.domain.meet.valueobject.MeetingSummary;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeetingSummaryDTO {

    @JsonProperty("summary")
    private String summary;

    @JsonProperty("meeting_notes")
    private List<String> notes;

    public static MeetingSummaryDTO from(MeetingSummary meetingSummary) {
        return MeetingSummaryDTO.builder()
                .summary(meetingSummary.getSummary())
                .notes(meetingSummary.getMeetingNotes())
                .build();
    }
}

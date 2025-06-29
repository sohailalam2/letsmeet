package com.yourkoder.letsmeet.api.trello.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@ToString
@EqualsAndHashCode
@RegisterForReflection
public class UserTrelloInfoRequestDTO {

    @JsonProperty("board_id")
    private String boardID;

    @JsonProperty("list_id")
    private String listID;
}

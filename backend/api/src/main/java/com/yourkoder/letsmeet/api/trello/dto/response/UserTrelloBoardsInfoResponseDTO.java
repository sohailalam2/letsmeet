package com.yourkoder.letsmeet.api.trello.dto.response;

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
public class UserTrelloBoardsInfoResponseDTO {

    @JsonProperty("user_id")
    private String userID;

    @JsonProperty("board_id")
    private String boardID;

    @JsonProperty("list_id")
    private String listID;
}

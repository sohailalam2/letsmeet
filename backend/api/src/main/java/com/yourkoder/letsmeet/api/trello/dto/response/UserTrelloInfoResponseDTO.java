package com.yourkoder.letsmeet.api.trello.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yourkoder.letsmeet.domain.auth.model.User;
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
public class UserTrelloInfoResponseDTO {

    @JsonProperty("user_id")
    private String userID;

    @JsonProperty("board_id")
    private String boardID;

    @JsonProperty("board_name")
    private String boardName;

    @JsonProperty("list_id")
    private String listID;

    @JsonProperty("list_name")
    private String listName;

    public static UserTrelloInfoResponseDTO from(User user) {
        return UserTrelloInfoResponseDTO.builder()
                .userID(user.getUserId())
                .boardID(user.getTrelloBoardID())
                .boardName(user.getTrelloBoardName())
                .listID(user.getTrelloListID())
                .listName(user.getTrelloListName())
                .build();
    }
}

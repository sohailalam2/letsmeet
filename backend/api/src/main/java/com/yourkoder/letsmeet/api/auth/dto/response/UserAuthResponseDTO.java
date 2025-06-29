package com.yourkoder.letsmeet.api.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties
@Schema
@RegisterForReflection
public class UserAuthResponseDTO {

    @JsonProperty("user_id")
    @Schema(required = true, example = "sid_1234", description = "The user's unique ID.")
    private String userID;

    @JsonProperty("access_token")
    @Schema(required = true, example = "abcd12xyz==", description = """
            This access token can be used to make requests to the backend APIs from
            another application owned by the user. Please do take to not leak the access token.""")
    private String accessToken;

    @JsonProperty("refresh_token")
    @Schema(required = true, example = "abcd12xyz==", description = """
            This refresh token can be used to make refrsh token grant request to the backend API. 
            Please do take to not leak the refresh token.""")
    private String refreshToken;

    @JsonProperty("creation_timestamp")
    @Schema(required = true, example = "1234235",
            description = """
                    The UTC timestamp in milliseconds of when the user was created 
                    since Unix epoch.""")
    private Long createdAt;


}

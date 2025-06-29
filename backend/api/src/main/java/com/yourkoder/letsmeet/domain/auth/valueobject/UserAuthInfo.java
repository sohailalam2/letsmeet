package com.yourkoder.letsmeet.domain.auth.valueobject;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.Instant;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties
public class UserAuthInfo {

    private String userID;

    private String accessToken;

    private String refreshToken;

    private Instant createdAt;


}

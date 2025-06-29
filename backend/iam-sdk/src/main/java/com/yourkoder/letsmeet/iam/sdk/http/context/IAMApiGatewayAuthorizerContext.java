package com.yourkoder.letsmeet.iam.sdk.http.context;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Data
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
@RegisterForReflection
@Builder
public final class IAMApiGatewayAuthorizerContext {
    private final Map<String, String> contextProperties = new HashMap<>();

    private String principalId;

    @JsonAnyGetter
    public String getContextValue(String key) {
        return this.contextProperties.get(key);
    }

    @JsonAnySetter
    public void setContextValue(String key, String value) {
        this.contextProperties.put(key, value);
    }

}

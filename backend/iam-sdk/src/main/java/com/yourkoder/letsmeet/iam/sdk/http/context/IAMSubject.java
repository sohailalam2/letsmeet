package com.yourkoder.letsmeet.iam.sdk.http.context;

import com.yourkoder.letsmeet.iam.sdk.http.context.enums.IAMContextKey;
import com.yourkoder.letsmeet.iam.sdk.http.context.exceptions.InvalidSubjectException;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;

@Getter
@RegisterForReflection
public final class IAMSubject {

    private final String value;

    public IAMSubject(
            IAMApiGatewayAuthorizerContext authorizerContext
    ) throws InvalidSubjectException {

        String iamSubject = authorizerContext.getPrincipalId();

        if (iamSubject == null) {
            throw new InvalidSubjectException(IAMContextKey.SUBJECT);
        }

        this.value = iamSubject;
    }
}

package com.yourkoder.letsmeet.iam.authorizer.token.valueobject;

import com.yourkoder.letsmeet.iam.authorizer.token.valueobject.exception.InvalidJwtFormatException;
import lombok.Getter;

import static java.lang.String.format;

@Getter
public class AuthorizationBearerToken {

    public static final String BEARER_TOKEN_PREFIX = "Bearer";
    private final String value;

    private final JWTType jwtType;

    public AuthorizationBearerToken(
            String authorisationHeader
    ) throws InvalidJwtFormatException {
        if (authorisationHeader == null) {
            throw new InvalidJwtFormatException(format(
                    "The authorisation token provided is null. %s",
                    "The format must be \"Bearer <token>\"."
            ));
        }
        String[] authorisationHeaderTokens = authorisationHeader.split("\\s+");
        if (authorisationHeaderTokens.length != 2) {
            throw new InvalidJwtFormatException(format(
                    "The authorisation token provided: [%s] does not have a valid format. %s",
                    authorisationHeader,
                    "The format must be \"Bearer <token>\"."
            ));
        }
        if (!authorisationHeaderTokens[0].equalsIgnoreCase(BEARER_TOKEN_PREFIX)) {
            throw new InvalidJwtFormatException(format(
                    "The authorisation token provided: [%s] is not a Bearer token.",
                    authorisationHeader
            ));
        }

        this.value = authorisationHeaderTokens[1];

        this.jwtType = JWTType.getTokenType(this.value);
    }

    public static AuthorizationBearerToken fromTokenString(String tokenString) throws InvalidJwtFormatException {
        return new AuthorizationBearerToken(format("%s %s", BEARER_TOKEN_PREFIX, tokenString));
    }

    @Override
    public String toString() {
        return this.value;
    }
}

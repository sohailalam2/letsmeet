package com.yourkoder.letsmeet.iam.authorizer.token.valueobject;

import com.yourkoder.letsmeet.iam.authorizer.token.valueobject.exception.InvalidJwtFormatException;
import lombok.Getter;

import static java.lang.String.format;

public enum JWTType {

    JWS(JWTTypeComponents.JWS_COMPONENTS),
    JWE(JWTTypeComponents.JWE_COMPONENTS),
    NONE(JWTTypeComponents.NONE_COMPONENTS);

    @Getter
    private final int components;

    JWTType(int newComponents) {
        this.components = newComponents;
    }

    public static JWTType getTokenType(String tokenString) throws InvalidJwtFormatException {
        int tokenComponents = getTokenComponents(tokenString);
        switch (tokenComponents) {
            case JWTTypeComponents.JWS_COMPONENTS:
                return JWS;
            case JWTTypeComponents.JWE_COMPONENTS:
                return JWE;
            case JWTTypeComponents.NONE_COMPONENTS:
                return NONE;
            default:
                throw new InvalidJwtFormatException(format(
                        "Invalid JWT: [%s] token received. Token is not JWS or JWE. Can not get token type.",
                        tokenString
                ));
        }
    }

    public static int getTokenComponents(String tokenString) {
        return tokenString.split("\\.").length;
    }

    private static final class JWTTypeComponents {

        public static final int JWS_COMPONENTS = 3;

        public static final int JWE_COMPONENTS = 5;

        public static final int NONE_COMPONENTS = 1;

        private JWTTypeComponents() {

        }
    }
}

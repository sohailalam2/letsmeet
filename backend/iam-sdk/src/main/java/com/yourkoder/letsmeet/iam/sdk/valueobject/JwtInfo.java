package com.yourkoder.letsmeet.iam.sdk.valueobject;

import com.nimbusds.jwt.JWTClaimsSet;

public record JwtInfo(String token, JWTClaimsSet claimsSet) {
}

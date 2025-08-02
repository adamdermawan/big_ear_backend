package org.bigear.api.bigearbackend.googleAuth;

public record GoogleAuthRequest(
        String idToken,
        String email,
        String name,
        String picture
) {}
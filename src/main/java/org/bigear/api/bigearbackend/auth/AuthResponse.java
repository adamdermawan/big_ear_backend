package org.bigear.api.bigearbackend.auth;

import org.bigear.api.bigearbackend.users.User;

public record AuthResponse(String token, User user) {}

package org.bigear.api.bigearbackend.dto;


public record ChangePasswordRequest(String currentPassword, String newPassword) {}

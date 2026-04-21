package com.redmuqui.platform.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RecoverRequest(
    @NotBlank @Email String email
) {}

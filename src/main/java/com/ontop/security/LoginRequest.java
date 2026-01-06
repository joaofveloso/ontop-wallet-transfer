package com.ontop.security;

import javax.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "Client secret is required")
    String clientSecret
) {}

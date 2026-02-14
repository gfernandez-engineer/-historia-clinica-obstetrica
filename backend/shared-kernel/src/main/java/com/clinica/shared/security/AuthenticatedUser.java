package com.clinica.shared.security;

import java.util.UUID;

public record AuthenticatedUser(UUID userId, String email, String rol) {
}

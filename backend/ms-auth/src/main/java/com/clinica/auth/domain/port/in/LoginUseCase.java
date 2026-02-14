package com.clinica.auth.domain.port.in;

public interface LoginUseCase {

    TokenResponse login(LoginCommand command);

    record LoginCommand(String email, String password) {
    }

    record TokenResponse(String accessToken, String refreshToken) {
    }
}

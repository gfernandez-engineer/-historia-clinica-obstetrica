package com.clinica.auth.domain.port.in;

public interface RefreshTokenUseCase {

    LoginUseCase.TokenResponse refresh(String refreshToken);
}

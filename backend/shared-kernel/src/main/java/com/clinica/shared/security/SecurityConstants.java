package com.clinica.shared.security;

public final class SecurityConstants {

    private SecurityConstants() {
    }

    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String ROLE_CLAIM = "rol";
    public static final String USER_ID_CLAIM = "userId";
    public static final String EMAIL_CLAIM = "email";

    public static final String ROLE_OBSTETRA = "OBSTETRA";
    public static final String ROLE_AUDITOR = "AUDITOR";
    public static final String ROLE_ADMIN = "ADMIN";
}

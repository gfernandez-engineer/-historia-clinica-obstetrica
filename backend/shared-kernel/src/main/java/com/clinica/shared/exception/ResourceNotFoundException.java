package com.clinica.shared.exception;

import java.util.UUID;

public class ResourceNotFoundException extends DomainException {

    public ResourceNotFoundException(String resourceName, UUID id) {
        super(String.format("%s no encontrado con id: %s", resourceName, id));
    }

    public ResourceNotFoundException(String resourceName, String field, String value) {
        super(String.format("%s no encontrado con %s: %s", resourceName, field, value));
    }
}

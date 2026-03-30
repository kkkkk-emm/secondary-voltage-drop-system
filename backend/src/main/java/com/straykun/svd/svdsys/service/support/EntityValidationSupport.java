package com.straykun.svd.svdsys.service.support;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Centralized helper for explicit entity validation in service write paths.
 */
@Component
public class EntityValidationSupport {

    private final Validator validator;

    public EntityValidationSupport(Validator validator) {
        this.validator = validator;
    }

    public <T> void validate(T target) {
        if (target == null) {
            return;
        }
        Set<ConstraintViolation<T>> violations = validator.validate(target);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}


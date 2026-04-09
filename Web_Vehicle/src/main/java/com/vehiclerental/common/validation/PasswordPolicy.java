package com.vehiclerental.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordPolicyValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordPolicy {
    String message() default "Password must be at least 8 characters with uppercase, number, and symbol";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
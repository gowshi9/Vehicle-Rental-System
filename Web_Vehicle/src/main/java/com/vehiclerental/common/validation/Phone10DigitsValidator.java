package com.vehiclerental.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class Phone10DigitsValidator implements ConstraintValidator<Phone10Digits, String> {

    @Override
    public void initialize(Phone10Digits constraintAnnotation) {
    }

    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        if (phone == null) {
            return true; // Let @NotNull handle null validation
        }

        // Remove all non-digit characters and check if exactly 10 digits remain
        String digitsOnly = phone.replaceAll("\\D", "");
        return digitsOnly.length() == 10;
    }
}
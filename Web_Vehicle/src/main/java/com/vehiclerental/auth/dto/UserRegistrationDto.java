package com.vehiclerental.auth.dto;

import com.vehiclerental.common.validation.PasswordPolicy;
import com.vehiclerental.common.validation.Phone10Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRegistrationDto {
    @NotBlank(message = "Username is required")
    private String username;

    @Email(message = "Please provide a valid email")
    @NotBlank(message = "Email is required")
    private String email;

    @Phone10Digits
    @NotBlank(message = "Phone number is required")
    private String phone;

    @PasswordPolicy
    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Please confirm your password")
    private String confirmPassword;
}
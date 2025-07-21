package com.mobilebanking.auth.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for login requests.
 * Contains phone number and PIN for authentication.
 */
public class LoginRequest {

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotBlank(message = "PIN is required")
    private String pin;

    // Default constructor for JSON deserialization
    public LoginRequest() {
    }

    public LoginRequest(String phoneNumber, String pin) {
        this.phoneNumber = phoneNumber;
        this.pin = pin;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    @Override
    public String toString() {
        return String.format("LoginRequest{phoneNumber='%s', pin='[PROTECTED]'}", phoneNumber);
    }
}
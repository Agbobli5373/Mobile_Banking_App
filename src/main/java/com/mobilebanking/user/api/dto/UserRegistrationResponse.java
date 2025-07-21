package com.mobilebanking.user.api.dto;

/**
 * Data Transfer Object for user registration responses.
 * Contains user information returned after successful registration.
 */
public class UserRegistrationResponse {

    private String userId;
    private String name;
    private String phoneNumber;
    private boolean success;
    private String message;

    // Default constructor for JSON serialization
    public UserRegistrationResponse() {
    }

    private UserRegistrationResponse(String userId, String name, String phoneNumber, boolean success, String message) {
        this.userId = userId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.success = success;
        this.message = message;
    }

    /**
     * Creates a successful registration response.
     *
     * @param userId      the user ID
     * @param name        the user's name
     * @param phoneNumber the user's phone number
     * @return successful response
     */
    public static UserRegistrationResponse success(String userId, String name, String phoneNumber) {
        return new UserRegistrationResponse(userId, name, phoneNumber, true, "User registered successfully");
    }

    /**
     * Creates a failed registration response.
     *
     * @param errorMessage the error message
     * @return failed response
     */
    public static UserRegistrationResponse failure(String errorMessage) {
        return new UserRegistrationResponse(null, null, null, false, errorMessage);
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return String.format(
                "UserRegistrationResponse{userId='%s', name='%s', phoneNumber='%s', success=%s, message='%s'}",
                userId, name, phoneNumber, success, message);
    }
}
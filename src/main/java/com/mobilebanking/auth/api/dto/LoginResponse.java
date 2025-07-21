package com.mobilebanking.auth.api.dto;

/**
 * Data Transfer Object for login responses.
 * Contains JWT token and success status.
 */
public class LoginResponse {

    private String token;
    private boolean success;
    private String message;

    // Default constructor for JSON serialization
    public LoginResponse() {
    }

    /**
     * Creates a successful login response with token.
     *
     * @param token the JWT token
     * @return LoginResponse instance
     */
    public static LoginResponse success(String token) {
        LoginResponse response = new LoginResponse();
        response.token = token;
        response.success = true;
        response.message = "Authentication successful";
        return response;
    }

    /**
     * Creates a failed login response with error message.
     *
     * @param message the error message
     * @return LoginResponse instance
     */
    public static LoginResponse failure(String message) {
        LoginResponse response = new LoginResponse();
        response.success = false;
        response.message = message;
        return response;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("LoginResponse{success=%s, message='%s'}", success, message);
    }
}
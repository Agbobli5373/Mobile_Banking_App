package com.mobilebanking.shared.domain.exception;

import com.mobilebanking.shared.domain.UserId;

/**
 * Exception thrown when a user is not found in the system.
 */
public class UserNotFoundException extends RuntimeException {

    private final String userId;

    /**
     * Creates a UserNotFoundException for the specified user ID.
     *
     * @param userId the ID of the user that was not found
     */
    public UserNotFoundException(UserId userId) {
        super("User not found with ID: " + userId.asString());
        this.userId = userId.asString();
    }

    /**
     * Creates a UserNotFoundException for the specified user ID string.
     *
     * @param userId the ID string of the user that was not found
     */
    public UserNotFoundException(String userId) {
        super("User not found with ID: " + userId);
        this.userId = userId;
    }

    /**
     * Gets the ID of the user that was not found.
     *
     * @return the user ID
     */
    public String getUserId() {
        return userId;
    }
}
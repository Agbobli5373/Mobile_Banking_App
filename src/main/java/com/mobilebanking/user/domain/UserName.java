package com.mobilebanking.user.domain;

import com.mobilebanking.shared.domain.exception.InvalidUserNameException;
import java.util.Objects;

/**
 * UserName value object that handles user name validation and formatting.
 * Ensures user names meet business requirements and validation rules.
 */
public final class UserName {
    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 50;

    private final String value;

    private UserName(String value) {
        this.value = value;
    }

    /**
     * Creates a UserName from a string value.
     * 
     * @param name the user name string
     * @return UserName instance
     * @throws InvalidUserNameException if name is invalid
     */
    public static UserName of(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw InvalidUserNameException.nullOrEmpty();
        }

        // Validate the original name first to catch leading/trailing spaces
        validateNameFormat(name);

        String trimmedName = name.trim();
        validateName(trimmedName);

        return new UserName(trimmedName);
    }

    /**
     * Validates the original name format to catch leading/trailing spaces.
     * 
     * @param name the original name to validate
     * @throws InvalidUserNameException if name has leading/trailing spaces
     */
    private static void validateNameFormat(String name) {
        // Check if name has leading or trailing spaces
        if (!name.equals(name.trim())) {
            throw InvalidUserNameException.invalidStartOrEnd();
        }
    }

    /**
     * Validates the user name according to business rules.
     * 
     * @param name the name to validate
     * @throws InvalidUserNameException if name is invalid
     */
    private static void validateName(String name) {
        if (name.length() < MIN_LENGTH || name.length() > MAX_LENGTH) {
            throw InvalidUserNameException.invalidLength(MIN_LENGTH, MAX_LENGTH);
        }

        // Allow letters, spaces, hyphens, and apostrophes
        if (!name.matches("^[a-zA-Z\\s\\-']+$")) {
            throw InvalidUserNameException.invalidFormat();
        }

        // Ensure name doesn't start or end with special characters
        if (name.matches("^[\\s\\-'].*") || name.matches(".*[\\s\\-']$")) {
            throw InvalidUserNameException.invalidStartOrEnd();
        }

        // Prevent consecutive special characters
        if (name.matches(".*[\\s\\-']{2,}.*")) {
            throw InvalidUserNameException.consecutiveSpecialCharacters();
        }
    }

    /**
     * Gets the user name value.
     * 
     * @return the user name string
     */
    public String getValue() {
        return value;
    }

    /**
     * Gets a formatted display version of the user name.
     * Capitalizes first letter of each word.
     * 
     * @return formatted user name for display
     */
    public String getDisplayValue() {
        String[] words = value.split("\\s+");
        StringBuilder formatted = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                formatted.append(" ");
            }
            String word = words[i];
            if (!word.isEmpty()) {
                formatted.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    formatted.append(word.substring(1).toLowerCase());
                }
            }
        }

        return formatted.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        UserName userName = (UserName) obj;
        return Objects.equals(value, userName.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
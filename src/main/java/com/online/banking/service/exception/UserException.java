package com.online.banking.service.exception;

public class UserException extends RuntimeException {

    private final UserError error;

    public UserException(UserError error) {
        super(error.message);
        this.error = error;
    }

    public int getStatusCode() {
        return error.code;
    }

    public UserError getError() {
        return error;
    }

    public enum UserError {
        USER_EMAIL_ALREADY_EXISTS("the user with this email already exists", 409),
        USER_NOT_FOUND("the user doesnt exist", 404);

        private final String message;
        private final int code;

        UserError(String message, int code) {
            this.message = message;
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public int getCode() {
            return code;
        }
    }
}

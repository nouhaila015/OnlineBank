package com.online.banking.service.exception;

public class TransactionException extends RuntimeException {

    private final TransactionError error;

    public TransactionException(TransactionError error) {
        super(error.message);
        this.error = error;
    }

    public int getStatusCode() {
        return error.code;
    }

    public TransactionError getError() {
        return error;
    }

    public enum TransactionError {
        TRANSACTION_NOT_FOUND("Transaction not found", 404);

        private final String message;
        private final int code;

        TransactionError(String message, int code) {
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

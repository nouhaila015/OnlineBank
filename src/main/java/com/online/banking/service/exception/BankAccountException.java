package com.online.banking.service.exception;

public class BankAccountException extends RuntimeException {

    private final BankAccountError error;

    public BankAccountException(BankAccountError error) {
        super(error.message);
        this.error = error;
    }

    public enum BankAccountError {
        BANK_ACCOUNT_NOT_FOUND("Bank account not found", 404),
        BANK_ACCOUNT_ALREADY_EXISTS("Bank account already exists", 409),
        INSUFFICIENT_BALANCE("Insufficient balance", 422),
        INVALID_AMOUNT("Amount must be greater than zero", 400);

        private final String message;
        private final int code;

        BankAccountError(String message, int code) {
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

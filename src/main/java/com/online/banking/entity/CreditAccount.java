package com.online.banking.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "credit_account")
public class CreditAccount extends BankAccount{
    private BigDecimal creditLimit;
    private BigDecimal interestRate;
    private BigDecimal outstandingBalance;
}

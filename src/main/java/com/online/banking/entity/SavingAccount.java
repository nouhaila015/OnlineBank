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
@Table(name = "saving_account")
public class SavingAccount extends BankAccount {
    private BigDecimal interestRate;
    private BigDecimal withdrawalLimit;
}

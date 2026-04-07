package com.online.banking.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@NoArgsConstructor
@SuperBuilder
@Data
@Table(name = "current_account")
public class CurrentAccount extends BankAccount{
    private BigDecimal overdraftLimit;
}

package com.online.banking.entity;

import com.online.banking.entity.enums.Status;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "fixed_deposit_account")
public class FixedDepositAccount extends BankAccount{
    private BigDecimal interestRate;
    private LocalDate maturityDate;
    private boolean locked;
}

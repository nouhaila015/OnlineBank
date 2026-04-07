package com.online.banking.entity;

import com.online.banking.entity.enums.Status;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@SuperBuilder
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class BankAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private BigDecimal balance;
    private LocalDate openedDate;
    @Enumerated(EnumType.STRING)
    private Status status;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "fromBankAccount")
    private List<Transaction> sentTransactions;
    @OneToMany(mappedBy = "toBankAccount")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Transaction> receivedTransactions;
    @ManyToMany(mappedBy = "bankAccounts")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<User> users;
}

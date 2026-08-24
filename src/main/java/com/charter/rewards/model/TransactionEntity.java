package com.charter.rewards.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 *  Data JPA — `TransactionEntity` maps to the `TRANSACTION` table
 */
@Entity
@Table(name = "transaction")
public class TransactionEntity {
    @Id
    @Column(name = "transaction_id", nullable = false)
    private String transactionId;
    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    protected TransactionEntity() {
    }

    public TransactionEntity(String transactionId, String customerId, String customerName,
                             BigDecimal amount, LocalDate transactionDate) {
        this.transactionId = Objects.requireNonNull(transactionId, "transactionId is required");
        this.customerId = Objects.requireNonNull(customerId, "customerId is required");
        this.customerName = Objects.requireNonNull(customerName, "customerName is required");
        this.amount = Objects.requireNonNull(amount, "amount is required");
        this.transactionDate = Objects.requireNonNull(transactionDate, "transactionDate is required");
    }


    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }
}

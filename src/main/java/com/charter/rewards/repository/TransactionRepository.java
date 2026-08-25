package com.charter.rewards.repository;

import com.charter.rewards.model.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 *  `TransactionRepository` (a `JpaRepository`) handles queries.
 */
@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

    /**
     *
     * @param startDate
     * @param endDate
     * @return {@link List}
     */
    List<TransactionEntity> findByTransactionDateBetween(LocalDate startDate, LocalDate endDate);


}

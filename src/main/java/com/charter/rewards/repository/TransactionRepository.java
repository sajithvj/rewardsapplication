package com.charter.rewards.repository;

import com.charter.rewards.model.TransactionEntity;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * `TransactionRepository` (a `JpaRepository`) handles queries.
 */
@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, String> {

  /**
   *
   * @param startDate
   * @param endDate
   * @return {@link List}
   */
  List<TransactionEntity> findByTransactionDateBetween(LocalDate startDate, LocalDate endDate);


}

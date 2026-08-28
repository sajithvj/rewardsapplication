package com.charter.rewards.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.charter.rewards.dto.MonthlyReward;
import com.charter.rewards.exception.CustomerNotFoundException;
import com.charter.rewards.exception.DateRangeException;
import com.charter.rewards.model.TransactionEntity;
import com.charter.rewards.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class RewardServiceTest {

  @MockitoBean
  TransactionRepository transactionRepository;

  @Autowired
  RewardService rewardService;

  private static List<TransactionEntity> sampleTransaction() {
    return List.of(new TransactionEntity("T00014", "C005", "Nirmal Xavier", new BigDecimal("60.00"),
            LocalDate.of(2026, 7, 9)),
        new TransactionEntity("T00011", "C004", "David John", new BigDecimal("30.00"),
            LocalDate.of(2026, 6, 9)),
        new TransactionEntity("T00012", "C004", "David John", new BigDecimal("49.99"),
            LocalDate.of(2026, 6, 30)),
        new TransactionEntity("T0008", "C003", "Priya Sharma", new BigDecimal("310.00"),
            LocalDate.of(2026, 6, 11)),
        new TransactionEntity("T0009", "C003", "Priya Sharma", new BigDecimal("260.40"),
            LocalDate.of(2026, 6, 17)),
        new TransactionEntity("T00010", "C003", "Priya Sharma", new BigDecimal("180.00"),
            LocalDate.of(2026, 7, 29))

    );
  }

  @Test
  void exampleFromSpec_120DollarPurchase_earns90Points() {
    assertThat(rewardService.calculatePoints(new BigDecimal("120"))).isEqualTo(90);
  }

  @Test
  void purchaseExactly50Dollars_earnsZeroPoints() {
    // Boundary is exclusive: points start accruing strictly above $50.
    assertThat(rewardService.calculatePoints(new BigDecimal("50"))).isEqualTo(0);
  }

  @Test
  void purchaseBetween50And100_earnsOnePointPerDollarOverFifty() {
    assertThat(rewardService.calculatePoints(new BigDecimal("75"))).isEqualTo(25);
  }

  @Test
  void purchaseLittleGreater100Dollars_earns50Points() {
    assertThat(rewardService.calculatePoints(new BigDecimal("100.5"))).isEqualTo(50);
  }

  @Test
  void zeroOrNegativeAmount_earnsZeroPoints() {
    assertThat(rewardService.calculatePoints(BigDecimal.ZERO)).isEqualTo(0);
    assertThat(rewardService.calculatePoints(new BigDecimal("-10"))).isEqualTo(0);
  }

  @Test
  void nullAmount_earnsZeroPoints() {
    assertThat(rewardService.calculatePoints(null)).isEqualTo(0);
  }

  @Test
  void summaries_areReturnedForCustomerInRepository()
      throws ExecutionException, InterruptedException {
    LocalDate startDate = LocalDate.now().minusMonths(3);
    LocalDate endDate = LocalDate.now();
    when(transactionRepository.findByTransactionDateBetween(startDate, endDate)).thenReturn(
        sampleTransaction());
    var summaries = rewardService.getRewardSummaries(startDate.toString(), endDate.toString());
    assertThat(summaries).hasSize(3);
    assertThat(summaries).extracting("customerName")
        .containsExactlyInAnyOrder("Nirmal Xavier", "David John", "Priya Sharma");
  }

  @Test
  void customerWithHighestValueOfPoints() throws ExecutionException, InterruptedException {
    LocalDate startDate = LocalDate.now().minusMonths(3);
    LocalDate endDate = LocalDate.now();
    when(transactionRepository.findByTransactionDateBetween(startDate, endDate)).thenReturn(
        sampleTransaction());
    var summaries = rewardService.getRewardSummaries(startDate.toString(), endDate.toString());
    var cust = summaries.stream()
        .filter(s -> s.customerName().equals("Priya Sharma"))
        .findFirst()
        .orElseThrow();
    assertEquals("Priya Sharma", cust.customerName());
    assertEquals(1050, cust.totalPoints());
    assertEquals(840, cust.monthlyRewards().get(0).points());
    assertEquals("T0008", cust.monthlyRewards().get(0).transactionIds().get(0).transactionId());

  }

  @Test
  void customerWithOnlySmallPurchases_hasZeroTotalPoints()
      throws ExecutionException, InterruptedException {
    LocalDate startDate = LocalDate.now().minusMonths(3);
    LocalDate endDate = LocalDate.now();
    when(transactionRepository.findByTransactionDateBetween(startDate, endDate)).thenReturn(
        sampleTransaction());
    var summaries = rewardService.getRewardSummaries(startDate.toString(), endDate.toString());
    var david = summaries.stream()
        .filter(s -> s.customerName().equals("David John"))
        .findFirst()
        .orElseThrow();

    assertThat(david.totalPoints()).isZero();
    assertThat(david.monthlyRewards().get(0).points()).isZero();
  }

  @Test
  void totalPoints_equalsSumOfMonthlyPoints() throws ExecutionException, InterruptedException {
    LocalDate startDate = LocalDate.now().minusMonths(3);
    LocalDate endDate = LocalDate.now();
    when(transactionRepository.findByTransactionDateBetween(startDate, endDate)).thenReturn(
        sampleTransaction());
    var summaries = rewardService.getRewardSummaries(startDate.toString(), endDate.toString());
    summaries.forEach(summary -> {
      int sumOfMonths = summary.monthlyRewards().stream()
          .mapToInt(MonthlyReward::points)
          .sum();
      assertThat(summary.totalPoints()).isEqualTo(sumOfMonths);
    });

  }

  @Test
  void getRewardSummaries_throwsWhenStartDateAfterEndDate() {
    LocalDate startDate = LocalDate.now().minusMonths(4);
    LocalDate endDate = LocalDate.now();

    assertThrows(DateRangeException.class,
        () -> rewardService.getRewardSummaries(endDate.toString(), startDate.toString()));
  }

  @Test
  void getRewardSummaries_throwsWhenRangeExceedsAllowedWindow() {
    LocalDate startDate = LocalDate.now().minusMonths(4);
    LocalDate endDate = LocalDate.now();

    assertThrows(DateRangeException.class,
        () -> rewardService.getRewardSummaries(startDate.toString(), endDate.toString()));
  }

  @Test
  void getRewardSummaries_throwsWhenEndDateIsNull() {
    LocalDate startDate = LocalDate.now().minusMonths(4);

    assertThrows(DateRangeException.class,
        () -> rewardService.getRewardSummaries(startDate.toString(), null));
  }

  @Test
  void getRewardSummaries_throwsWhenStartDateIsNull() {
    LocalDate endDate = LocalDate.now();

    assertThrows(DateRangeException.class,
        () -> rewardService.getRewardSummaries(null, endDate.toString()));
  }

  @Test
  void getRewardSummaries_throwsWhenDateRangeIsInThePast() {
    LocalDate invalidStartDate = LocalDate.now().minusYears(1).minusMonths(4);
    LocalDate invalidEndDate = LocalDate.now().minusYears(1).minusMonths(2);

    assertThrows(DateRangeException.class,
        () -> rewardService.getRewardSummaries(invalidStartDate.toString(),
            invalidEndDate.toString()));
  }

  @Test
  void summaries_CustomerNotFoundExceptionForInvalidDateRange() {
    LocalDate startDate = LocalDate.now().minusMonths(6);
    LocalDate endDate = LocalDate.now().minusMonths(4);

    assertThrows(CustomerNotFoundException.class,
        () -> rewardService.getRewardSummaries(startDate.toString(), endDate.toString()));

  }
}

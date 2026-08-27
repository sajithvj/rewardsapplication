package com.charter.rewards.service;

import com.charter.rewards.dto.CustomerRewardSummary;
import com.charter.rewards.dto.MonthlyReward;
import com.charter.rewards.dto.MonthlyTransaction;
import com.charter.rewards.exception.CustomerNotFoundException;
import com.charter.rewards.model.Transaction;
import com.charter.rewards.repository.TransactionRepository;
import com.charter.rewards.validation.DateRange;
import com.charter.rewards.validation.DateRangeValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
public class RewardService {

  private static final Logger log = LoggerFactory.getLogger(RewardService.class);
  private static final BigDecimal TIER_2_THRESHOLD = new BigDecimal("100");
  private static final BigDecimal TIER_1_THRESHOLD = new BigDecimal("50");

  private final TransactionRepository transactionRepository;

  private final DateRangeValidator dateRangeValidator;

  public RewardService(TransactionRepository transactionRepository,
      DateRangeValidator dateRangeValidator) {
    this.transactionRepository = transactionRepository;
    this.dateRangeValidator = dateRangeValidator;
  }

  /**
   * Points earned on a single transaction: 2 points per dollar spent over $100, plus 1 point per
   * dollar spent between $50 and $100.
   * <p>
   * e.g. $120 -> 2x$20 (over 100) + 1x$50 (50-100 band) = 90 points. Fractional dollars are
   * truncated per-transaction (standard rewards practice), matching the worked example in the
   * spec.
   */
  public int calculatePoints(BigDecimal amount) {
    if (amount == null || amount.signum() <= 0) {
      return 0;
    }

    BigDecimal normalizedAmount = amount.setScale(0, RoundingMode.FLOOR);
    BigDecimal points = BigDecimal.ZERO;
    points = points.add(normalizedAmount.subtract(TIER_2_THRESHOLD).max(BigDecimal.ZERO)
        .multiply(BigDecimal.valueOf(2)));
    points = points.add(
        normalizedAmount.min(TIER_2_THRESHOLD).subtract(TIER_1_THRESHOLD).max(BigDecimal.ZERO));
    return points.intValue();
  }

  /**
   * Builds a per-customer summary of reward points, broken down by calendar month, plus the total
   * across all months on record.
   */

  public List<CustomerRewardSummary> getRewardSummaries(String startDateStr, String endDateStr) {

    DateRange dateRange = dateRangeValidator.validateDateRange(startDateStr, endDateStr);

    Map<String, List<Transaction>> byCustomer = transactionRepository.findByTransactionDateBetween(
            dateRange.startDate(), dateRange.endDate()).stream().map(Transaction::fromEntity)
        .collect(Collectors.groupingBy(Transaction::customerId));
    List<CustomerRewardSummary> summaries = byCustomer.entrySet().stream()
        .map(entry -> buildSummary(entry.getKey(), entry.getValue()))
        .sorted(Comparator.comparing(CustomerRewardSummary::customerName))
        .toList();
    if (summaries.isEmpty()) {
      log.warn("No transactions found between {} and {}", dateRange.startDate(),
          dateRange.endDate());
      throw new CustomerNotFoundException(dateRange.startDate(), dateRange.endDate());
    }
    return summaries;
  }


  private CustomerRewardSummary buildSummary(String customerId,
      List<Transaction> customerTransactions) {
    String customerName = customerTransactions.stream().findFirst()
        .orElseThrow(NullPointerException::new).customerName();

    // TreeMap keeps months in chronological order in the response.
    Map<YearMonth, Integer> pointsByMonth = new TreeMap<>();
    Map<YearMonth, List<MonthlyTransaction>> transactionByMonth = new TreeMap<>();

    customerTransactions.forEach(t -> {
      YearMonth month = YearMonth.from(t.transactionDate());
      int points = calculatePoints(t.amount());
      pointsByMonth.merge(month, points, Integer::sum);
      MonthlyTransaction monthlyTransaction = new MonthlyTransaction(t.transactionId(), t.amount());
      transactionByMonth.computeIfAbsent(month, k -> new ArrayList<>()).add(monthlyTransaction);
    });

    List<MonthlyReward> monthlyRewards = pointsByMonth.entrySet().stream()
        .map(
            e -> new MonthlyReward(e.getKey().getYear(), e.getKey().getMonth().name(), e.getValue(),
                transactionByMonth.get(e.getKey())))
        .toList();

    int totalPoints = monthlyRewards.stream().mapToInt(MonthlyReward::points).sum();

    return new CustomerRewardSummary(customerId, customerName, monthlyRewards, totalPoints);
  }


}

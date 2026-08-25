package com.charter.rewards.controller;

import com.charter.rewards.dto.CustomerRewardSummary;
import com.charter.rewards.dto.MonthlyReward;
import com.charter.rewards.dto.MonthlyTransaction;
import com.charter.rewards.exception.CustomerNotFoundException;
import com.charter.rewards.exception.DateRangeException;
import com.charter.rewards.exception.InvalidDateFormatException;
import com.charter.rewards.service.RewardService;
import com.charter.rewards.validation.DateRange;
import com.charter.rewards.validation.DateRangeValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RewardController.class)
class RewardControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RewardService rewardService;

    @Test
    void getRewards_routesToServiceAndSerializesSuccessResult() throws Exception {
        CustomerRewardSummary alice = sampleSummary();
        given(rewardService.getRewardSummaries(isNull(), isNull()))
                .willReturn(List.of(alice));
        mockMvc.perform(get("/v1/calculateRewards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].customerName").value("Alice Nguyen"))
                .andExpect(jsonPath("$[0].totalPoints").value(115))
                .andExpect(jsonPath("$[0].monthlyRewards").isArray())
                .andExpect(jsonPath("$[0].monthlyRewards[0].points").value(115))
                .andExpect(jsonPath("$[0].monthlyRewards[0].transactionIds").isArray())
                .andExpect(jsonPath("$[0].monthlyRewards[0].transactionIds[0].transactionId").value(110));

    }

    @Test
    void exception_invalidStartDateThrowsException() throws Exception {
        given(rewardService.getRewardSummaries(eq("2026-06-09"), isNull()))
                .willThrow(new DateRangeException("Both start date and end date must be provided together or both must be null."));
        mockMvc.perform(get("/v1/calculateRewards?startDate=2026-06-09"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").value("Both start date and end date must be provided together or both must be null."))
                .andExpect(jsonPath("$.statusCode").exists());


    }

    @Test
    void exception_withProvidedWithProvidedEndDateOnly() throws Exception {
        given(rewardService.getRewardSummaries(isNull(), eq("2026-06-09")))
                .willThrow(new DateRangeException("Both start date and end date must be provided together or both must be null."));

        mockMvc.perform(get("/v1/calculateRewards?endDate=2026-06-09"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").exists())
                .andExpect(jsonPath("$.details").value("Both start date and end date must be provided together or both must be null."))
                .andExpect(jsonPath("$.statusCode").exists());

    }

    @Test
    void getRewards_withValidDateRange() throws Exception {
        CustomerRewardSummary alice = sampleSummary();
        given(rewardService.getRewardSummaries(eq("2026-06-09"), eq("2026-08-09")))
                .willReturn(List.of(alice));
        mockMvc.perform(get("/v1/calculateRewards?startDate=2026-06-09&endDate=2026-08-09"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].customerName").value("Alice Nguyen"))
                .andExpect(jsonPath("$[0].totalPoints").value(115))
                .andExpect(jsonPath("$[0].monthlyRewards").isArray())
                .andExpect(jsonPath("$[0].monthlyRewards[0].points").value(115))
                .andExpect(jsonPath("$[0].monthlyRewards[0].transactionIds").isArray())
                .andExpect(jsonPath("$[0].monthlyRewards[0].transactionIds[0].transactionId").value(110));


    }

    @Test
    void exception_invalidDateFormatThrowsException() throws Exception {
        given(rewardService.getRewardSummaries(eq("09-08-2026"), eq("09-09-2026")))
                .willThrow(new InvalidDateFormatException("startDate", "09-08-2026"));
        mockMvc.perform(get("/v1/calculateRewards?startDate=09-08-2026&endDate=09-09-2026"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").value("Invalid value for parameter 'startDate': '09-08-2026' - expected format yyyy-MM-dd"))
                .andExpect(jsonPath("$.statusCode").exists());

    }

    @Test
    void exception_withStartDateGreaterThanEndDate() throws Exception {
        given(rewardService.getRewardSummaries(eq("2026-10-09"), eq("2026-08-08")))
                .willThrow(new DateRangeException("Start date must be before or equal to end date."));
        mockMvc.perform(get("/v1/calculateRewards?startDate=2026-10-09&endDate=2026-08-08"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").value("Start date must be before or equal to end date."))
                .andExpect(jsonPath("$.statusCode").exists());

    }

    @Test
    void exception_withStartDateGreaterThanOneYear() throws Exception {
        given(rewardService.getRewardSummaries(eq("2024-12-09"), eq("2026-08-08")))
                .willThrow(new DateRangeException("Start date cannot be more than one year in the past."));
        mockMvc.perform(get("/v1/calculateRewards?startDate=2024-12-09&endDate=2026-08-08"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").value("Start date cannot be more than one year in the past."))
                .andExpect(jsonPath("$.statusCode").exists());
    }

    @Test
    void exception_withStartDateGreaterThanThreeMonths() throws Exception {
        given(rewardService.getRewardSummaries(eq("2025-12-09"), eq("2026-08-08")))
                .willThrow(new DateRangeException("Date range cannot exceed three months."));
        mockMvc.perform(get("/v1/calculateRewards?startDate=2025-12-09&endDate=2026-08-08"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").value("Date range cannot exceed three months."))
                .andExpect(jsonPath("$.statusCode").exists());
    }

    @Test
    void exception_customerNotFound() throws Exception {
        DateRangeValidator dateRangeValidator = new DateRangeValidator();
        DateRange dateRange = dateRangeValidator.validateDateRange("2026-01-09", "2026-02-08");
        when(rewardService.getRewardSummaries(eq("2026-01-09"), eq("2026-02-08"))).thenThrow(new CustomerNotFoundException(dateRange.startDate(), dateRange.endDate()));
        mockMvc.perform(get("/v1/calculateRewards?startDate=2026-01-09&endDate=2026-02-08"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.details").value("No transactions found for the given date range: 2026-01-09 to 2026-02-08"))
                .andExpect(jsonPath("$.statusCode").exists());
    }


    private static CustomerRewardSummary sampleSummary() {
        MonthlyReward august = new MonthlyReward(2026, "AUG", 115, List.of(new MonthlyTransaction("110", new BigDecimal(100))));
        return new CustomerRewardSummary("C001", "Alice Nguyen", List.of(august), 115);
    }

}

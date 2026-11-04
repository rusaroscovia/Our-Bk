package com.bk.web;

import com.bk.model.Product;
import com.bk.model.ProductName;
import com.bk.model.Vote;
import com.bk.security.SecurityConfig;
import com.bk.service.ExpenseService;
import com.bk.service.FinancialService;
import com.bk.service.FinancialSummary;
import com.bk.service.PaymentService;
import com.bk.service.ProductService;
import com.bk.service.SettingsService;
import com.bk.service.VoteCount;
import com.bk.service.VotingResult;
import com.bk.service.VotingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = PublicController.class, properties = {
        "admin.username=test-admin",
        "admin.password=test-password"
})
@Import(SecurityConfig.class)
class PublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private VotingService votingService;

    @MockBean
    private SettingsService settingsService;

    @MockBean
    private FinancialService financialService;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private ExpenseService expenseService;

    @BeforeEach
    void setUp() {
        LocalDate today = LocalDate.now();
        when(settingsService.areReportsPublished()).thenReturn(false);
        when(productService.getProducts()).thenReturn(Arrays.stream(ProductName.values())
                .map(name -> new Product(name, BigDecimal.valueOf(200)))
                .toList());
        when(votingService.isVotingOpen()).thenReturn(false);
        when(votingService.getResult(today)).thenReturn(new VotingResult(
                today,
                Arrays.stream(ProductName.values())
                        .map(name -> new VoteCount(name, name.getDisplayName(), 0))
                        .toList(),
                List.of(),
                false
        ));
    }

    @Test
    void publicVotingPageIsAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/vote"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("What should we have?")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Name-based voting cannot verify identity")));

        verifyNoInteractions(financialService, paymentService, expenseService);
    }

    @Test
    void publishedFinancialRecordsAreShownToEmployees() throws Exception {
        when(settingsService.areReportsPublished()).thenReturn(true);
        when(financialService.getSummary(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new FinancialSummary(
                        BigDecimal.valueOf(12000),
                        BigDecimal.valueOf(3000),
                        BigDecimal.valueOf(9000),
                        3
                ));
        when(paymentService.getPayments(any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of());
        when(expenseService.getExpenses(any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of());

        mockMvc.perform(get("/vote"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("This month's financial report")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("12,000 RWF")));
    }

    @Test
    void publicVoteIsAcceptedAndRedirectsBackToVotingPage() throws Exception {
        when(votingService.isVotingOpen()).thenReturn(true);
        when(votingService.castVote("Alex", ProductName.BREAD, LocalDate.now()))
                .thenReturn(new Vote("Alex", new Product(ProductName.BREAD, BigDecimal.valueOf(1700)), LocalDate.now()));

        mockMvc.perform(post("/vote")
                        .with(csrf())
                        .param("voterName", "Alex")
                        .param("productName", "BREAD"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vote"));

        verify(votingService).castVote("Alex", ProductName.BREAD, LocalDate.now());
    }
}

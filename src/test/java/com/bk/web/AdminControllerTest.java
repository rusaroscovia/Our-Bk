package com.bk.web;

import com.bk.model.Product;
import com.bk.model.ProductName;
import com.bk.security.SecurityConfig;
import com.bk.service.EmployeeService;
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
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = AdminController.class, properties = {
        "admin.username=test-admin",
        "admin.password=test-password"
})
@Import(SecurityConfig.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private ExpenseService expenseService;

    @MockBean
    private ProductService productService;

    @MockBean
    private FinancialService financialService;

    @MockBean
    private SettingsService settingsService;

    @MockBean
    private VotingService votingService;

    @BeforeEach
    void setUp() {
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);
        when(financialService.getSummary(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new FinancialSummary(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0));
        when(votingService.getResult(today)).thenReturn(new VotingResult(
                today,
                Arrays.stream(ProductName.values())
                        .map(name -> new VoteCount(name, name.getDisplayName(), 0))
                        .toList(),
                List.of(),
                false
        ));
        when(votingService.isVotingOpen()).thenReturn(false);
        when(settingsService.areReportsPublished()).thenReturn(false);
        when(employeeService.getEmployees()).thenReturn(List.of());
        when(productService.getProducts()).thenReturn(Arrays.stream(ProductName.values())
                .map(name -> new Product(name, BigDecimal.valueOf(200)))
                .toList());
        when(paymentService.getPayments(currentMonth.atDay(1), currentMonth.atEndOfMonth()))
                .thenReturn(List.of());
        when(expenseService.getExpenses(currentMonth.atDay(1), currentMonth.atEndOfMonth()))
                .thenReturn(List.of());
    }

    @Test
    void loginPageIsAvailableWithoutAdminAuthentication() throws Exception {
        mockMvc.perform(get("/admin/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Sign in")));
    }

    @Test
    void adminDashboardRendersForAnAuthenticatedAdministrator() throws Exception {
        mockMvc.perform(get("/admin/dashboard").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Breakfast overview")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Record a payment")));
    }
}

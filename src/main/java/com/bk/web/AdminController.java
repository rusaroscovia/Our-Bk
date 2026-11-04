package com.bk.web;

import com.bk.model.ProductName;
import com.bk.service.EmployeeService;
import com.bk.service.ExpenseService;
import com.bk.service.FinancialService;
import com.bk.service.PaymentService;
import com.bk.service.ProductService;
import com.bk.service.ServiceException;
import com.bk.service.SettingsService;
import com.bk.service.VotingResult;
import com.bk.service.VotingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;

@Controller
public class AdminController {

    private final EmployeeService employeeService;
    private final PaymentService paymentService;
    private final ExpenseService expenseService;
    private final ProductService productService;
    private final FinancialService financialService;
    private final SettingsService settingsService;
    private final VotingService votingService;

    public AdminController(
            EmployeeService employeeService,
            PaymentService paymentService,
            ExpenseService expenseService,
            ProductService productService,
            FinancialService financialService,
            SettingsService settingsService,
            VotingService votingService
    ) {
        this.employeeService = employeeService;
        this.paymentService = paymentService;
        this.expenseService = expenseService;
        this.productService = productService;
        this.financialService = financialService;
        this.settingsService = settingsService;
        this.votingService = votingService;
    }

    @GetMapping("/admin/login")
    public String login() {
        return "admin/login";
    }

    @GetMapping("/admin")
    public String adminHome() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);
        model.addAttribute("today", today);
        model.addAttribute("month", currentMonth.toString());
        model.addAttribute("summary", financialService.getSummary(
                currentMonth.atDay(1), currentMonth.atEndOfMonth()));
        model.addAttribute("todayResult", votingService.getResult(today));
        model.addAttribute("votingOpen", votingService.isVotingOpen());
        model.addAttribute("reportsPublished", settingsService.areReportsPublished());
        model.addAttribute("employees", employeeService.getEmployees());
        model.addAttribute("products", productService.getProducts());
        model.addAttribute("productNames", Arrays.asList(ProductName.values()));
        model.addAttribute("payments", paymentService.getPayments(
                currentMonth.atDay(1), currentMonth.atEndOfMonth()));
        model.addAttribute("expenses", expenseService.getExpenses(
                currentMonth.atDay(1), currentMonth.atEndOfMonth()));
        return "admin/dashboard";
    }

    @GetMapping("/admin/records")
    public String records(@RequestParam(required = false) String month, Model model) {
        YearMonth selectedMonth;
        try {
            selectedMonth = month == null || month.isBlank() ? YearMonth.now() : YearMonth.parse(month);
        } catch (DateTimeException exception) {
            selectedMonth = YearMonth.now();
            model.addAttribute("error", "Enter a month in YYYY-MM format.");
        }
        model.addAttribute("month", selectedMonth.toString());
        model.addAttribute("summary", financialService.getSummary(
                selectedMonth.atDay(1), selectedMonth.atEndOfMonth()));
        model.addAttribute("payments", paymentService.getPayments(
                selectedMonth.atDay(1), selectedMonth.atEndOfMonth()));
        model.addAttribute("expenses", expenseService.getExpenses(
                selectedMonth.atDay(1), selectedMonth.atEndOfMonth()));
        return "admin/records";
    }

    @PostMapping("/admin/employees")
    public String registerEmployee(@RequestParam String name, RedirectAttributes redirectAttributes) {
        return submit(redirectAttributes, () -> employeeService.register(name), "Employee registered.");
    }

    @PostMapping("/admin/employees/{id}/rename")
    public String renameEmployee(
            @PathVariable long id,
            @RequestParam String name,
            RedirectAttributes redirectAttributes
    ) {
        return submit(redirectAttributes, () -> employeeService.rename(id, name), "Employee name updated.");
    }

    @PostMapping("/admin/payments")
    public String recordPayment(
            @RequestParam long employeeId,
            @RequestParam BigDecimal amountRwf,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate paymentDate,
            RedirectAttributes redirectAttributes
    ) {
        return submit(redirectAttributes,
                () -> paymentService.record(employeeId, amountRwf, paymentDate), "Payment recorded.");
    }

    @PostMapping("/admin/expenses")
    public String recordExpense(
            @RequestParam String description,
            @RequestParam BigDecimal amountRwf,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expenseDate,
            RedirectAttributes redirectAttributes
    ) {
        return submit(redirectAttributes,
                () -> expenseService.record(description, amountRwf, expenseDate), "Expense recorded.");
    }

    @PostMapping("/admin/products/{name}/price")
    public String updateProductPrice(
            @PathVariable ProductName name,
            @RequestParam BigDecimal priceRwf,
            RedirectAttributes redirectAttributes
    ) {
        return submit(redirectAttributes,
                () -> productService.updatePrice(name, priceRwf), "Product price updated.");
    }

    @PostMapping("/admin/reports/publish")
    public String publishReports(
            @RequestParam boolean published,
            RedirectAttributes redirectAttributes
    ) {
        settingsService.publishReports(published);
        redirectAttributes.addFlashAttribute(
                "success", published ? "Financial reports are now public." : "Financial reports are hidden.");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/admin/voting/open")
    public String openVoting(RedirectAttributes redirectAttributes) {
        votingService.openVoting();
        redirectAttributes.addFlashAttribute("success", "Voting is open.");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/admin/voting/close")
    public String closeVoting(RedirectAttributes redirectAttributes) {
        try {
            VotingResult result = votingService.closeVoting(LocalDate.now());
            redirectAttributes.addFlashAttribute("success", winnerMessage(result));
        } catch (IllegalArgumentException | ServiceException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    private String submit(RedirectAttributes redirectAttributes, Runnable action, String successMessage) {
        try {
            action.run();
            redirectAttributes.addFlashAttribute("success", successMessage);
        } catch (IllegalArgumentException | ServiceException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    private String winnerMessage(VotingResult result) {
        if (result.winners().isEmpty()) {
            return "Voting closed. No votes were recorded today.";
        }
        if (result.tied()) {
            return "Voting closed. Today's result is a tie between " + String.join(", ", result.winners()) + ".";
        }
        return "Voting closed. Today's winning breakfast is " + result.winners().getFirst() + ".";
    }
}

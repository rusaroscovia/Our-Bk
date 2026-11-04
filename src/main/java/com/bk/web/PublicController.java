package com.bk.web;

import com.bk.model.ProductName;
import com.bk.service.ExpenseService;
import com.bk.service.FinancialService;
import com.bk.service.PaymentService;
import com.bk.service.ProductService;
import com.bk.service.ServiceException;
import com.bk.service.SettingsService;
import com.bk.service.VotingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Controller
public class PublicController {

    private final ProductService productService;
    private final VotingService votingService;
    private final SettingsService settingsService;
    private final FinancialService financialService;
    private final PaymentService paymentService;
    private final ExpenseService expenseService;

    public PublicController(
            ProductService productService,
            VotingService votingService,
            SettingsService settingsService,
            FinancialService financialService,
            PaymentService paymentService,
            ExpenseService expenseService
    ) {
        this.productService = productService;
        this.votingService = votingService;
        this.settingsService = settingsService;
        this.financialService = financialService;
        this.paymentService = paymentService;
        this.expenseService = expenseService;
    }

    @GetMapping("/vote")
    public String votePage(Model model) {
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);
        boolean reportsPublished = settingsService.areReportsPublished();

        model.addAttribute("today", today);
        model.addAttribute("products", productService.getProducts());
        model.addAttribute("productNames", List.of(ProductName.values()));
        model.addAttribute("votingOpen", votingService.isVotingOpen());
        model.addAttribute("todayResult", votingService.getResult(today));
        model.addAttribute("reportsPublished", reportsPublished);
        if (reportsPublished) {
            model.addAttribute("summary", financialService.getSummary(
                    currentMonth.atDay(1), currentMonth.atEndOfMonth()));
            model.addAttribute("payments", paymentService.getPayments(
                    currentMonth.atDay(1), currentMonth.atEndOfMonth()));
            model.addAttribute("expenses", expenseService.getExpenses(
                    currentMonth.atDay(1), currentMonth.atEndOfMonth()));
        }
        return "public/vote";
    }

    @PostMapping("/vote")
    public String castVote(
            @RequestParam String voterName,
            @RequestParam ProductName productName,
            RedirectAttributes redirectAttributes
    ) {
        try {
            votingService.castVote(voterName, productName, LocalDate.now());
            redirectAttributes.addFlashAttribute("success", "Your vote has been recorded.");
        } catch (IllegalArgumentException | ServiceException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/vote";
    }
}

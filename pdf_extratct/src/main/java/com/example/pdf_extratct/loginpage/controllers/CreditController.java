package com.example.pdf_extratct.loginpage.controllers;


import com.example.pdf_extratct.loginpage.credittransaction.CreditService;
import com.example.pdf_extratct.loginpage.credittransaction.Dtos.AddCreditsRequest;
import com.example.pdf_extratct.loginpage.credittransaction.Dtos.CreditBalanceResponse;
import com.example.pdf_extratct.loginpage.credittransaction.Dtos.TransactionResponse;
import com.example.pdf_extratct.loginpage.user.UserEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/credits")
@RequiredArgsConstructor
public class CreditController {

    private final CreditService creditService;

    @GetMapping("/balance")
    public ResponseEntity<CreditBalanceResponse> getBalance(
            @AuthenticationPrincipal UserEntity user) {
        return ResponseEntity.ok(creditService.getBalance(user));
    }

    @PostMapping("/add")
    public ResponseEntity<CreditBalanceResponse> addCredits(
            @AuthenticationPrincipal UserEntity user,
            @Valid @RequestBody AddCreditsRequest request) {
        return ResponseEntity.ok(creditService.addCredits(user, request));
    }

    @GetMapping("/history")
    public ResponseEntity<Page<TransactionResponse>> getTransactionHistory(
            @AuthenticationPrincipal UserEntity user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(creditService.getTransactionHistory(user, pageable));
    }
}
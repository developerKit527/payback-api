package com.payback.api.config;

import com.payback.api.service.MerchantService;
import com.payback.api.service.TransactionService;
import com.payback.api.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SeedDataLoader implements CommandLineRunner {

    private final MerchantService merchantService;
    private final WalletService walletService;
    private final TransactionService transactionService;

    @Override
    public void run(String... args) throws Exception {
        // Initialize seed data in order: merchants first, then wallet, then transactions
        merchantService.initializeSeedData();
        walletService.initializeSeedData();
        transactionService.initializeSeedData();
    }
}

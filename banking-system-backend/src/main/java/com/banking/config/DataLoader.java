package com.banking.config;

import com.banking.model.Account;
import com.banking.model.User;
import com.banking.repository.AccountRepository;
import com.banking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            System.out.println(">> Sample data already exists. Skipping DataLoader.");
            return;
        }

        System.out.println(">> Loading sample data...");

        // ---- User 1: Mary ----
        User mary = new User();
        mary.setName("Mary Anger");
        mary.setEmail("mary@example.com");
        mary.setPassword(passwordEncoder.encode("123456"));
        mary.setRole("USER");
        mary = userRepository.save(mary);

        Account maryAccount = new Account();
        maryAccount.setUser(mary);
        maryAccount.setBalance(new BigDecimal("5000.00"));
        accountRepository.save(maryAccount);

        // ---- User 2: John ----
        User john = new User();
        john.setName("John Doe");
        john.setEmail("john@example.com");
        john.setPassword(passwordEncoder.encode("123456"));
        john.setRole("USER");
        john = userRepository.save(john);

        Account johnAccount = new Account();
        johnAccount.setUser(john);
        johnAccount.setBalance(new BigDecimal("3000.00"));
        accountRepository.save(johnAccount);

        // ---- User 3: Admin ----
        User admin = new User();
        admin.setName("Admin User");
        admin.setEmail("admin@example.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole("ADMIN");
        admin = userRepository.save(admin);

        Account adminAccount = new Account();
        adminAccount.setUser(admin);
        adminAccount.setBalance(new BigDecimal("10000.00"));
        accountRepository.save(adminAccount);

        System.out.println(">> Sample data loaded successfully!");
        System.out.println("   - Mary  (mary@example.com  / 123456)   Balance: 5000.00");
        System.out.println("   - John  (john@example.com  / 123456)   Balance: 3000.00");
        System.out.println("   - Admin (admin@example.com / admin123)  Balance: 10000.00");
    }
}

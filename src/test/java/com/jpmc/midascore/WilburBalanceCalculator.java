package com.jpmc.midascore;

import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

// @SpringBootApplication
public class WilburBalanceCalculator {

    public static void main(String[] args) {
        SpringApplication.run(WilburBalanceCalculator.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public CommandLineRunner runner() {
        return args -> {
            // Initial balances
            float[] balances = new float[11]; // 1-indexed for user IDs
            balances[1] = 1200.23f;  // bernie
            balances[2] = 2215.37f;  // grommit
            balances[3] = 2774.14f;  // maria
            balances[4] = 12.34f;    // mario
            balances[5] = 444.55f;   // waldorf
            balances[6] = 888.90f;   // whosit
            balances[7] = 777.60f;   // whatsit
            balances[8] = 68.70f;    // howsit
            balances[9] = 3476.21f;  // wilbur
            balances[10] = 2121.54f; // antonio

            System.out.println("Initial wilbur balance: " + balances[9]);

            // Transactions
            String[] transactions = {
                "9, 10, 16",
                "4, 2, 166.75",
                "9, 5, 8",
                "6, 7, 63.55",
                "2, 6, 99.56",
                "8, 3, 108.1",
                "5, 1, 49.56",
                "8, 10, 33.39",
                "10, 8, 133.65",
                "3, 10, 105.96",
                "10, 5, 154.10",
                "5, 6, 75.67",
                "1, 5, 1.98",
                "6, 7, 112.43",
                "9, 1, 130.37",
                "7, 10, 197.5",
                "1, 7, 6.83",
                "9, 7, 128.47",
                "5, 6, 47.40",
                "9, 6, 103.95",
                "6, 5, 20.58",
                "8, 3, 168.57"
            };

            RestTemplate restTemplate = new RestTemplate();
            String incentiveApiUrl = "http://localhost:8080/incentive";

            for (String transactionLine : transactions) {
                String[] parts = transactionLine.split(", ");
                long senderId = Long.parseLong(parts[0]);
                long recipientId = Long.parseLong(parts[1]);
                float amount = Float.parseFloat(parts[2]);

                // Check if sender has sufficient balance
                if (balances[(int)senderId] >= amount) {
                    // Call incentive API
                    Transaction transaction = new Transaction(senderId, recipientId, amount);
                    float incentive = 0;
                    
                    try {
                        Incentive response = restTemplate.postForObject(incentiveApiUrl, transaction, Incentive.class);
                        if (response != null) {
                            incentive = response.getAmount();
                        }
                    } catch (Exception e) {
                        System.err.println("Error calling incentive API: " + e.getMessage());
                    }

                    // Update balances
                    balances[(int)senderId] -= amount;
                    balances[(int)recipientId] += amount + incentive;

                    System.out.printf("Transaction: %d->%d, amount=%.2f, incentive=%.2f, wilbur balance=%.2f%n", 
                        senderId, recipientId, amount, incentive, balances[9]);
                } else {
                    System.out.printf("Transaction failed: %d->%d, amount=%.2f (insufficient balance)%n", 
                        senderId, recipientId, amount);
                }
            }

            System.out.println("Final wilbur balance: " + balances[9]);
            System.out.println("Rounded down: " + (int)balances[9]);
        };
    }
}

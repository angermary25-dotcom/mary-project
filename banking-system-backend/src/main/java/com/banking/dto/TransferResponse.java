package com.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {

    private boolean success;
    private String message;
    private Long transactionId;
    private BigDecimal senderNewBalance;
    private BigDecimal receiverNewBalance;
}

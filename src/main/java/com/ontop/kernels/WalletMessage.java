package com.ontop.kernels;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletMessage implements ParentMessage {
    private Long clientId;
    private BigDecimal amount;
    private String transactionId;
}

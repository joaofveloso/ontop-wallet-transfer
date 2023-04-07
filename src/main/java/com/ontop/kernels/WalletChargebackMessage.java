package com.ontop.kernels;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletChargebackMessage implements ParentMessage {

    private String transactionId;
}

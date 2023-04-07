package com.ontop.kernels;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChargebackMessage implements ParentMessage {

    private String transactionId;
}

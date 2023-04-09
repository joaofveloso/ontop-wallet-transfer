package com.ontop.balance.infrastructure.entities;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document("recipients")
@AllArgsConstructor
public class RecipientEntity {

    @Id
    private String id;
    private Long clientId;
    private String name;
    private String routingNumber;
    private String nationalIdentification;
    private String accountNumber;
}

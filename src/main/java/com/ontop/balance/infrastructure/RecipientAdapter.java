package com.ontop.balance.infrastructure;

import com.ontop.balance.core.model.PaginatedWrapper;
import com.ontop.balance.core.model.PaginatedWrapper.PaginatedData;
import com.ontop.balance.core.model.RecipientData;
import com.ontop.balance.core.model.commands.CreateRecipientCommand;
import com.ontop.balance.core.model.queries.ObtainRecipientByClientQuery;
import com.ontop.balance.core.model.queries.ObtainRecipientByIdQuery;
import com.ontop.balance.core.ports.outbound.Recipient;
import com.ontop.balance.infrastructure.entity.RecipientEntity;
import com.ontop.balance.infrastructure.repositories.RecipientRepository;

import java.math.BigDecimal;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RecipientAdapter implements Recipient {

    @Value("${core.ontop.fee}")
    private BigDecimal fee;

    private final RecipientRepository recipientRepository;

    @Override
    @Transactional
    public void save(CreateRecipientCommand command, String uuid) {
        RecipientEntity recipientEntity = new RecipientEntity(uuid, command.clientId(), command.name(), command.routingNumber(), command.nationalIdentification(), command.accountNumber());
        this.recipientRepository.save(recipientEntity);
    }

    @Override
    public PaginatedWrapper<RecipientData> findRecipients(ObtainRecipientByClientQuery query) {
        Pageable pageable = PageRequest.of(query.page(), query.size());
        Page<RecipientEntity> paginatedData = this.recipientRepository.findAllByClientId(query.clientId(), pageable);
        List<RecipientData> recipientData = paginatedData.stream().map(recipient -> new RecipientData(recipient.getId(), recipient.getClientId(), recipient.getName(), recipient.getRoutingNumber(), recipient.getNationalIdentification(), recipient.getAccountNumber(), fee)).toList();
        return new PaginatedWrapper<>(recipientData, new PaginatedData(query.page(), paginatedData.getSize(), paginatedData.getTotalPages()));
    }

    @Override
    @Transactional
    public Optional<RecipientData> findRecipientById(ObtainRecipientByIdQuery query) {
        return this.recipientRepository.findById(query.id()).map(recipient -> new RecipientData(recipient.getId(), recipient.getClientId(), recipient.getName(), recipient.getRoutingNumber(), recipient.getNationalIdentification(), recipient.getAccountNumber(), fee));
    }
}

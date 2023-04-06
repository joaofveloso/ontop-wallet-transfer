package com.ontop.balance.app.controllers;

import static com.ontop.balance.app.mappers.ApplicationMapper.toRecipientResponse;

import com.ontop.balance.app.models.CreateRecipientAccountRequest;
import com.ontop.balance.app.models.RecipientItemWrapper;
import com.ontop.balance.app.models.RecipientResponse;
import com.ontop.balance.app.models.RecipientResponseWrapper;
import com.ontop.balance.core.model.commands.CreateRecipientCommand;
import com.ontop.balance.core.model.queries.ObtainRecipientByClientQuery;
import com.ontop.balance.core.ports.inbound.CreateRecipient;
import com.ontop.balance.core.ports.inbound.ObtainRecipientByClient;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RecipientController implements RecipientControllerDoc {

    private final CreateRecipient createRecipient;
    private final ObtainRecipientByClient obtainRecipientByClient;

    @Override
    public ResponseEntity<Void> createRecipient(Long clientId, CreateRecipientAccountRequest request) {
        String key = this.createRecipient.handler(
                new CreateRecipientCommand(clientId, request.name() + request.surname(),
                        request.routingNumber(), request.identificationNumber(),
                        request.accountNumber()));
        HttpHeaders headers = getHttpHeadersWithLocation(key);
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    private static HttpHeaders getHttpHeadersWithLocation(String key) {
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(key)
                .toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return headers;
    }

    @Override
    public ResponseEntity<RecipientResponseWrapper> obtainRecipients(
            Long clientId, int page, int size) {
        RecipientResponse recipientResponse = toRecipientResponse(
                obtainRecipientByClient.handler(new ObtainRecipientByClientQuery(clientId)));
        RecipientResponseWrapper recipientResponseWrapper = new RecipientResponseWrapper(
                recipientResponse);
        return ResponseEntity.ok(recipientResponseWrapper);
    }

    @Override
    public ResponseEntity<RecipientItemWrapper> obtainRecipientById(Long clientId, String id) {
        return null;
    }
}

package com.ontop.balance.app.controllers;

import static com.ontop.balance.app.mappers.ApplicationMapper.toRecipientResponse;

import com.ontop.balance.app.LocationUtils;
import com.ontop.balance.app.models.CreateRecipientAccountRequest;
import com.ontop.balance.app.models.RecipientItemWrapper;
import com.ontop.balance.app.models.RecipientResponse;
import com.ontop.balance.app.models.RecipientResponse.RecipientResponseItem;
import com.ontop.balance.app.models.RecipientResponseWrapper;
import com.ontop.balance.core.model.RecipientData;
import com.ontop.balance.core.model.commands.CreateRecipientCommand;
import com.ontop.balance.core.model.queries.ObtainRecipientByClientQuery;
import com.ontop.balance.core.model.queries.ObtainRecipientByIdQuery;
import com.ontop.balance.core.ports.inbound.CreateRecipient;
import com.ontop.balance.core.ports.inbound.ObtainRecipientByClient;
import com.ontop.balance.core.ports.inbound.ObtainRecipientById;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequiredArgsConstructor
public class RecipientController implements RecipientControllerDoc {

    private final CreateRecipient createRecipient;
    private final ObtainRecipientByClient obtainRecipientByClient;
    private final ObtainRecipientById obtainRecipientById;

    @Override
    public ResponseEntity<Void> createRecipient(Long clientId, CreateRecipientAccountRequest request) {
        String key = this.createRecipient.handler(
                new CreateRecipientCommand(clientId, request.name() + request.surname(),
                        request.routingNumber(), request.identificationNumber(),
                        request.accountNumber()));
        HttpHeaders headers = LocationUtils.getHttpHeadersWithLocation(key);
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<RecipientResponseWrapper> obtainRecipients(
            Long clientId, int page, int size) {
        RecipientResponse recipientResponse = toRecipientResponse(
                obtainRecipientByClient.handler(new ObtainRecipientByClientQuery(clientId, page, size)));
        RecipientResponseWrapper recipientResponseWrapper = new RecipientResponseWrapper(
                recipientResponse);
        return ResponseEntity.ok(recipientResponseWrapper);
    }

    @Override
    public ResponseEntity<RecipientItemWrapper> obtainRecipientById(Long clientId, String id) {
        RecipientData handler = this.obtainRecipientById.handler(new ObtainRecipientByIdQuery(id, clientId));
        RecipientItemWrapper recipientItemWrapper = new RecipientItemWrapper(
                new RecipientResponseItem(handler.id(), handler.name(),
                        handler.routingNumber(), handler.accountNumber()));
        return ResponseEntity.ok(recipientItemWrapper);
    }
}

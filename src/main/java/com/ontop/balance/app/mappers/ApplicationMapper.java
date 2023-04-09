package com.ontop.balance.app.mappers;

import com.ontop.balance.app.models.PaginationResponse;
import com.ontop.balance.app.models.RecipientResponse;
import com.ontop.balance.app.models.RecipientResponse.RecipientResponseItem;
import com.ontop.balance.core.model.PaginatedWrapper;
import com.ontop.balance.core.model.PaginatedWrapper.PaginatedData;
import com.ontop.balance.core.model.RecipientData;
import java.util.List;

public class ApplicationMapper {

    public static RecipientResponse toRecipientResponse(PaginatedWrapper<RecipientData> wrapper) {

        List<RecipientResponseItem> recipients = wrapper.data().stream()
                .map(ApplicationMapper::toRecipientItem).toList();
        PaginatedData pagination = wrapper.pagination();
        return new RecipientResponse(recipients,
                new PaginationResponse(pagination.page(), pagination.pageSize(),
                        pagination.totalPages()));
    }

    private static RecipientResponseItem toRecipientItem(RecipientData recipientData) {
        return new RecipientResponseItem(recipientData.id(), recipientData.name(),
                recipientData.routingNumber(), recipientData.accountNumber());
    }
}

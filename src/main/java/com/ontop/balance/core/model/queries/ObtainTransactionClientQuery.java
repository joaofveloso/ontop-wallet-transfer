package com.ontop.balance.core.model.queries;

import java.time.LocalDate;

public record ObtainTransactionClientQuery(Long clientId, LocalDate date, int page, int pageSize) {

}

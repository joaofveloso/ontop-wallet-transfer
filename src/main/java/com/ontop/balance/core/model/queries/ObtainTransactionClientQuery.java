package com.ontop.balance.core.model.queries;

import java.time.LocalDate;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * Query parameters for obtaining transactions by client ID.
 * <p>
 * Note: Validation annotations on this record provide documentation
 * but actual runtime validation occurs via @Validated on controller
 * methods with @RequestParam constraints.
 * </p>
 */
public record ObtainTransactionClientQuery(
        Long clientId,
        LocalDate date,
        @Min(value = 0, message = "Page must be 0 or greater") int page,
        @Min(value = 1, message = "Size must be at least 1") @Max(value = 100, message = "Size cannot exceed 100") int pageSize) {

}

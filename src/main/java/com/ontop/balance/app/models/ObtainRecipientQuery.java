package com.ontop.balance.app.models;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * Query record for obtaining recipients with pagination.
 * Includes validation to prevent excessive resource consumption.
 */
public record ObtainRecipientQuery(
        @Min(value = 0, message = "Page must be 0 or greater") int page,
        @Min(value = 1, message = "Size must be at least 1")
        @Max(value = 100, message = "Size cannot exceed 100") int size
) {
}

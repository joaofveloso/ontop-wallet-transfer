package com.ontop.balance.app.models;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(description = "Response object containing metadata")
public record MetadataResponse(
        @Schema(description = "A map of key-value pairs containing metadata") Map<String, String> properties) {

}

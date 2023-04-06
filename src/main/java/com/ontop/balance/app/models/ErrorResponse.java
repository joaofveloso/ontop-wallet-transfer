package com.ontop.balance.app.models;

import java.util.List;

public record ErrorResponse(String message, List<SubErrorResponse> subErrors) {
    public record SubErrorResponse(String key, String message){}
}

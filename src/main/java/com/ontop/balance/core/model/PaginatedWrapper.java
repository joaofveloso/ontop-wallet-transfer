package com.ontop.balance.core.model;

import java.util.List;

public record PaginatedWrapper<T>(List<T> data, PaginatedData pagination) {

    public record PaginatedData(int page, int pageSize, int totalPages){}
}

package org.mirza.util;

import org.mirza.dto.pagination.PaginationRequestDto;
import spark.Request;

import java.util.Optional;

public class PaginationUtil {
    public static PaginationRequestDto getPaginationRequestDto(Request req) {
        int page = Optional.ofNullable(req.queryParams("page")).map(Integer::parseInt).orElse(1);
        int size = Optional.ofNullable(req.queryParams("size")).map(Integer::parseInt).orElse(10);
        String orderBy = Optional.ofNullable(req.queryParams("orderBy")).map(String::trim).orElse("id");

        return new PaginationRequestDto(page, size, orderBy);
    }
}

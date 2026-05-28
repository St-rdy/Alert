package com.example.Alert.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaginationResponse {
    private int page;
    private int limit;
    private long total;
}

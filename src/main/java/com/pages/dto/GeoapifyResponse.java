package com.pages.dto;

import java.util.List;

public record GeoapifyResponse(
        List<GeoapifyResult> results
) {
}

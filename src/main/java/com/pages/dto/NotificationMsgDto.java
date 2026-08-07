package com.pages.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class NotificationMsgDto {
    private  Long id;
    private String message;
}


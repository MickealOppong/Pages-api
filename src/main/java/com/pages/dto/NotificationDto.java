package com.pages.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class NotificationDto {

    List<NotificationMsgDto> msg;
    private Map<String,Long> notif;
    private long totalCount;
}

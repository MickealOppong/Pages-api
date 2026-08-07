package com.pages.dto;

import java.time.Instant;
import java.time.LocalDateTime;

public class MatchDto {

    private Long matchId;
    private Long senderId;
    private Long receiverId;
    private String firstName;
    private String lastName;
    private String image;
    private String lastMessage;
    private Instant matchedDate;
}

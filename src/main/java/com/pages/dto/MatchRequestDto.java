package com.pages.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class MatchRequestDto {

    private Long matchId;
    private Long senderId;
    private Long receiverId;
    private String firstName;
    private String lastName;
    private LocalDate date_of_birth;
    private String image;
    private String lastMessage;
    private Instant lastMessageDate;
    private boolean isOnline;
    private Instant requestDate;
    private boolean hasReadMessage;
    private String activity;

}

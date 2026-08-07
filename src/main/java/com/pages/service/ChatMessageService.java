package com.pages.service;

import com.pages.dto.ChatMessageDto;
import com.pages.enums.NotificationStatus;
import com.pages.enums.NotificationType;
import com.pages.model.AppUser;
import com.pages.model.ChatMessage;
import com.pages.model.Match_request;
import com.pages.repository.ChatMessageRepo;
import com.pages.repository.Match_requestRepo;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;


@Slf4j
@Transactional
@Service
public class ChatMessageService {

    private final ChatMessageRepo chatMessageRepo;
    private final AppUserDetailsService appUserDetailsService;



    public ChatMessageService(ChatMessageRepo chatMessageRepo, AppUserDetailsService appUserDetailsService) {
        this.chatMessageRepo = chatMessageRepo;
        this.appUserDetailsService = appUserDetailsService;

    }

    public ChatMessage saveMessage(ChatMessageDto messageDto){

        ChatMessage chatMessage = ChatMessage.builder()
                .message(messageDto.getMessage())
                .sender(appUserDetailsService.getAppUserById(messageDto.getSenderId()))
                .matchId(messageDto.getMatchId())
                .receiver(appUserDetailsService.getAppUserById(messageDto.getReceiverId()))
                .build();
        appUserDetailsService.updateLastActive(chatMessage.getSender().getId());

      return   chatMessageRepo.save(chatMessage);

    }

    public List<ChatMessageDto> findByMatch(Long matchId){

        return chatMessageRepo.findByMatchIdOrderByCreatedAtAsc(matchId).stream()
                .map(chatMessage -> {

                return       ChatMessageDto.builder()
                            .message(chatMessage.getMessage())
                            .matchId(chatMessage.getMatchId())
                            .id(chatMessage.getId())
                            .receiverId(chatMessage.getReceiver().getId())
                            .senderId(chatMessage.getSender().getId())
                            .createdAt(chatMessage.getCreatedAt())
                            .build();

                }).collect(Collectors.toList());
    }



}

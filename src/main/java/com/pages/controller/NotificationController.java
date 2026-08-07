package com.pages.controller;

import com.pages.dto.NotificationMsgDto;
import com.pages.dto.NotificationDto;
import com.pages.enums.NotificationType;
import com.pages.service.AppUserDetailsService;
import com.pages.service.Match_requestService;
import com.pages.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/notif")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;
    @Autowired
    private AppUserDetailsService appUserDetailsService;
    @Autowired
    private Match_requestService matchRequestService;

    @PostMapping("/create")
    public void addNotification(Long sender,Long receiver,Long targetId){
        notificationService.createNotification(appUserDetailsService.getAppUserById(receiver),appUserDetailsService.getAppUserById(sender),
                NotificationType.MESSAGE,targetId);
    }

    @PutMapping("/update")
    public void markTarget(Long targetId,String type,Long recipient){
        notificationService.markTypeAsRead(type,targetId,recipient);
    }

    @GetMapping("/count")
    public NotificationDto unreadNotification(Long recipientId){
        return notificationService.getUnreadTypeCount(recipientId);
    }


    @GetMapping("/user-notif")
    public NotificationDto notification(Long recipientId){
        return notificationService.getUnreadNotifications(recipientId);
    }
}

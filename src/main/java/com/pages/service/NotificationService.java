package com.pages.service;

import com.pages.dto.NotificationMsgDto;
import com.pages.dto.NotificationDto;
import com.pages.enums.NotificationType;
import com.pages.model.AppUser;
import com.pages.repository.Match_requestRepo;
import com.pages.repository.NotificationRepo;
import com.pages.util.Notification;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;



@Transactional
@Slf4j
@Service
public class NotificationService {

    private final NotificationRepo notificationRepo;


    public NotificationService(NotificationRepo notificationRepo ){
        this.notificationRepo = notificationRepo;
    }

    // Send a notification from anywhere in the app
    public void createNotification(AppUser recipient, AppUser triggerUser, NotificationType type, Long targetId) {

        Notification notification = Notification.builder()
                .recipient(recipient)
                .triggerUser(triggerUser)
                .type(type)
                .targetId(targetId)
                .isRead(false)
                .build();
        notificationRepo.save(notification);
    }

    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepo.findByRecipientIdOrderByCreatedAtDesc(userId);
    }

    public Integer getUnreadCount(Long userId) {
        return notificationRepo.countUnreadNotificationsByRecipientId(userId);
    }
    public NotificationDto getUnreadTypeCount(Long userId) {

        Map<String, Long> countsMap = new HashMap<>();
        long totalSum=0;
        for(Object[] obj: notificationRepo.countUnreadGroupedByType(userId)){
            String typeKey = String.valueOf(obj[0]);
            long countValue = ((Number) obj[1]).longValue(); // Handles casting across database drivers

            // Put key-value pair and accumulate grand total metric
            countsMap.put(typeKey, countValue);
            totalSum += countValue;
        }
        return NotificationDto.builder()
                .notif(countsMap)
                .totalCount(totalSum)
                .build();
    }

    public NotificationDto getUnreadNotifications(Long userId) {

        Map<String, Long> countsMap = new HashMap<>();
        long totalCounter = 0;
        long counter =0;
        List<NotificationMsgDto> list = new ArrayList<>();

        for(Notification notification : notificationRepo.findAllByRecipientIdAndIsReadFalse(userId)) {


           String sender =notification.getTriggerUser().getFirstName();
           String receiver = notification.getTriggerUser().getFirstName();

           String msg = notification.getType().name().equalsIgnoreCase("accepted")?receiver+" "+notification.getType().name().toLowerCase()+" your request":
                   sender+" sent you a "+notification.getType().name().toLowerCase();

            countsMap.put(notification.getType().name(),++counter);
            ++totalCounter;

           NotificationMsgDto notificationMsgDto = NotificationMsgDto.builder()
                   .id(notification.getId())
                   .message(msg)
                   .build();

           list.add(notificationMsgDto);
        }

        return NotificationDto.builder()
                .msg(list)
                .notif(countsMap)
                .totalCount(totalCounter)
                .build();
    }

    public void markTypeAsRead(String type,Long target,Long recipient) {
        if (type.equalsIgnoreCase("like")) {

                    notificationRepo.markAsReadByTypeAndTargetIdAndRecipientId(NotificationType.LIKE, target,recipient);
        } else if (type.equalsIgnoreCase("message")) {

            notificationRepo.markAsReadByTypeAndTargetIdAndRecipientId(NotificationType.MESSAGE, target,recipient);
        }
    }
    public void markAllReadTarget(Long userId) {
        notificationRepo.markAllAsReadTargetId(userId);
    }

    public void deleteNotification(Long matchId){
        notificationRepo.deleteAllMatchNotifications(matchId);
    }

}



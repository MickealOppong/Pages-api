package com.pages.repository;

import com.pages.enums.NotificationType;
import com.pages.util.Notification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NotificationRepo extends JpaRepository<Notification, Long> {

    // Fetch user notifications ordered by newest first
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);


    List<Notification> findByRecipientIdOrTriggerUserId(Long recipientId,Long targetUserId);

    // Batch update to mark notifications read instantly
    @Transactional
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipient.id = :recipientId AND n.isRead = false")
    void markAllAsRead(@Param("recipientId") Long recipientId);

    //remove all notification for match id
    @Transactional
    @Modifying
    @Query("DELETE FROM Notification n  WHERE n.targetId = :matchId")
    void deleteAllMatchNotifications(@Param("matchId") Long matchId);

    @Transactional
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.targetId = :targetId AND n.isRead = false")
    void markAllAsReadTargetId(@Param("targetId") Long targetId);

    @Transactional
    @Modifying
    @Query("DELETE Notification n WHERE n.recipient.id = :userId OR n.triggerUser.id = :userId ")
    void deleteALlByRecipientOrTriggerUser(@Param("userId") Long userId);

    @Transactional
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.targetId = :targetId AND n.type=:type AND n.recipient.id=:recipientId")
    void markAsReadByTypeAndTargetIdAndRecipientId(NotificationType type, Long targetId,Long recipientId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipient.id = :recipientId AND n.isRead = false")
    int countUnreadNotificationsByRecipientId(@Param("recipientId") Long recipientId);


    @Query("SELECT n.type, COUNT(n) FROM Notification n " +
            "WHERE n.recipient.id = :recipientId AND n.isRead = false " +
            "GROUP BY n.type")
    List<Object[]> countUnreadGroupedByType(@Param("recipientId") Long recipientId);


    List<Notification> findAllByRecipientIdAndIsReadFalse(@Param("recipientId") Long recipientId);

}


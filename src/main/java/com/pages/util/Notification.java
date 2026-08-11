package com.pages.util;

import com.pages.enums.NotificationStatus;
import com.pages.enums.NotificationType;
import com.pages.model.AppUser;
import com.pages.model.Match_request;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends LogEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private AppUser recipient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trigger_user_id")
    private AppUser triggerUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;


    // Target ID for frontend deep-linking (e.g., postId, matchRequestId)
    private Long targetId;

    @Column(nullable = false)
    private boolean isRead = false;


}


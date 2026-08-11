package com.pages.model;

import com.pages.util.LogEntity;
import com.pages.util.Notification;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "match_request", uniqueConstraints = {
        // This blocks User A from sending a duplicate request to User B
        @UniqueConstraint(columnNames = {"senderId", "receiverId"})
})
public class Match_request extends LogEntity {

    @Id @GeneratedValue
    private Long Id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "senderId")
    private AppUser senderId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "receiverId")
    private AppUser receiverId;

    private Long postId;

    private String requestStatus;

    @OneToMany(mappedBy = "targetId",cascade = CascadeType.ALL)
    private List<Notification> notificationList= new ArrayList<>();

}

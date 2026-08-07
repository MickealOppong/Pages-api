package com.pages.model;

import com.pages.enums.Status;
import com.pages.util.LogEntity;
import com.pages.util.Media;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Entity
@Getter
@Setter
@Table
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Post extends LogEntity implements Serializable {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;
    @Column(length = 2050)
    private String content;
    private String status;
    private String visibility;
    private String type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private AppUser appUser;


    @OneToOne
    private Media media;

    @Column(name = "views_count", nullable = false)
    @Builder.Default
    private Integer viewsCount = 0;


    public Post(AppUser appUser, String type, String content, String visibility, Media image) {
        this.appUser = appUser;
        this.content = content;
        this.status = Status.ACTIVE.name();
        this.visibility = visibility;
        this.type = type;
        this.media = image;
    }
}

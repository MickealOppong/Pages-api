package com.pages.service;

import com.pages.exception.EntityNotFoundException;
import com.pages.model.AppUser;
import com.pages.model.Post;
import com.pages.model.PostView;
import com.pages.repository.AppUserRepo;
import com.pages.repository.PostRepo;
import com.pages.repository.PostViewRepo;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {

    private final PostViewRepo postViewRepo;
    private final PostRepo postRepo;
    private final AppUserRepo appUserRepo;

    public AnalyticsService(PostViewRepo postViewRepo, PostRepo postRepo, AppUserRepo appUserRepo) {
        this.postViewRepo = postViewRepo;
        this.postRepo = postRepo;
        this.appUserRepo = appUserRepo;
    }

    @Transactional
    public void recordPostView(Long postId, Long viewerId) {
        // 1. Instantly increment the cached counter on the main Post entity row
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));

        // Increment the field on the Java object
        post.setViewsCount(post.getViewsCount() + 1);

        // Saving triggers @PreUpdate / @LastModifiedDate automatically
        postRepo.save(post);

        // 2. Offload the auditable log creation to the background thread pool
        logViewAsynchronously(postId, viewerId);
    }


    @Async
    @Transactional
    public void logViewAsynchronously(Long postId, Long viewerId) {
        // Hibernate getReferenceById fetches lazy proxies, preventing heavy SELECT queries
        Post postProxy = postRepo.findById( postId).orElse(null);
        AppUser viewerProxy = appUserRepo.findById(viewerId).orElse(null);

        if(postProxy !=null && viewerProxy !=null){
            PostView analyticsLog = PostView.builder()
                    .post(postProxy)
                    .viewer(viewerProxy)
                    .build();

            postViewRepo.save(analyticsLog);
        }
    }
}

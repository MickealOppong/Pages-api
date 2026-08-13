package com.pages.repository;

import com.pages.model.Post;
import com.pages.model.PostView;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostViewRepo extends JpaRepository<PostView,Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM PostView pv WHERE pv.post.id= :postId")
    void deleteByPostId(Long postId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    DELETE FROM PostView pv
    WHERE pv.post.appUser.id = :userId""")
    void deleteByPostOwnerId(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM PostView pv WHERE pv.viewer.id = :userId")
    void deleteByViewerId(@Param("userId") Long userId);

    Optional<PostView> findByViewerId(Long userId);
}

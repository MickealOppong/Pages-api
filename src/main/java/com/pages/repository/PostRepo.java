package com.pages.repository;

import com.pages.model.AppUser;
import com.pages.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepo extends JpaRepository<Post,Long>, JpaSpecificationExecutor<Post> {


    List<Post> findAllByAppUserId(Long userId);
    /*
    Page<Post> findAllByLocationAndAgeBetweenAndTypeAndGender(
            String location, Integer minAge, Integer maxAge, String type, String gender, Pageable pageable
    );

     */

/*
    @Query("SELECT p FROM Post p JOIN p.appUser u WHERE " +
            "u.id != :currentUserId AND " +
            "(:myPref = 'BOTH' OR u.gender = :myPref) AND " +
            "(u.preference = 'BOTH' OR u.preference = :myGender)")
    Page<Post> findMatches(
            @Param("currentUserId") Long currentUserId,
            @Param("myGender") String myGender,
            @Param("myPref") String myPref,
            Pageable pageable
    );

 @Query("SELECT p FROM Post p JOIN p.appUser u WHERE " +
         "u.id != :currentUserId " +               // 1. Can't match yourself
         "AND p.appUser.id != :currentUserId " +   // 2. Can't see your own posts
         "AND (:myPref = 'BOTH' OR u.gender = :myPref) " + // 3. Fits your preference
         "AND (u.preference = 'BOTH' OR u.preference = :myGender) " + // 4. You fit their preference

         // 5. EXCLUDE people YOU sent a request to (Prevents duplicate swiping)
         "AND u.id NOT IN (" +
         "    SELECT mr.receiverId.id FROM Match_request mr " +
         "    WHERE mr.senderId.id = :currentUserId AND mr.requestStatus = 'PENDING'" +
         ") " +

         // 6. EXCLUDE people you are ALREADY matched with (Wipe accepted pairs out of discovery)
         "AND u.id NOT IN (" +
         "    SELECT mr.receiverId.id FROM Match_request mr WHERE mr.senderId.id = :currentUserId AND mr.requestStatus = 'ACCEPTED'" +
         ") " +
         "AND u.id NOT IN (" +
         "    SELECT mr.senderId.id FROM Match_request mr WHERE mr.receiverId.id = :currentUserId AND mr.requestStatus = 'ACCEPTED'" +
         ")")
 Page<Post> findDiscoveryFeed(
         @Param("currentUserId") Long currentUserId,
         @Param("myGender") String myGender,
         @Param("myPref") String myPref,
         Pageable pageable
 );


    @Query("SELECT p FROM Post p JOIN p.appUser u WHERE " +
            "u.id != :currentUserId " +               // 1. Can't see yourself
            "AND p.appUser.id != :currentUserId " +   // 2. Can't see your own posts
            "AND (:myPref = 'BOTH' OR u.gender = :myPref) " + // 3. Fits your gender preference
            "AND (u.preference = 'BOTH' OR u.preference = :myGender) " + // 4. You fit their preference

            // 5. STIPULATION: Only show PUBLIC posts in the swiping discovery deck
            "AND p.visibility = 'PUBLIC' " +

            // 6. EXCLUSION CORE: Completely exclude anyone you have an existing relationship row with
            // (Blocks Pending, Blocks Accepted, and Blocks Rejected)
            "AND u.id NOT IN (SELECT mr.receiverId.id FROM Match_request mr WHERE mr.senderId.id = :currentUserId) " +
            "AND u.id NOT IN (SELECT mr.senderId.id FROM Match_request mr WHERE mr.receiverId.id = :currentUserId)")
    Page<Post> findUnifiedDiscoveryFeed(
            @Param("currentUserId") Long currentUserId,
            @Param("myGender") String myGender,
            @Param("myPref") String myPref,
            Pageable pageable
    );

    @Query("SELECT p FROM Post p JOIN p.appUser u WHERE " +
            "u.id != :currentUserId " +
            "AND p.appUser.id != :currentUserId " +
            "AND (:myPref = 'BOTH' OR u.gender = :myPref) " +
            "AND (u.preference = 'BOTH' OR u.preference = :myGender) " +
            "AND p.visibility = 'PUBLIC' " +

            // COALESCE matches the column to itself if the parameter is null
            "AND u.city = COALESCE(:city, u.city) " +
            "AND p.type = COALESCE(:activity, p.type) " +

            // Age logic updated with COALESCE fallback behaviors
            "AND (:minAge IS NULL OR (YEAR(CURRENT_DATE) - YEAR(u.date_of_birth)) >= :minAge) " +
            "AND (:maxAge IS NULL OR (YEAR(CURRENT_DATE) - YEAR(u.date_of_birth)) <= :maxAge) " +

            "AND u.id NOT IN (SELECT mr.receiverId.id FROM Match_request mr WHERE mr.senderId.id = :currentUserId) " +
            "AND u.id NOT IN (SELECT mr.senderId.id FROM Match_request mr WHERE mr.receiverId.id = :currentUserId)")
    Page<Post> findUnifiedDiscovery(
            @Param("currentUserId") Long currentUserId,
            @Param("myGender") String myGender,
            @Param("myPref") String myPref,
            @Param("city") String city,
            @Param("activity") String activity,
            @Param("minAge") Integer minAge,
            @Param("maxAge") Integer maxAge,
            Pageable pageable
    );


 */

    boolean existsByAppUserIdAndVisibility(Long appUserId, String visibility);

    long countByAppUserIdAndVisibility(Long appUserId, String visibility);


 @Query("SELECT COUNT(DISTINCT p1.type) FROM Post p1, Post p2 " +
         "WHERE p1.type = p2.type " +
         "AND p1.appUser.id = :user_one_Id " +
         "AND p2.appUser.id = :user_two_Id")
 Long countCommonActivities(@Param("user_one_Id") Long user_one_Id, @Param("user_two_Id") Long user_two_Id);
 Long countTypeByAppUserId(AppUser user);

    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM Post p WHERE p.appUser.id = :userId")
    void deleteAllPostByAppUserId(@Param("userId") Long userId);

    @Query("select p.media.path from Post p where p.appUser.id = :userId")
    List<String> findMediaPathsByUserId(Long userId);

    @Query("select p.media.path from Post p where p.postId = :postId")
    String findMediaPathsByPostId(Long postId);
}

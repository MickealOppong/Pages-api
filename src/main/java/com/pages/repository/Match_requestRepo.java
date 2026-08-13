package com.pages.repository;

import com.pages.model.AppUser;
import com.pages.model.Match_request;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface Match_requestRepo extends JpaRepository<Match_request,Long> {

    @Modifying
    @Query(value = "DELETE FROM Match_request m WHERE " +
            "(m.senderId.id = :user1Id AND m.receiverId.id = :user2Id) OR " +
            "(m.senderId.id = :user2Id AND m.receiverId.id = :user1Id)")
    void deleteMatchBetweenPair(Long user1Id, Long user2Id);

    @Modifying
    @Query(value = "DELETE FROM Match_request m WHERE m.senderId.id = :userId OR m.receiverId.id = :userId")
    void deleteUserMatchHistory(Long userId);



    // Checked and fully compliant with Spring Data parsing mechanics
    @Query("""
    SELECT m
    FROM Match_request m
    WHERE (m.receiverId.id = :userId AND m.requestStatus ='PENDING')
""")
    List<Match_request> findUserLikeRequests(@Param("userId") Long userId);

    // Checked and fully compliant with Spring Data parsing mechanics
    @Query("""
    SELECT m
    FROM Match_request m
    WHERE (m.receiverId.id = :userId AND m.requestStatus = 'ACCEPTED')
       OR (m.senderId.id = :userId AND m.requestStatus = 'ACCEPTED')
""")
    List<Match_request> findUserMatchRequests(@Param("userId") Long userId);

    @Query("SELECT l.receiverId.id FROM Match_request l WHERE l.senderId.id = :senderId AND l.requestStatus='PENDING'")
    List<Match_request> findLikedUserIdsBySenderId(AppUser senderId);


    @Query("SELECT l.senderId.id FROM Match_request l WHERE l.receiverId.id = :receiverId AND l.requestStatus='PENDING'")
    List<Match_request> findLikedUserIdsByReceiverId(AppUser receiverId);

    boolean existsBySenderIdAndReceiverIdOrSenderIdAndReceiverId(
            AppUser sender1, AppUser receiver1, AppUser sender2, AppUser receiver2
    );

    boolean existsBySenderIdIdAndReceiverIdIdAndRequestStatusOrSenderIdIdAndReceiverIdIdAndRequestStatus(
            Long sender1, Long receiver1,String requestStatus1, Long receiver2,Long sender2,String requestStatus2
    );

    @Query("SELECT COUNT(m) > 0 FROM Match_request m " +
            "WHERE (m.senderId.id = :userId OR m.receiverId.id = :userId) " +
            "AND m.requestStatus =:status")
    boolean existsMatchRequest(@Param("userId") Long userId,@Param("status") String status);


    @Query("""
    SELECT m
    FROM Match_request m
    WHERE (m.senderId.id = :user1Id AND m.receiverId.id = :user2Id)
       OR (m.senderId.id = :user2Id AND m.receiverId.id = :user1Id)
""")
    Optional<Match_request> findBetweenUsers(
            @Param("user1Id") Long user1Id,
            @Param("user2Id") Long user2Id
    );



}

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

    List<Match_request> findAllBySenderId(Long senderId);

    Set<Match_request> findAllByReceiverIdIdAndRequestStatus(Long receiverId, String requestStatus);
    List<Match_request> findAllByReceiverIdOrSenderId(Long receiverId,Long senderId);

    Set<Match_request> findAllByReceiverIdAndRequestStatus(AppUser receiverId,String requestStatus);

   List<Match_request> findByReceiverIdIdAndRequestStatus(Long receiverId,String requestStatus);

    // Checked and fully compliant with Spring Data parsing mechanics
    List<Match_request> findAllByReceiverIdIdAndRequestStatusOrSenderIdIdAndRequestStatus(
            Long receiverId, String status1, Long senderId, String status2
    );

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

    Optional<Match_request> findBySenderIdIdAndReceiverIdIdOrReceiverIdIdAndSenderIdId(Long senderId,Long receiver1Id,Long receiver2Id,Long sender2Id);

}

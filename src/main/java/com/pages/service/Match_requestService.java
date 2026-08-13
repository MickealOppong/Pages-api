package com.pages.service;

import com.pages.dto.MatchRequestDto;
import com.pages.dto.ResponseDto;
import com.pages.enums.NotificationType;
import com.pages.enums.Request_Status;
import com.pages.exception.EntityNotFoundException;
import com.pages.exception.InvalidOperationException;
import com.pages.model.AppUser;
import com.pages.model.ChatMessage;
import com.pages.model.Match_request;
import com.pages.model.Post;
import com.pages.repository.ChatMessageRepo;
import com.pages.repository.Match_requestRepo;
import com.pages.repository.PostRepo;
import com.pages.util.Media;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Transactional
@Slf4j
@Service
public class Match_requestService {

    private final Match_requestRepo matchRequestsRepo;
    private final AppUserDetailsService appUserDetailsService;
    private final PostRepo postRepo;
    private final MediaService mediaService;
    private final ChatMessageRepo chatMessageRepo;
    private final NotificationService notificationService;

    public Match_requestService(Match_requestRepo matchRequestsRepo, AppUserDetailsService appUserDetailsService, PostRepo postRepo, MediaService mediaService, ChatMessageRepo chatMessageRepo, NotificationService notificationService) {
        this.matchRequestsRepo = matchRequestsRepo;
        this.appUserDetailsService = appUserDetailsService;
        this.postRepo = postRepo;
        this.mediaService = mediaService;
        this.chatMessageRepo = chatMessageRepo;
        this.notificationService = notificationService;
    }


    private Long getNumberOfActivitiesInCommon(Long user_one_id,Long user_two_id){
        return postRepo.countCommonActivities(user_one_id,user_two_id);

    }

    private Long getNumberOfActivitiesCreated(AppUser userId){
        return postRepo.countTypeByAppUserId(userId);

    }


    public ResponseDto<Object> addToLike(Long senderId, Long receiverId, Long postId){
        try{
            //check whether user exist for send and receiver
            AppUser sender = appUserDetailsService.getAppUserById(senderId);
            AppUser receiver =appUserDetailsService.getAppUserById(receiverId);

            //check the sender has not sent like or receiver has not been liked
            boolean exist = matchRequestsRepo.existsBySenderIdAndReceiverIdOrSenderIdAndReceiverId(sender,receiver,receiver,sender);

           if(exist){
               throw new IllegalStateException("A match request or connection already exists between these users.");
            }

           //get post object
            Post post = postRepo.getReferenceById(postId);
            //save like
            Match_request newMatchRequests = Match_request.builder()
                    .requestStatus(Request_Status.PENDING.name())
                    .receiverId(receiver)
                    .postId(postId)
                    .senderId(sender)
                    .build();
          Match_request saveRequest= matchRequestsRepo.save(newMatchRequests);
           notificationService.createNotification(receiver,sender, NotificationType.LIKE,saveRequest.getId());
           return ResponseDto.builder()
                   .data(true)
                   .message("Request sent")
                   .httpStatus(HttpStatus.OK)
                   .build();

        }catch (Exception e){
            return ResponseDto.builder()
                    .data(false)
                    .message(e.getMessage())
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .build();
        }
    }


    public Set<Long> getAllLikesSent(AppUser sender){
        try{
            Set<Long> receiversList =  new HashSet<>();

            for(Match_request matchRequests : matchRequestsRepo.findLikedUserIdsBySenderId(sender)){
                    receiversList.add(matchRequests.getReceiverId().getId());
            }
            return  receiversList;
        }catch (Exception e){
            return null;
        }
    }

    public Set<Long> getAllLikesReceived(AppUser sender){
        try{
            Set<Long> receiversList =  new HashSet<>();

            for(Match_request matchRequests : matchRequestsRepo.findLikedUserIdsByReceiverId(sender)){
                receiversList.add(matchRequests.getReceiverId().getId());
            }
            return  receiversList;
        }catch (Exception e){
            return null;
        }
    }

    public List<MatchRequestDto> getUserLikes(Long id){


      return matchRequestsRepo.findUserLikeRequests(id).stream().map(mr->{
            AppUser targetUser=mr.getSenderId();

                    String picture = Optional.ofNullable(targetUser)
                           .map(AppUser::getMedia)  // Safely moves to photo if user is not null
                           .map(Media::getId)       // Safely extracts ID if photo is not null
                           .map(mediaService::getImage) // Calls your service if ID exists
                           .orElse(null);

                    //get the post on which user sends request
                    Post post = postRepo.getReferenceById(mr.getPostId());

                    if(targetUser!=null){
                        return MatchRequestDto.builder()
                                .matchId(mr.getId())
                                .firstName(targetUser.getFirstName())
                                .lastName(targetUser.getLastName())
                                .senderId(targetUser.getId())
                                .activity(post.getType())
                                .requestDate(mr.getCreatedAt())
                                .date_of_birth(targetUser.getDate_of_birth())
                                .image(picture)
                                .build();
                    }
                    return null;

               })
               .collect(Collectors.toList());
    }


    public MatchRequestDto getMatchById(Long id,Long currentUserId){


        Optional<Match_request> matchRequest=  matchRequestsRepo.findById(id);
        if(matchRequest.isPresent()){
            Match_request retrieved = matchRequest.get();

            //find the current user and return other users image
            AppUser targetUser;

                if( retrieved.getSenderId().getId().equals(currentUserId)){
                    targetUser = retrieved.getReceiverId();
                }else{
                    targetUser = retrieved.getSenderId();
                }

                if(targetUser==null){
                    throw new UsernameNotFoundException("User does not exist");
                }
            // Optional to safely extract the photo ID without crashing if the photo is missing
            String picture = Optional.of(targetUser)
                    .map(AppUser::getMedia)  // Safely moves to photo if user is not null
                    .map(Media::getId)       // Safely extracts ID if photo is not null
                    .map(mediaService::getImage) // Calls your service if ID exists
                    .orElse(null);

                //check is user active
            Instant lastActivity =appUserDetailsService.getAppUserById(targetUser.getId()).getLastActive();

            boolean isOnline = lastActivity!= null &&
                    lastActivity.isAfter(Instant.now().minus(20,ChronoUnit.MINUTES));
            //retrieve last message
            String lastMessage = chatMessageRepo.findFirstByMatchIdOrderByCreatedAtDesc(id)
                    .map(ChatMessage::getMessage).toString();

            String post = postRepo.getReferenceById(retrieved.getPostId()).getType();

            return MatchRequestDto.builder()
                    .matchId(retrieved.getId())
                    .senderId(retrieved.getSenderId().getId())
                    .isOnline(isOnline)
                    .receiverId(targetUser.getId())
                    .activity(post)
                    .firstName(targetUser.getFirstName())
                    .lastName(targetUser.getLastName())
                    .date_of_birth(targetUser.getDate_of_birth())
                    .image(picture)
                    .build();
        }
        return null;
    }

    public Match_request getMatch(Long matchId){
        return matchRequestsRepo.findById(matchId).orElse(null);
    }



    public List<MatchRequestDto> getMyMatchesDashboard(Long currentUserId) {
        List<Match_request> rawMatches = matchRequestsRepo.findUserMatchRequests(currentUserId);

        // Map each row by dynamically selecting the opposite user
        return rawMatches.stream().map(matchRequests -> {
            AppUser targetUser;

            // If I am the sender, then the receiver is the target match
            if (matchRequests.getSenderId().getId().equals(currentUserId)) {
                targetUser = matchRequests.getReceiverId();
            } else {
                // If I am the receiver, then the sender is the target match
                targetUser = matchRequests.getSenderId();
            }

            AppUser user = appUserDetailsService.getAppUserById(targetUser.getId());

           // Optional to safely extract the photo ID without crashing if the photo is missing
            String picture = Optional.ofNullable(user)
                    .map(AppUser::getMedia)  // Safely moves to photo if user is not null
                    .map(Media::getId)       // Safely extracts ID if photo is not null
                    .map(mediaService::getImage) // Calls your service if ID exists
                    .orElse(null);

            //retrieve last message
            Optional<ChatMessage> lastMessageDto= chatMessageRepo.findFirstByMatchIdOrderByCreatedAtDesc(matchRequests.getId());

            String lastMessage=null;
            Instant lastMessageDate=null;
             if(lastMessageDto.isPresent()){
                 lastMessageDate = lastMessageDto.get().getCreatedAt();
                 lastMessage = lastMessageDto.get().getMessage();
             }

            //check is user active
            Instant lastActivity =appUserDetailsService.getAppUserById(targetUser.getId()).getLastActive();

            boolean isOnline = lastActivity!= null &&
                    lastActivity.isAfter(Instant.now().minus(20,ChronoUnit.MINUTES));

            return MatchRequestDto.builder()
                    .matchId(matchRequests.getId())
                    .senderId(matchRequests.getSenderId().getId())
                    .receiverId(matchRequests.getReceiverId().getId())
                    .firstName(targetUser.getFirstName())
                    .lastName(targetUser.getLastName())
                    .date_of_birth(targetUser.getDate_of_birth())
                    .image(picture)
                    .lastMessage(lastMessage)
                    .isOnline(isOnline)
                    .lastMessageDate(lastMessageDate)
                    .build();
        }).collect(Collectors.toList());
    }


    public ResponseDto<Object> deleteLike(Long matchId){

        try {
            Match_request match =matchRequestsRepo.findById(matchId)
                    .orElseThrow(()->new EntityNotFoundException("No valid match found for "+matchId));

            // 1. Delete the shared chat history between this exact pair first
            chatMessageRepo.deleteMatchChatHistory(match.getId());

            //2. remove notifications
            notificationService.deleteNotification(match.getId());

            // 3 Delete the match request link itself
            matchRequestsRepo.delete(match);
            return ResponseDto.builder()
                    .message("Unmatched")
                    .data(true)
                    .httpStatus(HttpStatus.OK)
                    .build();
        }catch (Exception e){
            return ResponseDto.builder()
                    .message(e.getMessage())
                    .data(false)
                    .httpStatus(HttpStatus.FORBIDDEN)
                    .build();
        }
    }

    // MatchRequestService.java
    public boolean acceptRequestById(Long id, Long currentUserId) {
        Match_request request = matchRequestsRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Request not found"));

        if(request.getSenderId().getId().equals(currentUserId)){
            throw new AccessDeniedException("You are not authorized to accept this request");
        }

        request.setRequestStatus(Request_Status.ACCEPTED.name());
        notificationService.markTypeAsRead(NotificationType.LIKE.name(),request.getId(),request.getReceiverId().getId());
        matchRequestsRepo.save(request);
        notificationService.createNotification(request.getSenderId(),request.getReceiverId()
                ,NotificationType.ACCEPTED,request.getId());
        return true;

    }

    public boolean acceptRequest(Long sender,Long receiver) {

        Match_request request = matchRequestsRepo.findBetweenUsers(sender,receiver)
                .orElseThrow(() -> new EntityNotFoundException("Request not found"));

        request.setRequestStatus(Request_Status.ACCEPTED.name());
        matchRequestsRepo.save(request);

        return true;

    }

}

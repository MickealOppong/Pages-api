package com.pages.service;

import com.pages.dto.*;
import com.pages.exception.EntityNotFoundException;
import com.pages.interfaces.ValidMedia;
import com.pages.model.AppUser;
import com.pages.model.Post;
import com.pages.repository.PostRepo;
import com.pages.specs.PostSpecs;
import com.pages.util.Media;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@Transactional
public class PostService {

    private final PostRepo postRepo;
    private final MediaService mediaService;
    private final AppUserDetailsService appUserDetailsService;


    public PostService(PostRepo postRepo, MediaService mediaService, AppUserDetailsService appUserDetailsService) {
        this.postRepo = postRepo;
        this.mediaService = mediaService;
        this.appUserDetailsService = appUserDetailsService;
    }


    public ResponseDto<Object> saveBroadcast(CreateMomentDto createMomentDto, MultipartFile media) {

        Media savedMedia = null;

        try {

            if (media == null || media.isEmpty()) {
                throw new IllegalArgumentException("Media is required");
            }

            if (media.getSize() > 30L * 1024 * 1024) {
                throw new IllegalArgumentException("Media must be smaller than 30MB");
            }

            String contentType = media.getContentType();

            Set<String> allowedTypes = Set.of(
                    "image/jpeg",
                    "image/png",
                    "image/webp",
                    "video/mp4",
                    "video/webm"
            );

            if (contentType == null ||
                    !allowedTypes.contains(contentType.toLowerCase())) {

                throw new IllegalArgumentException("Invalid media type");
            }

            AppUser user = appUserDetailsService.getAppUserById(createMomentDto.getUserId());

            Long userId = user.getId();


            savedMedia = mediaService.saveMedia(media, createMomentDto.getMediaOrientation());

            Post post = Post.builder()
                    .appUser(user)
                    .type(createMomentDto.getActivity())
                    .viewsCount(0)
                    .visibility(createMomentDto.getVisibility())
                    .content(createMomentDto.getContent())
                    .media(savedMedia)
                    .build();


            postRepo.save(post);


            appUserDetailsService.updateLastActive(userId);


            return ResponseDto.builder()
                    .data(true)
                    .message("Post created")
                    .httpStatus(HttpStatus.OK)
                    .build();


        } catch (Exception e) {


            return ResponseDto.builder()
                    .data(null)
                    .message(e.getMessage())
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .build();
        }
    }



    public DeleteResponseDto deletePostById(Long postId){
        try{
            String mediaPath = postRepo.findMediaPathsByPostId(postId);
            postRepo.deleteById(postId);
            mediaService.delete(mediaPath);
           return DeleteResponseDto.builder()
                            .deleted(true)
                             .message("Deleted")
                            .httpStatus(HttpStatus.OK)
                            .build();
        }catch (Exception e){
            return DeleteResponseDto.builder()
                    .deleted(false)
                    .message(e.getMessage())
                    .httpStatus(HttpStatus.FORBIDDEN)
                    .build();
        }
    }
    public ResponseDto<Object> postsByUser(Long id){
        try {
            List<PostDto> dataToSend = new ArrayList<>();

            for(Post post:postRepo.findAllByAppUserId(id)){

               MediaDto media= mediaService.getImageAndType(post.getMedia().getId());
                AppUser user =appUserDetailsService.getAppUserById(post.getAppUser().getId());

                PostDto postDto = PostDto.builder()
                        .postId(post.getPostId())
                        .content(post.getContent())
                        .type(post.getType())
                        .visibility(post.getVisibility())
                        .userId(post.getAppUser().getId())
                        .viewsCount(post.getViewsCount())
                        .status(post.getStatus())
                        .location(user.getCity())
                        .firstName(user.getFirstName())
                        .media(media.getMedia())
                        .mediaOrientation(media.getOrientation())
                        .createdAt(post.getCreatedAt())
                        .modifiedAt(post.getModifiedAt())
                        .build();
                dataToSend.add(postDto);
            }
            return ResponseDto.builder()
                    .data(dataToSend)
                    .message("Success")
                    .httpStatus(HttpStatus.OK)
                    .build();
        }catch (Exception e){
            return ResponseDto.builder()
                    .data(null)
                    .message(e.getMessage())
                    .httpStatus(HttpStatus.BAD_REQUEST)
                    .build();
        }
    }

    public List<PostDto> allPostsByUser(Long id){

            List<PostDto> dataToSend = new ArrayList<>();

            for(Post post:postRepo.findAllByAppUserId(id)){

                MediaDto mediaDto = mediaService.getImageAndType(post.getMedia().getId());

                PostDto postDto = PostDto.builder()
                        .postId(post.getPostId())
                        .content(post.getContent())
                        .type(post.getType())
                        .visibility(post.getVisibility())
                        .media(mediaDto.getMedia())
                        .mediaOrientation(mediaDto.getOrientation())
                        .build();
                dataToSend.add(postDto);

        }
            return dataToSend;
    }

    public Long getNumberOfActivitiesInCommon(Long user_one_id,Long user_two_id){
        return postRepo.countCommonActivities(user_one_id,user_two_id);

    }





    public ResponseDto<Object> getDiscovery(Long userId, String city,String lookingFor, String activity, Integer fromAge, Integer toAge, int page,String genderPreference ,int size) {

        try{
            List<PostDto> dataToSend = new ArrayList<>();


            AppUser currentUser = appUserDetailsService.getAppUserById(userId);
            String myGender = currentUser.getGender();

            // Explicitly define specifications using an optimized root join
            Specification<Post> spec = (root, query, cb) -> {
                // 1. Force a single explicit JOIN to prevent dynamic cross join multiplication bugs
                jakarta.persistence.criteria.Join<Post, AppUser> appUserJoin = root.join("appUser");

                // 2. Base conditions
                jakarta.persistence.criteria.Predicate predicate = cb.and(
                        cb.equal(root.get("visibility"), "PUBLIC"),
                        cb.notEqual(appUserJoin.get("id"), userId)
                );


                // 3. Gender preference dynamic additions
                if (genderPreference != null && !genderPreference.trim().isEmpty() && !genderPreference.equalsIgnoreCase("null")) {
                    predicate = cb.and(predicate, cb.equal(appUserJoin.get("gender"), genderPreference.trim()));
                }
            /*
            if (myGender != null && !myGender.equalsIgnoreCase("BOTH")) {
                predicate = cb.and(predicate, cb.equal(appUserJoin.get("preference"), myGender));
            }
            */
                // 4. City filter activation
                if (city != null && !city.trim().isEmpty() && !city.equalsIgnoreCase("null")) {
                    predicate = cb.and(predicate, cb.equal(appUserJoin.get("city"), city.trim()));
                }

                // 5. Activity type filter activation
                if (activity != null && !activity.trim().isEmpty() && !activity.equalsIgnoreCase("null")) {
                    predicate = cb.and(predicate, cb.equal(root.get("type"), activity.trim()));
                }

                // 5. Activity type filter activation
                if (lookingFor != null && !lookingFor.trim().isEmpty() && !lookingFor.equalsIgnoreCase("null")) {
                    predicate = cb.and(predicate, cb.equal(appUserJoin.get("lookingFor"), lookingFor.trim()));
                }

                // 7. Age bracket dynamic calculation activation
                int currentYear = java.time.LocalDate.now().getYear();
                if (fromAge != null && fromAge > 0) {
                    jakarta.persistence.criteria.Expression<Integer> ageExpr = cb.diff(
                            currentYear,
                            cb.function("YEAR", Integer.class, appUserJoin.get("date_of_birth"))
                    );
                    predicate = cb.and(predicate, cb.greaterThanOrEqualTo(ageExpr, fromAge));
                }
                if (toAge != null && toAge > 0) {
                    jakarta.persistence.criteria.Expression<Integer> ageExpr = cb.diff(
                            currentYear,
                            cb.function("YEAR", Integer.class, appUserJoin.get("date_of_birth"))
                    );
                    predicate = cb.and(predicate, cb.lessThanOrEqualTo(ageExpr, toAge));
                }


                return predicate;
            };

            // 7. Combine with your existing subquery match exclusions from PostSpecs
            spec = spec.and(PostSpecs.excludeExistingMatches(userId));

            // Create your sort order
            Sort sort = Sort.by(Sort.Direction.DESC, "createdAt")
                    .and(Sort.by(Sort.Direction.ASC,"postId"));
            //pagination
            Pageable pageable = PageRequest.of(page == 0 ? page : page - 1, size,sort);
            //data from database
            Page<Post> matchingPosts = postRepo.findAll(spec, pageable);

            // Mapping loop remains clean and optimized
            for (Post post : matchingPosts) {
                AppUser author = post.getAppUser();

                MediaDto userMediaDto = Optional.ofNullable(author)
                        .map(AppUser::getMedia)
                        .map(Media::getId)
                        .map(mediaService::getImageAndType)
                        .orElse(null);

                MediaDto postMediaDto = Optional.of(post)
                        .map(Post::getMedia)
                        .map(Media::getId)
                        .map(mediaService::getImageAndType)
                        .orElse(null);




                PostDto postDto = PostDto.builder()
                        .postId(post.getPostId())
                        .content(post.getContent())
                        .type(post.getType())
                        .visibility(post.getVisibility())
                        .firstName(author.getFirstName())
                        .status(post.getStatus())
                        .userId(author.getId())
                        .viewsCount(post.getViewsCount())
                        .date_of_birth(author.getDateOfBirth())
                        .location(author.getCity())
                        .media(postMediaDto.getMedia())
                        .profileImage(userMediaDto==null?null:userMediaDto.getMedia())
                        .mediaOrientation(postMediaDto.getOrientation())
                        .lookingFor(author.getLookingFor())
                        .height(author.getHeight())
                        .profession(author.getProfession())
                        .createdAt(post.getCreatedAt())
                        .modifiedAt(post.getModifiedAt())
                        .build();
                dataToSend.add(postDto);

            }

            appUserDetailsService.updateLastActive(userId);
            return ResponseDto.builder()
                    .data(dataToSend)
                    .message("Success")
                    .httpStatus(HttpStatus.OK)
                    .build();
        }catch (Exception e){
            return ResponseDto.builder()
                    .data(null)
                    .message(e.getMessage())
                    .httpStatus(HttpStatus.FORBIDDEN)
                    .build();
        }
    }



}

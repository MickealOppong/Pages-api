package com.pages.service;

import com.pages.controller.MediaController;
import com.pages.dto.MediaDto;
import com.pages.impl.MediaUtilImpl;
import com.pages.repository.MediaRepo;
import com.pages.util.Media;
import com.pages.util.MediaStorageLocation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;


import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class MediaService {

    private final MediaRepo mediaRepo;
    private final MediaUtilImpl MediaUtil;
    private final MediaStorageLocation mediaStorageLocation;


    public MediaService(MediaRepo mediaRepo, MediaUtilImpl MediaUtil, MediaStorageLocation mediaStorageLocation) {
        this.mediaRepo = mediaRepo;
        this.MediaUtil = MediaUtil;
        this.mediaStorageLocation = mediaStorageLocation;
    }



    public Resource getResource(String fileName){
        return MediaUtil.loadAsResource(fileName);
    }

    public Media saveMedia(MultipartFile image,String orientation) {
        try{
            Media media = Media.builder()
                    .contentType(image.getContentType())
                    .fileName(image.getOriginalFilename())
                    .mediaOrientation(orientation)
                    .path(mediaStorageLocation.getLocation() +"/"+ image.getOriginalFilename())
                    .build();
           Media savedMedia = mediaRepo.save(media);
            MediaUtil.store(image);
            return savedMedia;
        }catch (Exception e){
            return null;
        }
    }

    public Media saveMedia(MultipartFile image) {
        try{
            Media media = Media.builder()
                    .contentType(image.getContentType())
                    .fileName(image.getOriginalFilename())
                    .path(mediaStorageLocation.getLocation() +"/"+ image.getOriginalFilename())
                    .build();
            Media savedMedia = mediaRepo.save(media);
            MediaUtil.store(image);
            return savedMedia;
        }catch (Exception e){
            return null;
        }
    }


    public Media saveImage(String username, MultipartFile file) {
        try{
            if (file.getContentType()== null ) {
                throw new IllegalArgumentException("Empty file! Please upload again.");
            }
            Media media = Media.builder()
                    .contentType(file.getContentType())
                    .fileName(mediaStorageLocation.getLocation()+username+"-"+file.getOriginalFilename())
                    .path(file.getOriginalFilename())
                    .build();
            Media savedMedia = mediaRepo.save(media);
            MediaUtil.store(file,username);
            return savedMedia;
        }catch (Exception e){
            return null;
        }
    }


    /*
     Retrieves images metadata using product id from repository and actual image from local directory
     */
    public MediaDto getImageAndType(Long imageId)  {

       Media media = mediaRepo.findById(imageId).orElse(null);

        if (media != null) {

            return MediaDto.builder().media(MvcUriComponentsBuilder.fromMethodName(
                    MediaController.class, "serveFile", media.getFileName()).build().toUri().toString())
                    .orientation(media.getMediaOrientation())
                    .build();
        }
        return null;

    }

    /*
 Retrieves images metadata using product id from repository and actual image from local directory
 */
    public String getImage(Long imageId)  {

        Media media = mediaRepo.findById(imageId).orElse(null);

        if (media != null) {

            return MvcUriComponentsBuilder.fromMethodName(
                    MediaController.class, "serveFile", media.getFileName()).build().toUri().toString();
        }
        return null;

    }


    public void delete(Long id)throws IOException{
       Media media= mediaRepo.findById(id).orElse(null);
       if(media!=null) {
           //delete photo from database
           mediaRepo.delete(media);

           //check photo deleted
           boolean isDeleted= mediaRepo.findById(media.getId()).isPresent();
           if(!isDeleted){
               MediaUtil.delete(media.getPath());
           }
       }

    }

    private void deleteMediaFromDatabase(String path){
        mediaRepo.deleteByPath(path);
    }

    private void deleteMediaFromDatabase(List<String> path){
        for(String item:path){
            log.info("{}",path);
            mediaRepo.deleteByPath(item);
        }
    }

    public void delete(String path)throws IOException {
        deleteMediaFromDatabase(path);
        MediaUtil.delete(path);

    }

    public void deleteAll(List<String> media) throws IOException{
       deleteMediaFromDatabase(media);
        MediaUtil.delete(media);

    }
}


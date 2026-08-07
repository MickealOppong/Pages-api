package com.pages.util;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class MediaStorageLocation {

    public String getLocation(){
        return System.getProperty("user.home")+ "/App-media-dir";
    }

}

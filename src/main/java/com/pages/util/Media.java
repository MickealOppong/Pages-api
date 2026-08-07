package com.pages.util;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Media {
    @Id
    @GeneratedValue
    private Long id;
    private String fileName;
    private String contentType;
    private String path;
    private String mediaOrientation;


}

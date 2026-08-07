package com.pages.model;

import com.pages.util.LogEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Getter
@Setter
@Table
@AllArgsConstructor
@NoArgsConstructor
public class AppUserRole extends LogEntity {

    @Id @GeneratedValue
    private Long roleId;
    private String role;

    public AppUserRole(String role) {
        this.role = role;
    }
}

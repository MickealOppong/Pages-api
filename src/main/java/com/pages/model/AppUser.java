package com.pages.model;

import com.fasterxml.jackson.annotation.JsonDeserializeAs;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pages.util.LogEntity;
import com.pages.util.Media;
import com.pages.util.Notification;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.swing.text.View;
import java.sql.Ref;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@EqualsAndHashCode(
        callSuper = false,
        onlyExplicitlyIncluded = true
)
@Entity
@Table
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AppUser extends LogEntity implements UserDetails {

    @Id@GeneratedValue
    @Column(name = "userId")
    private Long id;

    private String firstName;
    private String lastName;
    private String username;

    private String gender;
    private LocalDate date_of_birth;

    private String password;

    private String city;
    private String country;
    private String preference;

    private boolean isTermsChecked;
    //dating information
    private String drinking;
    private String pets;
    private String exercises;
    private String smoking;
    private String profession;
    private String education;
    private String language;
    private String height;

    private String lookingFor;
    @Column(length = 1024)
    private String aboutMe;
    @Column(length = 1024)
    private String aboutThem;
    //account status
    private boolean isEnabled;
    private boolean isAccountNonExpired;
    private boolean isAccountNonLocked;
    private boolean isCredentialsNonExpired;

    private boolean hideMyAge;



    private Instant lastActive;

    //User role
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "app_user_roles", // The name of your join table
            joinColumns = @JoinColumn(name = "user_id"), // Foreign key for AppUser
            inverseJoinColumns = @JoinColumn(name = "role_id") // Foreign key for AppUserRole
    )
    private Set<AppUserRole> userRole = new HashSet<>();


    //profile picture
    @OneToOne
    private Media media;

    @OneToMany(mappedBy = "appUser",cascade = CascadeType.ALL)
    private List<RefreshToken> tokens = new ArrayList<>();




    public AppUser(String firstName,String lastName,String username, String gender,LocalDate dob,String password,String location,boolean isTermsChecked){
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.date_of_birth = dob;
        this.password = password;
        this.gender = gender;
        this.preference = getDefaultPreference(gender);
        this.isAccountNonExpired =true;
        this.lookingFor = getDefaultLookingFor();
        this.isEnabled = true;
        this.isAccountNonLocked = true;
        this.isCredentialsNonExpired = true;
        this.city = location;
        this.lastActive = Instant.now();
        this.isTermsChecked = isTermsChecked;
        this.country = getDefaultCountry();
    }


    public AppUser(String firstName,String lastName,String username, String gender,LocalDate date_of_birth,String password,Set<AppUserRole> role,String location){
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.gender = gender;
        this.city = location;
        this.date_of_birth = date_of_birth;
        this.userRole = role;
        this.isAccountNonExpired =true;
        this.isEnabled = true;
        this.isAccountNonLocked = true;
        this.isCredentialsNonExpired = true;
        this.lastActive = Instant.now();
        this.country = getDefaultCountry();
    }

    public String getDefaultPreference(String gender){
        String myPref = null;
        if(gender.equalsIgnoreCase("male")){
            myPref = "Female";
        }
        if(gender.equalsIgnoreCase("female")){
            myPref = "Male";
        }
        if(gender.equalsIgnoreCase("non-binary")){
            myPref = "Non-binary";
        }

        return myPref;
    }

    public String getDefaultLookingFor(){
        return "Long-term Relationship";
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for(AppUserRole role: userRole){
            authorities.add(new SimpleGrantedAuthority(role.getRole()));
        }
        return authorities;
    }

    @Override
    public @Nullable String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.isAccountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.isAccountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.isCredentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return this.isEnabled;
    }


    public String getDefaultCountry(){
        return this.country = "Poland";
    }


}

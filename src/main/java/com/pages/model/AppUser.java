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

    @Column(nullable = false, unique = true)
    private String username;

    private String gender;
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    private String password;

    private String city;
    private String country;
    private String countryCode;
    private String preference;

    @Builder.Default
    private boolean isTermsChecked =false;
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
    @Column(nullable = false)
    @Builder.Default
    private boolean enabled =true;

    @Column(nullable = false)
    @Builder.Default
    private boolean accountNonExpired =true;
    @Column(nullable = false)
    @Builder.Default
    private boolean accountNonLocked =true;
    @Column(nullable = false)
    @Builder.Default
    private boolean credentialsNonExpired =true;

    @Column(nullable = false)
    @Builder.Default
    private boolean hideMyAge = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean rulesAccepted = false;


    private String planningStyle;
    private String socialEnergy;
    private String chronoType;

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




    public AppUser(String firstName,String lastName,String username, String gender,LocalDate dob,String password,String city,String country,String countryCode,boolean isTermsChecked){
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.dateOfBirth = dob;
        this.password = password;
        this.gender = gender;
        this.preference = getDefaultPreference(gender);
        this.lookingFor = getDefaultLookingFor();
        this.city = city;
        this.lastActive = Instant.now();
        this.isTermsChecked = isTermsChecked;
        this.country = country;
        this.countryCode = countryCode;
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
        return this.accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }


    public String getDefaultCountry(){
        return this.country = "Poland";
    }


}

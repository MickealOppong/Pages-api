package com.pages.model;

import com.pages.util.LogEntity;
import com.pages.util.Media;
import jakarta.persistence.*;
import lombok.*;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@EqualsAndHashCode(
        callSuper = false,
        onlyExplicitlyIncluded = true
)
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser extends LogEntity implements UserDetails {

    // =========================================================
    // IDENTITY
    // =========================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userId")
    @EqualsAndHashCode.Include
    private Long id;

    private String firstName;

    private String lastName;

    @Column(nullable = false, unique = true)
    private String username;

    private String gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    private String password;


    // =========================================================
    // LOCATION / PREFERENCES
    // =========================================================

    private String city;

    private String country;

    private String preference;


    // =========================================================
    // ACCOUNT / CONSENT
    // =========================================================

    @Column(nullable = false)
    @Builder.Default
    private boolean termsChecked = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean rulesAccepted = false;



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


    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean accountNonExpired = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean accountNonLocked = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean credentialsNonExpired = true;


    // =========================================================
    // PRIVACY
    // =========================================================

    @Column(nullable = false)
    @Builder.Default
    private boolean hideMyAge = false;


    // =========================================================
    // ACTIVITY
    // =========================================================

    @Column(nullable = false)
    @Builder.Default
    private Instant lastActive = Instant.now();


    // =========================================================
    // RELATIONSHIPS
    // =========================================================

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "app_user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<AppUserRole> userRole = new HashSet<>();


    // Profile picture
    @OneToOne
    private Media media;


    // Refresh tokens
    @OneToMany(
            mappedBy = "appUser",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<RefreshToken> tokens = new ArrayList<>();


    // =========================================================
    // REGISTRATION CONSTRUCTOR
    // =========================================================

    public AppUser(
            String firstName,
            String lastName,
            String username,
            String gender,
            LocalDate dateOfBirth,
            String password,
            String city,
            boolean termsChecked
    ) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.password = password;
        this.city = city;

        this.country = "Poland";

        this.preference = getDefaultPreference(gender);

        this.lookingFor = getDefaultLookingFor();

        this.termsChecked = termsChecked;

        /*
         * Account defaults
         */
        this.enabled = true;
        this.accountNonExpired = true;
        this.accountNonLocked = true;
        this.credentialsNonExpired = true;

        /*
         * Consent defaults
         */
        this.rulesAccepted = false;

        /*
         * Privacy defaults
         */
        this.hideMyAge = false;

        /*
         * Activity
         */
        this.lastActive = Instant.now();
    }


    // =========================================================
    // DEFAULT VALUES
    // =========================================================

    private String getDefaultPreference(String gender) {

        if (gender == null) {
            return null;
        }

        return switch (gender.trim().toLowerCase()) {

            case "male" -> "Female";

            case "female" -> "Male";

            case "non-binary" -> "Non-binary";

            default -> null;
        };
    }


    private String getDefaultLookingFor() {
        return "Long-term Relationship";
    }


    // =========================================================
    // SPRING SECURITY
    // =========================================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        for (AppUserRole role : userRole) {

            authorities.add(
                    new SimpleGrantedAuthority(role.getRole())
            );
        }

        return authorities;
    }


    @Override
    public String getPassword() {
        return password;
    }


    @Override
    public String getUsername() {
        return username;
    }


    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }


    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }


    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }


    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
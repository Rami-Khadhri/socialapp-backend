package projetvue.springboot_backend.model;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

@Document(collection = "users")
public class User implements UserDetails {

    @Getter
    @Id
    private String id;
    private String username;

    @Getter
    private String email;
    private String password;
    @Getter
    private String role;
    private boolean enabled;
    @Setter
    @Getter
    private boolean verified = false;
    @Getter
    @Setter
    private String verificationToken;
@Getter
@Setter
    private Binary photo;
    @Setter
    @Getter
    private boolean isGoogleUser;



    private List<String> authorities = new ArrayList<>();

    private String photoUrl; // Add this field
    public User(String username, String email, String password, String role, boolean enabled, boolean verified, String verificationToken, List<String> authorities, Binary photo, String photoUrl) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.enabled = enabled;
        this.verified = verified;
        this.verificationToken = verificationToken;
        this.authorities = authorities;
        this.photo = photo;
        this.photoUrl = photoUrl;
    }

    public User() {
    }

    // Getters and Setters

    public void setPhoto(Binary photo) {
        this.photo = photo;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
    // Existing constructors and basic getters/setters remain the same...

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authoritiesList = new ArrayList<>();
        if (role != null) {
            authoritiesList.add(new SimpleGrantedAuthority(role));
        }
        if (authorities != null) {
            authorities.stream()
                    .map(SimpleGrantedAuthority::new)
                    .forEach(authoritiesList::add);
        }
        return authoritiesList;
    }

    public Binary getPhoto() {
        return photo;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setAuthorities(List<String> authorities) {
        this.authorities = authorities;
    }


    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * @return
     */
    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
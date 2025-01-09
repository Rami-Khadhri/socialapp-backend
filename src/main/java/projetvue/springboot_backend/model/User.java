package projetvue.springboot_backend.model;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

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

    @DBRef(lazy = true)
    @JsonIdentityReference(alwaysAsId = true)
    @Getter
    @Setter
    private List<String> friendIds = new ArrayList<>();

    @DBRef(lazy = true)
    @JsonIdentityReference(alwaysAsId = true)
    @Getter
    @Setter
    private List<String> sentFriendRequests = new ArrayList<>();

    @DBRef(lazy = true)
    @JsonIdentityReference(alwaysAsId = true)
    @Getter
    @Setter
    private List<String> receivedFriendRequests = new ArrayList<>();
    private List<String> authorities = new ArrayList<>();

    private String photoUrl; // Add this field

    private String coverPhoto;



    public User() {
    }

    public User(String id, String username, String email, String password, String role, boolean enabled, boolean verified, String verificationToken, Binary photo, boolean isGoogleUser, List<String> friendIds, List<String> sentFriendRequests, List<String> receivedFriendRequests, List<String> authorities, String photoUrl, String coverPhoto) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.enabled = enabled;
        this.verified = verified;
        this.verificationToken = verificationToken;
        this.photo = photo;
        this.isGoogleUser = isGoogleUser;
        this.friendIds = friendIds;
        this.sentFriendRequests = sentFriendRequests;
        this.receivedFriendRequests = receivedFriendRequests;
        this.authorities = authorities;
        this.photoUrl = photoUrl;
        this.coverPhoto = coverPhoto;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getVerificationToken() {
        return verificationToken;
    }

    public void setVerificationToken(String verificationToken) {
        this.verificationToken = verificationToken;
    }

    public Binary getPhoto() {
        return photo;
    }

    public void setPhoto(Binary photo) {
        this.photo = photo;
    }

    public boolean isGoogleUser() {
        return isGoogleUser;
    }

    public void setGoogleUser(boolean googleUser) {
        isGoogleUser = googleUser;
    }

    public List<String> getFriendIds() {
        return friendIds;
    }

    public void setFriendIds(List<String> friendIds) {
        this.friendIds = friendIds;
    }

    public List<String> getSentFriendRequests() {
        return sentFriendRequests;
    }

    public void setSentFriendRequests(List<String> sentFriendRequests) {
        this.sentFriendRequests = sentFriendRequests;
    }

    public List<String> getReceivedFriendRequests() {
        return receivedFriendRequests;
    }

    public void setReceivedFriendRequests(List<String> receivedFriendRequests) {
        this.receivedFriendRequests = receivedFriendRequests;
    }

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

    public void setAuthorities(List<String> authorities) {
        this.authorities = authorities;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getCoverPhoto() {
        return coverPhoto;
    }

    public void setCoverPhoto(String coverPhoto) {
        this.coverPhoto = coverPhoto;
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
package projetvue.springboot_backend.dto;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import lombok.*;
import org.bson.types.Binary;
import org.springframework.data.mongodb.core.mapping.DBRef;
import projetvue.springboot_backend.model.User;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String id;
    private String token;
    private String username;
    private String email;
    private String role;
    private boolean verified;
    private boolean enabled;
    private Binary photo;
    private String photoUrl;
    private boolean isGoogleUser;
    private List<String> friendIds = new ArrayList<>();
    private List<String> sentFriendRequests = new ArrayList<>();
    private List<String> receivedFriendRequests = new ArrayList<>();
    private String coverPhoto;
}

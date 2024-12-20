package projetvue.springboot_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.Binary;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String username;
    private String email;
    private String role;
    private boolean verified;   // Added verified field
    private boolean enabled;
    private Binary photo; // Base64 photo string
    private String photoUrl;
    private boolean isGoogleUser;

}

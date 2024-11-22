package projetvue.springboot_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private boolean enabled;    // Added enabled field
    private String photoUrl;
    private byte[] photo;
}

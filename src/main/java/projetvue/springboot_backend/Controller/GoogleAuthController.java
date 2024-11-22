package projetvue.springboot_backend.Controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import projetvue.springboot_backend.Repository.UserRepository;
import projetvue.springboot_backend.Security.JwtService;
import projetvue.springboot_backend.Service.UserService;
import projetvue.springboot_backend.dto.AuthResponse;
import projetvue.springboot_backend.model.User;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8080")
public class GoogleAuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final UserService userService;
    private static final String CLIENT_ID = "715234715602-o90t6moc905m83pteand460cqctmi9i9.apps.googleusercontent.com";
    private static final String GOOGLE_TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo";
    @PostMapping("/google")
    public ResponseEntity<?> googleAuth(@RequestBody Map<String, String> body) {
        try {
            String idToken = body.get("credential");
            if (idToken == null || idToken.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "ID token is missing"));
            }

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    new GsonFactory())
                    .setAudience(Collections.singletonList(CLIENT_ID))
                    .build();

            GoogleIdToken googleIdToken = verifier.verify(idToken);
            if (googleIdToken == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid Google ID token"));
            }

            GoogleIdToken.Payload payload = googleIdToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String photoUrl = (String) payload.get("picture"); // Extract the profile picture URL

            // Find or create the user
            User user = userService.findOrCreateUserByEmail(email, name);

            // Update the user's profile photo if it exists
            if (photoUrl != null && !photoUrl.isEmpty()) {
                user.setPhotoUrl(photoUrl);
                userRepository.save(user); // Save the updated user
            }

            // Generate JWT token
            String token = jwtService.generateToken(user);

            // Return the token and user details
            AuthResponse response = AuthResponse.builder()
                    .token(token)
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .photoUrl(user.getPhotoUrl()) // Include the photo URL in the response
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "An error occurred: " + e.getMessage()));
        }
    }


    private Map<String, String> verifyGoogleToken(String idToken) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = GOOGLE_TOKEN_INFO_URL + "?id_token=" + idToken;

            // Get the response from Google's token info endpoint
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            // Check if the response is valid and the client ID matches
            if (response != null && CLIENT_ID.equals(response.get("aud"))) {
                // Extract user information from the response
                Map<String, String> userInfo = new HashMap<>();
                userInfo.put("email", (String) response.get("email"));
                userInfo.put("name", (String) response.get("name"));
                userInfo.put("photoUrl", (String) response.get("picture")); // Add photo URL from Google's response

                return userInfo;  // Return the user info including the photo URL
            }
        } catch (Exception e) {
            e.printStackTrace();  // Log the error for debugging
        }
        return null;  // Return null if something goes wrong
    }

}
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

import java.util.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8080")
public class GoogleAuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final UserService userService;
    private static final String CLIENT_ID ="32864941396-ompl4sjmaotscebv5jreaol07ts15jtl.apps.googleusercontent.com";
    private static final String GOOGLE_TOKEN_INFO_URL ="https://oauth2.googleapis.com/tokeninfo";



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

            // Check if user exists
            Optional<User> existingUser = userRepository.findByEmail(email);

            if (existingUser.isPresent()) {
                User user = existingUser.get();
                // If user exists but is not a Google user, return error
                if (!user.isGoogleUser()) {
                    return ResponseEntity.badRequest().body(Map.of("error",
                            "An account with this email already exists. Please use regular login."));
                }

                // Update existing Google user's information
                String photoUrl = (String) payload.get("picture");
                if (photoUrl != null && !photoUrl.isEmpty()) {
                    user.setPhotoUrl(photoUrl);
                    userRepository.save(user);
                }

                // Generate JWT token for existing user
                String token = jwtService.generateToken(user);
                return ResponseEntity.ok(createAuthResponse(user, token));
            }

            // Create new user if doesn't exist
            String name = (String) payload.get("name");
            String photoUrl = (String) payload.get("picture");

            // Append random number to username
            Random random = new Random();
            int randomValue = 100 + random.nextInt(900);
            String newUsername = name + randomValue;

            // Create new Google user
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setUsername(newUsername);
            newUser.setPhotoUrl(photoUrl);
            newUser.setGoogleUser(true);  // Set as Google user
            newUser.setVerified(true);    // Google users are automatically verified
            newUser.setRole("USER");      // Set default role

            userRepository.save(newUser);

            // Generate JWT token for new user
            String token = jwtService.generateToken(newUser);
            return ResponseEntity.ok(createAuthResponse(newUser, token));

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "An error occurred: " + e.getMessage()));
        }
    }

    private AuthResponse createAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .photoUrl(user.getPhotoUrl())
                .build();
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
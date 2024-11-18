package projetvue.springboot_backend.Controller;

import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import projetvue.springboot_backend.Repository.UserRepository;
import projetvue.springboot_backend.Service.UserService;
import projetvue.springboot_backend.dto.*;
import projetvue.springboot_backend.model.User;
import projetvue.springboot_backend.Security.JwtService;
import projetvue.springboot_backend.Service.EmailService;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8080")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final UserService userService;




    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        // Check if username already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));
        }

        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
        }

        // Generate verification token
        String verificationToken = UUID.randomUUID().toString();

        // Create and save the new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setVerified(false);
        user.setEnabled(true);
        user.setRole("ROLE_USER");
        user.setVerificationToken(verificationToken);
        user.setAuthorities(Collections.singletonList("ROLE_USER"));

        userRepository.save(user);

        // Send verification email
        emailService.sendVerificationEmail(user.getEmail(), verificationToken);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Registration successful. Please check your email to verify your account."));
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody AuthRequest request) {
        try {
            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            // Set authentication context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Retrieve the authenticated user
            User user = (User) authentication.getPrincipal();

            // Check if the account is verified
            if (!user.isVerified()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Account not verified. Please check your email."));
            }

            // Generate JWT token
            String token = jwtService.generateToken(user);

            // Build the response
            AuthResponse response = AuthResponse.builder()
                    .token(token)
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password"));
        }
    }

    @GetMapping("/verify-token")
    public ResponseEntity<?> verifyToken(@RequestHeader("Authorization") String token) {
        // Check if the Authorization header is present and starts with "Bearer"
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Authorization token is missing or invalid"));
        }

        try {
            // Extract the token without the "Bearer " prefix
            token = token.substring(7);

            // Extract the username from the token
            String username = jwtService.extractUsername(token);

            // Find the user from the database
            Optional<User> optionalUser = userRepository.findByUsername(username);
            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User not found"));
            }

            User user = optionalUser.get();

            // Check if the token is valid
            if (!jwtService.isTokenValid(token, user)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid or expired token"));
            }

            // Build the response with the user's details
            AuthResponse response = AuthResponse.builder()
                    .token(token)
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .verified(user.isVerified())  // Include the verified status
                    .enabled(user.isEnabled())   // Include the enabled status
                    .build();

            return ResponseEntity.ok(response);

        } catch (JwtException e) {
            // Specific error for JWT issues
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid JWT token"));
        } catch (Exception e) {
            // Generic error handling for other issues
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyAccount(@RequestParam String token) {
        try {
            User user = userRepository.findByVerificationToken(token)
                    .orElseThrow(() -> new RuntimeException("Invalid verification token"));

            user.setVerified(true);
            user.setVerificationToken(null);
            userRepository.save(user);

            // HTML confirmation message with auto-redirect
            String htmlResponse = """
            <html>
                <body style="text-align: center; font-family: Arial, sans-serif; margin-top: 50px;">
                    <h1>Account Verified Successfully!</h1>
                    <p>You will be redirected to your profile page shortly...</p>
                    <script>
                        setTimeout(() => {
                            window.location.href = "http://localhost:8080/profile";
                        }, 3000);
                    </script>
                </body>
            </html>
        """;

            return ResponseEntity.ok()
                    .header("Content-Type", "text/html")
                    .body(htmlResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetRequest passwordResetRequest) {
        try {
            // Retrieve the email from the request body
            String email = passwordResetRequest.getEmail();

            // Ensure the email is provided
            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Email is required"));
            }

            // Retrieve user by email
            User user = userService.findByEmail(email);  // Ensure your userService has the correct method

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse("User not found with provided email"));
            }

            // Validate old password
            if (!passwordEncoder.matches(passwordResetRequest.getOldPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("Old password is incorrect"));
            }

            // Validate new password and confirm password
            if (!passwordResetRequest.getNewPassword().equals(passwordResetRequest.getConfirmPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("New password and confirm password do not match"));
            }

            // Encode the new password
            String encodedPassword = passwordEncoder.encode(passwordResetRequest.getNewPassword());
            user.setPassword(encodedPassword);

            // Save the user with the updated password
            userRepository.save(user);

            // Return success response
            return ResponseEntity.ok(new SuccessResponse("Password reset successfully"));

        } catch (Exception e) {
            // Log the exception for debugging purposes
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An unexpected error occurred"));
        }
    }




    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Inform the client to remove the JWT token
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}

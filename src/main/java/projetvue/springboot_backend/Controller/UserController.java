package projetvue.springboot_backend.Controller;

import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import projetvue.springboot_backend.Repository.UserRepository;
import projetvue.springboot_backend.Service.UserService;
import projetvue.springboot_backend.dto.UserDTO;
import projetvue.springboot_backend.model.User;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    @Autowired

    public UserController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<User>> getActiveUsers() {
        List<User> activeUsers = userRepository.findAll(); // Assuming 'isOnline' is a boolean indicating active users
        return ResponseEntity.ok(activeUsers);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User createdUser = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody User updatedUser) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    existingUser.setUsername(updatedUser.getUsername());
                    existingUser.setEmail(updatedUser.getEmail());
                    existingUser.setRole(updatedUser.getRole());
                    existingUser.setEnabled(updatedUser.isEnabled());
                    existingUser.setVerified(updatedUser.isVerified());
                    existingUser.setSentFriendRequests(updatedUser.getSentFriendRequests());
                    existingUser.setFriendIds(updatedUser.getFriendIds());
                    existingUser.setReceivedFriendRequests(updatedUser.getReceivedFriendRequests());
                    existingUser.setCoverPhoto(updatedUser.getCoverPhoto());

                    User savedUser = userRepository.save(existingUser);
                    return ResponseEntity.ok(savedUser);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/upload-photo")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> uploadPhoto(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            validatePhotoFile(file);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Validate file size and type
            byte[] photoBytes = file.getBytes();

            // Limit photo size to 5MB
            if (photoBytes.length > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest().body("File size must be less than 5MB");
            }

            user.setPhoto(new Binary(photoBytes));
            userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                    "message", "Photo uploaded successfully",
                    "photo", Base64.getEncoder().encodeToString(photoBytes)
            ));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload photo: " + e.getMessage());
        }
    }

    private void validatePhotoFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file uploaded");
        }

        String[] ALLOWED_MIME_TYPES = {"image/jpeg", "image/png", "image/gif"};
        String contentType = file.getContentType();

        boolean isValidType = false;
        for (String allowedType : ALLOWED_MIME_TYPES) {
            if (allowedType.equals(contentType)) {
                isValidType = true;
                break;
            }
        }

        if (!isValidType) {
            throw new IllegalArgumentException("Only JPEG, PNG, and GIF files are allowed");
        }
    }
    // Method to search users by username
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<User>> searchUsers(@RequestParam("q") String query) {
        List<User> users = userRepository.findByUsernameContainingIgnoreCase(query);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/photo")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getCurrentUserPhoto() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (user.getPhoto() == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No photo found for user");
            }

            String base64Photo = Base64.getEncoder().encodeToString(user.getPhoto().getData());
            return ResponseEntity.ok(Map.of("photo", base64Photo));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve photo: " + e.getMessage());
        }
    }
    @GetMapping("/photo/{username}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getUserPhotoByUsername(@PathVariable String username) {
        try {
            Optional<User> userOptional = userRepository.findByUsername(username);

            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }

            User user = userOptional.get();

            // Check if user has a photo
            if (user.getPhoto() == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No photo found for user");
            }

            // For binary photos, convert to Base64
            if (user.getPhoto() instanceof Binary) {
                Binary photoBinary = user.getPhoto();
                String base64Photo = Base64.getEncoder().encodeToString(photoBinary.getData());
                return ResponseEntity.ok(Map.of("photo", base64Photo));
            }

            // If photo is already a string (e.g., URL), return as is
            return ResponseEntity.ok(Map.of("photo", user.getPhoto().toString()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve photo: " + e.getMessage());
        }
    }
    @PostMapping("/friendDetails")
    public ResponseEntity<?> getFriendDetails(@RequestBody List<String> friendIds) {
        try {
            List<User> friends = userService.getUsersByIds(friendIds);
            return ResponseEntity.ok(friends);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching friend details: " + e.getMessage());
        }
    }
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/userprofile/{id}")
    public ResponseEntity<?> getUserProfile(@PathVariable String id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Filter sensitive information
            UserDTO userDTO = new UserDTO(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole(),
                    user.getPhotoUrl(),
                    user.getCoverPhoto(),
                    user.getFriendIds(),
                    user.getSentFriendRequests(),
                    user.getReceivedFriendRequests()

            );
            return ResponseEntity.ok(userDTO);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    }

}
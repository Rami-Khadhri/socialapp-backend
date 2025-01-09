package projetvue.springboot_backend.Service;

import org.bson.types.Binary;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import projetvue.springboot_backend.model.User;
import projetvue.springboot_backend.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;


    public User getUserByUsername(String username) {
        return userRepository.getUserByUsername(username);
    }
    public User createUser(User user) {
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }


    public User updateUser(String id, User updatedUser) {
        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setUsername(updatedUser.getUsername());
            user.setEmail(updatedUser.getEmail());
            user.setPassword(updatedUser.getPassword());
            return userRepository.save(user);
        } else {
            return null;
        }
    }
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found by email"));
    }
    public Optional<User> findUserById(String id) {
        return userRepository.findById(id);
    }
    // Check if the password is correct
    public boolean checkPassword(User user, String oldPassword) {
        return passwordEncoder.matches(oldPassword, user.getPassword());  // Use PasswordEncoder for checking password
    }

    // Update user password
    public boolean updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));  // Encode new password
        userRepository.save(user);
        return true;
    }
    public boolean resetPassword(String oldPassword, String newPassword, String confirmPassword) {
        // Validate new password and confirm password
        if (!newPassword.equals(confirmPassword)) {
            return false; // New password doesn't match confirm password
        }

        // Get the logged-in user's ID from the SecurityContext (JWT Authentication)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName(); // The user ID is stored in the JWT token

        // Fetch the user by their ObjectId (userId)
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false; // User not found
        }

        // Check if the old password matches the current password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false; // Old password doesn't match
        }

        // Update the password (ensure it's hashed before saving)
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true; // Password reset successful
    }
    public User findOrCreateUserByEmail(String email, String username) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setUsername(username != null
                            ? username.replaceAll("\\s+", "_").toLowerCase()
                            : email.split("@")[0]);
                    newUser.setVerified(true);
                    newUser.setEnabled(true);
                    newUser.setGoogleUser(true);
                    newUser.setRole("ROLE_USER");
                    newUser.setAuthorities(Collections.singletonList("ROLE_USER"));
                    newUser.setReceivedFriendRequests(null);
                    newUser.setSentFriendRequests(null);
                    newUser.setFriendIds(null);
                    return userRepository.save(newUser);
                });
    }


    public List<User> getUsersByIds(List<String> userIds) {
        return userRepository.findAllById(userIds);
    }
    // Delete a user by ID
    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }
}

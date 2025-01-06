package projetvue.springboot_backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import projetvue.springboot_backend.Service.NotificationService;
import projetvue.springboot_backend.Service.UserService;
import projetvue.springboot_backend.model.Notification;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:8080")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService; // Inject UserService

    @GetMapping
    public ResponseEntity<?> getNotifications() {
        try {
            // Get the current authenticated user's ID
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            String userId = userService.getUserByUsername(username).getId(); // Add this method to UserService

            if (userId == null) {
                return ResponseEntity.badRequest().body("User not authenticated");
            }

            List<Notification> notifications = notificationService.getUnreadNotifications(userId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching notifications: " + e.getMessage());
        }
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(@PathVariable String notificationId) {
        try {
            notificationService.markNotificationAsRead(notificationId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error marking notification as read: " + e.getMessage());
        }
    }
}


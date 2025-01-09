package projetvue.springboot_backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projetvue.springboot_backend.Service.FriendService;
import projetvue.springboot_backend.model.User;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friends")
@CrossOrigin(origins = "http://localhost:8080")
public class FriendController {

    @Autowired
    private FriendService friendService;

    @PostMapping("/send")
    public ResponseEntity<?> sendFriendRequest(
            @RequestParam String senderId,
            @RequestParam String receiverId) {
        try {
            boolean success = friendService.sendFriendRequest(senderId, receiverId);
            if (success) {
                return ResponseEntity.ok()
                        .body(Map.of(
                                "message", "Friend request sent successfully",
                                "success", true
                        ));
            }
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message", "Friend request could not be sent. Request may already exist.",
                            "success", false
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "An error occurred: " + e.getMessage(),
                            "success", false
                    ));
        }
    }

    @PostMapping("/accept")
    public ResponseEntity<?> acceptFriendRequest(
            @RequestParam String userId,
            @RequestParam String senderId) {
        try {
            boolean success = friendService.acceptFriendRequest(userId, senderId);
            if (success) {
                return ResponseEntity.ok()
                        .body(Map.of(
                                "message", "Friend request accepted successfully",
                                "success", true
                        ));
            }
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message", "Could not accept friend request",
                            "success", false
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "An error occurred: " + e.getMessage(),
                            "success", false
                    ));
        }
    }

    @PostMapping("/decline")
    public ResponseEntity<?> declineFriendRequest(
            @RequestParam String userId,
            @RequestParam String senderId) {
        try {
            boolean success = friendService.declineFriendRequest(userId, senderId);
            if (success) {
                return ResponseEntity.ok()
                        .body(Map.of(
                                "message", "Friend request declined successfully",
                                "success", true
                        ));
            }
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message", "Could not decline friend request",
                            "success", false
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "An error occurred: " + e.getMessage(),
                            "success", false
                    ));
        }
    }

    @GetMapping("/requests/sent/{userId}")
    public ResponseEntity<?> getSentFriendRequests(@PathVariable String userId) {
        try {
            List<User> sentRequests = friendService.getSentFriendRequests(userId);
            return ResponseEntity.ok(sentRequests);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Error fetching sent friend requests: " + e.getMessage(),
                            "success", false
                    ));
        }
    }

    @GetMapping("/requests/received/{userId}")
    public ResponseEntity<?> getReceivedFriendRequests(@PathVariable String userId) {
        try {
            List<User> receivedRequests = friendService.getReceivedFriendRequests(userId);
            return ResponseEntity.ok(receivedRequests);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Error fetching received friend requests: " + e.getMessage(),
                            "success", false
                    ));
        }
    }

    @GetMapping("/list/{userId}")
    public ResponseEntity<?> getFriendsList(@PathVariable String userId) {
        try {
            List<User> friends = friendService.getFriendsList(userId);
            return ResponseEntity.ok(friends);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "Error fetching friends list: " + e.getMessage(),
                            "success", false
                    ));
        }
    }

    @PostMapping("/remove")
    public ResponseEntity<?> removeFriend(
            @RequestParam String userId,
            @RequestParam String friendId) {
        try {
            boolean success = friendService.removeFriend(userId, friendId);
            if (success) {
                return ResponseEntity.ok()
                        .body(Map.of(
                                "message", "Friend removed successfully",
                                "success", true
                        ));
            }
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message", "Could not remove friend",
                            "success", false
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "An error occurred: " + e.getMessage(),
                            "success", false
                    ));
        }
    }

    @PostMapping("/cancel")
    public ResponseEntity<?> cancelFriendRequest(
            @RequestParam String senderId,
            @RequestParam String receiverId) {
        try {
            boolean success = friendService.cancelFriendRequest(senderId, receiverId);
            if (success) {
                return ResponseEntity.ok()
                        .body(Map.of(
                                "message", "Friend request canceled successfully",
                                "success", true
                        ));
            }
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message", "Could not cancel friend request",
                            "success", false
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "An error occurred: " + e.getMessage(),
                            "success", false
                    ));
        }
    }
}
package projetvue.springboot_backend.Controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import projetvue.springboot_backend.Repository.CommentRepository;
import projetvue.springboot_backend.Service.NotificationService;
import projetvue.springboot_backend.dto.CommentDTO;
import projetvue.springboot_backend.dto.PostDTO;
import projetvue.springboot_backend.model.Comment;
import projetvue.springboot_backend.model.Poll;
import projetvue.springboot_backend.model.Post;
import projetvue.springboot_backend.model.User;
import projetvue.springboot_backend.Repository.PostRepository;
import projetvue.springboot_backend.Repository.UserRepository;
import projetvue.springboot_backend.Service.FileStorageService;
import projetvue.springboot_backend.Service.PostService;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "http://localhost:8080")
public class PostController {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostService postService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private NotificationService notificationService;
    @PostMapping("/create")
    public ResponseEntity<?> createPost(
            @RequestParam("content") String content,
            @RequestParam(value = "images", required = false) List<MultipartFile> imageFiles,
            @RequestParam(value = "videos", required = false) List<MultipartFile> videoFiles,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "pollData", required = false) String pollJson // Poll as JSON string
    ) {
        try {
            // Parse the Poll object from JSON
            Poll poll = null;
            if (pollJson != null && !pollJson.isEmpty()) {
                ObjectMapper objectMapper = new ObjectMapper();
                poll = objectMapper.readValue(pollJson, Poll.class);
            }

            // Get authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Process files
            List<String> imageUrls = processFiles(imageFiles);
            List<String> videoUrls = processFiles(videoFiles);

            // Create the post
            Post createdPost = postService.createPost(user, content, imageUrls, videoUrls, category, poll);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }


    @PutMapping("/edit/{postId}")
    public ResponseEntity<?> editPost(
            @PathVariable String postId,
            @RequestParam("content") String content,
            @RequestParam(value = "images", required = false) List<MultipartFile> imageFiles,
            @RequestParam(value = "videos", required = false) List<MultipartFile> videoFiles,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "pollData", required = false) String pollJson // Poll as JSON string
    ) {
        try {
            // Parse the Poll object from JSON
            Poll poll = null;
            if (pollJson != null && !pollJson.isEmpty()) {
                ObjectMapper objectMapper = new ObjectMapper();
                poll = objectMapper.readValue(pollJson, Poll.class);
            }

            // Get authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Unauthorized"));

            // Process files
            List<String> imageUrls = processFiles(imageFiles);
            List<String> videoUrls = processFiles(videoFiles);

            // Edit the post
            Post editedPost = postService.editPost(postId, user, content, imageUrls, videoUrls, category, poll);

            // Notify users who liked the post
            List<String> likerIds = postService.getLikerIds(postId); // Get list of user IDs who liked the post
            notificationService.createNotifications(likerIds, postId, username); // Notify these users

            return ResponseEntity.ok(editedPost);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }






    // --- Remove Poll from a Post ---
    @DeleteMapping("/{postId}/poll")
    public ResponseEntity<Post> removePoll(@PathVariable String postId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userOptional.get();

        try {
            Post post = postService.removePollFromPost(postId, user);
            return ResponseEntity.ok(post);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // --- Helper Method to Process Files ---
    private List<String> processFiles(List<MultipartFile> files) throws IOException {
        List<String> fileUrls = new ArrayList<>();
        if (files != null) {
            for (MultipartFile file : files) {
                String fileUrl = fileStorageService.storeFile(file);
                fileUrls.add(fileUrl);
            }
        }
        return fileUrls;
    }

    @GetMapping("/all")
    public ResponseEntity<List<Post>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        List<Post> posts = postRepository.findAll(pageable).getContent();

        posts.forEach(post -> {
            post.setComments(null); // Exclude comments for better performance
        });

        return ResponseEntity.ok(posts);
    }



    @PostMapping("/{postId}/comment")
    public ResponseEntity<Comment> addComment(
            @PathVariable String postId,
            @RequestBody CommentDTO commentDTO
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Comment newComment = postService.addComment(postId, userOptional.get(), commentDTO.getContent());
            return ResponseEntity.status(HttpStatus.CREATED).body(newComment);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDTO> getPostWithComments(@PathVariable String postId) {
        Optional<Post> postOptional = postRepository.findById(postId);
        if (postOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Post post = postOptional.get();
        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtDesc(postId);

        PostDTO postDTO = new PostDTO();
        postDTO.setId(post.getId());
        postDTO.setContent(post.getContent());
        postDTO.setImageUrls(post.getImageUrls());
        postDTO.setVideoUrls(post.getVideoUrls());
        postDTO.setLikeCount(post.getLikeCount());
        postDTO.setCommentCount(post.getCommentCount());
        postDTO.setShareCount(post.getShareCount());
        postDTO.setCreatedAt(post.getCreatedAt());
        postDTO.setComments(comments);

        return ResponseEntity.ok(postDTO);
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<Comment>> getCommentsForPost(
            @PathVariable String postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        List<Comment> comments = commentRepository.findByPostId(postId);
        return ResponseEntity.ok(comments);
    }

    @DeleteMapping("/comments/delete/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable String commentId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Comment> commentOptional = commentRepository.findById(commentId);
        if (commentOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Comment comment = commentOptional.get();
        if (!comment.getUser().getUsername().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Update post's comment count
        Post post = postRepository.findPostById(comment.getPostId());
        post.setCommentCount(post.getCommentCount() - 1);
        postRepository.save(post);

        commentRepository.delete(comment);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable String postId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Post> postOptional = postRepository.findById(postId);
        if (postOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Post post = postOptional.get();

        if (!post.getUser().getUsername().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        commentRepository.deleteAllByPostId(postId);
        postRepository.delete(post);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/comments/delete/all/{postId}")
    @Transactional
    public ResponseEntity<Void> deleteAllComments(@PathVariable String postId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Post> postOptional = postRepository.findById(postId);
        if (postOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Post post = postOptional.get();
        if (!post.getUser().getUsername().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        commentRepository.deleteAllByPostId(postId);
        post.setCommentCount(0);
        postRepository.save(post);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/comments/edit/{commentId}")
    public ResponseEntity<Comment> editComment(
            @PathVariable String commentId,
            @RequestBody CommentDTO commentDTO
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Comment> commentOptional = commentRepository.findById(commentId);
        if (commentOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Comment comment = commentOptional.get();
        if (!comment.getUser().getUsername().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        comment.setContent(commentDTO.getContent());
        comment.setEditedAt(LocalDateTime.now());
        commentRepository.save(comment);

        return ResponseEntity.ok(comment);
    }

    @PutMapping("/{postId}/vote")
    public ResponseEntity<?> voteOnPoll(@PathVariable String postId, @RequestParam("option") String option) {
        try {
            // Get authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            // Get user object
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Unauthorized"));

            // Get the post and poll
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new RuntimeException("Post not found"));

            Poll poll = post.getPoll();
            if (poll == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("This post does not have a poll.");
            }

            // Check if the user has already voted
            if (poll.getVoters().containsKey(user.getId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You have already voted.");
            }

            // Update the poll with the new vote
            poll.getVoters().put(user.getId(), option);
            poll.getVotes().put(option, poll.getVotes().getOrDefault(option, 0) + 1);

            // Save the updated poll and post
            post.setPoll(poll);
            postRepository.save(post);

            return ResponseEntity.ok(poll);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonMap("error", e.getMessage()));
        }
    }
    @GetMapping("/{postId}/poll/{option}/voters")
    public ResponseEntity<List<User>> getVotersForPollOption(@PathVariable String postId, @PathVariable String option) {
        try {
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new RuntimeException("Post not found"));

            Poll poll = post.getPoll();
            if (poll == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.emptyList());
            }

            List<User> voters = poll.getVoters().entrySet().stream()
                    .filter(entry -> option.equals(entry.getValue()))
                    .map(entry -> userRepository.findById(entry.getKey()).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(voters);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @PostMapping("/{postId}/like")
    public ResponseEntity<?> toggleLikePost(@PathVariable String postId, Principal principal) {
        Optional<Post> optionalPost = postRepository.findById(postId);
        if (!optionalPost.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found");
        }

        Post post = optionalPost.get();
        String currentUsername = principal.getName(); // Fetch the logged-in user
        User currentUser = userRepository.findByUsername(currentUsername).orElse(null);

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authorized");
        }

        boolean alreadyLiked = post.getLikedBy().stream()
                .anyMatch(user -> user.getId().equals(currentUser.getId()));

        if (alreadyLiked) {
            // Dislike logic: Remove user and decrement like count
            post.getLikedBy().removeIf(user -> user.getId().equals(currentUser.getId()));
            post.setLikeCount(post.getLikeCount() - 1);
        } else {
            // Like logic: Add user and increment like count
            post.getLikedBy().add(currentUser);
            post.setLikeCount(post.getLikeCount() + 1);
        }

        // Save the post and return updated data
        postRepository.save(post);

        Map<String, Object> response = new HashMap<>();
        response.put("liked", !alreadyLiked); // true if liked, false if disliked
        response.put("likeCount", post.getLikeCount());

        return ResponseEntity.ok(response);
    }


    @GetMapping("/{postId}/likes")
    public ResponseEntity<List<User>> getPostLikes(@PathVariable String postId) {
        // Replace with your service logic to fetch the users who liked the post
        List<User> likedUsers = postService.getLikedUsers(postId);
        return ResponseEntity.ok(likedUsers);
    }
    @GetMapping("/{postId}/liked-by")
    public ResponseEntity<?> getLikedByUsers(@PathVariable String postId) {
        List<Map<String, Object>> likedByUsers = postService.getLikedByUsers(postId);
        return ResponseEntity.ok(likedByUsers);
    }

}
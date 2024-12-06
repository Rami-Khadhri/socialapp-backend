package projetvue.springboot_backend.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import projetvue.springboot_backend.Repository.CommentRepository;
import projetvue.springboot_backend.dto.CommentDTO;
import projetvue.springboot_backend.model.Comment;
import projetvue.springboot_backend.model.Post;
import projetvue.springboot_backend.model.User;
import projetvue.springboot_backend.Repository.PostRepository;
import projetvue.springboot_backend.Repository.UserRepository;
import projetvue.springboot_backend.Service.FileStorageService;
import projetvue.springboot_backend.Service.PostService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    @PostMapping("/create")
    public ResponseEntity<Post> createPost(
            @RequestParam("content") String content,
            @RequestParam(value = "image", required = false) MultipartFile imageFile
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        User user = userOptional.get();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(postService.createPost(user, content, imageFile));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Post>> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        // Ensure only authenticated users can fetch posts
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Debugging: log the page and size values
        System.out.println("Page: " + page + ", Size: " + size);

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            List<Post> posts = postRepository.findAll(pageable).getContent();
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            // Log the error and return a 500 status with a message
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<Map<String, Object>> likePost(@PathVariable String postId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new RuntimeException("Post not found"));
            User currentUser = userOptional.get();

            boolean liked;
            if (post.getLikedBy().contains(currentUser)) {
                // Unlike the post
                post.getLikedBy().remove(currentUser);
                liked = false;
            } else {
                // Like the post only if not already liked
                post.getLikedBy().add(currentUser);
                liked = true;
            }

            post.setLikeCount(post.getLikedBy().size());
            postRepository.save(post);

            Map<String, Object> response = new HashMap<>();
            response.put("liked", liked);
            response.put("likeCount", post.getLikeCount());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/{postId}/comment")
    public ResponseEntity<Comment> addComment(
            @PathVariable String postId,
            @RequestBody CommentDTO commentDTO // Change to @RequestBody
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
    public ResponseEntity<Map<String, Object>> getPostWithComments(@PathVariable String postId) {
        Optional<Post> postOptional = postRepository.findById(postId);
        if (postOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Post post = postOptional.get();

        // Fetch comments manually since @DBRef doesn't automatically populate
        List<Comment> comments = commentRepository.findAllById(post.getComments().stream()
                .map(Comment::getId).toList());

        // Construct a response with full post and comments
        Map<String, Object> response = new HashMap<>();
        response.put("post", post);
        response.put("comments", comments);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<Comment>> getCommentsForPost(@PathVariable String postId) {
        Optional<Post> postOptional = postRepository.findById(postId);
        if (postOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Fetch comments manually
        List<Comment> comments = commentRepository.findAllById(
                postOptional.get().getComments().stream().map(Comment::getId).toList()
        );

        return ResponseEntity.ok(comments);
    }


    @DeleteMapping("/delete/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable String postId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean deleted = postService.deletePost(postId, userOptional.get());
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
}
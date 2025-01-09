package projetvue.springboot_backend.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projetvue.springboot_backend.Controller.ResourceNotFoundException;
import projetvue.springboot_backend.dto.UserDTO;
import projetvue.springboot_backend.model.Comment;
import projetvue.springboot_backend.model.Poll;
import projetvue.springboot_backend.model.Post;
import projetvue.springboot_backend.model.User;
import projetvue.springboot_backend.Repository.CommentRepository;
import projetvue.springboot_backend.Repository.PostRepository;
import projetvue.springboot_backend.Repository.UserRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Post createPost(User user, String content, List<String> imageUrls, List<String> videoUrls,
                           String category, Poll poll) {
        Post post = new Post();
        post.setContent(content);
        post.setUser(user);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setShareCount(0);

        if (imageUrls != null) {
            post.getImageUrls().addAll(imageUrls);
        }

        if (videoUrls != null) {
            post.getVideoUrls().addAll(videoUrls);
        }

        post.setCategory(category);

        if (poll != null) {
            poll.setId(null); // Ensure we create a new poll
            initializePollVotes(poll);
            post.setPoll(poll);
        }

        return postRepository.save(post);
    }

    @Transactional
    public Post editPost(String postId, User user, String newContent, List<String> newImageUrls,
                         List<String> newVideoUrls, String newCategory, Poll newPoll) {
        // Fetch the post by ID
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // Check if the editing user is the owner of the post
        if (!post.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to edit this post");
        }

        // Update content and timestamp
        post.setContent(newContent);
        post.setUpdatedAt(LocalDateTime.now());

        // Update image URLs if provided
        if (newImageUrls != null) {
            post.setImageUrls(newImageUrls); // Directly replace with new URLs
        }

        // Update video URLs if provided
        if (newVideoUrls != null) {
            post.setVideoUrls(newVideoUrls); // Directly replace with new URLs
        }

        // Update category
        post.setCategory(newCategory);

        // Handle poll updates
        if (newPoll != null) {
            newPoll.setId(null); // Reset poll ID to ensure creation of a new instance
            initializePollVotes(newPoll); // Initialize vote data for the new poll
            post.setPoll(newPoll);
        } else {
            post.setPoll(null); // Remove existing poll if no new poll is provided
        }

        // Save the updated post
        return postRepository.save(post);
    }

    @Transactional
    public Post recordPollVote(String postId, User user, String option) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Poll poll = post.getPoll();
        if (poll == null) {
            throw new RuntimeException("This post has no poll");
        }

        if (!poll.getOptions().contains(option)) {
            throw new RuntimeException("Invalid poll option");
        }

        // Initialize votes map if null
        if (poll.getVotes() == null) {
            poll.setVotes(new HashMap<>());
        }

        // Initialize or update voter tracking if null
        if (poll.getVoters() == null) {
            poll.setVoters(new HashMap<>());
        }

        // Check if user has already voted
        String previousVote = poll.getVoters().get(user.getId());
        if (previousVote != null) {
            // Remove previous vote
            poll.getVotes().merge(previousVote, -1, Integer::sum);
        }

        // Record new vote
        poll.getVoters().put(user.getId(), option);
        poll.getVotes().merge(option, 1, Integer::sum);

        return postRepository.save(post);
    }

    private void initializePollVotes(Poll poll) {
        if (poll.getOptions() != null) {
            poll.setVotes(new HashMap<>());
            poll.setVoters(new HashMap<>());
            poll.getOptions().forEach(option -> poll.getVotes().put(option, 0));
        }
    }

    @Transactional
    public Post removePollFromPost(String postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to remove poll from this post");
        }

        post.setPoll(null);
        return postRepository.save(post);
    }

    @Transactional
    public boolean deletePost(String postId, User user) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));

        // Only allow deletion by the post owner
        if (!post.getUser().getId().equals(user.getId())) {
            return false; // Alternatively, throw an UnauthorizedException
        }

        commentRepository.deleteAllByPostId(postId);
        postRepository.delete(post);
        return true;
    }

    @Transactional
    public Comment addComment(String postId, User user, String content) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setUser(user);
        comment.setPostId(postId);
        comment.setCreatedAt(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);

        // Increment the post's comment count
        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);

        return savedComment;
    }

    @Transactional
    public Map<String, Object> likePost(String postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isLiked = post.getLikedBy().contains(user);
        Map<String, Object> response = new HashMap<>();

        if (!isLiked) {
            post.getLikedBy().add(user);
            response.put("liked", true);
        } else {
            post.getLikedBy().remove(user);
            response.put("liked", false);
        }

        post.setLikeCount(post.getLikedBy().size());
        postRepository.save(post);

        response.put("likeCount", post.getLikeCount());
        return response;
    }

    public Map<String, Object> getPostLikeStatus(String postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> response = new HashMap<>();
        response.put("liked", post.getLikedBy().contains(user));
        response.put("likeCount", post.getLikeCount());

        return response;
    }

    public List<User> getLikedUsers(String postId) {
        // Fetch the post by its ID
        Optional<Post> optionalPost = postRepository.findById(postId);

        // If post doesn't exist, throw an exception
        if (optionalPost.isEmpty()) {
            throw new ResourceNotFoundException("Post not found with ID: " + postId);
        }

        // Return the list of users who liked the post
        return optionalPost.get().getLikedBy();
    }

    public List<User> getLikedBy(String postId) {
        Optional<Post> optionalPost = postRepository.findById(postId);

        if (optionalPost.isEmpty()) {
            throw new RuntimeException("Post not found");
        }

        return optionalPost.get().getLikedBy();
    }

    public List<Map<String, Object>> getLikedByUsers(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        return post.getLikedBy().stream()
                .map(user -> {
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("username", user.getUsername());
                    userInfo.put("id", user.getId());
                    // Add other user fields you want to return
                    return userInfo;
                })
                .collect(Collectors.toList());
    }
    public List<String> getLikerIds(String postId) {
        // Fetch the post from the repository
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // Extract and return the user IDs of likers
        return post.getLikedBy().stream()
                .map(User::getId) // Assuming `likes` is a collection of `User` objects
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getUserPostsWithDetails(String userId) {
        // Find the user by ID (you can fetch this from your user repository or use the username)
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        // Get the posts for the user
        List<Post> posts = postRepository.findByUser(user);

        // Map the posts into a list of details for charting
        return posts.stream()
                .map(post -> {
                    Map<String, Object> postDetails = Map.of(
                            "content", post.getContent(),
                            "likeCount", post.getLikeCount(),
                            "commentCount", post.getCommentCount(),
                            "shareCount", post.getShareCount()
                    );
                    return postDetails;
                })
                .collect(Collectors.toList());
    }
}
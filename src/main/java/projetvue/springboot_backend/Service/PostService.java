package projetvue.springboot_backend.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import projetvue.springboot_backend.model.Comment;
import projetvue.springboot_backend.model.Post;
import projetvue.springboot_backend.model.User;
import projetvue.springboot_backend.Repository.CommentRepository;
import projetvue.springboot_backend.Repository.PostRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private FileStorageService fileStorageService;

    public Post createPost(User user, String content, MultipartFile imageFile) {
        Post post = new Post();
        post.setContent(content);
        post.setUser(user);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setShareCount(0);

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String imageUrl = fileStorageService.storeFile(imageFile);
                post.setImageUrl(imageUrl);
            } catch (Exception e) {
                throw new RuntimeException("Failed to store file", e); // Consider using a custom exception
            }
        }

        return postRepository.save(post);
    }

    public Post likePost(String postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (post.getLikedBy().contains(user)) {
            post.getLikedBy().remove(user);
            post.setLikeCount(post.getLikedBy().size());
        } else {
            post.getLikedBy().add(user);
            post.setLikeCount(post.getLikedBy().size());
        }

        return postRepository.save(post);
    }
    public Comment addComment(String postId, User user, String content) {
        // Ensure the post exists
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setUser(user);
        comment.setPostId(postId); // Link to post by ID
        comment.setCreatedAt(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);

        // Increment the post's comment count
        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);

        return savedComment;
    }
    public Post editPost(String postId, User user, String newContent, MultipartFile newImageFile) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // Verify the user is the post owner
        if (!post.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to edit this post");
        }

        // Update content
        post.setContent(newContent);
        post.setUpdatedAt(LocalDateTime.now());

        // Handle image update if provided
        if (newImageFile != null && !newImageFile.isEmpty()) {
            try {
                // Delete old image if exists
                if (post.getImageUrl() != null) {
                    fileStorageService.deleteFile(post.getImageUrl());
                }

                // Store new image
                String newImageUrl = fileStorageService.storeFile(newImageFile);
                post.setImageUrl(newImageUrl);
            } catch (Exception e) {
                throw new RuntimeException("Failed to update post image", e);
            }
        }

        return postRepository.save(post);
    }

    public boolean deletePost(String postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // Only allow deletion by the post owner
        if (!post.getUser().getId().equals(user.getId())) {
            return false; // Alternatively, throw an UnauthorizedException
        }

        // Delete post and its associated comments
        commentRepository.deleteAllByPostId(postId);
        postRepository.delete(post);
        return true;
    }
}

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
            String imageUrl = fileStorageService.storeFile(imageFile);
            post.setImageUrl(imageUrl);
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
            post.setLikeCount(post.getLikeCount() + 1);
        }

        return postRepository.save(post);
    }

    public Comment addComment(String postId, User user, String content) {
        Optional<Post> postOptional = postRepository.findById(postId);
        if (postOptional.isEmpty()) {
            throw new RuntimeException("Post not found");
        }

        Post post = postOptional.get();
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setUser(user);
        comment.setPost(post);
        comment.setCreatedAt(LocalDateTime.now());

        // Save the comment first
        Comment savedComment = commentRepository.save(comment);

        // Add the saved comment to the post's comments list
        post.getComments().add(savedComment);
        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);

        return savedComment;
    }

    public boolean deletePost(String postId, User user) {
        Optional<Post> postOptional = postRepository.findById(postId);
        if (postOptional.isEmpty()) {
            return false;
        }

        Post post = postOptional.get();
        // Only allow deletion by post owner or admin
        if (!post.getUser().getId().equals(user.getId())) {
            return false;
        }

        postRepository.delete(post);
        return true;
    }
}
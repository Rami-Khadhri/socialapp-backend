package projetvue.springboot_backend.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.DBRef;
import projetvue.springboot_backend.model.Comment;
import projetvue.springboot_backend.model.Poll;
import projetvue.springboot_backend.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Getter
public class PostDTO {
    private String id;

    @Setter
    private String content;

    @Setter
    private List<String> imageUrls = new ArrayList<>(); // URLs for images
    @Setter
    private List<String> videoUrls = new ArrayList<>(); // URLs for videos

    @Setter
    private int likeCount;

    @Setter
    private int commentCount;

    @Setter
    private int shareCount;
    @Setter
    private List<User> likedBy = new ArrayList<>(); // List of usernames who liked the post

    @Setter
    private LocalDateTime createdAt;
    @Setter
    private LocalDateTime updatedAt;
    @Setter
    private List<Comment> comments ;
    @Setter
    private UserDTO user;

    @Setter
    private String category; // Category for the post

    @Setter
    private Poll poll;

    public void setPoll(Poll poll) {
        this.poll = poll;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public void setVideoUrls(List<String> videoUrls) {
        this.videoUrls = videoUrls;
    }


    public void setCategory(String category) {
        this.category = category;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setContent(String content) {
        this.content = content;
    }


    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public void setShareCount(int shareCount) {
        this.shareCount = shareCount;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }


}
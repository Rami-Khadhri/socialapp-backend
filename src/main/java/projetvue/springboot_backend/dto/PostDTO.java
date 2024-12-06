package projetvue.springboot_backend.dto;

import lombok.Getter;
import lombok.Setter;
import projetvue.springboot_backend.model.Comment;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class PostDTO {
    private String id;

    @Setter
    private String content;

    @Setter
    private String imageUrl;

    @Setter
    private int likeCount;

    @Setter
    private int commentCount;

    @Setter
    private int shareCount;

    @Setter
    private LocalDateTime createdAt;

    @Setter
    private List<Comment> comments ;

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public int getShareCount() {
        return shareCount;
    }

    public void setShareCount(int shareCount) {
        this.shareCount = shareCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    @Setter
    private UserDTO user;

}
package projetvue.springboot_backend.model;

import lombok.Getter;
import lombok.Setter;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Getter
@Document(collection = "posts")
public class Post {
    @Id
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
    private LocalDateTime updatedAt;

    @DBRef
    @Getter
    @Setter
    private List<Comment> comments = new ArrayList<>();

    @DBRef
    @Setter
    private User user;


    @DBRef(lazy = true)
    @Setter
    private List<User> likedBy = new ArrayList<>(); // No comments reference here
}

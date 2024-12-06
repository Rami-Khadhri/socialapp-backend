package projetvue.springboot_backend.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.DBRef;
import projetvue.springboot_backend.model.Post;
import projetvue.springboot_backend.model.User;

import java.time.LocalDateTime;

public class CommentDTO {
    @Getter
    private String id;

    @Getter
    @Setter
    private String content;

    @Getter
    @Setter
    private LocalDateTime createdAt;

    @DBRef
    @Getter
    @Setter
    private User user;

    @DBRef
    @Getter
    @Setter
    private Post post;



}

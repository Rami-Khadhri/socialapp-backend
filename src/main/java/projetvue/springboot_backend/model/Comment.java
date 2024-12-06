package projetvue.springboot_backend.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "comments")
public class Comment {
    @Id
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

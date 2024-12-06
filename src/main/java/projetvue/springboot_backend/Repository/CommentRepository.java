package projetvue.springboot_backend.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import projetvue.springboot_backend.model.Comment;
import projetvue.springboot_backend.model.Post;

import java.util.List;

public interface CommentRepository extends MongoRepository<Comment, String> {
    List<Comment> findByPostOrderByCreatedAtDesc(Post post);
    List<Comment> findByIdIn(List<String> ids);
    List<Comment> findByPostId(String postId);
}
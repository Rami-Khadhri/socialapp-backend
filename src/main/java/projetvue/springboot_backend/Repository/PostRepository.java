package projetvue.springboot_backend.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import projetvue.springboot_backend.model.Post;
import projetvue.springboot_backend.model.User;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends MongoRepository<Post, String> {
    List<Post> findByUserOrderByCreatedAtDesc(User user);
    List<Post> findAllByOrderByCreatedAtDesc();
    Post findPostById(String id);
    List<Post> findByUser(User user);
}

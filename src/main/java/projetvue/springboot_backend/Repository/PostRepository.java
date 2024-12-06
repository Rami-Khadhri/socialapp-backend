package projetvue.springboot_backend.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import projetvue.springboot_backend.model.Post;
import projetvue.springboot_backend.model.User;

import java.util.List;

public interface PostRepository extends MongoRepository<Post, String> {
    List<Post> findByUserOrderByCreatedAtDesc(User user);
    List<Post> findAllByOrderByCreatedAtDesc();
}

package projetvue.springboot_backend.Repository;

import projetvue.springboot_backend.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
    User getUserByUsername(String username);
        List<User> findByUsernameContainingIgnoreCase(String username);

    List<User> findAllById(Iterable<String> ids);

    // You can add more query methods if needed
    Optional<User> findByEmail(String email);
    Optional<User> findByVerificationToken(String verificationToken);// Example for finding by email
}

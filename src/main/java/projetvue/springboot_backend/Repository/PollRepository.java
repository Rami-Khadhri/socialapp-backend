package projetvue.springboot_backend.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import projetvue.springboot_backend.model.Poll;

public interface PollRepository extends MongoRepository<Poll, String> {

}

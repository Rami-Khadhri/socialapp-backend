package projetvue.springboot_backend.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import projetvue.springboot_backend.model.Notification;

import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByUserIdAndIsReadFalse(String userId);
}

package projetvue.springboot_backend.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import projetvue.springboot_backend.Repository.NotificationRepository;
import projetvue.springboot_backend.model.Notification;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public void createNotifications(List<String> userIds, String postId, String editorUsername) {
        for (String userId : userIds) {
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setPostId(postId);
            notification.setMessage(editorUsername + " has edited a post you've liked.");
            notificationRepository.save(notification);
        }
    }

    public List<Notification> getUnreadNotifications(String userId) {
        return notificationRepository.findByUserIdAndIsReadFalse(userId);
    }

    public void markNotificationAsRead(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification != null) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
    }
}

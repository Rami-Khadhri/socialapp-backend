package projetvue.springboot_backend.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import projetvue.springboot_backend.model.User;
import projetvue.springboot_backend.Repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FriendService {

    @Autowired
    private UserRepository userRepository;

    public boolean sendFriendRequest(String senderId, String receiverId) {
        Optional<User> senderOpt = userRepository.findById(senderId);
        Optional<User> receiverOpt = userRepository.findById(receiverId);

        if (senderOpt.isPresent() && receiverOpt.isPresent()) {
            User sender = senderOpt.get();
            User receiver = receiverOpt.get();

            // Check if they're already friends or if a request already exists
            if (sender.getFriendIds().contains(receiverId) ||
                    sender.getSentFriendRequests().contains(receiverId) ||
                    receiver.getReceivedFriendRequests().contains(senderId)) {
                return false;
            }

            sender.getSentFriendRequests().add(receiverId);
            receiver.getReceivedFriendRequests().add(senderId);

            userRepository.save(sender);
            userRepository.save(receiver);
            return true;
        }
        return false;
    }

    public boolean acceptFriendRequest(String userId, String senderId) {
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<User> senderOpt = userRepository.findById(senderId);

        if (userOpt.isPresent() && senderOpt.isPresent()) {
            User user = userOpt.get();
            User sender = senderOpt.get();

            if (user.getReceivedFriendRequests().contains(senderId) &&
                    sender.getSentFriendRequests().contains(userId)) {

                // Add to friends lists
                user.getFriendIds().add(senderId);
                sender.getFriendIds().add(userId);

                // Remove from request lists
                user.getReceivedFriendRequests().remove(senderId);
                sender.getSentFriendRequests().remove(userId);

                userRepository.save(user);
                userRepository.save(sender);
                return true;
            }
        }
        return false;
    }

    public boolean declineFriendRequest(String userId, String senderId) {
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<User> senderOpt = userRepository.findById(senderId);

        if (userOpt.isPresent() && senderOpt.isPresent()) {
            User user = userOpt.get();
            User sender = senderOpt.get();

            if (user.getReceivedFriendRequests().contains(senderId) &&
                    sender.getSentFriendRequests().contains(userId)) {

                user.getReceivedFriendRequests().remove(senderId);
                sender.getSentFriendRequests().remove(userId);

                userRepository.save(user);
                userRepository.save(sender);
                return true;
            }
        }
        return false;
    }

    public List<User> getSentFriendRequests(String userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            List<String> requestIds = userOpt.get().getSentFriendRequests();
            return getUserDetailsByIds(requestIds);
        }
        return new ArrayList<>();
    }

    public List<User> getReceivedFriendRequests(String userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            List<String> requestIds = userOpt.get().getReceivedFriendRequests();
            return getUserDetailsByIds(requestIds);
        }
        return new ArrayList<>();
    }

    public List<User> getFriendsList(String userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            List<String> friendIds = userOpt.get().getFriendIds();
            return getUserDetailsByIds(friendIds);
        }
        return new ArrayList<>();
    }

    public boolean removeFriend(String userId, String friendId) {
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<User> friendOpt = userRepository.findById(friendId);

        if (userOpt.isPresent() && friendOpt.isPresent()) {
            User user = userOpt.get();
            User friend = friendOpt.get();

            if (user.getFriendIds().contains(friendId) &&
                    friend.getFriendIds().contains(userId)) {

                user.getFriendIds().remove(friendId);
                friend.getFriendIds().remove(userId);

                userRepository.save(user);
                userRepository.save(friend);
                return true;
            }
        }
        return false;
    }

    public List<User> getUserDetailsByIds(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new ArrayList<>();
        }
        return userRepository.findAllById(userIds);
    }

    public boolean cancelFriendRequest(String senderId, String receiverId) {
        Optional<User> senderOpt = userRepository.findById(senderId);
        Optional<User> receiverOpt = userRepository.findById(receiverId);

        if (senderOpt.isPresent() && receiverOpt.isPresent()) {
            User sender = senderOpt.get();
            User receiver = receiverOpt.get();

            if (sender.getSentFriendRequests().contains(receiverId) &&
                    receiver.getReceivedFriendRequests().contains(senderId)) {

                sender.getSentFriendRequests().remove(receiverId);
                receiver.getReceivedFriendRequests().remove(senderId);

                userRepository.save(sender);
                userRepository.save(receiver);
                return true;
            }
        }
        return false;
    }
}
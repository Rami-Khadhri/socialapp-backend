package projetvue.springboot_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import projetvue.springboot_backend.model.User;


import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private String id;
    private String username;
    private String email;
    private String role;
    private String photoUrl;
    private String coverPhoto;
    private List<String> friendIds = new ArrayList<>();
    private List<String> sentFriendRequests = new ArrayList<>();
    private List<String> receivedFriendRequests = new ArrayList<>();
}
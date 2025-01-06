package projetvue.springboot_backend.dto;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.Binary;

public class UserDTO {
    @Getter
    @Setter
    private String id;

    @Getter
    @Setter
    private String username;

    @Getter
    @Setter
    private String photoUrl;
    @Getter
    @Setter
    private Binary photo;

}

package projetvue.springboot_backend.dto;



import lombok.Data;

@Data
public class PasswordResetResponse {
    private boolean success;
    private String message;
}

package projetvue.springboot_backend.Security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import projetvue.springboot_backend.Repository.UserRepository;
import projetvue.springboot_backend.Security.JwtService;
import projetvue.springboot_backend.model.User;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        String email = token.getPrincipal().getAttribute("email");
        System.out.println("Authenticated user email: " + email); // Log email to debug

        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            System.out.println("User not found, creating a new user..."); // Log for debugging
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setUsername(token.getPrincipal().getAttribute("name"));
            newUser.setRole("ROLE_USER");
            newUser.setVerified(true);
            newUser.setEnabled(true);
            newUser.setPhotoUrl(token.getPrincipal().getAttribute("picture"));
            userRepository.save(newUser);  // Save new user
            user = Optional.of(newUser);  // Re-fetch the user after saving
        }
    }
}
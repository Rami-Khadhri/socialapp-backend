package projetvue.springboot_backend.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import projetvue.springboot_backend.Repository.UserRepository;
import projetvue.springboot_backend.Security.JwtService;
import projetvue.springboot_backend.model.User;

import java.util.ArrayList;
import java.util.Collections;
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");
        // Check if user exists, if not create a new user
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    assert name != null;
                    newUser.setUsername(name.replaceAll("\\s+", "_").toLowerCase());
                    newUser.setRole("ROLE_USER");
                    newUser.setVerified(true);
                    newUser.setEnabled(true);
                    newUser.setGoogleUser(true);
                    newUser.setPhotoUrl(picture);
                    newUser.setFriendIds(new ArrayList<>());
                    newUser.setCoverPhoto("lien");
                    newUser.setSentFriendRequests(new ArrayList<>());
                    newUser.setReceivedFriendRequests(new ArrayList<>());
                    return userRepository.save(newUser);
                });

        // Make sure to generate and return a JWT or authenticated user details
        return new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole())),
                oAuth2User.getAttributes(),
                "email"
        );
    }
}

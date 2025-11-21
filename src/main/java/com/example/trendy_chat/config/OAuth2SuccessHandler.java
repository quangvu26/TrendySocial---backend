package com.example.trendy_chat.config;

import com.example.trendy_chat.entity.User;
import com.example.trendy_chat.repository.UserRepository;
import com.example.trendy_chat.service.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Optional;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Value("${app.frontend.urls}")
    private String frontendUrls;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        try {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            
            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");
            String picture = oauth2User.getAttribute("picture");

            if (email == null || email.trim().isEmpty()) {
                response.sendRedirect(getFrontendUrl() + "/login?error=no_email");
                return;
            }

            // Check if user exists in database
            Optional<User> userOpt = userRepository.findByEmail(email);
            
            String redirectUrl;
            
            if (userOpt.isPresent()) {
                // User exists - generate token and redirect to chat
                User user = userOpt.get();
                String token = jwtService.genToken(user.getEmail(), user.getId());
                
                redirectUrl = UriComponentsBuilder.fromUriString(getFrontendUrl() + "/trendy/auth/oauth2/redirect")
                    .queryParam("token", token)
                    .queryParam("email", email)
                    .queryParam("name", name)
                    .build()
                    .toUriString();
            } else {
                // User doesn't exist - redirect to registration page with OAuth data
                redirectUrl = UriComponentsBuilder.fromUriString(getFrontendUrl() + "/register-oauth2")
                    .queryParam("email", email)
                    .queryParam("name", name)
                    .queryParam("picture", picture)
                    .build()
                    .toUriString();
            }

            System.out.println("OAuth2 redirect: " + redirectUrl);
            getRedirectStrategy().sendRedirect(request, response, redirectUrl);
        } catch (Exception e) {
            System.err.println("OAuth2 error: " + e.getMessage());
            e.printStackTrace();
            response.sendRedirect(getFrontendUrl() + "/login?error=oauth2_failed");
        }
    }

    private String getFrontendUrl() {
        // Get first URL from comma-separated list
        if (frontendUrls != null && !frontendUrls.isEmpty()) {
            return frontendUrls.split(",")[0].trim();
        }
        return "http://localhost:5173";
    }
}

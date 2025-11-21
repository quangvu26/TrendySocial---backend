package com.example.trendy_chat.controller;

import com.example.trendy_chat.entity.User;
import com.example.trendy_chat.repository.UserRepository;
import com.example.trendy_chat.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class TokenController {
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public TokenController(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    /**
     * Refresh authentication token
     * Takes the current token and returns a new valid token
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> body) {
        try {
            String token = body.get("token");
            
            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    Map.of("error", "Token không được để trống")
                );
            }

            // Validate current token
            if (!jwtService.validateToken(token)) {
                return ResponseEntity.status(401).body(
                    Map.of("error", "Token không hợp lệ hoặc đã hết hạn")
                );
            }

            // Extract user ID from current token
            String userId = jwtService.getUserIdFromToken(token);
            String email = jwtService.extractEmail(token);
            
            // Verify user still exists
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.status(401).body(
                    Map.of("error", "Người dùng không tồn tại")
                );
            }

            // Generate new token (with 30-minute expiry as configured)
            String newToken = jwtService.generateToken(email != null ? email : userId, userId);

            return ResponseEntity.ok(
                Map.of(
                    "token", newToken,
                    "userId", userId,
                    "message", "Token đã được làm mới"
                )
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                Map.of("error", "Lỗi khi làm mới token: " + e.getMessage())
            );
        }
    }
}

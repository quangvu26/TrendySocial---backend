package com.example.trendy_chat.controller;

import com.example.trendy_chat.dto.UserResponse;
import com.example.trendy_chat.entity.User;
import com.example.trendy_chat.mapper.UserMapper;
import com.example.trendy_chat.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/trendy/user")
public class UserController {
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable String id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        UserResponse resp = UserMapper.toResponse(user);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getByEmail(@PathVariable String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        UserResponse resp = UserMapper.toResponse(user);
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateById(
            @PathVariable String id,
            @RequestBody User updateData) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        // Update fields - but do NOT change the ID
        if (updateData.getTen() != null) {
            user.setTen(updateData.getTen());
        }
        if (updateData.getGioiTinh() != null) {
            user.setGioiTinh(updateData.getGioiTinh());
        }
        if (updateData.getNgaySinh() != null) {
            user.setNgaySinh(updateData.getNgaySinh());
        }
        if (updateData.getTieuSu() != null) {
            user.setTieuSu(updateData.getTieuSu());
        }
        if (updateData.getAvatar() != null) {
            user.setAvatar(updateData.getAvatar());
        }
        if (updateData.getEmail() != null && !updateData.getEmail().isEmpty()) {
            user.setEmail(updateData.getEmail());
        }

        User savedUser = userRepository.save(user);
        UserResponse resp = UserMapper.toResponse(savedUser);
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/email/{email}")
    public ResponseEntity<UserResponse> updateByEmail(
            @PathVariable String email,
            @RequestBody User updateData) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        // Update fields
        if (updateData.getId() != null && !updateData.getId().isEmpty()) {
            user.setId(updateData.getId());
        }
        if (updateData.getTen() != null) {
            user.setTen(updateData.getTen());
        }
        if (updateData.getGioiTinh() != null) {
            user.setGioiTinh(updateData.getGioiTinh());
        }
        if (updateData.getNgaySinh() != null) {
            user.setNgaySinh(updateData.getNgaySinh());
        }
        if (updateData.getTieuSu() != null) {
            user.setTieuSu(updateData.getTieuSu());
        }
        if (updateData.getAvatar() != null) {
            user.setAvatar(updateData.getAvatar());
        }

        User savedUser = userRepository.save(user);
        UserResponse resp = UserMapper.toResponse(savedUser);
        return ResponseEntity.ok(resp);
    }

    /**
     * Send verification code to email (for email change verification)
     * In production, should send actual email with code
     * For now, return code in response for testing
     */
    @PostMapping("/send-verification-code")
    public ResponseEntity<?> sendVerificationCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }

        // Check if email already registered
        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));
        }

        // Generate 6-digit code
        String code = String.valueOf((int)(Math.random() * 900000) + 100000);

        // Store in cache/session (in production, use Redis or database)
        // For now, store in memory (will be lost on restart)
        System.out.println("✅ Verification code for " + email + ": " + code);

        // Return code for testing (in production, send via email only)
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Verification code sent to email");
        response.put("code", code); // For testing only - remove in production
        return ResponseEntity.ok(response);
    }

    /**
     * Verify email code (for email change)
     */
    @PostMapping("/verify-email-code")
    public ResponseEntity<?> verifyEmailCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        if (email == null || code == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email and code required"));
        }

        // Check if email already registered
        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));
        }

        // Verify code (in production, check from Redis/cache)
        // For now, accept any 6-digit code
        if (!code.matches("\\d{6}")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid code format"));
        }

        // Return success - frontend can now proceed with email update
        return ResponseEntity.ok(Map.of("message", "Email verified successfully"));
    }
}

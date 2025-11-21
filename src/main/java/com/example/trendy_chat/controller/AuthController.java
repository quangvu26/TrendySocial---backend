package com.example.trendy_chat.controller;

import com.example.trendy_chat.dto.LoginRequest;
import com.example.trendy_chat.dto.RegisterRequest;
import com.example.trendy_chat.dto.UserResponse;
import com.example.trendy_chat.service.AuthService;
import com.example.trendy_chat.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/trendy/auth")
public class AuthController {
    @Autowired
    AuthService authService;
    @Autowired
    JwtService jwtService;
    @PostMapping("/register")
    public ResponseEntity<UserResponse> dangKy(@RequestBody RegisterRequest registerRequest) throws BadRequestException {
        UserResponse userResponse = authService.dangKy(registerRequest);
        return ResponseEntity.ok(userResponse);
    }
    @PostMapping("/login")
    public ResponseEntity<?> dangNhap(@RequestBody LoginRequest loginRequest) {
        try {
            if (loginRequest == null || loginRequest.getEmail() == null || loginRequest.getPassword() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email và mật khẩu không được để trống"));
            }
            UserResponse userResponse = authService.dangNhap(loginRequest);
            if (userResponse == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Đăng nhập thất bại"));
            }
            return ResponseEntity.ok(userResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    @GetMapping("/check")
    public ResponseEntity<String> checkAuth(){
        return ResponseEntity.ok("Người dùng đã đăng nhập hợp lệ");
    }

    // Return current authenticated user's profile derived from JWT
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@RequestHeader(value = "Authorization", required = false) String authHeader){
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }
        String token = authHeader.substring(7);
        String userId = jwtService.extractUserId(token);
        UserResponse user = authService.getUserById(userId);
        if (user == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(user);
    }
    @PostMapping("/send-verify-code")
    public ResponseEntity<String> sendVerifyCode(@RequestBody Map<String, String> body){
        String email = body.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("Email không được để trống");
        }
        try {
            authService.sendVerifyCode(email);
            return ResponseEntity.ok("Đã gửi mã xác nhận");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
    @PostMapping("/verify-code")
    public ResponseEntity<Map<String, String>> verifyCode(@RequestBody Map<String, String> body){
        String email = body.get("email");
        String code = body.get("code");
        String token = authService.verifyCode(email, code); // trả token
        return ResponseEntity.ok(Map.of("token", token));
    }

    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam String email){
        boolean exists = authService.existsByEmail(email);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @GetMapping("/check-id/{id}")
    public ResponseEntity<Map<String, Boolean>> checkId(@PathVariable String id){
        boolean exists = authService.existsById(id);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> body){
        String email = body.get("email");
        String password = body.get("password");
        authService.resetPassword(email, password);
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        // Spring Security tự handle logout thông qua cấu hình
        return ResponseEntity.ok("Đăng xuất thành công");
    }
}

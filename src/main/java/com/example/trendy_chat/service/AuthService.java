package com.example.trendy_chat.service;

import com.example.trendy_chat.dto.*;
import com.example.trendy_chat.entity.User;
import com.example.trendy_chat.exception.UnauthorizedException;
import com.example.trendy_chat.mapper.UserMapper;
import com.example.trendy_chat.repository.UserRepository;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
public class AuthService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    JwtService jwtService;
    @Autowired
    EmailService emailService;
    public UserResponse dangKy(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())){
           throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email đã tồn tại");
        }
        User user = new User();
        user.setId(req.getId());
        user.setTen(req.getTen());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setGioiTinh(req.getGioiTinh());
        user.setNgaySinh(req.getNgaySinh());
        
        // Set required fields with defaults
        user.setKieuDangNhap(req.getKieuDangNhap() != null ? req.getKieuDangNhap() : "LOCAL");
        user.setTrangThai(req.getTrangThai() != null ? req.getTrangThai() : false);
        user.setNgayTao(LocalDateTime.now());
        
        userRepository.save(user);
        UserResponse resp = UserMapper.toResponse(user);
        // Generate JWT token with id_user
        String token = jwtService.genToken(user.getEmail(), user.getId());
        resp.setToken(token);
        return resp;
    }
    public UserResponse dangNhap(LoginRequest loginRequest){
        User user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow(()-> new UnauthorizedException("Email không tồn tại"));
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())){
            throw new UnauthorizedException("Sai mật khẩu");
        }
        String token = jwtService.genToken(user.getEmail(), user.getId());
        UserResponse res = UserMapper.toResponse(user);
        res.setToken(token); // <-- set token vào response
        return res;
    }


    public void sendVerifyCode(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        String code = String.valueOf(new Random().nextInt(900_000) + 100_000);
        user.setMaXacNhan(code);
        user.setThoiGianHetHan(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        emailService.sendEmail(email, "Mã xác nhận", "Code: " + code);
    }


    public String verifyCode(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        if (user.getMaXacNhan() == null) {
            throw new RuntimeException("Hãy gửi mã xác nhận trước");
        }
        
        if (!user.getMaXacNhan().equals(code)) {
            throw new RuntimeException("Sai mã xác nhận");
        }
        
        if (user.getThoiGianHetHan() == null || 
            user.getThoiGianHetHan().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Mã xác nhận đã hết hạn");
        }

        // Kích hoạt tài khoản
        user.setTrangThai(true);
        user.setMaXacNhan(null);
        user.setThoiGianHetHan(null);
        userRepository.save(user);

        // Trả về JWT token
        return jwtService.genToken(user.getEmail());
    }


    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // Utility: check if email exists
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // Utility: check if id exists (id is the username in this project)
    public boolean existsById(String id){
        return userRepository.existsById(id);
    }

    // utility to return full user response by email
    public UserResponse getUserByEmail(String email) {
        return userRepository.findByEmail(email).map(UserMapper::toResponse).orElse(null);
    }

    public UserResponse getUserById(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;
        return UserMapper.toResponse(user);
    }

}


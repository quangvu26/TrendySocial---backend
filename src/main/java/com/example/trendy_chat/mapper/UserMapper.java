package com.example.trendy_chat.mapper;

import com.example.trendy_chat.dto.RegisterRequest;
import com.example.trendy_chat.dto.UserResponse;
import com.example.trendy_chat.entity.User;

public class UserMapper {
    
    /**
     * Convert RegisterRequest → User Entity
     */
    public static User toEntity(RegisterRequest registerUser) {
        User user = new User();
        user.setId(registerUser.getId());
        user.setTen(registerUser.getTen());
        user.setEmail(registerUser.getEmail());
        user.setPassword(registerUser.getPassword());
        user.setGioiTinh(registerUser.getGioiTinh());
        user.setNgaySinh(registerUser.getNgaySinh());
        user.setKieuDangNhap("LOCAL");
        user.setTrangThai(false);
        return user;
    }

    /**
     * Convert User Entity → UserResponse DTO - HOÀN THIỆN đầy đủ field
     */
    public static UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setTen(user.getTen());
        response.setEmail(user.getEmail());
        response.setAvatar(user.getAvatar());
        response.setTieuSu(user.getTieuSu());
        response.setGioiTinh(user.getGioiTinh());
        response.setNgaySinh(user.getNgaySinh());
        response.setKieuDangNhap(user.getKieuDangNhap());
        response.setTrangThai(user.getTrangThai());
        response.setNgayTao(user.getNgayTao());
        return response;
    }
}

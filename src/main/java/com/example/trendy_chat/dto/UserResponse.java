package com.example.trendy_chat.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserResponse {
    private String id;
    private String ten;
    private String avatar;
    private String tieuSu;
    private String email;
    private Boolean gioiTinh;
    private LocalDate ngaySinh;
    private String kieuDangNhap;
    private Boolean trangThai;
    private LocalDateTime ngayTao;
    private String token;
}

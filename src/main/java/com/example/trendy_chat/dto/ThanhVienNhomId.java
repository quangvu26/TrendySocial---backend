package com.example.trendy_chat.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ThanhVienNhomId implements Serializable {
    private String maNhom;
    private String idUser;
}

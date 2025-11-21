package com.example.trendy_chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostUpdateRequest {
    private String noiDung;
    private String cheDoRiengTu;
    private Boolean hideComments;
    private Boolean hideLikes;
    private Boolean hideViews;
}

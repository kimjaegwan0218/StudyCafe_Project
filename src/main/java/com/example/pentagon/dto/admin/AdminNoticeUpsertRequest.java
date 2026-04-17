package com.example.pentagon.dto.admin;

import lombok.Data;

@Data
public class AdminNoticeUpsertRequest {
    private String cat;     // "emergency" | "event" | "normal"
    private String title;
    private String content;
}
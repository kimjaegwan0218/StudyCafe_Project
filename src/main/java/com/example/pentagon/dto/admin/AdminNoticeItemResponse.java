package com.example.pentagon.dto.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminNoticeItemResponse {
    private Long id;
    private String cat;     // "emergency" | "event" | "normal"
    private String title;
    private String content;
    private String date;    // "yyyy.MM.dd"
}
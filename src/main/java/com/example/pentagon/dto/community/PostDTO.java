package com.example.pentagon.dto.community;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostDTO {

    private Long id;

    @NotEmpty
    @Size(min = 3, max = 100)
    private String title;

    @NotEmpty
    private String content;

    private Long userId; //등록할 때 누가 쓰는지 알려주는 ID
    private String userName; // 화면에 보여줄 이름

    private int likedCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


}

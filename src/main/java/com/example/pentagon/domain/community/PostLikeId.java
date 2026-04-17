package com.example.pentagon.domain.community;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode
public class PostLikeId implements Serializable {

    @Column(name = "post_id")
    private Long postId;

    @Column(name = "user_id")
    private Long userId;
}

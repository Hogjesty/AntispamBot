package org.example.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    private Long userId;
    private String username;
    private Integer joinMessageId;
    private Integer questionMessageId;
}

package com.example.mc.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayerJoinDto {
    private String type;
    private String name;
    private String uuid;
    private String time;
}
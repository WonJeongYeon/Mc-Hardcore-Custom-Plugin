package com.example.mc.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StatusRes {

    private boolean online;
    private int players;
    private int maxPlayers;
    private double tps;

}

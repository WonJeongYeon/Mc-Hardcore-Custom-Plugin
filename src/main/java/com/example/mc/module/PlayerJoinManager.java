package com.example.mc.module;

import com.example.mc.dto.PlayerJoinDto;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.player.PlayerJoinEvent;

@RequiredArgsConstructor
public class PlayerJoinManager {

    private final RabbitMqManager rabbitMqManager;
    private final UtilManager utilManager;
    private final Gson gson = new Gson();

    public void sendJoinLog(PlayerJoinEvent e) {

        PlayerJoinDto info = PlayerJoinDto.builder()
                .type("JOIN")
                .name(e.getPlayer().getName())
                .uuid(e.getPlayer().getUniqueId().toString())
                .time(utilManager.now())
                .build();


        String message = gson.toJson(info);

        rabbitMqManager.publish(message);
    }
}

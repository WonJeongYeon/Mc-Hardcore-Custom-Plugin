package com.example.mc.listener;

import com.example.mc.module.PlayerJoinManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final PlayerJoinManager playerJoinManager;

    public PlayerJoinListener(PlayerJoinManager playerJoinManager) {
        this.playerJoinManager = playerJoinManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        playerJoinManager.sendJoinLog(e);
    }

}

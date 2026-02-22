package com.example.mc.hud;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class HudManager {

    private final Set<UUID> enabledPlayers = new HashSet<>();

    public boolean isEnabled(Player p) {
        return enabledPlayers.contains(p.getUniqueId());
    }

    public void toggle(Player p) {
        if (isEnabled(p)) {
            enabledPlayers.remove(p.getUniqueId());
        } else {
            enabledPlayers.add(p.getUniqueId());
        }
    }

    public void enable(Player p) {
        enabledPlayers.add(p.getUniqueId());
    }

    public void disable(Player p) {
        enabledPlayers.remove(p.getUniqueId());
    }
}

package com.example.mc.listener;

import com.example.mc.hud.HudManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

@RequiredArgsConstructor
public class MoveListener implements Listener {

    private final HudManager manager;

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        manager.enable(e.getPlayer()); // 로그인 자동 ON

        e.getPlayer().setScoreboard(
                org.bukkit.Bukkit.getScoreboardManager().getNewScoreboard());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {

        if (!manager.isEnabled(e.getPlayer())) return;

        Location from = e.getFrom();
        Location to = e.getTo();

        if (from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()) return;

        Scoreboard board = e.getPlayer().getScoreboard();

        update(board, "x", "§fX: §e" + to.getBlockX());
        update(board, "y", "§fY: §e" + to.getBlockY());
        update(board, "z", "§fZ: §e" + to.getBlockZ());
    }

    private void update(Scoreboard board, String id, String text) {
        Team team = board.getTeam(id);
        if (team != null) team.setPrefix(text);
    }
}

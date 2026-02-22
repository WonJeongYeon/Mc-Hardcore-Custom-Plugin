package com.example.mc.hud;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

@RequiredArgsConstructor
public class ScoreBoardManager extends BukkitRunnable {

    private final Plugin plugin;
    private final HudManager manager;

    @Override
    public void run() {

        double tps = Bukkit.getTPS()[0];

        for (Player player : Bukkit.getOnlinePlayers()) {

            if (!manager.isEnabled(player)) continue;

            Scoreboard board = player.getScoreboard();
            Objective obj = board.getObjective("hud");

            if (obj == null) {
                obj = board.registerNewObjective("hud", "dummy", "§a§lPLAYER INFO");
                obj.setDisplaySlot(DisplaySlot.SIDEBAR);

                createLine(board, obj, "x", 6);
                createLine(board, obj, "y", 5);
                createLine(board, obj, "z", 4);
                createLine(board, obj, "world", 3);
                createLine(board, obj, "ping", 2);
                createLine(board, obj, "tps", 1);
            }

            update(board, "world", "§7World: §b" + player.getWorld().getName());
            update(board, "ping", "§7Ping: " + pingColor(player.getPing()) + player.getPing() + "ms");
            update(board, "tps", "§7TPS: " + tpsColor(tps) + String.format("%.2f", tps));
        }
    }

    private String pingColor(int ping) {
        if (ping <= 80) return "§a";
        if (ping <= 150) return "§6";
        return "§c";
    }

    private String tpsColor(double tps) {
        if (tps >= 19.5) return "§a";
        if (tps >= 17) return "§6";
        return "§c";
    }

    private void createLine(Scoreboard board, Objective obj, String id, int score) {
        Team team = board.getTeam(id);
        if (team == null) team = board.registerNewTeam(id);

        String entry = "§" + Integer.toHexString(score);
        team.addEntry(entry);
        obj.getScore(entry).setScore(score);
    }

    private void update(Scoreboard board, String id, String text) {
        Team team = board.getTeam(id);
        if (team != null) team.setPrefix(text);
    }

    public void start() {
        this.runTaskTimer(plugin, 0L, 20L);
    }

    public void stop() {
        this.cancel();
    }
}

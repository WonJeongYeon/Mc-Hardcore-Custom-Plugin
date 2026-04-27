package com.example.mc;

import com.example.mc.api.HttpApiServer;
import com.example.mc.hud.ActionBarManager;
import com.example.mc.hud.HudManager;
import com.example.mc.hud.ScoreBoardManager;
import com.example.mc.item.Tomato;
import com.example.mc.listener.MoveListener;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;


public class McCollectorPlugin extends JavaPlugin {

    private HttpApiServer httpApiServer;

    private HudManager hudManager;

    private ActionBarManager actionBarManager;
    private ScoreBoardManager scoreBoardManager;


    @Override
    public void onEnable() {
        getLogger().info("[Main] McCollector Enabled");

        Tomato tomato = new Tomato(this);
        getServer().getPluginManager().registerEvents(tomato, this);
        Bukkit.getScheduler().runTaskTimer(this, tomato::growTomatoes, 0L, 20L * 10);
        PluginCommand cmd = getCommand("tomato");
        if (cmd != null) {
            cmd.setExecutor((sender, command, label, args) -> {
                if (!(sender instanceof Player p)) return true;

                p.getInventory().addItem(tomato.createTomatoSeed());
                p.sendMessage("§a토마토 씨앗 지급됨");
                return true;
            });
        }

        httpApiServer = new HttpApiServer(this);
        try {
            httpApiServer.start(9000);
        } catch (Exception e) {
            getLogger().severe("[Main] HTTP Server start failed");
            e.printStackTrace();
        }
        try {
            initHud();
        } catch (Exception e) {
            getLogger().severe("[Main] Hud Manager start failed");
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("[Main] McCollector Disabled");
        if (httpApiServer != null) {
            httpApiServer.stop();
        }
        if (actionBarManager != null) actionBarManager.stop();
        if (scoreBoardManager != null) scoreBoardManager.stop();
    }

    private void initHud() {
        hudManager = new HudManager();

        getServer().getPluginManager().registerEvents(new MoveListener(hudManager), this);

        scoreBoardManager = new ScoreBoardManager(this, hudManager);
        actionBarManager = new ActionBarManager(this, hudManager);

        scoreBoardManager.start();
        actionBarManager.start();

        PluginCommand cmd = getCommand("hud");
        if (cmd != null) {
            cmd.setExecutor((sender, command, label, args) -> {
                if (!(sender instanceof Player p)) return true;

                hudManager.toggle(p);

                if (hudManager.isEnabled(p)) {
                    p.sendMessage("§aHUD 활성화됨");
                } else {
                    p.getScoreboard().clearSlot(org.bukkit.scoreboard.DisplaySlot.SIDEBAR);
                    p.sendMessage("§cHUD 비활성화됨");
                }
                return true;
            });
        } else {
            getLogger().severe("[HudManager] Command 'hud' is null");
        }
    }


}

package com.example.mc;

import com.example.mc.api.HttpApiServer;
import com.example.mc.hud.ActionBarManager;
import com.example.mc.hud.HudManager;
import com.example.mc.hud.ScoreBoardManager;
import com.example.mc.listener.MoveListener;
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

        scoreBoardManager = new ScoreBoardManager(this, hudManager);
        actionBarManager = new ActionBarManager(this, hudManager);

        scoreBoardManager.start();
        actionBarManager.start();

        getServer().getPluginManager().registerEvents(new MoveListener(hudManager), this);

        getCommand("hud").setExecutor((sender, command, label, args) -> {
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
    }


}

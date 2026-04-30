package com.example.mc;

import com.example.mc.api.HttpApiServer;
import com.example.mc.api.VaultManager;
import com.example.mc.command.BillCommand;
import com.example.mc.hud.ActionBarManager;
import com.example.mc.hud.HudManager;
import com.example.mc.hud.ScoreBoardManager;
import com.example.mc.item.Bill;
import com.example.mc.listener.BillListener;
import com.example.mc.listener.MoveListener;
import com.example.mc.listener.PlayerJoinListener;
import com.example.mc.module.PlayerJoinManager;
import com.example.mc.module.RabbitMqManager;
import com.example.mc.module.UtilManager;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;


public class McCollectorPlugin extends JavaPlugin {

    private HttpApiServer httpApiServer;

    private RabbitMqManager rabbitMqManager;

    private UtilManager utilManager;

    private HudManager hudManager;

    private VaultManager vaultManager;

    private PlayerJoinManager playerJoinManager;

    private ActionBarManager actionBarManager;
    private ScoreBoardManager scoreBoardManager;


    @Override
    public void onEnable() {
        getLogger().info("[Main] McCollector Enabled");

        initVault();
        utilManager = new UtilManager();
        initTestVaultCommand();
        initBill();

        initMQ();
        initListener();

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

    private void initListener() {
        playerJoinManager = new PlayerJoinManager(rabbitMqManager, utilManager);
        getServer().getPluginManager().registerEvents(
                new PlayerJoinListener(playerJoinManager),
                this
        );
    }

    private void initHud() {
        hudManager = new HudManager();

        getServer().getPluginManager().registerEvents(new MoveListener(hudManager), this);

        scoreBoardManager = new ScoreBoardManager(this, hudManager);
        actionBarManager = new ActionBarManager(this, hudManager, vaultManager);

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

    private void initVault() {
        vaultManager = new VaultManager();

        if (!vaultManager.setup(this)) {
            getLogger().severe("Vault 연결 실패");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("Vault 연결 성공");
    }

    private void initTestVaultCommand() {
        getCommand("moneytest").setExecutor((sender, cmd, label, args) -> {
            if (!(sender instanceof Player p)) return true;

            vaultManager.getEconomy().depositPlayer(p, 1000);
            p.sendMessage("1000원 지급됨");

            return true;
        });
    }

    private void initBill() {
        Bill bill = new Bill(this);

        getServer().getPluginManager().registerEvents(
                new BillListener(bill, vaultManager.getEconomy()),
                this
        );

        getCommand("money").setExecutor(new BillCommand(bill, vaultManager));
    }

    private void initMQ() {
        rabbitMqManager = new RabbitMqManager(
                this,
                "localhost",
                5672,
                "mc",
                "mc1234"
        );

        rabbitMqManager.start();
    }

}

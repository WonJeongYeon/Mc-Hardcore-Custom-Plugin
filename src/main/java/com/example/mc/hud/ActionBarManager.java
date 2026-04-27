package com.example.mc.hud;

import com.example.mc.api.VaultManager;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

@RequiredArgsConstructor
public class ActionBarManager extends BukkitRunnable {

    private final Plugin plugin;
    private final HudManager manager;

    private final VaultManager vaultManager;

    @Override
    public void run() {

        for (Player player : Bukkit.getOnlinePlayers()) {

            if (!manager.isEnabled(player)) continue;

            int level = player.getLevel();

            double health = player.getHealth();
            AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);

            double maxHealth = 20;
            if (attr != null) {
                maxHealth = attr.getValue();
            }

            double money = vaultManager.getEconomy().getBalance(player);

            String text = String.format(
                    "§aLV %d §7| §cHP %d/%d §7| §6₩ %,d",
                    level,
                    (int) health,
                    (int) maxHealth,
                    (int) money
            );

            player.sendActionBar(Component.text(text));
        }
    }

    public void start() {
        this.runTaskTimer(plugin, 0L, 5L);
    }

    public void stop() {
        this.cancel();
    }
}

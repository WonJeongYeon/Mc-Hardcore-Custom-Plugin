package com.example.mc.api;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class VaultManager {

    private Economy economy;

    public boolean setup(JavaPlugin plugin) {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp =
                plugin.getServer().getServicesManager().getRegistration(Economy.class);

        if (rsp == null) return false;

        economy = rsp.getProvider();
        return economy != null;
    }

    public Economy getEconomy() {
        return economy;
    }

    public double getCurrentMoney(Player player) {
        return getEconomy().getBalance(player);
    }

    public void withdrawMoney(Player player, int amount) {
        getEconomy().withdrawPlayer(player, amount);
    }

    public void depositMoney(Player player, int amount) {
        getEconomy().depositPlayer(player, amount);
    }
}

package com.example.mc.item;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class Bill {

    private final NamespacedKey key;

    public Bill(JavaPlugin plugin) {
        this.key = new NamespacedKey(plugin, "money_value");
    }

    public ItemStack create(double amount) {
        ItemStack item = new ItemStack(Material.PAPER);

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§6" + String.format("%,d원 권", (int) amount));

        // 🔥 핵심: 실제 값 저장
        meta.getPersistentDataContainer().set(key, PersistentDataType.DOUBLE, amount);

        item.setItemMeta(meta);
        return item;
    }

    public Double getValue(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;

        ItemMeta meta = item.getItemMeta();

        return meta.getPersistentDataContainer().get(key, PersistentDataType.DOUBLE);
    }
}

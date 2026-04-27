package com.example.mc.listener;

import com.example.mc.item.Bill;
import lombok.RequiredArgsConstructor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public class BillListener implements Listener {
    private final Bill bill;
    private final Economy economy;

    @EventHandler
    public void onUse(PlayerInteractEvent e) {

        if (e.getItem() == null) return;

        ItemStack item = e.getItem();

        Double value = bill.getValue(item);
        if (value == null) return;

        // 💰 돈 지급
        economy.depositPlayer(e.getPlayer(), value);

        // 📦 아이템 감소
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            e.getPlayer().getInventory().removeItem(item);
        }

        e.getPlayer().sendMessage("§a" + value + "원 획득");
    }
}

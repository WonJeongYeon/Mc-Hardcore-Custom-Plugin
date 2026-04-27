package com.example.mc.command;

import com.example.mc.api.VaultManager;
import com.example.mc.item.Bill;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class BillCommand implements CommandExecutor {

    private final Bill bill;
    private final VaultManager vaultManager;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player player)) return true;

        if (args.length != 1) {
            player.sendMessage("§c사용법: /money <금액>");
            return true;
        }

        int amount;

        try {
            amount = Integer.parseInt(args[0]);
        } catch (Exception e) {
            player.sendMessage("§c올바른 금액 입력을 입력하세요.");
            return true;
        }

        if (vaultManager.getCurrentMoney(player) < amount) {
            player.sendMessage("§c보유금액이 모자랍니다.");
            return true;
        }

        player.getInventory().addItem(bill.create(amount));
        vaultManager.withdrawMoney(player, amount);
        player.sendMessage("§6" + amount + "원 출금");

        return true;
    }
}
package com.example.mc.item;

import lombok.RequiredArgsConstructor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

@RequiredArgsConstructor
public class Tomato implements Listener {

    private final JavaPlugin plugin;

    // 🔥 키 캐싱 (성능 + 깔끔함)
    private NamespacedKey STAGE_KEY;
    private NamespacedKey PLANTED_TIME_KEY;
    private NamespacedKey LAST_GROWTH_KEY;

    private void initKeys() {
        if (STAGE_KEY != null) return;

        STAGE_KEY = new NamespacedKey(plugin, "tomato_stage");
        PLANTED_TIME_KEY = new NamespacedKey(plugin, "planted_time");
        LAST_GROWTH_KEY = new NamespacedKey(plugin, "last_growth_time");
    }

    // ========================
    // 아이템 생성
    // ========================

    public ItemStack createTomatoSeed() {
        ItemStack item = new ItemStack(Material.BEETROOT_SEEDS);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§c토마토 씨앗");
        meta.setCustomModelData(2001);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createTomato() {
        ItemStack item = new ItemStack(Material.BEETROOT);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§c토마토");
        meta.setCustomModelData(2002);
        item.setItemMeta(meta);
        return item;
    }

    // ========================
    // 심기
    // ========================

    @EventHandler
    public void onPlant(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if (e.getItem() == null) return;

        ItemStack item = e.getItem();
        if (!item.hasItemMeta()) return;

        if (item.getItemMeta().getCustomModelData() != 2001) return;

        Block block = e.getClickedBlock();
        if (block == null) return;

        Location loc = block.getLocation().add(0.5, 1, 0.5);

        spawnTomato(loc, 0);

        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            e.getPlayer().getInventory().removeItem(item);
        }
    }

    // ========================
    // 생성
    // ========================

    public void spawnTomato(Location loc, int stage) {
        initKeys();

        ArmorStand stand = loc.getWorld().spawn(loc, ArmorStand.class);

        stand.setInvisible(true);
        stand.setGravity(false);
//        stand.setMarker(true);

        long now = System.currentTimeMillis();

        stand.getPersistentDataContainer().set(STAGE_KEY, PersistentDataType.INTEGER, stage);
        stand.getPersistentDataContainer().set(PLANTED_TIME_KEY, PersistentDataType.LONG, now);
        stand.getPersistentDataContainer().set(LAST_GROWTH_KEY, PersistentDataType.LONG, now);

        updateModel(stand, stage);
    }

    // ========================
    // 모델 변경
    // ========================

    private void updateModel(ArmorStand stand, int stage) {
        ItemStack model = new ItemStack(Material.CARROT_ON_A_STICK);
        ItemMeta meta = model.getItemMeta();
        meta.setCustomModelData(2100 + stage);
        model.setItemMeta(meta);

        stand.getEquipment().setHelmet(model);
    }

    // ========================
    // 성장 스케줄러 (핵심)
    // ========================

    public void growTomatoes() {
        initKeys();

        long now = System.currentTimeMillis();

        for (World world : Bukkit.getWorlds()) {
            for (ArmorStand stand : world.getEntitiesByClass(ArmorStand.class)) {

                Integer stage = stand.getPersistentDataContainer().get(STAGE_KEY, PersistentDataType.INTEGER);
                if (!stand.isValid()) continue;
                if (stage == null) continue;
                if (stage >= 3) continue;

                Long plantedTime = stand.getPersistentDataContainer().get(PLANTED_TIME_KEY, PersistentDataType.LONG);
                Long lastGrowth = stand.getPersistentDataContainer().get(LAST_GROWTH_KEY, PersistentDataType.LONG);

                if (plantedTime == null || lastGrowth == null) continue;

                // ⏱ 최소 성장 간격 (20초)
                if (now - lastGrowth < 20_000) continue;

                // ⏱ 최소 전체 성장 시간 (30초 이후부터 성장 가능)
                if (now - plantedTime < 30_000) continue;

                // 🎲 기본 확률
                double chance = 0.3;

                // 🔥 확장 포인트
                chance += getWaterBonus(stand);
                chance += getWeatherBonus(stand);
                chance += getFertilizerBonus(stand);

                if (Math.random() < chance) {
                    stage++;

                    stand.getPersistentDataContainer().set(STAGE_KEY, PersistentDataType.INTEGER, stage);
                    stand.getPersistentDataContainer().set(LAST_GROWTH_KEY, PersistentDataType.LONG, now);

                    updateModel(stand, stage);
                }
            }
        }
    }

    // ========================
    // 수확
    // ========================

    @EventHandler
    public void onHarvest(PlayerInteractAtEntityEvent e) {
        if (!(e.getRightClicked() instanceof ArmorStand stand)) return;

        initKeys();

        Integer stage = stand.getPersistentDataContainer().get(STAGE_KEY, PersistentDataType.INTEGER);
        if (stage == null) return;

        if (stage == 3) {
            stand.remove();

            int amount = 1 + (int)(Math.random() * 3); // 1~3개

            for (int i = 0; i < amount; i++) {
                e.getPlayer().getInventory().addItem(createTomato());
            }
        }
    }

    // ========================
    // 확장 포인트 (나중용)
    // ========================

    private double getWaterBonus(ArmorStand stand) {
        Location loc = stand.getLocation();
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                Block b = loc.clone().add(x, 0, z).getBlock();
                if (b.getType() == Material.WATER) {
                    return 0.1;
                }
            }
        }
        return 0.0;
    }

    private double getWeatherBonus(ArmorStand stand) {
        return stand.getWorld().hasStorm() ? 0.1 : 0.0;
    }

    private double getFertilizerBonus(ArmorStand stand) {
        return 0.0; // TODO
    }
}
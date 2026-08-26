package com.github.upgradeablehopper.listener;

import com.github.upgradeablehopper.UpgradeableHoppers;
import com.github.upgradeablehopper.config.ConfigManager;
import com.github.upgradeablehopper.config.HopperTier;
import com.github.upgradeablehopper.gui.UpgradeGui;
import com.github.upgradeablehopper.hopper.HopperData;
import com.github.upgradeablehopper.hopper.HopperManager;
import com.github.upgradeablehopper.util.ChatUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

public class GuiListener implements Listener {

    private final UpgradeableHoppers plugin;
    private final HopperManager hopperManager;

    public GuiListener(UpgradeableHoppers plugin) {
        this.plugin = plugin;
        this.hopperManager = plugin.getHopperManager();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inv = event.getInventory();
        if (!(inv.getHolder() instanceof UpgradeGui gui)) return;

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;

        int slot = event.getRawSlot();
        int upgradeSlot = plugin.getConfig().getInt("gui.items.upgrade-slot", 15);
        int closeSlot = plugin.getConfig().getInt("gui.items.close-slot", 22);

        if (slot == closeSlot) {
            player.closeInventory();
        } else if (slot == upgradeSlot) {
            handleUpgrade(player, gui);
        }
    }

    private void handleUpgrade(Player player, UpgradeGui gui) {
        ConfigManager config = plugin.getConfigManager();
        if (plugin.getGuiManager().isDebounced(player)) {
            ChatUtil.sendMessage(player, config.getPrefix() + config.getMessage("rapid-click"));
            return;
        }

        if (!player.hasPermission("upgradeablehopper.upgrade")) {
            ChatUtil.sendMessage(player, config.getPrefix() + config.getMessage("no-permission"));
            return;
        }

        HopperData data = gui.getHopperData();
        if (!data.isValidBlock()) {
            ChatUtil.sendMessage(player, config.getPrefix() + config.getMessage("hopper-invalid"));
            player.closeInventory();
            return;
        }

        int currentTierNum = data.getTier();
        if (!config.hasNextTier(currentTierNum)) {
            ChatUtil.sendMessage(player, config.getPrefix() + config.getMessage("already-max-tier"));
            return;
        }

        HopperTier nextTier = config.getNextTier(currentTierNum);
        if (nextTier == null) return;

        double cost = nextTier.getCost();
        if (cost > 0 && !plugin.getEconomyManager().hasBalance(player, cost)) {
            double balance = plugin.getEconomyManager().getBalance(player);
            String msg = config.getMessage("not-enough-money")
                    .replace("{COST}", plugin.getEconomyManager().format(cost))
                    .replace("{BALANCE}", plugin.getEconomyManager().format(balance));
            ChatUtil.sendMessage(player, config.getPrefix() + msg);
            return;
        }

        boolean success = hopperManager.upgradeHopper(player, data);
        if (success) {
            gui.buildGui();
            if (config.isEnableSounds()) {
                player.playSound(player.getLocation(), config.getUpgradeSound(), config.getUpgradeSoundVolume(), config.getUpgradeSoundPitch());
            }
            if (config.isEnableParticles()) {
                Location loc = data.getLocation();
                if (loc != null) {
                    loc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc.clone().add(0.5, 0.7, 0.5), 15, 0.3, 0.3, 0.3, 0.05);
                }
            }
            String successMsg = config.getMessage("upgrade-success")
                    .replace("{TIER}", String.valueOf(data.getTier()))
                    .replace("{SPEED}", String.format("%.0f", nextTier.getSpeed()))
                    .replace("{COST}", plugin.getEconomyManager().format(cost));
            ChatUtil.sendMessage(player, config.getPrefix() + successMsg);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof UpgradeGui) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            plugin.getGuiManager().removePlayer(player.getUniqueId());
        }
    }
}
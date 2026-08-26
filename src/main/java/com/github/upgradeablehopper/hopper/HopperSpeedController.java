package com.github.upgradeablehopper.hopper;

import com.github.upgradeablehopper.UpgradeableHoppers;
import com.github.upgradeablehopper.config.HopperTier;
import org.bukkit.Bukkit;
import org.bukkit.block.Hopper;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class HopperSpeedController {

    private final UpgradeableHoppers plugin;
    private final HopperManager hopperManager;

    public HopperSpeedController(UpgradeableHoppers plugin, HopperManager hopperManager) {
        this.plugin = plugin;
        this.hopperManager = hopperManager;
    }

    public void processHopperTransfer(Hopper hopper, Inventory sourceInventory, Inventory destinationInventory) {
        if (hopper == null || !hopper.isPlaced()) return;

        HopperData data = hopperManager.getHopper(hopper.getLocation());
        if (data == null || data.getTier() <= 1) return;

        HopperTier tier = plugin.getConfigManager().getTier(data.getTier());
        if (tier == null) return;

        int extraItems = tier.getItemsPerTransfer() - 1;
        if (extraItems > 0 && sourceInventory != null && destinationInventory != null) {
            transferExtraItems(sourceInventory, destinationInventory, extraItems, data);
        }

        int cooldownTicks = Math.max(1, tier.getCooldownTicks());
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (hopper.isPlaced() && hopper.getBlock().getState() instanceof Hopper activeHopper) {
                activeHopper.setTransferCooldown(cooldownTicks);
                activeHopper.update(false, false);
            }
        });

        data.incrementItemsTransferred(tier.getItemsPerTransfer());
    }

    public void processHopperPickup(Hopper hopper) {
        if (hopper == null || !hopper.isPlaced()) return;

        HopperData data = hopperManager.getHopper(hopper.getLocation());
        if (data == null || data.getTier() <= 1) return;

        HopperTier tier = plugin.getConfigManager().getTier(data.getTier());
        if (tier == null) return;

        int cooldownTicks = Math.max(1, tier.getCooldownTicks());
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (hopper.isPlaced() && hopper.getBlock().getState() instanceof Hopper activeHopper) {
                activeHopper.setTransferCooldown(cooldownTicks);
                activeHopper.update(false, false);
            }
        });
    }

    private void transferExtraItems(Inventory source, Inventory destination, int count, HopperData data) {
        int transferred = 0;
        for (int i = 0; i < source.getSize() && transferred < count; i++) {
            ItemStack stack = source.getItem(i);
            if (stack == null || stack.getType().isAir() || stack.getAmount() <= 0) continue;

            int toMove = Math.min(stack.getAmount(), count - transferred);
            ItemStack itemToMove = stack.clone();
            itemToMove.setAmount(toMove);

            HashMap<Integer, ItemStack> leftover = destination.addItem(itemToMove);
            int moved = toMove;
            if (!leftover.isEmpty()) {
                for (ItemStack remaining : leftover.values()) {
                    moved -= remaining.getAmount();
                }
            }

            if (moved > 0) {
                stack.setAmount(stack.getAmount() - moved);
                if (stack.getAmount() <= 0) source.setItem(i, null);
                else source.setItem(i, stack);
                transferred += moved;
            }
        }
    }
}
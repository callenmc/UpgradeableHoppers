package com.github.upgradeablehopper.listener;

import com.github.upgradeablehopper.UpgradeableHoppers;
import com.github.upgradeablehopper.hopper.HopperSpeedController;
import org.bukkit.block.Hopper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.InventoryHolder;

public class HopperListener implements Listener {

    private final HopperSpeedController speedController;

    public HopperListener(UpgradeableHoppers plugin) {
        this.speedController = plugin.getHopperManager().getSpeedController();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        InventoryHolder initiator = event.getInitiator().getHolder();
        InventoryHolder source = event.getSource().getHolder();
        InventoryHolder dest = event.getDestination().getHolder();

        if (initiator instanceof Hopper hopper) {
            speedController.processHopperTransfer(hopper, event.getSource(), event.getDestination());
        } else if (source instanceof Hopper sourceHopper) {
            speedController.processHopperTransfer(sourceHopper, event.getSource(), event.getDestination());
        } else if (dest instanceof Hopper destHopper) {
            speedController.processHopperTransfer(destHopper, event.getSource(), event.getDestination());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryPickupItem(InventoryPickupItemEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof Hopper hopper) {
            speedController.processHopperPickup(hopper);
        }
    }
}
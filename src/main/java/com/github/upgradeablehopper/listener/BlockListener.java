package com.github.upgradeablehopper.listener;

import com.github.upgradeablehopper.UpgradeableHoppers;
import com.github.upgradeablehopper.config.ConfigManager;
import com.github.upgradeablehopper.hopper.HopperData;
import com.github.upgradeablehopper.hopper.HopperManager;
import com.github.upgradeablehopper.storage.PdcStorage;
import com.github.upgradeablehopper.util.ChatUtil;
import com.github.upgradeablehopper.util.ItemBuilder;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class BlockListener implements Listener {

    private final UpgradeableHoppers plugin;
    private final HopperManager hopperManager;

    public BlockListener(UpgradeableHoppers plugin) {
        this.plugin = plugin;
        this.hopperManager = plugin.getHopperManager();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.HOPPER) return;

        Player player = event.getPlayer();
        ConfigManager config = plugin.getConfigManager();

        if ("SNEAK_RIGHT_CLICK".equalsIgnoreCase(config.getInteractionMode()) && !player.isSneaking()) {
            return;
        }

        if (!player.hasPermission("upgradeablehopper.use")) {
            ChatUtil.sendMessage(player, config.getPrefix() + config.getMessage("no-permission"));
            return;
        }

        event.setCancelled(true);
        HopperData data = hopperManager.getOrCreateHopper(block.getLocation(), player);
        plugin.getGuiManager().openUpgradeGui(player, data);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();
        if (block.getType() != Material.HOPPER) return;

        ItemStack item = event.getItemInHand();
        if (item.getType() != Material.HOPPER || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        if (hopperManager.getStorage() instanceof PdcStorage pdcStorage) {
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            if (pdc.has(pdcStorage.getTierKey(), PersistentDataType.INTEGER)) {
                Integer tier = pdc.get(pdcStorage.getTierKey(), PersistentDataType.INTEGER);
                if (tier != null && tier > 1) {
                    HopperData data = new HopperData(
                            block.getWorld().getName(),
                            block.getX(),
                            block.getY(),
                            block.getZ(),
                            tier,
                            event.getPlayer().getUniqueId(),
                            System.currentTimeMillis(),
                            0L
                    );
                    hopperManager.registerHopper(data);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.HOPPER) return;

        Location loc = block.getLocation();
        HopperData data = hopperManager.getHopper(loc);
        if (data == null) return;

        int tier = data.getTier();
        hopperManager.removeHopper(loc);

        if (tier > 1 && plugin.getConfigManager().isPreserveTierOnBreak() && event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            event.setDropItems(false);
            ItemBuilder builder = new ItemBuilder(Material.HOPPER)
                    .name("&6Tier " + tier + " Hopper")
                    .addLoreLine("&7Tier: &6Tier " + tier)
                    .addLoreLine("&7Speed: &e" + (tier * 4) + " items/sec")
                    .addLoreLine("&8Place down to use this upgraded hopper.")
                    .addGlow(true);

            if (hopperManager.getStorage() instanceof PdcStorage pdcStorage) {
                builder.setPdc(pdcStorage.getTierKey(), PersistentDataType.INTEGER, tier);
            }

            block.getWorld().dropItemNaturally(loc, builder.build());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkUnload(ChunkUnloadEvent event) {
        hopperManager.handleChunkUnload(event.getChunk().getX(), event.getChunk().getZ(), event.getWorld().getName());
    }
}
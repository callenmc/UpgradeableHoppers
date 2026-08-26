package com.github.upgradeablehopper.storage;

import com.github.upgradeablehopper.UpgradeableHoppers;
import com.github.upgradeablehopper.hopper.HopperData;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class PdcStorage implements StorageManager {

    private final UpgradeableHoppers plugin;
    private final NamespacedKey tierKey;
    private final NamespacedKey ownerKey;
    private final NamespacedKey createdKey;
    private final NamespacedKey transfersKey;

    public PdcStorage(UpgradeableHoppers plugin) {
        this.plugin = plugin;
        this.tierKey = new NamespacedKey(plugin, "hopper_tier");
        this.ownerKey = new NamespacedKey(plugin, "hopper_owner");
        this.createdKey = new NamespacedKey(plugin, "hopper_created");
        this.transfersKey = new NamespacedKey(plugin, "hopper_transfers");
    }

    public NamespacedKey getTierKey() { return tierKey; }

    @Override public void init() { plugin.getLogger().info("PDC Storage backend initialized."); }
    @Override public void shutdown() {}

    @Override
    public Optional<HopperData> loadHopper(Location location) {
        if (location == null || !location.isWorldLoaded()) return Optional.empty();
        Block block = location.getBlock();
        if (!(block.getState() instanceof TileState tileState)) return Optional.empty();

        PersistentDataContainer pdc = tileState.getPersistentDataContainer();
        if (!pdc.has(tierKey, PersistentDataType.INTEGER)) return Optional.empty();

        Integer tier = pdc.get(tierKey, PersistentDataType.INTEGER);
        if (tier == null || tier <= 0) return Optional.empty();

        String ownerStr = pdc.get(ownerKey, PersistentDataType.STRING);
        UUID owner = (ownerStr != null && !ownerStr.isEmpty()) ? UUID.fromString(ownerStr) : null;
        Long created = pdc.get(createdKey, PersistentDataType.LONG);
        Long transfers = pdc.get(transfersKey, PersistentDataType.LONG);

        return Optional.of(new HopperData(
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ(),
                tier,
                owner,
                created != null ? created : System.currentTimeMillis(),
                transfers != null ? transfers : 0L
        ));
    }

    @Override
    public void saveHopper(HopperData data) {
        if (data == null) return;
        Location loc = data.getLocation();
        if (loc == null || !loc.isWorldLoaded()) return;

        Block block = loc.getBlock();
        if (block.getState() instanceof TileState tileState) {
            PersistentDataContainer pdc = tileState.getPersistentDataContainer();
            pdc.set(tierKey, PersistentDataType.INTEGER, data.getTier());
            if (data.getOwnerUuid() != null) {
                pdc.set(ownerKey, PersistentDataType.STRING, data.getOwnerUuid().toString());
            }
            pdc.set(createdKey, PersistentDataType.LONG, data.getCreationTime());
            pdc.set(transfersKey, PersistentDataType.LONG, data.getTotalItemsTransferred());
            tileState.update(true, false);
        }
    }

    @Override
    public void deleteHopper(Location location) {
        if (location == null || !location.isWorldLoaded()) return;
        Block block = location.getBlock();
        if (block.getState() instanceof TileState tileState) {
            PersistentDataContainer pdc = tileState.getPersistentDataContainer();
            pdc.remove(tierKey);
            pdc.remove(ownerKey);
            tileState.update(true, false);
        }
    }

    @Override
    public Collection<HopperData> loadAllHoppers() { return Collections.emptyList(); }
}
package com.github.upgradeablehopper.gui;

import com.github.upgradeablehopper.UpgradeableHoppers;
import com.github.upgradeablehopper.config.ConfigManager;
import com.github.upgradeablehopper.config.HopperTier;
import com.github.upgradeablehopper.hopper.HopperData;
import com.github.upgradeablehopper.util.ChatUtil;
import com.github.upgradeablehopper.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class UpgradeGui implements InventoryHolder {

    private final UpgradeableHoppers plugin;
    private final Player player;
    private final HopperData hopperData;
    private final Inventory inventory;

    public UpgradeGui(UpgradeableHoppers plugin, Player player, HopperData hopperData) {
        this.plugin = plugin;
        this.player = player;
        this.hopperData = hopperData;

        int tierNum = hopperData.getTier();
        String rawTitle = plugin.getConfig().getString("gui.title", "&8Upgradeable Hopper &7(&eTier {CURRENT_TIER}&7)");
        String title = ChatUtil.colorize(rawTitle.replace("{CURRENT_TIER}", String.valueOf(tierNum)));

        int size = plugin.getConfig().getInt("gui.size", 27);
        this.inventory = Bukkit.createInventory(this, size, title);

        buildGui();
    }

    public void buildGui() {
        inventory.clear();
        FileConfiguration config = plugin.getConfig();
        ConfigManager configManager = plugin.getConfigManager();

        ItemStack filler = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).name(" ").hideFlags().build();
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, filler);
        }

        int currentTierNum = hopperData.getTier();
        HopperTier currentTier = configManager.getTier(currentTierNum);
        boolean hasNext = configManager.hasNextTier(currentTierNum);
        HopperTier nextTier = hasNext ? configManager.getNextTier(currentTierNum) : null;

        double currentSpeed = currentTier != null ? currentTier.getSpeed() : 2.0;
        double nextSpeed = nextTier != null ? nextTier.getSpeed() : currentSpeed;
        double cost = nextTier != null ? nextTier.getCost() : 0.0;
        String costFormatted = plugin.getEconomyManager().format(cost);

        int infoSlot = config.getInt("gui.items.info-slot", 11);
        ItemStack infoItem = new ItemBuilder(Material.HOPPER)
                .name(config.getString("gui.item-display.hopper-icon.name", "&e&lHopper Information"))
                .addLoreLine("&7Status: &aActive")
                .addLoreLine("&7Location: &f" + hopperData.getX() + ", " + hopperData.getY() + ", " + hopperData.getZ() + " &8(" + hopperData.getWorldName() + ")")
                .addLoreLine("")
                .addLoreLine("&7Current Tier: &6Tier " + currentTierNum)
                .addLoreLine("&7Current Speed: &a" + (int)currentSpeed + " items/sec")
                .addLoreLine("")
                .addLoreLine("&7Next Tier: &eTier " + (hasNext ? (currentTierNum + 1) : currentTierNum))
                .addLoreLine("&7Next Speed: &b" + (int)nextSpeed + " items/sec")
                .addLoreLine("&7Upgrade Cost: &a" + costFormatted)
                .hideFlags()
                .build();
        inventory.setItem(infoSlot, infoItem);

        int upgradeSlot = config.getInt("gui.items.upgrade-slot", 15);
        if (hasNext) {
            ItemStack upgradeButton = new ItemBuilder(Material.LIME_CONCRETE)
                    .name("&a&l[ UPGRADE HOPPER ]")
                    .addLoreLine("&7Click to upgrade this hopper to &eTier " + (currentTierNum + 1) + "&7.")
                    .addLoreLine("")
                    .addLoreLine("&7Cost: &a" + costFormatted)
                    .addLoreLine("&7New Speed: &b" + (int)nextSpeed + " items/sec")
                    .addLoreLine("")
                    .addLoreLine("&e▶ Click to purchase upgrade")
                    .addGlow(true)
                    .build();
            inventory.setItem(upgradeSlot, upgradeButton);
        } else {
            ItemStack maxButton = new ItemBuilder(Material.NETHER_STAR)
                    .name("&6&l[ MAXIMUM TIER REACHED ]")
                    .addLoreLine("&7This hopper is operating at maximum output!")
                    .addLoreLine("")
                    .addLoreLine("&7Tier: &6Tier " + currentTierNum + " &8(MAX)")
                    .addLoreLine("&7Speed: &a" + (int)currentSpeed + " items/sec")
                    .addLoreLine("")
                    .addLoreLine("&a✓ Fully Upgraded")
                    .addGlow(true)
                    .build();
            inventory.setItem(upgradeSlot, maxButton);
        }

        int closeSlot = config.getInt("gui.items.close-slot", 22);
        ItemStack closeButton = new ItemBuilder(Material.BARRIER)
                .name("&c&lClose")
                .addLoreLine("&7Click to close this menu.")
                .build();
        inventory.setItem(closeSlot, closeButton);
    }

    public HopperData getHopperData() { return hopperData; }
    public Player getPlayer() { return player; }
    @Override public Inventory getInventory() { return inventory; }
}
package com.github.upgradeablehopper;

import com.github.upgradeablehopper.command.HopperCommand;
import com.github.upgradeablehopper.config.ConfigManager;
import com.github.upgradeablehopper.economy.EconomyManager;
import com.github.upgradeablehopper.gui.GuiManager;
import com.github.upgradeablehopper.hopper.HopperManager;
import com.github.upgradeablehopper.listener.BlockListener;
import com.github.upgradeablehopper.listener.GuiListener;
import com.github.upgradeablehopper.listener.HopperListener;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class UpgradeableHoppers extends JavaPlugin {

    private static UpgradeableHoppers instance;
    private ConfigManager configManager;
    private EconomyManager economyManager;
    private HopperManager hopperManager;
    private GuiManager guiManager;

    @Override
    public void onEnable() {
        instance = this;
        long startTime = System.currentTimeMillis();

        this.configManager = new ConfigManager(this);
        this.configManager.loadConfigurations();

        this.economyManager = new EconomyManager(this);
        this.economyManager.setupEconomy();

        this.hopperManager = new HopperManager(this);
        this.hopperManager.init();

        this.guiManager = new GuiManager(this);

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new BlockListener(this), this);
        pm.registerEvents(new GuiListener(this), this);
        pm.registerEvents(new HopperListener(this), this);

        PluginCommand cmd = getCommand("upgradeablehoppers");
        if (cmd != null) {
            HopperCommand executor = new HopperCommand(this);
            cmd.setExecutor(executor);
            cmd.setTabCompleter(executor);
        }

        getLogger().info("UpgradeableHoppers enabled in " + (System.currentTimeMillis() - startTime) + "ms!");
    }

    @Override
    public void onDisable() {
        if (hopperManager != null) {
            hopperManager.shutdown();
        }
        instance = null;
    }

    public static UpgradeableHoppers getInstance() { return instance; }
    public ConfigManager getConfigManager() { return configManager; }
    public EconomyManager getEconomyManager() { return economyManager; }
    public HopperManager getHopperManager() { return hopperManager; }
    public GuiManager getGuiManager() { return guiManager; }
}
package com.github.upgradeablehopper.economy;

import com.github.upgradeablehopper.UpgradeableHoppers;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyManager {

    private final UpgradeableHoppers plugin;
    private Economy economy = null;
    private boolean economyEnabled = false;

    public EconomyManager(UpgradeableHoppers plugin) {
        this.plugin = plugin;
    }

    public boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("Vault plugin not found! Paid upgrades disabled until Vault is installed.");
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            plugin.getLogger().warning("No Vault Economy Provider found (e.g. EssentialsX).");
            return false;
        }

        this.economy = rsp.getProvider();
        this.economyEnabled = (this.economy != null);
        return this.economyEnabled;
    }

    public boolean isEconomyAvailable() {
        return economyEnabled && economy != null;
    }

    public boolean hasBalance(Player player, double amount) {
        if (amount <= 0) return true;
        if (!isEconomyAvailable()) return false;
        return economy.has(player, amount);
    }

    public boolean withdraw(Player player, double amount) {
        if (amount <= 0) return true;
        if (!isEconomyAvailable()) return false;
        EconomyResponse response = economy.withdrawPlayer(player, amount);
        return response.transactionSuccess();
    }

    public double getBalance(OfflinePlayer player) {
        if (!isEconomyAvailable() || player == null) return 0.0;
        return economy.getBalance(player);
    }

    public String format(double amount) {
        if (isEconomyAvailable()) return economy.format(amount);
        return String.format("$%,.2f", amount);
    }
}
package com.github.upgradeablehopper.command;

import com.github.upgradeablehopper.UpgradeableHoppers;
import com.github.upgradeablehopper.config.ConfigManager;
import com.github.upgradeablehopper.config.HopperTier;
import com.github.upgradeablehopper.hopper.HopperData;
import com.github.upgradeablehopper.hopper.HopperManager;
import com.github.upgradeablehopper.util.ChatUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HopperCommand implements CommandExecutor, TabCompleter {

    private final UpgradeableHoppers plugin;
    private final HopperManager hopperManager;

    public HopperCommand(UpgradeableHoppers plugin) {
        this.plugin = plugin;
        this.hopperManager = plugin.getHopperManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ConfigManager config = plugin.getConfigManager();

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            ChatUtil.sendMessage(sender, config.getMessage("help-header"));
            ChatUtil.sendMessage(sender, config.getMessage("help-info"));
            if (sender.hasPermission("upgradeablehopper.admin")) {
                ChatUtil.sendMessage(sender, config.getMessage("help-set"));
                ChatUtil.sendMessage(sender, config.getMessage("help-reload"));
            }
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("reload")) {
            if (!sender.hasPermission("upgradeablehopper.admin")) {
                ChatUtil.sendMessage(sender, config.getPrefix() + config.getMessage("no-permission"));
                return true;
            }
            plugin.reloadPlugin();
            ChatUtil.sendMessage(sender, config.getPrefix() + config.getMessage("reload-success"));
            return true;
        }

        if (!(sender instanceof Player player)) {
            ChatUtil.sendMessage(sender, config.getPrefix() + config.getMessage("not-a-player"));
            return true;
        }

        if (sub.equals("info")) {
            Block target = player.getTargetBlockExact(5);
            if (target == null || target.getType() != Material.HOPPER) {
                ChatUtil.sendMessage(player, config.getPrefix() + config.getMessage("no-target-hopper"));
                return true;
            }

            HopperData data = hopperManager.getOrCreateHopper(target.getLocation(), player);
            HopperTier tier = config.getTier(data.getTier());
            double speed = tier != null ? tier.getSpeed() : 2.0;

            String msg = config.getMessage("admin-inspect")
                    .replace("{X}", String.valueOf(target.getX()))
                    .replace("{Y}", String.valueOf(target.getY()))
                    .replace("{Z}", String.valueOf(target.getZ()))
                    .replace("{TIER}", String.valueOf(data.getTier()))
                    .replace("{SPEED}", String.format("%.0f", speed));
            ChatUtil.sendMessage(player, config.getPrefix() + msg);
            return true;
        }

        if (sub.equals("set")) {
            if (!player.hasPermission("upgradeablehopper.admin")) {
                ChatUtil.sendMessage(player, config.getPrefix() + config.getMessage("no-permission"));
                return true;
            }

            if (args.length < 2) {
                ChatUtil.sendMessage(player, config.getPrefix() + "&cUsage: /uh set <1-" + config.getMaxTier() + ">");
                return true;
            }

            int targetTier;
            try {
                targetTier = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                ChatUtil.sendMessage(player, config.getPrefix() + config.getMessage("invalid-tier").replace("{MAX_TIER}", String.valueOf(config.getMaxTier())));
                return true;
            }

            Block target = player.getTargetBlockExact(5);
            if (target == null || target.getType() != Material.HOPPER) {
                ChatUtil.sendMessage(player, config.getPrefix() + config.getMessage("no-target-hopper"));
                return true;
            }

            HopperData data = hopperManager.getOrCreateHopper(target.getLocation(), player);
            data.setTier(targetTier);
            hopperManager.registerHopper(data);

            String msg = config.getMessage("admin-tier-set")
                    .replace("{X}", String.valueOf(target.getX()))
                    .replace("{Y}", String.valueOf(target.getY()))
                    .replace("{Z}", String.valueOf(target.getZ()))
                    .replace("{TIER}", String.valueOf(targetTier));
            ChatUtil.sendMessage(player, config.getPrefix() + msg);
            return true;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            list.add("info");
            list.add("help");
            if (sender.hasPermission("upgradeablehopper.admin")) {
                list.add("set");
                list.add("reload");
            }
            return list;
        }
        return Collections.emptyList();
    }
}
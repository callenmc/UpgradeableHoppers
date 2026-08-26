package com.github.upgradeablehopper.util;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {

    private final ItemStack itemStack;
    private final ItemMeta meta;

    public ItemBuilder(Material material) {
        this(material, 1);
    }

    public ItemBuilder(Material material, int amount) {
        this.itemStack = new ItemStack(material != null ? material : Material.STONE, Math.max(1, amount));
        this.meta = this.itemStack.getItemMeta();
    }

    public ItemBuilder name(String displayName) {
        if (meta != null && displayName != null) {
            meta.setDisplayName(ChatUtil.colorize(displayName));
        }
        return this;
    }

    public ItemBuilder lore(List<String> loreLines) {
        if (meta != null && loreLines != null) {
            List<String> coloredLore = new ArrayList<>(loreLines.size());
            for (String line : loreLines) {
                coloredLore.add(ChatUtil.colorize(line));
            }
            meta.setLore(coloredLore);
        }
        return this;
    }

    public ItemBuilder addGlow(boolean glow) {
        if (meta != null && glow) {
            Enchantment ench = CompatibilityUtil.getGlowEnchantment();
            if (ench != null) {
                meta.addEnchant(ench, 1, true);
            }
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        return this;
    }

    public ItemBuilder hideFlags() {
        if (meta != null) {
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
        }
        return this;
    }

    public <T, Z> ItemBuilder setPdc(NamespacedKey key, PersistentDataType<T, Z> type, Z value) {
        if (meta != null && key != null && type != null && value != null) {
            meta.getPersistentDataContainer().set(key, type, value);
        }
        return this;
    }

    public ItemStack build() {
        if (meta != null) {
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }
}
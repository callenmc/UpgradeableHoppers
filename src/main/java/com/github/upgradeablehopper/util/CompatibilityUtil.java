package com.github.upgradeablehopper.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;

import java.lang.reflect.Field;
import java.util.Locale;

public final class CompatibilityUtil {

    private static Enchantment cachedGlowEnchantment = null;

    private CompatibilityUtil() {}

    public static Enchantment getGlowEnchantment() {
        if (cachedGlowEnchantment != null) return cachedGlowEnchantment;
        try {
            Enchantment ench = Enchantment.getByKey(NamespacedKey.minecraft("unbreaking"));
            if (ench != null) {
                cachedGlowEnchantment = ench;
                return ench;
            }
        } catch (Throwable ignored) {}
        try {
            Field field = Enchantment.class.getField("UNBREAKING");
            Object val = field.get(null);
            if (val instanceof Enchantment ench) {
                cachedGlowEnchantment = ench;
                return ench;
            }
        } catch (Throwable ignored) {}
        try {
            Field field = Enchantment.class.getField("DURABILITY");
            Object val = field.get(null);
            if (val instanceof Enchantment ench) {
                cachedGlowEnchantment = ench;
                return ench;
            }
        } catch (Throwable ignored) {}
        return null;
    }

    public static void spawnUpgradeParticles(Location loc, String configuredType, int count) {
        if (loc == null || loc.getWorld() == null) return;
        Particle particle = null;
        if (configuredType != null && !configuredType.isBlank()) {
            particle = matchParticle(configuredType.trim().toUpperCase(Locale.ROOT));
        }
        if (particle == null) particle = matchParticle("HAPPY_VILLAGER");
        if (particle == null) particle = matchParticle("VILLAGER_HAPPY");
        if (particle == null) particle = matchParticle("FIREWORK");

        if (particle != null) {
            try {
                loc.getWorld().spawnParticle(particle, loc.clone().add(0.5, 0.7, 0.5), Math.max(1, count), 0.3, 0.3, 0.3, 0.05);
            } catch (Throwable ignored) {}
        }
    }

    public static Particle matchParticle(String name) {
        if (name == null || name.isEmpty()) return null;
        try { return Particle.valueOf(name); } catch (IllegalArgumentException e) { return null; }
    }

    public static Sound matchSound(String name, Sound fallback) {
        if (name == null || name.isEmpty()) return fallback;
        try { return Sound.valueOf(name.toUpperCase(Locale.ROOT)); } catch (IllegalArgumentException e) { return fallback; }
    }

    public static Material matchMaterial(String name, Material fallback) {
        if (name == null || name.isEmpty()) return fallback;
        Material mat = Material.matchMaterial(name);
        return mat != null ? mat : fallback;
    }
}
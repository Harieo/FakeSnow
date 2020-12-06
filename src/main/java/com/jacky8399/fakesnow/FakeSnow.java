package com.jacky8399.fakesnow;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.google.common.collect.Maps;
import com.jacky8399.fakesnow.events.Events;
import com.jacky8399.fakesnow.events.PacketListener;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.EnumFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Logger;

public final class FakeSnow extends JavaPlugin {
    public enum WeatherType {
        DEFAULT(null),
        RAIN(Biome.FOREST),
        SNOW(Biome.SNOWY_TAIGA),
        NONE(Biome.THE_VOID);
        public Biome biome;
        WeatherType(Biome biome) {
            this.biome = biome;
        }
    }

    public static EnumFlag<WeatherType> CUSTOM_WEATHER_TYPE;
    private static FakeSnow INSTANCE;

    private final HashMap<ChunkCoordIntPair, HashSet<ProtectedRegion>> regionChunkCache = Maps.newHashMap();
    private final WeakHashMap<World, ProtectedRegion> regionWorldCache = new WeakHashMap<>();

    public Logger logger;

    @Override
    public void onEnable() {
        INSTANCE = this;
        logger = getLogger();
        logger.info("FakeSnow is loading");

        Bukkit.getPluginManager().registerEvents(new Events(), this);
        getCommand("fakesnow").setExecutor(new CommandFakesnow());
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketListener());
    }

    @Override
    public void onLoad() {
        super.onLoad();
        try {
            CUSTOM_WEATHER_TYPE = new EnumFlag<>("custom-weather-type", WeatherType.class);
            WorldGuard.getInstance().getFlagRegistry().register(CUSTOM_WEATHER_TYPE);
        } catch (FlagConflictException e) {
            CUSTOM_WEATHER_TYPE = null;
            throw new Error("Another plugin already registered 'custom-weather-type' flag!", e);
        }
    }

    @Override
    public void onDisable() {
        regionChunkCache.clear();
        regionWorldCache.clear();
    }

    public Map<ChunkCoordIntPair, HashSet<ProtectedRegion>> getRegionChunkCache() {
        return regionChunkCache;
    }

    public Map<World, ProtectedRegion> getRegionWorldCache() {
        return regionWorldCache;
    }

    public static FakeSnow get() {
        return INSTANCE;
    }
}

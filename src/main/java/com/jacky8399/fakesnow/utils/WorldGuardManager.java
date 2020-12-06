package com.jacky8399.fakesnow.utils;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldLoadEvent;

import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jacky8399.fakesnow.FakeSnow;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.GlobalProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.*;

public class WorldGuardManager implements Listener {

	private final HashMap<ChunkCoordIntPair, HashSet<ProtectedRegion>> regionChunkCache = Maps.newHashMap();
	private final WeakHashMap<World, ProtectedRegion> regionWorldCache = new WeakHashMap<>();

	@EventHandler(priority = EventPriority.HIGH)
	public void onWorldLoad(WorldLoadEvent e) {
		scanWorldForRegions(e.getWorld());
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onChunkLoad(ChunkLoadEvent e) {
		scanChunkForRegions(e.getChunk());
	}

	public void scanWorldForRegions(World world) {
		RegionManager manager = getRegionManager(world);
		if (manager != null) {
			for (ProtectedRegion region : manager.getRegions().values()) {
				addRegionToCache(region, world);
			}
			FakeSnow.get().logger.info(manager.getRegions().size() + " regions have been put in the cache.");
		}
	}

	public void scanChunkForRegions(Chunk chunk) {
		World world = chunk.getWorld();
		RegionManager manager = getRegionManager(world);
		if (manager != null) {
			ProtectedCuboidRegion area = new ProtectedCuboidRegion("dummy",
					BlockVector3.at(chunk.getX() * 16, 0, chunk.getZ() * 16),
					BlockVector3.at(chunk.getX() * 16 + 15, 0, chunk.getZ() * 16 + 15));
			ApplicableRegionSet set = manager.getApplicableRegions(area);
			for (ProtectedRegion region : set) {
				addRegionToCache(region, world);
			}
		}
	}

	public void addRegionToCache(ProtectedRegion region, World world) {
		if (region instanceof GlobalProtectedRegion) {
			regionWorldCache.put(world, region);
			return;
		}

		BlockVector3 min = region.getMinimumPoint(), max = region.getMaximumPoint();
		for (int i = min.getBlockX(); i < max.getBlockX(); i += 16) {
			for (int k = min.getBlockZ(); k < max.getBlockZ(); k += 16) {
				ChunkCoordIntPair coords = new ChunkCoordIntPair((int) Math.floor(i / 16f), (int) Math.floor(k / 16f));
				regionChunkCache.computeIfAbsent(coords, ignored -> Sets.newHashSet()).add(region);
			}
		}
	}

	public void changeGlobalRegionBiome(World world, Chunk primaryChunk) {
		ProtectedRegion globalRegion = getGlobalRegion(world);
		if (globalRegion != null) {
			FakeSnow.WeatherType weatherType = globalRegion.getFlag(FakeSnow.CUSTOM_WEATHER_TYPE);
			if (weatherType != null && weatherType != FakeSnow.WeatherType.DEFAULT) {
				Object biomeStorage = NMSUtils.cloneBiomeStorage(NMSUtils.getBiomeStorage(primaryChunk));
				// set entire chunk to be that biome
				for (int i = 0; i < 4; i++) {
					for (int j = 0; j < 16; j++) {
						for (int k = 0; k < 4; k++) {
							NMSUtils.setBiome(biomeStorage, i << 2, j << 2, k << 2, weatherType.biome);
						}
					}
				}
			}
		}
	}

	public Set<ProtectedRegion> getRegionChunks(ChunkCoordIntPair coordIntPair) {
		return getRegionChunkCache().get(coordIntPair);
	}

	public Map<ChunkCoordIntPair, HashSet<ProtectedRegion>> getRegionChunkCache() {
		return regionChunkCache;
	}

	public ProtectedRegion getGlobalRegion(World world) {
		return getRegionWorldCache().get(world);
	}

	public Map<World, ProtectedRegion> getRegionWorldCache() {
		return regionWorldCache;
	}

	public void clearCaches() {
		regionChunkCache.clear();
		regionWorldCache.clear();
	}

	public RegionManager getRegionManager(World world) {
		return WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
	}

}

package com.jacky8399.fakesnow.events;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.jacky8399.fakesnow.FakeSnow;
import com.jacky8399.fakesnow.utils.ChunkManager;
import com.jacky8399.fakesnow.utils.NMSUtils;
import com.jacky8399.fakesnow.utils.WorldGuardManager;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Set;

public class PacketListener extends PacketAdapter {

	private static final FakeSnow PLUGIN = FakeSnow.get();

	public PacketListener() {
		super(PLUGIN, ListenerPriority.NORMAL, PacketType.Play.Server.MAP_CHUNK);
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		Player player = event.getPlayer();
		if (player.getWorld().getEnvironment() != World.Environment.NORMAL) {
			return;
		}
		World world = player.getWorld();
		PacketContainer packet = event.getPacket();
		int x = packet.getIntegers().read(0), z = packet.getIntegers().read(1);
		Chunk chunk = world.getChunkAt(x, z);

		WorldGuardManager worldGuardManager = PLUGIN.getWorldGuardManager();
		Object biomeStorage = NMSUtils.getBiomeStorage(chunk);

		// check for __global__ first
		worldGuardManager.changeGlobalRegionBiome(world, chunk);

		// Update the chunk with any API changes
		ChunkManager.updateChunkBiome(chunk, biomeStorage);

		ChunkCoordIntPair chunkCoords = new ChunkCoordIntPair(x, z);
		Set<ProtectedRegion> regions = worldGuardManager.getRegionChunks(chunkCoords);
		if (regions == null || regions.size() == 0) {
			return;
		}

		RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer()
				.get(BukkitAdapter.adapt(world));
		if (manager != null) {
			// find things to change
			for (ProtectedRegion region : regions) {
				// check if in the correct world
				FakeSnow.WeatherType weather = region.getFlag(FakeSnow.CUSTOM_WEATHER_TYPE);
				// If there is a region and the weather has been set properly
				if (manager.hasRegion(region.getId()) && weather != null && weather != FakeSnow.WeatherType.DEFAULT) {
					// Update the chunk with WorldGuard defined biome, if applicable
					ChunkManager.updateChunkBiome(biomeStorage, weather.biome);
				}
			}
		}

		// write it back
		int[] biomes = NMSUtils.getBiomes(biomeStorage);
		packet.getIntegerArrays().write(0, biomes);
	}

}

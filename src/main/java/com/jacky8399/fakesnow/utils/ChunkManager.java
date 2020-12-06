package com.jacky8399.fakesnow.utils;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Biome;

import java.util.HashMap;
import java.util.Map;

public class ChunkManager {

	private static final Map<World, Biome> globalBiomes = new HashMap<>();
	private static final Map<Chunk, Biome> chunkBiomes = new HashMap<>();

	/**
	 * Calls {@link #updateChunkBiome(Chunk, Biome)} but retrieves the biome from {@link #getBiome(Chunk)}
	 *
	 * @param chunk to set the biome for
	 * @return the edited biome storage object
	 */
	public static Object updateChunkBiome(Chunk chunk) {
		return updateChunkBiome(chunk, getBiome(chunk));
	}

	/**
	 * Calls {@link #updateChunkBiome(Object, Biome)} but retrieves the biome from {@link #getBiome(Chunk)}
	 *
	 * @param chunk to get the API biome for
	 * @param biomeStorage to be used to set biome data in
	 * @return the edited biome storage object
	 */
	public static Object updateChunkBiome(Chunk chunk, Object biomeStorage) {
		return updateChunkBiome(biomeStorage, getBiome(chunk));
	}

	/**
	 * Updates a chunk to a provided biome with {@link NMSUtils#setBiome(Object, int, int, int, Biome)}. This method
	 * clones the biome storage object of the provided chunk as an alternative to providing one manually.
	 *
	 * @param chunk to set the biome of
	 * @param biome to set the chunk to
	 * @return the edited biome storage object
	 */
	public static Object updateChunkBiome(Chunk chunk, Biome biome) {
		return updateChunkBiome(NMSUtils.cloneBiomeStorage(NMSUtils.getBiomeStorage(chunk)), biome);
	}

	/**
	 * Updates a chunk to a provided biome with {@link NMSUtils#setBiome(Object, int, int, int, Biome)} using
	 * a provided biome storage object instead of cloning one with a chunk
	 *
	 * @param biomeStorage which holds the biome data for a given chunk
	 * @param biome to set the chunk to
	 * @return edited the biome storage object
	 */
	public static Object updateChunkBiome(Object biomeStorage, Biome biome) {
		if (biome != null) {
			for (int i = 0; i < 16; i++) {
				for (int j = 0; j < 64; j++) {
					for (int k = 0; k < 16; k++) {
						NMSUtils.setBiome(biomeStorage, i, j, k, biome);
					}
				}
			}
		}

		return biomeStorage;
	}

	/**
	 * Sets the biome of an entire world with the exception of individual chunks
	 *
	 * @param world to set the biome for
	 * @param biome to set the world's unrecognised chunks to
	 */
	public static void setGlobalBiome(World world, Biome biome) {
		globalBiomes.put(world, biome);
	}

	/**
	 * Sets the biome for a specific chunk in the world, which takes priority over the global world biome
	 *
	 * @param chunk to set the biome for
	 * @param biome to set the chunk to
	 */
	public static void setChunkBiome(Chunk chunk, Biome biome) {
		chunkBiomes.put(chunk, biome);
	}

	/**
	 * Retrieves the biome for a specific chunk, if it has been set at the global or chunk level
	 *
	 * @param chunk to get the custom biome for
	 * @return the custom biome or null if there is no precedent for this chunk available
	 */
	public static Biome getBiome(Chunk chunk) {
		if (chunkBiomes.containsKey(chunk))
			return chunkBiomes.get(chunk);
		else
			return globalBiomes.getOrDefault(chunk.getWorld(), null);
	}

}

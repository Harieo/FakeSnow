package com.jacky8399.fakesnow;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import com.jacky8399.fakesnow.utils.WorldGuardManager;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class CommandFakesnow implements TabExecutor {

    private final FakeSnow plugin;

    public CommandFakesnow(FakeSnow plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0)
            return false;
        switch (args[0]) {
            case "refreshregions": {
                // Clear old cache first
                WorldGuardManager worldGuardManager = plugin.getWorldGuardManager();
                worldGuardManager.clearCaches();
                for (World world : Bukkit.getWorlds())
                    worldGuardManager.scanWorldForRegions(world);
                sender.sendMessage(ChatColor.GREEN + "Reloaded " + Bukkit.getWorlds() + " worlds");
                sender.sendMessage(ChatColor.GREEN + "Discovered " +
                        worldGuardManager.getRegionChunkCache().values().stream().mapToInt(HashSet::size).sum() + " region(s)");
                return true;
            }
            case "realbiome": {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "You can only run this command as a player!");
                    return false;
                }
                Player player = ((Player) sender);
                player.sendMessage(ChatColor.GREEN + "The current biome you are in: " + player.getLocation().getBlock().getBiome().getKey().toString());
                return true;
            }
        }
        sender.sendMessage(ChatColor.RED + "/fakesnow <refreshregions/realbiome>");
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length <= 1)
            return Arrays.asList("refreshregions", "realbiome");
        else
            return Collections.emptyList();
    }

}

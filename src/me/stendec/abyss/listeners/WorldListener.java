package me.stendec.abyss.listeners;

import me.stendec.abyss.AbyssPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

public class WorldListener implements Listener {

    private final AbyssPlugin plugin;

    public WorldListener(final AbyssPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldLoad(WorldLoadEvent event) {
        // Pass it straight on to the PluginManager.
        plugin.getManager().worldLoad(event.getWorld());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldUnload(WorldUnloadEvent event) {
        // Pass it straight on to the PluginManager.
        plugin.getManager().worldUnload(event.getWorld());
    }

}

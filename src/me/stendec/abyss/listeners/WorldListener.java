package me.stendec.abyss.listeners;

import me.stendec.abyss.ABPortal;
import me.stendec.abyss.AbyssPlugin;
import me.stendec.abyss.PortalManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public class WorldListener implements Listener {

    private final AbyssPlugin plugin;

    public WorldListener(AbyssPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        // If it's a new chunk, it can't possibly have any portal frames.
        if (event.isNewChunk())
            return;

        final PortalManager manager = plugin.getManager();

        for(Entity entity: event.getChunk().getEntities()) {
            if (!(entity instanceof ItemFrame))
                continue;

            // Try to get the portal for this frame.
            final ABPortal portal = manager.getAt(entity.getLocation());
            if (portal == null || !portal.frameIDs.containsKey(entity.getUniqueId()))
                continue;

            // Set metadata for fast lookup.
            portal.applyMetadata((ItemFrame) entity);
        }
    }

}

package me.stendec.abyss.listeners;

import me.stendec.abyss.ABPortal;
import me.stendec.abyss.AbyssPlugin;
import me.stendec.abyss.util.ColorBuilder;
import me.stendec.abyss.util.EntityUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.world.StructureGrowEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BlockListener implements Listener {

    private final AbyssPlugin plugin;

    public BlockListener(final AbyssPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        final Block block = event.getBlock();
        if (block == null)
            return;

        // For now, return on sneak for debugging.
        if (event.getPlayer().isSneaking())
            return;

        // Check to see if the block is protected.
        final ArrayList<ABPortal> portals = plugin.protectBlock(block);
        if ( portals == null ) {
            new ColorBuilder().red("This block is part of a portal. To destroy " +
                    "the portal, left click the network frame twice.").
                send(event.getPlayer());

            event.setCancelled(true);
            return;
        }

        // After the block is gone, update all our portals.
        plugin.getServer().getScheduler().runTask(plugin, new UpdatePortals(event, portals));
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        final Block block = event.getBlock();
        if ( block == null )
            return;

        // Check to see if the block is protected.
        final ArrayList<ABPortal> portals = plugin.protectBlock(block);
        if ( portals == null ) {
            event.setCancelled(true);
            return;
        }

        // After the block is gone, update all our portals.
        plugin.getServer().getScheduler().runTask(plugin, new UpdatePortals(event, portals));
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent event) {
        final Block block = event.getBlock();

        // Check to see if the block is protected.
        final ArrayList<ABPortal> portals = plugin.protectBlock(block);
        if ( portals == null ) {
            event.setCancelled(true);
            return;
        }

        // After the block is gone, update all our portals.
        plugin.getServer().getScheduler().runTask(plugin, new UpdatePortals(event, portals));
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent event) {
        final Block block = event.getBlock();
        if ( block == null || !AbyssPlugin.validLiquid(block) )
            return;

        // Check to see if the block is protected.
        final ABPortal portal = plugin.getManager().getAt(block);
        if ( portal != null ) {
            event.setCancelled(true);
            return;
        }

        // After the block is gone, update all our portals.
        plugin.getServer().getScheduler().runTask(plugin, new UpdatePortals(event, portal));
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        final Block block = event.getToBlock();
        if ( block == null || AbyssPlugin.validLiquid(event.getBlock()) )
            return;

        // Check to see if the block is protected.
        final ABPortal portal = plugin.getManager().getAt(block);
        if ( portal != null && portal.isInPortal(block) )
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void afterBlockFromTo(BlockFromToEvent event) {
        final Block block = event.getBlock();
        final Block to = event.getToBlock();
        if ( block == null || to == null || !AbyssPlugin.validLiquid(block) )
            return;

        // Get the portal at the block that's flowing.
        final ABPortal portal = plugin.getManager().getAt(block);
        if ( portal == null )
            return;

        portal.update();
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        final Block block = event.getBlock();
        if (block == null)
            return;

        // Check to see if the block is protected.
        final ArrayList<ABPortal> portals = plugin.protectBlock(block);
        if ( portals == null )
            event.setCancelled(true);
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        final Block block = event.getBlock();
        if ( block == null )
            return;

        final Material mat = block.getType();
        if ( mat != Material.SAND && mat != Material.GRAVEL )
            return;

        // Check to see if the block is protected.
        final ArrayList<ABPortal> portals = plugin.protectBlock(block);
        if ( portals == null ) {
            event.setCancelled(true);
            return;
        }

        // After the block is gone, update all our portals.
        plugin.getServer().getScheduler().runTask(plugin, new UpdatePortals(event, portals));
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        final Block block = event.getBlockPlaced();
        if (block == null)
            return;

        // For now, return on sneak for debugging.
        if (event.getPlayer().isSneaking())
            return;

        // Check to see if the block is protected.
        final ArrayList<ABPortal> portals = plugin.protectBlock(block);
        if ( portals == null ) {
            event.setCancelled(true);
            return;
        }

        // After the block is gone, update all our portals.
        plugin.getServer().getScheduler().runTask(plugin, new UpdatePortals(event, portals));
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        final Block block = event.getBlock();
        if ( block == null )
            return;

        // Get the location and target block.
        final Location loc = event.getRetractLocation();
        if ( loc == null )
            return;

        final Block target = loc.getBlock();
        if ( target == null || target.getType() == Material.AIR )
            return;

        final Material mat = target.getType();
        final PistonMoveReaction reaction = target.getPistonMoveReaction();

        // Determine if the piston is sticky.
        final boolean sticky = event.isSticky();

        // Figure out if we should even care about this.
        if ( (sticky && reaction == PistonMoveReaction.BLOCK) || (!sticky && (target.getFace(block) != BlockFace.DOWN || (mat != Material.SAND && mat != Material.GRAVEL))) )
            return;

        // Check to see if the block is protected.
        final ArrayList<ABPortal> portals = plugin.protectBlock(target);
        if ( portals == null ) {
            event.setCancelled(true);
            return;
        }

        // After the block is gone, update all our portals.
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new UpdatePortals(event, portals), 10L);
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        final Block piston = event.getBlock();
        if ( piston == null )
            return;

        final BlockFace dir = event.getDirection();
        final Block b1 = piston.getRelative(dir);
        final Block b2 = b1.getRelative(dir, event.getLength());

        // Do ridiculous stuff to get the minimum and maximum points of this cube.
        Location[] cube = EntityUtils.fixCubeoid(b1.getLocation(), b2.getLocation());

        // See if there are any portals involved.
        final ArrayList<ABPortal> portals = plugin.getManager().getWithin(cube[0], cube[1]);
        if ( portals == null || portals.size() == 0)
            return;

        // See if any important frame blocks were affected.
        final int min_y = cube[0].getBlockY(), max_y = cube[1].getBlockY();
        for(final ABPortal portal: portals) {
            final int py = portal.getLocation().getBlockY();
            if ( max_y >= (py - 1) && min_y <= py ) {
                // Protect the important layers of the frame.
                event.setCancelled(true);
                return;
            }
        }

        // After the block is gone, update all our portals.
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new UpdatePortals(event, portals), 10L);

    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        final Block block = event.getBlock();
        if ( block == null )
            return;

        // Check to see if the block is protected.
        final ArrayList<ABPortal> portals = plugin.protectBlock(block);
        if ( portals == null ) {
            event.setCancelled(true);
            return;
        }

        // After the block is gone, update all our portals.
        plugin.getServer().getScheduler().runTask(plugin, new UpdatePortals(event, portals));
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent event) {
        final Block block = event.getBlock();
        if ( block == null )
            return;

        final ArrayList<ABPortal> portals = plugin.protectBlock(block);
        if ( portals == null ) {
            event.setCancelled(true);
            return;
        }

        // After the block is gone, update all our portals.
        plugin.getServer().getScheduler().runTask(plugin, new UpdatePortals(event, portals));
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event) {
        // Get the bounding box for all the blocks involved.
        final List<BlockState> blocks = event.getBlocks();
        Location[] cube = getBounds(blocks);

        // Now, see if any portals are involved.
        final ArrayList<ABPortal> portals = plugin.getManager().getWithin(cube[0], cube[1]);
        if ( portals == null || portals.size() == 0 )
            return;

        // See if any important frame blocks were affected. This is awful. A nested for loop.
        // Thankfully it won't come up often. Use an iterator and remove the blocks that
        // intersect with portal frames.
        for(final Iterator<BlockState> it = blocks.iterator(); it.hasNext(); ) {
            final BlockState bs = it.next();
            final World world = bs.getWorld();
            final int x = bs.getX(), y = bs.getY(), z = bs.getZ();

            for(final ABPortal portal: portals) {
                if ( portal.isInFrame(world, x, y, z) ) {
                    it.remove();
                    break;
                }
            }
        }

        // Did we remove everything?
        if ( blocks.size() == 0 ) {
            event.setCancelled(true);
            return;
        }

        // After the block is gone, update all our portals.
        plugin.getServer().getScheduler().runTask(plugin, new UpdatePortals(event, portals));
    }


    private class UpdatePortals implements Runnable {

        private final Cancellable event;
        private final ABPortal[] portals;

        UpdatePortals(final Cancellable event, final ArrayList<ABPortal> portals) {
            this.event = event;
            portals.trimToSize();
            this.portals = portals.toArray(new ABPortal[portals.size()]);
        }

        UpdatePortals(final Cancellable event, final ABPortal portal) {
            this.event = event;
            this.portals = new ABPortal[]{portal};
        }

        @Override
        public void run() {
            // If the event was cancelled, don't do this.
            if (event.isCancelled())
                return;

            // Iterate through all the portals, updating them.
            for(final ABPortal portal: portals)
                if ( portal != null )
                    portal.update();
        }
    }


    private static Location[] getBounds(List<BlockState> blocks) {
        if ( blocks == null || blocks.size() == 0 )
            return null;

        Iterator<BlockState> it = blocks.iterator();

        BlockState bs = it.next();
        final World world = bs.getWorld();

        int x1 = bs.getX(), y1 = bs.getY(), z1 = bs.getZ();
        int x2 = x1, y2 = y1, z2 = z1;

        while( it.hasNext() ) {
            bs = it.next();
            final int x = bs.getX(), y = bs.getY(), z = bs.getZ();
            x1 = Math.min(x1, x); x2 = Math.max(x2, x);
            y1 = Math.min(y1, y); y2 = Math.max(y2, y);
            z1 = Math.min(z1, z); z2 = Math.max(z2, z);
        }

        return new Location[]{new Location(world, x1, y1, z1), new Location(world, x2, y2, z2)};
    }

}

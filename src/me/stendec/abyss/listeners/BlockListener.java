package me.stendec.abyss.listeners;

import me.stendec.abyss.ABPortal;
import me.stendec.abyss.AbyssPlugin;
import me.stendec.abyss.util.BlockUtils;
import me.stendec.abyss.util.ColorBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.material.Rails;

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
        if ( portals.size() > 0 )
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
        if ( portals.size() > 0 )
            plugin.getServer().getScheduler().runTask(plugin, new UpdatePortals(event, portals));
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent event) {
        final Block block = event.getBlock();

        // If we're not changing a block in an important way, who cares?
        if ( ! AbyssPlugin.validLiquid(block) && plugin.frameMaterials.contains(event.getNewState().getType()) )
            return;

        // Check to see if the block is protected.
        final ArrayList<ABPortal> portals = plugin.protectBlock(block);
        if ( portals == null ) {
            event.setCancelled(true);
            return;
        }

        // After the block is gone, update all our portals.
        if ( portals.size() > 0 )
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
        final Block to = event.getToBlock();
        if ( to == null )
            return;

        final Block block = to.getRelative(BlockFace.UP);
        if ( block == null || !AbyssPlugin.validLiquid(block) )
            return;

        // Get the portal at the block that's above the to block.
        final ABPortal portal = plugin.getManager().getAt(block);
        if ( portal == null )
            return;

        // TODO: Make this work right.

        // After the block is gone, update the portal. Give it half a second to make sure.
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new UpdatePortals(event, portal), 10L);
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


    @EventHandler(ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        final Block block = event.getBlock();
        if ( block == null )
            return;

        // Handle falling sand, falling gravel, and rails.
        final Material mat = block.getType();
        final boolean falling = mat == Material.SAND || mat == Material.GRAVEL;

        if ( !falling && !BlockUtils.isRail(mat) )
            return;

        if ( falling ) {
            final ArrayList<ABPortal> portals = plugin.protectBlock(block);
            if ( portals == null ) {
                event.setCancelled(true);
                return;
            }

            // After the block is gone, update all our portals.
            if ( portals.size() > 0 )
                plugin.getServer().getScheduler().runTask(plugin, new UpdatePortals(event, portals));

        } else if ( plugin.smartRails ) {
            // See if we should do fancy rail manipulation.
            final ArrayList<ABPortal> portals = plugin.getManager().getNear(block);
            if ( portals.size() > 0 )
                plugin.getServer().getScheduler().runTask(plugin, new UpdateRails(event, block, portals));
        }
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        final Block block = event.getBlockPlaced();
        if (block == null)
            return;

        // See if the player can bypass block protection.
        final Player player = event.getPlayer();
        if ( player.isSneaking() && player.hasPermission("abyss.bypass_protection") )
            return;

        // Check to see if the block is protected.
        final ArrayList<ABPortal> portals = plugin.protectBlock(block);
        if ( portals == null ) {
            event.setCancelled(true);
            return;
        }

        // After the block is placed, update all our portals.
        if ( portals.size() > 0 ) {
            // If we've got a rail, do special stuff.
            if ( plugin.smartRails && BlockUtils.isRail(block) )
                plugin.getServer().getScheduler().runTask(plugin, new UpdateRails(event, block, portals));

            plugin.getServer().getScheduler().runTask(plugin, new UpdatePortals(event, portals));
        }
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
        if ( portals.size() > 0 )
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
        Location[] cube = BlockUtils.fixCubeoid(b1.getLocation(), b2.getLocation());

        // See if there are any portals involved.
        final ArrayList<ABPortal> portals = plugin.getManager().getWithin(cube[0], cube[1]);
        if ( portals == null || portals.size() == 0)
            return;

        // See if any important frame blocks were affected.
        for(final Block block: event.getBlocks()) {
            final World world = block.getWorld();
            final int x = block.getX(), y = block.getY(), z = block.getZ();

            for(final ABPortal portal: portals) {
                if ( portal.isInFrame(world, x, y, z) ) {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        // After the block is gone, update all our portals.
        if ( portals.size() > 0 )
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new UpdatePortals(event, portals), 10L);
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        final Block block = event.getBlock();
        if ( block == null )
            return;

        // If we're not changing a block in an important way, who cares?
        if ( ! AbyssPlugin.validLiquid(block) && plugin.frameMaterials.contains(event.getTo()) )
            return;

        // Check to see if the block is protected.
        final ArrayList<ABPortal> portals = plugin.protectBlock(block);
        if ( portals == null ) {
            event.setCancelled(true);
            return;
        }

        // After the block is gone, update all our portals.
        if ( portals.size() > 0 )
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
        if ( portals.size() > 0 )
            plugin.getServer().getScheduler().runTask(plugin, new UpdatePortals(event, portals));
    }


    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event) {
        // Get the bounding box for all the blocks involved.
        final List<BlockState> blocks = event.getBlocks();
        Location[] cube = BlockUtils.getBounds(blocks);

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
        if ( portals.size() > 0 )
            plugin.getServer().getScheduler().runTask(plugin, new UpdatePortals(event, portals));
    }


    private class UpdateRails implements Runnable {

        private final Cancellable event;
        private final Block block;
        private final ABPortal[] portals;

        UpdateRails(final Cancellable event, final Block block, final ArrayList<ABPortal> portals) {
            this.event = event;
            this.block = block;
            portals.trimToSize();
            this.portals = portals.toArray(new ABPortal[portals.size()]);
        }

        UpdateRails(final Cancellable event, final Block block, final ABPortal portal) {
            this.event = event;
            this.block = block;
            this.portals = new ABPortal[]{portal};
        }

        @Override
        public void run() {
            // If the event was cancelled, stop right now.
            if ( event.isCancelled() )
                return;

            // If the block isn't still a rail, abort.
            if ( !BlockUtils.isRail(block) )
                return;

            // Get the block state.
            BlockState bs = block.getState();
            Rails data = (Rails) bs.getData();

            // Find the first portal and stick to it.
            for(final ABPortal portal: portals) {
                BlockFace face = portal.getFace(block);
                if ( face == null )
                    continue;

                // Try finding a rail block.
                Block target = null;
                boolean sloped = false;

                // First, look ahead.
                Block other = block.getRelative(face);
                if ( (BlockUtils.isRail(other) && !BlockUtils.isBusy(other, block)) || (BlockUtils.isRail(other.getRelative(BlockFace.DOWN)) && !BlockUtils.isBusy(other.getRelative(BlockFace.DOWN), block)) ) {
                    target = other;

                } else {
                    // We can go up if it's straight away.
                    other = other.getRelative(BlockFace.UP);
                    if ( BlockUtils.isRail(other) && !BlockUtils.isBusy(other, block) ) {
                        target = other;
                        sloped = true;

                    } else {
                        // Check the sides.
                        BlockFace f = BlockUtils.toLeft(face);
                        other = block.getRelative(f);
                        if ( BlockUtils.isRail(other) && !BlockUtils.isBusy(other, block) && portal.getFace(other) == null ) {
                            target = other;
                            face = BlockUtils.toRightSub(face);
                        } else {
                            f = BlockUtils.toRight(face);
                            other = block.getRelative(f);
                            if ( BlockUtils.isRail(other) && !BlockUtils.isBusy(other, block) && portal.getFace(other) == null ) {
                                target = other;
                                face = BlockUtils.toLeftSub(face);
                            }
                        }
                    }
                }

                // Rotate this track.
                data.setDirection(face, sloped);
                bs.setData(data);
                bs.update();
                return;
            }
        }

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

}

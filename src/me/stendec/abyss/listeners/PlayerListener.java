package me.stendec.abyss.listeners;

import me.stendec.abyss.*;
import me.stendec.abyss.util.ColorBuilder;
import me.stendec.abyss.util.EntityUtils;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dye;
import org.bukkit.material.Wool;

public class PlayerListener implements Listener {

    private final AbyssPlugin plugin;

    public PlayerListener(AbyssPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }


    ///////////////////////////////////////////////////////////////////////////
    // Utilities
    ///////////////////////////////////////////////////////////////////////////

    private static ColorBuilder t() {
        return new ColorBuilder();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Update Notification
    ///////////////////////////////////////////////////////////////////////////

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        if ( plugin.updateMessage == null )
            return;

        Player player = event.getPlayer();
        if ( ! player.hasPermission("abyss.update") )
            return;

        player.sendMessage(plugin.updateMessage);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Portal Use
    ///////////////////////////////////////////////////////////////////////////

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(final PlayerMoveEvent event) {
        final Player player = event.getPlayer();

        // Don't handle a sneaking player, or a player with no permissions.
        if ( player.isSneaking() || !player.hasPermission("abyss.use") )
            return;

        // Do portal logic.
        final Location to = plugin.usePortal(player, event.getFrom(), event.getTo());
        if ( to == null )
            return;

        // Set the event location.
        event.setFrom(to);
        event.setTo(to);
    }


    ///////////////////////////////////////////////////////////////////////////
    // Buckets
    ///////////////////////////////////////////////////////////////////////////

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerBucketFill(final PlayerBucketFillEvent event) {
        final Block clicked = event.getBlockClicked();
        if ( clicked == null )
            return;

        final Block block = clicked.getRelative(event.getBlockFace());
        final int y = block.getY();

        // See if there's a portal at this location. We don't just use the
        // protectBlock method because we don't need to search for every
        // portal. Portals can't share water blocks.
        final ABPortal portal = plugin.getManager().getAt(block);
        if ( portal == null )
            return;

        // See if we're in the important layers.
        final int py = portal.getLocation().getBlockY();
        if ( y <= py && y > py - 2 ) {
            event.setCancelled(true);
            return;
        }
    }


    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerBucketEmpty(final PlayerBucketEmptyEvent event) {
        // Let the player place a water bucket. We don't care if they want to fix the flow.
        if ( event.getBucket() == Material.WATER_BUCKET )
            return;

        final Block clicked = event.getBlockClicked();
        if ( clicked == null )
            return;

        final Block block = clicked.getRelative(event.getBlockFace());
        final int y = block.getY();

        // See if there's a portal at this location.
        final ABPortal portal = plugin.getManager().getAt(block);
        if ( portal == null )
            return;

        // See if we're in the important layers.
        final int py = portal.getLocation().getBlockY();
        if ( y <= py && y > py - 2 ) {
            event.setCancelled(true);
            return;
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    // Portal Configuration
    ///////////////////////////////////////////////////////////////////////////

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(final PlayerInteractEntityEvent event) {
        final Player player = event.getPlayer();
        final Entity entity = event.getRightClicked();

        // Only handle clicks on item frames.
        if ( ! (entity instanceof ItemFrame) )
            return;

        final ItemFrame frame = (ItemFrame) entity;
        final PortalManager manager = plugin.getManager();

        // Try getting the portal for the entity.
        final ABPortal portal = manager.getByFrame(frame);
        if (portal == null)
            return;

        // Get the info object. If we don't have it, abort.
        final FrameInfo info = portal.frameIDs.get(frame.getUniqueId());
        if (info == null)
            return;

        // It's our event, so cancel it.
        event.setCancelled(true);

        if (! portal.canManipulate(player) ) {
            t().red("Permission Denied").send(player);
            return;
        }

        final ItemStack item = player.getItemInHand();
        final Material material = item.getType();

        // Check for a Portal Wand.
        final String tool = plugin.validatePortalWand(item);
        if ( tool != null ) {
            ABCommand cmd = plugin.commands.get(tool);
            if ( cmd == null ) {
                t().red("Unknown Command: ").reset(tool).send(player);
                return;
            }

            cmd.useWand(player, item, event, entity.getLocation().getBlock(), portal);
            return;
        }

        // Get a color, for use with color, ID, and Destination frames.
        DyeColor color = null;

        if (item.getType() == Material.WOOL)
            color = ((Wool) item.getData()).getColor();
        else if (item.getType() == Material.INK_SACK)
            color = ((Dye) item.getData()).getColor();

        try {
            if ( info.type == FrameInfo.Frame.NETWORK) {
                if ( material != Material.AIR )
                    portal.setNetwork(item);

            } else if ( info.type == FrameInfo.Frame.COLOR) {
                if (color == null)
                    portal.modColor(-1);
                else
                    portal.setColor(color);

            } else if (info.type == FrameInfo.Frame.MOD) {
                if ( material == Material.AIR )
                    return;

                // If the frame already has an item, abort.
                if (frame.getItem().getType() != Material.AIR) {
                    t().red("There's already a modifier in that frame.").send(player);
                    return;
                }

                // Try setting this mod item.
                if (!portal.setMod(player, frame, item))
                    throw new IllegalArgumentException(material.name() + " is not a valid portal modifier.");

                // If the player is in CREATIVE, don't remove anything.
                if (player.getGameMode() == GameMode.CREATIVE)
                    return;

                // Remove just the one.
                item.setAmount(item.getAmount() - 1);
                player.setItemInHand(item);

            } else if (info.type == FrameInfo.Frame.ID1) {
                if ( color == null )
                    portal.modID(-1, true);
                else
                    portal.setPartialID(color.getWoolData() + 1, true);

            } else if (info.type == FrameInfo.Frame.ID2) {
                if ( color == null )
                    portal.modID(-1, false);
                else
                    portal.setPartialID(color.getWoolData() + 1, false);

            } else if (info.type == FrameInfo.Frame.DEST1) {
                if ( color == null )
                    portal.modDestination(-1, true);
                else
                    portal.setPartialDestination(color.getWoolData() + 1, true);

            } else if (info.type == FrameInfo.Frame.DEST2) {
                if ( color == null )
                    portal.modDestination(-1, false);
                else
                    portal.setPartialDestination(color.getWoolData() + 1, false);
            }
        } catch(IllegalArgumentException ex) {
            t().red("Error Configuring Portal").send(player);
            t().append("   ").gray(ex.getMessage()).send(player);
        }

    }

    ///////////////////////////////////////////////////////////////////////////
    // Portal Tool
    ///////////////////////////////////////////////////////////////////////////

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR)
            return;

        Player player = event.getPlayer();
        ItemStack wand = event.getItem();

        // See if we've got a portal wand.
        String tool = plugin.validatePortalWand(wand);
        if (tool == null)
            return;

        // We handle all Portal Wand events.
        event.setCancelled(true);

        // Try to find the command.
        ABCommand cmd = plugin.commands.get(tool);
        if ( cmd == null ) {
            t().red("Unknown Command: ").reset(tool).send(player);
            return;
        }

        // Get the block involved.
        Block block = event.getClickedBlock();
        if ( block == null )
            block = player.getTargetBlock(EntityUtils.transparentBytes, plugin.wandRange);
        else
            block = block.getRelative(event.getBlockFace());

        // Finally, perform the command.
        cmd.useWand(player, wand, event, block, plugin.getManager().getAt(block));
    }

}


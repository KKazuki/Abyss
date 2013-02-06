package me.stendec.abyss.listeners;

import me.stendec.abyss.*;
import me.stendec.abyss.util.ColorBuilder;
import me.stendec.abyss.util.ParseUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Dye;
import org.bukkit.material.Wool;

import java.util.*;

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

    private static ColorBuilder t(final String string) {
        return new ColorBuilder(string);
    }


    private static HashSet<Byte> crap;

    static {
        crap = new HashSet<Byte>();
        crap.add((byte) Material.AIR.getId());
        crap.add((byte) Material.STONE_BUTTON.getId());
        crap.add((byte) Material.WOOD_BUTTON.getId());
    }

    private Block getLiquid(Player player) {
        for(Block b: player.getLineOfSight(crap, 6)) {
            plugin.getLogger().info("Block: " + b.getType().name() + " @ " + b.getLocation().toVector());
            if (AbyssPlugin.validLiquid(b))
                return b;
        }
        return null;
    }


    private static BlockFace getFacing(Player player) {
        double rot = (player.getLocation().getYaw() - 180) % 360;
        if ( rot < 0 )
            rot += 360;

        if ( 0 <= rot && rot < 45 )
            return BlockFace.NORTH;

        else if ( 45 <= rot && rot < 135 )
            return BlockFace.EAST;

        else if ( 135 <= rot && rot < 225 )
            return BlockFace.SOUTH;

        else if ( 225 <= rot && rot < 315 )
            return BlockFace.WEST;

        return BlockFace.NORTH;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Portal Use
    ///////////////////////////////////////////////////////////////////////////

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(final PlayerMoveEvent event) {
        final Player player = event.getPlayer();

        // Don't handle a sneaking player, or a player with no permissions.
        if (player.isSneaking() || !player.hasPermission("abyss.use"))
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

        // Only handle clicks on item frames by players with the use permission.
        if (!(entity instanceof ItemFrame) || !player.hasPermission("abyss.use"))
            return;

        final ItemFrame frame = (ItemFrame) entity;
        final PortalManager manager = plugin.getManager();

        // Try getting the portal for the entity.
        final ABPortal portal = manager.getByMetadata(frame);
        if (portal == null)
            return;

        // Get the info object. If we don't have it, abort.
        final FrameInfo info = portal.frameIDs.get(frame.getUniqueId());
        if (info == null)
            return;

        // It's our event, so cancel it.
        event.setCancelled(true);

        if (! portal.canManipulate(player) ) {
            t().red("Access Denied").send(player);
            return;
        }

        final ItemStack item = player.getItemInHand();
        final Material material = item.getType();

        // Get a color, for use with color, ID, and Destination frames.
        DyeColor color = null;

        if (item.getType() == Material.WOOL)
            color = ((Wool) item.getData()).getColor();
        else if (item.getType() == Material.INK_SACK)
            color = ((Dye) item.getData()).getColor();


        // Check for the Modifier wand.
        final String tool = plugin.validatePortalWand(item);
        final boolean mtool = tool != null && (tool.equals("mod") || tool.equals("modifier"));
        if (mtool && info.type != FrameInfo.Frame.MOD) {
            t().red("You must click a modifier frame to use the modifier wand.").send(player);
            return;
        }

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

                if ( mtool ) {
                    if (frame.getItem().getType() == Material.AIR)
                        throw new IllegalArgumentException("Cannot configure empty modifier.");

                    // Get the ModInfo instance for this modifier.
                    final ModInfo minfo = portal.getModFromFrame(frame);
                    if ( minfo == null )
                        throw new IllegalArgumentException("Error getting modifier information.");

                    if ( ! item.getItemMeta().hasLore() )
                        throw new IllegalArgumentException("This modifier wand has no configuration to apply.");

                    final Map<String, String> config = ParseUtils.tokenizeLore(item.getItemMeta().getLore());
                    if (config != null && config.size() > 0)
                        for(final Map.Entry<String,String> entry: config.entrySet()) {
                            String key = entry.getKey();
                            if ( key.startsWith("-") ) {
                                key = key.substring(1);
                                if (minfo.flags.containsKey(key))
                                    minfo.flags.remove(key);
                            } else {
                                minfo.flags.put(key, entry.getValue());
                            }
                        }

                    t().gold("Portal [").yellow(portal.getName()).gold("]").
                            white("'s modifier was updated successfully.").send(player);
                    return;
                }

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
            t().red("Error Configuring Portal").lf().
                gray("    ").append(ex.getMessage()).send(player);
        }

    }

    ///////////////////////////////////////////////////////////////////////////
    // Portal Tool
    ///////////////////////////////////////////////////////////////////////////

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR)
            return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // See if we've got a portal wand.
        final String tool = plugin.validatePortalWand(item);
        if (tool == null)
            return;

        // We handle all Portal Wand events.
        event.setCancelled(true);

        // Now, fork based on the tool.
        if ( tool.equals("info") )
            onInfo(player, item, event);

        else if ( tool.equals("delete") )
            onDelete(player, item, event);

        else if ( tool.equals("create") )
            onCreate(player, item, event);

        else if ( tool.equals("config") || tool.equals("configure") )
            onConfigure(player, item, event);

        else if ( tool.equals("mod") || tool.equals("modifier") )
            t().red("You must click a modifier frame to use the modifier wand.").send(player);

        else if ( tool.equals("transport") || tool.equals("goto") )
            onTransport(player, item, event);

        else
            t().red("Unknown Tool: ").white(tool);
    }


    void onTransport(final Player player, final ItemStack item, final PlayerInteractEvent event) {
        final ItemMeta im = item.getItemMeta();
        final List<String> lore = im.hasLore() ? im.getLore() : null;
        if ( lore == null || lore.size() == 0 )
            return;

        final ABPortal portal = plugin.getManager().getByName(lore.get(0).trim());
        if ( portal == null ) {
            t().red("Invalid Portal").send(player);
        }

        // Teleport to the portal.
        plugin.doTeleport(player, null, portal, player.getLocation(), player.getVelocity());
    }


    void onInfo(Player player, ItemStack item, PlayerInteractEvent event) {
        // Get the block the player is looking at.

        Block block = event.getClickedBlock();
        if ( block == null )
            block = getLiquid(player);
        else
            block = block.getRelative(event.getBlockFace());

        final ABPortal portal = plugin.getManager().getAt(block);
        if (portal == null) {
            t().red("No portal detected.").send(player);
            return;
        }

        final Location center = portal.getCenter();

        final ItemStack n = portal.network;
        String network = n.getData().toString();

        if ( n.getType() == Material.SKULL_ITEM && n.getDurability() == 3 )
            network = t("Personal ").gray("(").white(portal.owner).gray(")").toString();
        else if ( n.getItemMeta().hasDisplayName() )
            network = t(n.getItemMeta().getDisplayName()).gray("(").white(network).gray(")").toString();

        ColorBuilder out = t().gold().bold("Portal [").yellow(portal.getName()).gold().bold("]").lf().
            gray("UUID: ").white(portal.uid).lf().
            gray("Center: ").white(center.getBlockX()).darkgray(", ").white(center.getBlockY()).
                darkgray(", ").white(center.getBlockZ()).gray(" [").white(center.getWorld().getName()).
                gray("]").lf().

            gray("Depth: ").white("%-4d", portal.depth).gray("   Size: ").white("%-4d", portal.getSize()).
                gray("   Rotation: ").white(portal.getRotation().name()).lf().

            gray("Network: ").white(network).gray(" [").white(portal.color.name()).gray("]").lf().

            gray("ID: ").white((portal.id == 0) ? "None" : Short.toString(portal.id)).
                gray("   Destination: ").
                white((portal.destination == 0) ? "None" : Short.toString(portal.destination)).lf().

            gray("Closed: ").white(!portal.valid).gray("   Velocity: ").
                white(portal.velocityMultiplier).lf().lf().

            gray().bold("Modifiers");

        boolean had_mods = false;

        if ( portal.mods != null && portal.mods.size() > 0 ) {
            for(final ModInfo info: portal.mods) {
                if ( info.item == null || info.item.getType() == Material.AIR )
                    continue;

                had_mods = true;
                out.lf().white("  ").append(info.item.getType().name());
                if ( !info.flags.isEmpty() ) {
                    final ColorBuilder c = new ColorBuilder();
                    boolean first = true;
                    for(Map.Entry<String, String> entry: info.flags.entrySet()) {
                        if ( entry.getKey().startsWith("*") )
                            continue;

                        if (!first)
                            c.darkgray("; ");
                        first = false;

                        c.gray(entry.getKey());
                        final String value = entry.getValue();
                        if ( value.length() > 0 )
                            c.darkgray(": ").darkpurple(value);
                    }

                    if ( !first )
                        out.darkgray(" [").append(c.toString()).darkgray("]");
                }
            }
        }

        if ( ! had_mods )
            out.lf().darkgray("  None");

        out.send(player);
    }

    void onCreate(Player player, ItemStack item, PlayerInteractEvent event) {
        final Block block = getLiquid(player);
        if (block == null) {
            t().red("Invalid portal location.").send(player);
            return;
        }

        // Tokenize the lore now, so that we can deal with early configuration. Namely, size.
        final ItemMeta im = item.getItemMeta();
        int size = -1;

        Map<String, String> tokens = null;
        if ( im.hasLore() ) {
            tokens = ParseUtils.tokenizeLore(im.getLore());
            if ( tokens.containsKey("size") ) {
                try {
                    size = Integer.parseInt(tokens.get("size"));
                } catch(NumberFormatException ex) { }

                if ( size < 2 ) {
                    t().red("Configuration Error").lf().
                        gray("    size must be at least 2").send(player);
                    return;
                }

                // Get rid of this before the configuration takes place.
                tokens.remove("size");
            }
        }

        // Get the root location. Iterate all possible sizes.
        Location loc = null;

        if (size != -1) {
            loc = plugin.findRoot(block.getLocation(), size);
            if (loc == null || !plugin.validLayer(loc, size))
                loc = null;
        } else {
            for(size = plugin.minimumSize; size <= plugin.maximumSize; size++) {
                loc = plugin.findRoot(block.getLocation(), size);
                if (loc == null || !plugin.validLayer(loc, size))
                    loc = null;
                else
                    break;
            }
        }

        if (loc == null) {
            t().red("Invalid portal location.").send(player);
            return;
        }

        // Check for existing portals.
        final PortalManager manager = plugin.getManager();

        ABPortal portal = manager.getByRoot(loc);
        if (portal != null) {
            t().gold("Portal [").yellow(portal.getName()).gold("]").
                    red(" already exists here.").send(player);
            return;
        }

        // Check for depth.
        int depth = plugin.getDepthAt(loc, size);
        if (depth < plugin.minimumDepth) {
            t().red("Portals must be at least ").darkred(plugin.minimumDepth).red(" blocks deep.").
                append("This space is ").darkred(depth).red(" blocks deep.").send(player);
            return;
        }

        // Run validation.
        if (!plugin.validateLocation(loc, size) ) {
            t().red("Invalid portal location.");
            return;
        }

        // Determine the facing direction.
        BlockFace facing = event.hasBlock() ? event.getBlockFace().getOppositeFace() : null;
        if ( facing == null || facing == BlockFace.UP || facing == BlockFace.DOWN )
            facing = getFacing(player);

        // Create the portal.
        portal = new ABPortal(plugin);

        // Set some basic stuff.
        portal.owner = player.getName();

        // Set the portal rotation.
        if ( facing == BlockFace.NORTH ) portal.setRotation(Rotation.NONE);
        if ( facing == BlockFace.EAST) portal.setRotation(Rotation.CLOCKWISE);
        if ( facing == BlockFace.SOUTH ) portal.setRotation(Rotation.FLIPPED);
        if ( facing == BlockFace.WEST ) portal.setRotation(Rotation.COUNTER_CLOCKWISE);

        // Use any configuration.
        if ( tokens != null ) {
            try {
                configFromLore(portal, tokens);
            } catch(IllegalArgumentException ex) {
                t().red("Configuration Error").lf().
                    gray("    ").append(ex.getMessage()).send(player);
                return;
            }
        }

        // Set the location and add it to the system.
        portal.setLocation(loc, size);
        manager.add(portal);

        t().gold("Portal [").yellow(portal.getName()).gold("]").darkgreen(" was created successfully.").send(player);
    }


    private void onConfigure(final Player player, final ItemStack item, final PlayerInteractEvent event) {
        // Get the block the player is looking at.
        final PortalManager manager = plugin.getManager();
        final ABPortal portal = manager.getAt(getLiquid(player));
        if (portal == null) {
            t().red("No portal detected.").send(player);
            return;
        }

        // Let's remove the portal from networks temporarily.
        final ItemStack network = portal.network;
        final String owner = portal.owner;
        final DyeColor color = portal.color;

        final int index = manager.removeFromNetwork(portal);
        manager.removeFromNetworkIds(portal);

        // Also, destroy the item frames.
        portal.destroyEntities(true);

        // Now, update it.
        try {
            configFromLore(portal, item.getItemMeta());

        } catch(IllegalArgumentException ex) {
            t().red("Configuration Error").lf().
                gray("    ").append(ex.getMessage()).send(player);
            return;

        } finally {
            // Make sure it gets added again.
            if ( portal.network.equals(network) && portal.owner.equals(owner) && portal.color == color)
                manager.addToNetwork(portal, index);
            else
                manager.addToNetwork(portal);

            portal.createEntities();
        }

        t().gold("Portal [").yellow(portal.getName()).gold("]").darkgreen(" was updated successfully.").send(player);
    }

    private void configFromLore(final ABPortal portal, final Map<String, String> config) {
        if (config == null || portal == null || config.size() == 0)
            return;

        for(Map.Entry<String,String> entry: config.entrySet())
            loreConfig(portal, entry.getKey(), entry.getValue());
    }

    private void configFromLore(final ABPortal portal, final ItemMeta meta) {
        // If there's no lore, obviously this does nothing.
        if (!meta.hasLore())
            return;

        configFromLore(portal, ParseUtils.tokenizeLore(meta.getLore()));
    }

    private static void requireValue(final String key, final String value) {
        if (value == null || value.length() == 0)
            throw new IllegalArgumentException(key + " must have a value.");
    }

    private static void loreConfig(final ABPortal portal, final String key, final String value) {
        if (key.equals("owner")) {
            requireValue(key, value);
            portal.owner = value;

        } else if (key.equals("color")) {
            requireValue(key, value);
            DyeColor color = ParseUtils.matchColor(value);
            if ( color == null )
                throw new IllegalArgumentException("Invalid color: " + value);

            portal.color = color;

        } else if (key.equals("id")) {
            requireValue(key, value);
            try {
                portal.id = Short.parseShort(value);
            } catch(NumberFormatException ex) {
                throw new IllegalArgumentException("id must be a number between " + Short.MIN_VALUE + " and " + Short.MAX_VALUE);
            }

        } else if (key.equals("dest") || key.equals("destination")) {
            requireValue(key, value);
            try {
                portal.destination = Short.parseShort(value);
            } catch(NumberFormatException ex) {
                throw new IllegalArgumentException("destination must be a number between " + Short.MIN_VALUE + " and " + Short.MAX_VALUE);
            }

        } else if (key.equals("velocity") || key.equals("speed")) {
            requireValue(key, value);
            try {
                portal.velocityMultiplier = Integer.parseInt(value);
            } catch(NumberFormatException ex) {
                throw new IllegalArgumentException("velocity must be a number.");
            }

        } else if (key.equals("rot") || key.equals("rotation")) {
            requireValue(key, value);
            portal.setRotation(ParseUtils.matchRotation(value));

        } else if (key.equals("size")) {
            throw new IllegalArgumentException("size cannot be applied to existing portals.");

        } else {
            throw new IllegalArgumentException("Invalid option: " + key);
        }
    }

    private void onDelete(final Player player, final ItemStack item, final PlayerInteractEvent event) {
        // Get the portal.
        final ABPortal portal = plugin.getManager().getAt(getLiquid(player));
        if (portal == null) {
            t().red("No portal detected.").send(player);
            return;
        }

        // Destroy the portal.
        ColorBuilder p = t().gold("Portal [").yellow(portal.getName()).gold("]");

        if (!plugin.getManager().destroy(portal))
            p.red(" could not be deleted.").send(player);
        else
            p.darkgreen(" was deleted successfully.").send(player);
    }

}


package me.stendec.abyss.commands;

import com.google.common.base.Joiner;
import me.stendec.abyss.ABCommand;
import me.stendec.abyss.ABPortal;
import me.stendec.abyss.AbyssPlugin;
import me.stendec.abyss.PortalManager;
import me.stendec.abyss.util.EntityUtils;
import me.stendec.abyss.util.ParseUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Rotation;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.Map;

public class CreateCommand extends ABCommand {

    public CreateCommand(final AbyssPlugin plugin) {
        super(plugin);

        color = ChatColor.DARK_GREEN;

    }

    public boolean run(final CommandSender sender, final PlayerInteractEvent event, final Block target, ABPortal portal, final ArrayList<String> args) throws NeedsHelp {
        // If we don't have a block, abort.
        if ( target == null ) {
            t().red("You must specify a target block.").send(sender);
            return false;
        }

        if ( portal != null ) {
            portal.getDisplayName().red(" already exists here.").send(sender);
            return false;
        }

        // Make sure we have a water block.
        if (!AbyssPlugin.validLiquid(target)) {
            t().red("Invalid portal location.").send(sender);
            return false;
        }

        // Default Configuration
        int size = -1;

        // First, tokenize our configuration.
        Map<String, String> config = ParseUtils.tokenize(Joiner.on(" ").skipNulls().join(args));

        // Do we have a size value?
        if ( config.containsKey("size") ) {
            final String sz = config.remove("size");
            try {
                size = Integer.parseInt(sz);
            } catch(NumberFormatException ex) {
                t().red("Configuration Error").send(sender);
                t().gray("    Unable to parse ").bold("size").gray(": ").reset(sz).send(sender);
                return false;
            }

            if ( size < 2 ) {
                t().red("Configuration Error").send(sender);
                t("    ").gray().bold("size").gray(" must be at least 2.").send(sender);
                return false;
            }
        }

        // Get the root location, iterating through all possible sizes.
        Location loc = null;

        if ( size != -1 ) {
            loc = plugin.findRoot(target.getLocation(), size);
            if ( loc == null || !plugin.validLayer(loc, size) )
                loc = null;
        } else {
            for( size = plugin.minimumSize; size <= plugin.maximumSize; size++ ) {
                loc = plugin.findRoot(target.getLocation(), size);
                if ( loc == null || !plugin.validLayer(loc, size) )
                    loc = null;
                else
                    break;
            }
        }

        if ( loc == null ) {
            t().red("Invalid portal location.").send(sender);
            return false;
        }

        // Check for existing portals.
        final PortalManager manager = plugin.getManager();
        portal = manager.getByRoot(loc);
        if ( portal != null ) {
            portal.getDisplayName().red(" already exists here.").send(sender);
            return false;
        }

        // Check for the required depth.
        int depth = plugin.getDepthAt(loc, size);
        if ( depth < plugin.minimumDepth ) {
            t().red(ChatColor.DARK_RED, "Portals must be at least %d blocks deep. This space is " +
                    "%d blocks deep.", plugin.minimumDepth, depth).send(sender);
            return false;
        }

        // Run validation.
        if ( !plugin.validateLocation(loc, size) ) {
            t().red("Invalid portal location.");
            return false;
        }

        // Determine the facing direction.
        Rotation rot = null;

        // See if we've got a config setting for that.
        try {
            if ( config.containsKey("rotation") )
                rot = ParseUtils.matchRotation(config.remove("rotation"));
            else if ( config.containsKey("rot") )
                rot = ParseUtils.matchRotation(config.remove("rot"));

        } catch(IllegalArgumentException ex) {
            t().red("Configuration Error").send(sender);
            t("    ").gray(ex.getMessage()).send(sender);
            return false;
        }

        if ( rot == null ) {
            BlockFace facing = (event != null && event.hasBlock()) ? event.getBlockFace().getOppositeFace() : null;
            if ( sender instanceof Player && (facing == null || facing == BlockFace.UP || facing == BlockFace.DOWN || facing == BlockFace.SELF))
                facing = EntityUtils.getFacing((Player) sender);

            if ( facing == BlockFace.EAST ) rot = Rotation.CLOCKWISE;
            else if ( facing == BlockFace.SOUTH ) rot = Rotation.FLIPPED;
            else if ( facing == BlockFace.WEST) rot = Rotation.COUNTER_CLOCKWISE;
            else rot = Rotation.NONE;
        }

        // Make sure we've got a valid owner.
        String owner = (sender instanceof Player) ? sender.getName() : null;
        if ( config.containsKey("owner") )
            owner = config.remove("owner");

        if ( owner == null || owner.length() == 0 ) {
            t().red("Configuration Error").send(sender);
            t().gray("    You must provide an ").bold("owner").gray(".").send(sender);
            return false;
        }

        // Create the new portal.
        portal = new ABPortal(plugin);
        portal.owner = owner;

        // Use any remaining configuration.
        // TODO: Apply remaining configuration.

        // Set the portal location and rotation, and add it to the system.
        portal.setRotation(rot);
        portal.setLocation(loc, size);
        manager.add(portal);

        // Let the sender know it was created successfully and return.
        portal.getDisplayName().darkgreen(" was created successfully.").send(sender);
        return true;
    }

}

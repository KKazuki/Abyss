package me.stendec.abyss.commands;

import me.stendec.abyss.ABCommand;
import me.stendec.abyss.ABPortal;
import me.stendec.abyss.AbyssPlugin;
import me.stendec.abyss.util.ColorBuilder;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;

public class DestinationsCommand extends ABCommand {

    public DestinationsCommand(final AbyssPlugin plugin) {
        super(plugin);

        color = ChatColor.LIGHT_PURPLE;
        require_portal = true;
    }

    public boolean run(final CommandSender sender, final PlayerInteractEvent event, final Block target, ABPortal portal, final ArrayList<String> args) throws ABCommand.NeedsHelp {
        if ( (sender instanceof Player) && !portal.canManipulate((Player) sender) ) {
            t().red("Permission Denied").send(sender);
            return false;
        }

        if ( args != null && args.size() > 0 ) {
            t().red("Too many arguments.").send(sender);
            return false;
        }

        final ColorBuilder out = new ColorBuilder();

        final boolean dist = sender.hasPermission("abyss.detail.distance");

        // Get the destination portals.
        ArrayList<ABPortal> portals = plugin.getManager().getNetworkForDestination(portal, false);
        if ( portals.size() > 0 ) {
            for(final ABPortal p: portals) {
                final String n = p.getName();
                out.lf();

                final double distance = portal.getDistance(p);
                final double range = portal.checkRange(p, distance);

                if ( range > 0 )
                    out.darkgray("%s is %d blocks out of range.", n, Math.round(range));

                else if ( ! p.valid )
                    out.darkgray("%s is obstructed.", n);

                else
                    out.reset("%s is %d blocks within range.", n, Math.round(-range));

                if ( dist )
                    out.darkgray(" [Dist: %d]", Math.round(distance));
            }
        } else {
            out.lf().darkgray("There are no destinations.");
        }

        String[] output = out.toString().split("\n");
        output[0] = t().gold().bold("-- ").yellow().bold("Destinations").gold().bold(" --").toString();
        sender.sendMessage(output);
        return true;
    }

}

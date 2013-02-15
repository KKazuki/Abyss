package me.stendec.abyss.commands;

import me.stendec.abyss.ABCommand;
import me.stendec.abyss.ABPortal;
import me.stendec.abyss.AbyssPlugin;
import me.stendec.abyss.PortalModifier;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class TeleportCommand extends ABCommand {

    public TeleportCommand(final AbyssPlugin plugin) {
        super(plugin, "teleport");

        color = ChatColor.GOLD;
        maximumArguments = 4;
        require_portal = true;

        usage = "<player> <speed-x> <speed-y> <speed-z>";
        description = "Teleport the given player to the targeted portal, optionally with a specific velocity.";
    }

    public List<String> complete(final CommandSender sender, final Block target, final ABPortal portal, final List<String> args) {
        if ( args.size() != 1 || ! sender.hasPermission("abyss.command.teleport_other") )
            return null;

        Player p = (sender instanceof Player) ? (Player) sender : null;
        List<String> out = new ArrayList<String>();
        final String m = args.get(0).toLowerCase();

        for(final Player player: plugin.getServer().getOnlinePlayers()) {
            if ( (p == null || p.canSee(player)) && (m.length() == 0 || player.getName().toLowerCase().startsWith(m)) )
                out.add(player.getName());
        }

        return out;
    }

    public boolean run(final CommandSender sender, final Event event, final Block target, ABPortal portal, final ArrayList<String> args) throws NeedsHelp {
        Player player = (sender instanceof Player) ? (Player) sender : null;

        if ( args.size() > 0 ) {
            final String arg = args.remove(0);
            try {
                player = getPlayer(arg);
            } catch(IllegalArgumentException ex) {
                // Let players omit their own name, if there's three values for velocity.
                boolean valid = true;
                try {
                    Double.parseDouble(arg);
                } catch(NumberFormatException e) { valid = false; }

                if ( !(sender instanceof Player) || !valid || args.size() != 2 ) {
                    sender.sendMessage(ex.getMessage());
                    return false;
                }

                player = (Player) sender;
                args.add(0, arg);
            }
        }

        if ( player == null ) {
            t().red("Non-players using this command must specify a player to be teleported.").send(sender);
            return false;
        }

        if ( !player.equals(sender) && !sender.hasPermission("abyss.command.teleport_other") ) {
            t().red("You do not have permission to teleport other players with this command.").send(sender);
            return false;
        }

        Vector v = new Vector(0,0,0);

        boolean has_input = args.size() > 0;

        if ( args.size() > 0 ) {
            final String arg = args.remove(0);
            try {
                v.setX(Double.parseDouble(arg));
            } catch(NumberFormatException ex) {
                t().red("Invalid number: ").reset(arg).send(sender);
                return false;
            }
        }

        if ( args.size() > 0 ) {
            final String arg = args.remove(0);
            try {
                v.setY(Double.parseDouble(arg));
            } catch(NumberFormatException ex) {
                t().red("Invalid number: ").reset(arg).send(sender);
                return false;
            }
        }

        if ( args.size() > 0 ) {
            final String arg = args.remove(0);
            try {
                v.setZ(Double.parseDouble(arg));
            } catch(NumberFormatException ex) {
                t().red("Invalid number: ").reset(arg).send(sender);
                return false;
            }
        }

        if ( args.size() > 0 ) {
            t().red("Too many arguments.").send(sender);
            return false;
        }

        // Check the velocity.
        double speed = v.length();

        if ( speed < plugin.minimumVelocity ) {
            if ( has_input )
                t().yellow("The speed %.4f is too slow. Using: %.4f", speed, plugin.minimumVelocity).send(sender);

            if ( speed == 0 )
                v.setY(plugin.minimumVelocity);
            else
                v.multiply(plugin.minimumVelocity / speed);

        } else if ( speed > plugin.maximumVelocity ) {
            if ( has_input )
                t().yellow("The speed %.4f is too fast. Using: %.4f", speed, plugin.maximumVelocity).send(sender);

            v.multiply(plugin.maximumVelocity / speed);
        }

        // Attempt to teleport the player.
        Entity ent = null;
        try {
            ent = plugin.doTeleport(player, null, portal, player.getLocation(), v);
        } catch(PortalModifier.Message msg) {
            if ( player.equals(sender) )
                sender.sendMessage(msg.getMessage());
        }

        if ( ent != null ) {
            if ( player.equals(sender) )
                t().darkgreen("You were teleported to ").append(portal.getDisplayName()).
                        darkgreen(" successfully.").send(sender);
            else {
                t().darkgreen("The player ").reset(player.getDisplayName()).darkgreen(" was teleported to ").
                        append(portal.getDisplayName()).darkgreen(" successfully.").send(sender);

                // Let the player know who teleported them to avoid abuse.
                t().darkgreen("You were teleported to ").append(portal.getDisplayName()).darkgreen(" by ").
                        reset((sender instanceof Player) ? ((Player) sender).getDisplayName() : sender.getName()).
                        send(sender);
            }

            return true;
        }

        if ( player.equals(sender) )
            t().red("You could not be teleported to ").append(portal.getDisplayName()).send(sender);
        else
            t().red("The player ").reset(player.getDisplayName()).red(" could not be teleported to ").
                    append(portal.getDisplayName()).send(sender);

        return false;
    }

}

package me.stendec.abyss.commands;

import me.stendec.abyss.ABCommand;
import me.stendec.abyss.ABPortal;
import me.stendec.abyss.AbyssPlugin;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class TeleportCommand extends ABCommand {

    public TeleportCommand(final AbyssPlugin plugin) {
        super(plugin);

        color = ChatColor.GOLD;
        usage = "[portal] <player>";
        require_portal = true;
    }

    public boolean run(final CommandSender sender, final PlayerInteractEvent event, final Block target, ABPortal portal, final ArrayList<String> args) throws NeedsHelp {
        Player player = (sender instanceof Player) ? (Player) sender : null;

        if ( args.size() > 1 ) {
            t().red("Too many arguments.").send(sender);
            return false;
        }

        if ( args.size() > 0 ) {
            final String arg = args.remove(0);
            player = plugin.getServer().getPlayer(arg);
            if ( player == null ) {
                t().white(arg).red(" is not online.").send(sender);
                return false;
            }
        }

        if ( player == null ) {
            t().red("You must specify a player to be teleported.").send(sender);
            return false;
        }

        Vector v = new Vector(0, plugin.minimumVelocity, 0);

        // Attempt to teleport the player.
        if ( plugin.doTeleport(player, null, portal, player.getLocation(), v) != null ) {
            t().white(player.getDisplayName()).darkgreen(" was teleported successfully.").send(sender);

            if ( !player.equals(sender) )
                t().white(sender.getName()).darkgreen(" has teleported you to ").white(portal.getName()).send(player);
            return true;
        }

        t().white(player.getDisplayName()).red(" could not be teleported to ").white(portal.getName()).red(".").send(sender);
        return false;
    }

}

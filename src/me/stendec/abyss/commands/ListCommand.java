package me.stendec.abyss.commands;

import javafx.scene.effect.ColorInputBuilder;
import me.stendec.abyss.*;
import me.stendec.abyss.util.ColorBuilder;
import me.stendec.abyss.util.ParseUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ListCommand extends ABCommand {

    public ListCommand(final AbyssPlugin plugin) {
        super(plugin);

        color = ChatColor.BLUE;
        usage = "<+player> <personal|item|-> <color>";
    }

    public boolean run(final CommandSender sender, final PlayerInteractEvent event, final Block target, ABPortal portal, final ArrayList<String> args) throws NeedsHelp {

        // See if we've got a network to iterate.
        String owner = (portal != null) ? portal.owner : null;
        ItemStack network = (portal != null) ? portal.network : null;
        DyeColor color = (portal != null) ? portal.color : null;

        if ( args.size() > 0 ) {
            final String arg = args.get(0);
            if ( arg.length() > 0 && arg.charAt(0) == '+' ) {
                owner = args.remove(0).substring(1);
                if ( owner.length() == 0 )
                    owner = null;
            }
        }

        if ( args.size() > 0 ) {
            // Try reading an item stack.
            final String arg = args.remove(0);
            if ( ! arg.equals("-") ) {
                network = ParseUtils.matchItem(arg);
                if ( network == null )
                    if ( "personal".startsWith(arg.toLowerCase()) )
                        network = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
                    else {
                        t().red("Invalid network: ").reset(arg).send(sender);
                        return false;
                    }
            }
        }

        if ( args.size() > 0 ) {
            final String arg = args.remove(0);
            color = ParseUtils.matchColor(arg);
            if ( color == null ) {
                t().red("Invalid color: ").reset(arg).send(sender);
                return false;
            }
        }

        // Iterate over the worlds. Store our output in a ColorBuilder for now,
        // so we can paginate later.
        final ColorBuilder out = new ColorBuilder();
        final PortalManager manager = plugin.getManager();

        final ArrayList<ABPortal> portals = new ArrayList<ABPortal>();

        int longest_color = 0;
        int total = 0;

        for(Map.Entry<UUID, ABPortal> entry: manager.entrySet()) {
            ABPortal p = entry.getValue();
            total++;

            if ( owner != null && !(owner.equals(p.owner)) )
                continue;
            else if ( network != null && !(network.getType() == p.network.getType() && network.getDurability() == p.network.getDurability()) )
                continue;
            else if ( color != null && color != p.color )
                continue;

            longest_color = Math.max(longest_color, p.color.name().length());
            portals.add(p);
        }

        Collections.sort(portals);

        for(final ABPortal p: portals) {
            final ItemMeta im = p.network.getItemMeta();
            String nw = ParseUtils.prettyName(p.network);
            if ( p.network.getType() == Material.SKULL_ITEM && p.network.getDurability() == 3 ) {
                nw = t().gray("@").reset(p.owner).toString();
                while ( nw.length() < 16 ) nw = " " + nw;

            } else if ( im.hasDisplayName() ) {
                nw = t(im.getDisplayName()).gray(" (").reset(nw).gray(")").toString();
                while ( nw.length() < 16 ) nw = " " + nw;
            }

            final Location center = p.getCenter();
            out.lf();
            if ( p.valid )
                out.white("+ ").append(p.getName());
            else
                out.gray("- ").white(p.getName());

            // Display less in-game.
            if (! (sender instanceof Player) ) {
                out.darkgray(ChatColor.RESET, " [%12s|%-" + longest_color + "s] @ %d, %d, %d [%s]",
                        nw, ParseUtils.prettyName(p.color), center.getBlockX(),
                        center.getBlockY(), center.getBlockZ(), center.getWorld().getName());

            } else {
                out.darkgray(ChatColor.RESET, ": %s [%s]", nw, ParseUtils.prettyName(p.color));
            }
        }

        // Update the header.
        String[] output = out.toString().split("\n");
        output[0] = t().gold().bold("-- ").yellow().bold("Portals").gold().bold(" -- ").
                yellow(ChatColor.YELLOW, "%d of %d Portals", portals.size(), total).
                gold().bold(" --").toString();

        // Send the lines to the sender.
        sender.sendMessage(output);
        return true;
    }

}

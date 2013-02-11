package me.stendec.abyss.commands;

import com.google.common.collect.ImmutableList;
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
        super(plugin, "list");

        color = ChatColor.DARK_PURPLE;
        maximumArguments = 3;

        usage = "<@owner> <personal|item|all> <color>";
        description = "List the portals that match the given criteria.";
    }

    public List<String> complete(final CommandSender sender, final Block target, final ABPortal portal, final List<String> args) {
        if ( args.size() == 0 )
            return ImmutableList.of("@", "personal", "all");

        // Handle the optional argument.
        if ( args.size() == 1 && args.get(0).startsWith("@") ) {
            final String arg = args.remove(0);
            List<String> out = new ArrayList<String>();
            final String m = arg.substring(1).toLowerCase();
            for(final Iterator<String> it = plugin.getManager().getOwners().iterator(); it.hasNext(); ) {
                final String name = it.next();
                if ( name.startsWith(m) )
                    out.add("@" + name);
            }

            return out;
        }

        // Handle the first argument.
        if ( args.size() == 1 ) {
            final String arg = args.remove(0);
            final String m = arg.toLowerCase();
            final List<String> out = new ArrayList<String>();

            // Make a list of all matching materials, personal, and all.
            // Only use materials if we have *something* to filter by, because there's
            // tons and tons of them.
            if ( m.length() > 0 )
                for(final Material mat: Material.values()) {
                    final String name = mat.name().toLowerCase();
                    if ( name.startsWith(m) )
                        out.add(mat.name());
                }

            if ( "personal".startsWith(m) )
                out.add("personal");

            if ( "all".startsWith(m) )
                out.add("all");

            return out;
        }

        // Handle the second argument.
        else if ( args.size() == 2 ) {
            final String arg = args.remove(1);
            final String m = arg.toLowerCase();
            final List<String> out = new ArrayList<String>();

            for(final DyeColor c: DyeColor.values()) {
                final String name = c.name().toLowerCase();
                if ( name.startsWith(m) )
                    out.add(c.name());
            }

            return out;
        }

        return null;
    }

    public boolean run(final CommandSender sender, final PlayerInteractEvent event, final Block target, ABPortal portal, final ArrayList<String> args) throws NeedsHelp {
        // See if we've got a network to iterate.
        String owner = (portal != null) ? portal.owner : null;
        ItemStack network = (portal != null) ? portal.network : null;
        DyeColor color = (portal != null) ? portal.color : null;

        if ( args.size() > 0 && args.get(0).startsWith("@") ) {
            final String arg = args.remove(0).substring(1);
            OfflinePlayer player = plugin.getServer().getOfflinePlayer(arg);
            if ( ! player.hasPlayedBefore() ) {
                t().red("Invalid player name: ").reset(arg).send(sender);
                return false;
            }

            owner = player.getName();
        }

        if ( args.size() > 0 ) {
            // Try reading the network.
            final String arg = args.remove(0);
            if ( arg.equalsIgnoreCase("all") ) {
                network = null;
            } else if ( arg.equalsIgnoreCase("personal") ) {
                network = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            } else {
                network = ParseUtils.matchItem(arg);
                if ( network == null ) {
                    t().red("Invalid network item: ").reset(arg).send(sender);
                    return false;
                }
            }
        }

        // Try reading a color.
        if ( args.size() > 0 ) {
            final String arg = args.remove(0);
            color = ParseUtils.matchColor(arg);
            if ( color == null ) {
                t().red("Invalid color: ").reset(arg).send(sender);
                return false;
            }
        }

        // If we've still got arguments, we've got too many.
        if ( args.size() > 0 ) {
            t().red("Too many arguments.").send(sender);
            return false;
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

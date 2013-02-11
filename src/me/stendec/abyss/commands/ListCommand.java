package me.stendec.abyss.commands;

import com.google.common.collect.ImmutableList;
import me.stendec.abyss.*;
import me.stendec.abyss.util.ColorBuilder;
import me.stendec.abyss.util.ParseUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
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

    public boolean run(final CommandSender sender, final Event event, final Block target, ABPortal portal, final ArrayList<String> args) throws NeedsHelp {
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
        ColorBuilder out = new ColorBuilder();
        final PortalManager manager = plugin.getManager();

        final ArrayList<ABPortal> portals = new ArrayList<ABPortal>();

        int longest_color = 0;
        int longest_net = 0;
        int biggest_xz = 0;
        int biggest_y = 0;
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

            String nw = ParseUtils.prettyName(p.network);
            if ( p.network.getType() == Material.SKULL_ITEM && p.network.getDurability() == 3 )
                nw = "Personal";
            else if ( p.network.getItemMeta().hasDisplayName() )
                nw = p.network.getItemMeta().getDisplayName() + " (" + nw + ")";

            final Location center = p.getCenter();

            biggest_xz = Math.max(Math.max(biggest_xz, Math.abs(center.getBlockX())), Math.abs(center.getBlockZ()));
            biggest_y = Math.max(biggest_y, center.getBlockY());
            longest_color = Math.max(longest_color, p.color.name().length());
            longest_net = Math.max(longest_net, nw.length());
            portals.add(p);
        }

        Collections.sort(portals);

        biggest_xz = String.valueOf(biggest_xz).length() + 1;
        biggest_y = String.valueOf(biggest_y).length() + 1;

        final String fmt = String.format(" [%%%ds|%%-%ds] @ %%+%dd, %%+%dd, %%+%dd, %%s", longest_net, longest_color, biggest_xz, biggest_y, biggest_xz);

        for(final ABPortal p: portals) {
            final ItemMeta im = p.network.getItemMeta();
            String nw = ParseUtils.prettyName(p.network);
            if ( p.network.getType() == Material.SKULL_ITEM && p.network.getDurability() == 3 ) {
                nw = "Personal";
            } else if ( im.hasDisplayName() ) {
                nw = t(im.getDisplayName()).gray(" (").reset(nw).gray(")").toString();
            }

            final Location center = p.getCenter();
            if ( p.valid )
                out.white("+ ").bold(p.getName());
            else
                out.darkgray("- ").white().bold(p.getName());

            // Display less in-game.
            if (! (sender instanceof Player) ) {
                // Adjust network length based on the longest length.
                int bare = ChatColor.stripColor(nw).length();
                if ( bare < longest_net )
                    nw = StringUtils.repeat(" ", longest_net - bare) + nw;

                out.darkgray(ChatColor.RESET, fmt, nw, ParseUtils.prettyName(p.color), center.getBlockX(),
                        center.getBlockY(), center.getBlockZ(), center.getWorld().getName());

            } else {
                out.reset(" ").append(nw).darkgray(" [").reset(ParseUtils.prettyName(p.color)).darkgray("]");
            }

            out.lf();
        }

        // Trim the last line return.
        out.deleteCharAt(out.length() - 1);

        // Update the header.
        ColorBuilder header = t().gold().bold("-- ").yellow().bold("Portals").gold().bold(" -- ");
        if ( owner != null )
            header.yellow("Owner: ").white(owner).gold().bold(" --");

        header.lf();

        boolean added = false;
        if ( network != null ) {
            String nw = ParseUtils.prettyName(network);
            if ( network.getType() == Material.SKULL_ITEM && network.getDurability() == 3 )
                nw = "Personal";
            header.yellow("  Network: ").white(nw);
            added = true;
        }

        if ( color != null ) {
            header.yellow("  Color: ").white(ParseUtils.prettyName(color));
            added = true;
        }

        if ( added )
            header.lf();

        // Send the lines to the sender.
        sender.sendMessage((header.toString() + out.toString()).split("\n"));
        return true;
    }

}

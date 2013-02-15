package me.stendec.abyss.commands;

import me.stendec.abyss.ABCommand;
import me.stendec.abyss.ABPortal;
import me.stendec.abyss.AbyssPlugin;
import me.stendec.abyss.ModInfo;
import me.stendec.abyss.util.ColorBuilder;
import me.stendec.abyss.util.ParseUtils;
import me.stendec.abyss.util.SafeLocation;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Map;

public class InfoCommand extends ABCommand {

    public InfoCommand(final AbyssPlugin plugin) {
        super(plugin, "information");

        require_portal = true;
        maximumArguments = 0;

        description = "Display information about the targeted portal.";
    }

    public boolean run(final CommandSender sender, final Event event, final Block target, final ABPortal portal, final ArrayList<String> args) throws NeedsHelp {
        if ( (sender instanceof Player) && !portal.canManipulate((Player) sender) ) {
            t().red("Permission Denied").send(sender);
            return false;
        }

        final SafeLocation center = portal.getCenter();

        final ItemMeta im = portal.network.getItemMeta();
        String network = ParseUtils.prettyName(portal.network);
        if ( portal.network.getType() == Material.SKULL_ITEM && portal.network.getDurability() == 3 )
            network = t("Personal ").gray("(").white(portal.owner).gray(")").toString();
        else if ( im.hasDisplayName() )
            network = t(im.getDisplayName()).gray(" (").white(network).gray(")").toString();

        // The Portal Name
        portal.getDisplayName(true).send(sender);

        // The UUID
        if ( sender.hasPermission("abyss.detail.uuid") )
            t().gray("UUID: ").white(portal.uid).send(sender);

        // Network Information
        t().gray(ChatColor.WHITE, "Network: %s [%s]", network, portal.color.name()).send(sender);

        // Center Coordinates
        if ( sender.hasPermission("abyss.detail.location") )
            t().gray("Center: ").darkgray(ChatColor.WHITE, "%d, %d, %d [%s]",
                center.getBlockX(), center.getBlockY(), center.getBlockZ(), center.getWorld().getName()).send(sender);

        // Portal Depth, Size, and Rotation
        final int sx = portal.getSizeX(), sz = portal.getSizeZ();
        String size = (false && sx == sz) ? String.valueOf(sx) : String.format("%dx%d", sx, sz);

        t().gray(ChatColor.WHITE, "Depth: %-4d  Size: %-4s  Rotation: %s", portal.depth, size, portal.getRotation().name()).send(sender);

        // Other Stuff
        t().gray(ChatColor.WHITE, "Closed: %-5s  Velocity: %4.2f  Range: %4.2f", !portal.valid, portal.velocityMultiplier, portal.getRange()).send(sender);

        // ID and Destination
        t().gray(ChatColor.WHITE, "ID: %-5s  Destination: %s",
                (portal.id == 0) ? "None" : Short.toString(portal.id),
                (portal.destination == 0) ? "None" : Short.toString(portal.destination)).send(sender);

        // Modifiers
        t().gray().bold("Modifiers").send(sender);

        boolean has_mods = false;

        if ( portal.mods != null ) {
            for(final ModInfo info: portal.mods)
                if ( info.item != null && info.item.getType() != Material.AIR ) {
                    has_mods = true;
                    break;
                }

            if ( has_mods ) {
                int index = -1;
                for(final ModInfo info: portal.mods) {
                    index++;
                    final ColorBuilder out = t().darkgray().bold(" [%02d] ", index);

                    if ( info.item == null || info.item.getType() == Material.AIR )
                        out.darkgray("None");
                    else
                        out.white(ParseUtils.prettyName(info.item.getType()));

                    if ( !info.flags.isEmpty() ) {
                        final ColorBuilder c = new ColorBuilder();
                        boolean first = true;
                        for(Map.Entry<String, String> entry: info.flags.entrySet()) {
                            final String key = entry.getKey();
                            if ( key.charAt(0) == '*' )
                                continue;

                            if ( ! first )
                                c.darkgray("; ");
                            first = false;

                            c.gray(key);
                            final String value = entry.getValue();
                            if ( value.length() > 0 )
                                c.darkgray(": ").darkpurple(value);
                        }

                        if ( ! first )
                            out.darkgray(" [").append(c.toString()).darkgray("]");
                    }

                    out.send(sender);
                }
            }
        }

        if ( ! has_mods )
            t().darkgray("  None").send(sender);

        return true;
    }

}

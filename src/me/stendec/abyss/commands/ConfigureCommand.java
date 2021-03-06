package me.stendec.abyss.commands;

import com.google.common.base.Joiner;
import me.stendec.abyss.ABCommand;
import me.stendec.abyss.ABPortal;
import me.stendec.abyss.AbyssPlugin;
import me.stendec.abyss.PortalManager;
import me.stendec.abyss.util.ParseUtils;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Map;

public class ConfigureCommand extends ABCommand {

    public ConfigureCommand(final AbyssPlugin plugin) {
        super(plugin, "configure");

        color = ChatColor.AQUA;
        require_portal = true;
        minimumArguments = 1;

        usage = "[<[key]: value>; ...]";
        description = "Apply the given configuration to the targeted portal.";
    }

    public boolean run(final CommandSender sender, final Event event, final Block target, ABPortal portal, final ArrayList<String> args) throws NeedsHelp {
        if ( (sender instanceof Player) && !portal.canManipulate((Player) sender) ) {
            t().red("Permission Denied").send(sender);
            return false;
        }

        Map<String, String> config = ParseUtils.tokenize(Joiner.on(" ").skipNulls().join(args).trim());
        if ( config.isEmpty() ) {
            t().red("Configuration Error").send(sender);
            t().gray("    No configuration to apply.").send(sender);
            return false;
        }

        final PortalManager manager = plugin.getManager();

        // Remove the portal from networks temporarily.
        final ItemStack network = portal.network;
        final String owner = portal.owner;
        final DyeColor color = portal.color;

        final int index = manager.removeFromNetwork(portal);
        manager.removeFromNetworkIds(portal);

        // Also, destroy the item frames.
        portal.destroyEntities(true);

        // Now, apply our configuration.
        try {
            ParseUtils.configFromLore(portal, config);
        } catch(IllegalArgumentException ex) {
            t().red("Configuration Error").send(sender);
            t("    ").gray(ex.getMessage()).send(sender);
            return false;
        } finally {
            // Put the portal back on the networks.
            if ( portal.network.equals(network) && portal.owner.equals(owner) && portal.color == color)
                manager.addToNetwork(portal, index);
            else
                manager.addToNetwork(portal);

            manager.addToNetworkIds(portal);
            portal.createEntities();
        }

        portal.getDisplayName().darkgreen(" was updated successfully.").send(sender);
        return true;
    }

}

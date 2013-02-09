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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Map;

public class ConfigureCommand extends ABCommand {

    public ConfigureCommand(final AbyssPlugin plugin) {
        super(plugin);

        color = ChatColor.AQUA;
        require_portal = true;
        usage = "[@block|@player|portal] <[key: value]; ...>";
    }

    public boolean run(final CommandSender sender, final PlayerInteractEvent event, final Block target, ABPortal portal, final ArrayList<String> args) throws NeedsHelp {
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

        // Remove the portal from networks temporarilly.
        final ItemStack network = portal.network;
        final String owner = portal.owner;
        final DyeColor color = portal.color;

        final int index = manager.removeFromNetwork(portal);
        manager.removeFromNetworkIds(portal);

        // Also, destroy the item frames.
        portal.destroyEntities(true);

        // Now, apply our configuration.
        try {
            configFromLore(portal, config);
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



}

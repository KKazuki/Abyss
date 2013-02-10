package me.stendec.abyss.commands;

import me.stendec.abyss.ABCommand;
import me.stendec.abyss.ABPortal;
import me.stendec.abyss.AbyssPlugin;
import me.stendec.abyss.PortalManager;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;

public class ReloadCommand extends ABCommand {

    public ReloadCommand(final AbyssPlugin plugin) {
        super(plugin, "reload");

        allow_wand = false;
        maximumArguments = 0;

        description = "Reload Abyss's configuration from file.";
    }

    public boolean run(final CommandSender sender, final PlayerInteractEvent event, final Block target, final ABPortal portal, final ArrayList<String> args) throws NeedsHelp {
        // First, save out the current portals.
        plugin.savePortals();

        // Clear the current portals.
        PortalManager manager = plugin.getManager();
        manager.clear();

        // Now, reload the configuration.
        plugin.configure();

        // And reload the portals.
        plugin.loadPortals();

        sender.sendMessage("Abyss has been reloaded.");
        return true;
    }

}

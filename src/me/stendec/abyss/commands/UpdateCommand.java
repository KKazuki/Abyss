package me.stendec.abyss.commands;

import me.stendec.abyss.ABCommand;
import me.stendec.abyss.ABPortal;
import me.stendec.abyss.AbyssPlugin;
import me.stendec.abyss.util.Updater;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;

import java.util.ArrayList;

public class UpdateCommand extends ABCommand {

    public UpdateCommand(final AbyssPlugin plugin) {
        super(plugin, "update");

        allow_wand = false;
        maximumArguments = 1;
        usage = "<check>";
        description = "Download an update for Abyss. Optionally, just check for an available update.";
    }

    public boolean run(final CommandSender sender, final Event event, final Block target, ABPortal portal, final ArrayList<String> args) throws NeedsHelp {
        if ( ! sender.hasPermission("abyss.update") ) {
            t().red("Permission Denied").send(sender);
            return false;
        }

        boolean check = false;

        if ( args.size() > 0 ) {
            final String arg = args.remove(0);
            if ( arg.equalsIgnoreCase("check") )
                check = true;
            else {
                t().red("Invalid argument: ").reset(arg).send(sender);
                return false;
            }
        }

        // See if we've currently got state.
        if ( plugin.updater != null ) {
            Updater.UpdateResult result = plugin.updater.getResult(false);
            if ( result == Updater.UpdateResult.DOWNLOADING || result == Updater.UpdateResult.SUCCESS ) {
                sender.sendMessage(plugin.updateMessage);
                return true;

            } else if ( result == Updater.UpdateResult.NOT_READY ) {
                sender.sendMessage("Checking for updates...");
                plugin.updateCheckers.add(sender.getName());
                return true;

            } else if ( check && result == Updater.UpdateResult.UPDATE_AVAILABLE ) {
                sender.sendMessage(plugin.updateMessage);
                return true;
            }
        }

        // Make a new updater.
        plugin.startUpdater((byte) (check ? 1 : 2));
        sender.sendMessage("Checking for updates...");
        plugin.updateCheckers.add(sender.getName());

        return true;
    }

}

package me.stendec.abyss.commands;

import me.stendec.abyss.ABCommand;
import me.stendec.abyss.ABPortal;
import me.stendec.abyss.AbyssPlugin;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;

public class ModifierCommand extends ABCommand {

    public ModifierCommand(final AbyssPlugin plugin) {
        super(plugin);

        color = ChatColor.DARK_AQUA;
        require_portal = true;
    }

    public boolean run(final CommandSender sender, final PlayerInteractEvent event, final Block target, ABPortal portal, final ArrayList<String> args) throws ABCommand.NeedsHelp {
        if (!(sender instanceof Player)) {
            t().red("This tool can only be used by players, as it requires clicking an item frame in the world.").send(sender);
            return false;
        }

        t().red("You must click a modifier frame to use the modifier wand.").send(sender);
        return false;
    }
}

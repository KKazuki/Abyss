package me.stendec.abyss.commands;

import com.google.common.base.Joiner;
import me.stendec.abyss.ABCommand;
import me.stendec.abyss.ABPortal;
import me.stendec.abyss.AbyssPlugin;
import me.stendec.abyss.ModInfo;
import me.stendec.abyss.util.ParseUtils;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.ArrayList;
import java.util.Map;

public class ModifierCommand extends ABCommand {

    public ModifierCommand(final AbyssPlugin plugin) {
        super(plugin, "modifier");

        color = ChatColor.DARK_AQUA;
        require_portal = true;
        minimumArguments = 1;

        usage = "[index] <[key: value]; ...>";
        description = "Apply the given configuration to the given portal modifier.";
    }

    public boolean run(final CommandSender sender, final Event event, final Block target, ABPortal portal, final ArrayList<String> args) throws ABCommand.NeedsHelp {
        if ( (sender instanceof Player) && !portal.canManipulate((Player) sender) ) {
            t().red("Permission Denied").send(sender);
            return false;
        }

        int index = -1;

        // If we have an entity event, use that for our default index.
        if ( event != null && event instanceof PlayerInteractEntityEvent ) {
            PlayerInteractEntityEvent entityEvent = (PlayerInteractEntityEvent) event;
            Entity entity = entityEvent.getRightClicked();
            if ( entity instanceof ItemFrame ) {
                final ModInfo info = portal.getModFromFrame((ItemFrame) entity);
                if ( info != null )
                    index = portal.mods.indexOf(info);
            }
        }

        // Check for an index.
        if ( args.size() > 0 ) {
            final String arg = args.remove(0);

            try {
                index = Integer.parseInt(arg);
            } catch(NumberFormatException ex) {
                args.add(0, arg);
            }
        }

        // Reset the index to make it error if it's out of bounds.
        if ( portal.mods == null || index >= portal.mods.size() ) {
            if ( event != null )
                args.add(0, String.valueOf(index));
            index = -1;
        }

        // Error if we don't have an index.
        if ( index == -1 ) {
            if ( event != null ) {
                t().red("You must click a valid modifier frame to use the modifier wand.").send(sender);
            } else {
                t().red("Invalid modifier index: ").reset(args.get(0)).send(sender);
            }
            return false;
        }

        // Make sure we actually have configuration after getting rid of the index.
        if ( args.size() == 0 ) {
            t().red("Not enough arguments.").send(sender);
            return false;
        }

        // Get the modifier info for that frame.
        final ModInfo info = portal.mods.get(index);
        if ( info == null ) {
            t().red("Error fetching modifier info for index %d.", index).send(sender);
            return false;
        }

        // Tokenize the arguments and apply them.
        Map<String, String> config = ParseUtils.tokenize(Joiner.on(" ").skipNulls().join(args));
        if ( config != null )
            for(final Map.Entry<String, String> entry: config.entrySet()) {
                String key = entry.getKey();
                if ( key.startsWith("-") ) {
                    key = key.substring(1);
                    if ( info.flags.containsKey(key) )
                        info.flags.remove(key);
                } else {
                    info.flags.put(key, entry.getValue());
                }
            }

        t().darkgreen("Modifier %02d of ", index).append(portal.getDisplayName()).
                darkgreen(" was updated successfully.").send(sender);
        return true;
    }
}

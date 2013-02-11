package me.stendec.abyss.commands;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import me.stendec.abyss.ABCommand;
import me.stendec.abyss.ABPortal;
import me.stendec.abyss.AbyssPlugin;
import org.apache.commons.lang.WordUtils;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WandCommand extends ABCommand {

    public WandCommand(final AbyssPlugin plugin) {
        super(plugin, "wand");

        minimumArguments = 1;
        try_block = true;

        usage = "<uses> [command] <arguments>";
        description = "Create a wand for the given command, optionally with the" +
                " given number of uses and the given arguments.";
    }

    public List<String> complete(final CommandSender sender, final Block target, final ABPortal portal, final List<String> args) {
        // Deal with the optional use count.
        if ( args.size() > 0 ) {
            String arg = args.remove(0);
            try {
                Integer.parseInt(arg);
            } catch(NumberFormatException ex) {
                args.add(0, arg);
            }
        }

        // See if we've got a command.
        final String cmdkey = plugin.getABCommand("abyss", args, false);
        if ( cmdkey == null )
            // No sub-command? Auto-complete it!
            return ImmutableList.copyOf(plugin.commands.keySet());

        // Try getting a command.
        ABCommand cmd = plugin.commands.get(cmdkey);
        if ( cmd == null ) {
            List<String> out = new ArrayList<String>(plugin.commands.keySet());
            for(final Iterator<String> it = out.iterator(); it.hasNext(); )
                if ( ! it.next().startsWith(cmdkey) )
                    it.remove();

            return out;
        }

        // We have a command... so pass the buck to *its* auto-completer.
        return cmd.complete(sender, target, portal, args);
    }


    public boolean run(final CommandSender sender, final Event event, Block target, final ABPortal portal, final ArrayList<String> args) throws NeedsHelp {
        // First, try for the optional use count.
        String arg = args.remove(0);
        int uses = 0;

        try {
            uses = Integer.parseInt(arg);
        } catch(NumberFormatException ex) {
            // If it's not a valid number, put it back.
            args.add(0, arg);
        }

        // Make sure we've still got at least one argument.
        if ( args.size() == 0 ) {
            t().red("Not enough arguments.").send(sender);
            return false;
        }

        // Now, get the command.
        final String cmdkey = plugin.getABCommand("abyss", args, true);
        final ABCommand cmd = plugin.commands.get(cmdkey);
        if ( cmd == null ) {
            t().red("No such command: ").reset(cmdkey).send(sender);
            return false;
        }

        // See if the sender has the permission for that command.
        Permission perm = plugin.getServer().getPluginManager().getPermission("abyss.command." + cmdkey);
        if ( perm != null && !sender.hasPermission(perm) ) {
            t().red("You cannot make a wand for a command you are not allowed to use.").send(sender);
            return false;
        }

        // Prettify the command name.
        final String name = WordUtils.capitalize(cmd.name.replaceAll("_", " "));

        // Make sure it can be made into a wand.
        if ( ! cmd.allow_wand ) {
            t().red("The command ").append(cmd.color).bold(name).red(" cannot be made into a wand.").send(sender);
            return false;
        }

        // Make sure we've got enough arguments.
        if ( cmd.minimumArguments > args.size() ) {
            t().red("The command ").append(cmd.color).bold(name).red(" requires at least %d arguments.", cmd.minimumArguments).send(sender);
            return false;
        }

        // Create the basic item.
        final ItemStack wand = new ItemStack(plugin.wandMaterial);
        final ItemMeta im = wand.getItemMeta();

        im.setDisplayName(t().white(plugin.wandName).darkgray(cmd.color, " [%s]", name).toString());

        if ( args.size() > 0 || uses > 0 ) {
            ArrayList<String> lore = new ArrayList<String>(2);
            if ( uses > 0 )
                lore.add(t().gray("Remaining Uses: ").white(uses).toString());

            // Join the rest of the arguments back together.
            if ( args.size() > 0 )
                lore.add(Joiner.on(" ").join(args));

            im.setLore(lore);
        }

        // Set the item meta.
        wand.setItemMeta(im);

        if ( target != null ) {
            target.getWorld().dropItemNaturally(target.getLocation(), wand);
        } else {
            ((Player) sender).getInventory().addItem(wand);
        }

        return true;
    }

}

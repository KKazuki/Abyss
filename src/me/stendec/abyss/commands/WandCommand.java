package me.stendec.abyss.commands;

import com.google.common.base.Joiner;
import me.stendec.abyss.ABCommand;
import me.stendec.abyss.ABPortal;
import me.stendec.abyss.AbyssPlugin;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;

import java.util.ArrayList;
import java.util.Collections;

public class WandCommand extends ABCommand {

    public WandCommand(final AbyssPlugin plugin) {
        super(plugin);

        usage = "<@block|@player> [command] <uses> <arguments>";
    }

    public boolean run(final CommandSender sender, final PlayerInteractEvent event, final Block target, final ABPortal portal, final ArrayList<String> args) throws NeedsHelp {
        if ( args.size() == 0 )
            throw new NeedsHelp();

        // If the sender isn't a player, and we don't have a block to drop the wand at, die.
        if ( !(sender instanceof Player) && target == null) {
            t().red("You must provide a target block or player when using this command from the console.").send(sender);
            return false;
        }

        // Get the command.
        String cmd = args.remove(0).toLowerCase();
        if ( plugin.aliases.containsKey(cmd) )
            cmd = plugin.aliases.get(cmd);

        if ( ! plugin.commands.containsKey(cmd) ) {
            final ArrayList<String> possible = new ArrayList<String>();
            for(final String key: plugin.commands.keySet())
                if ( key.startsWith(cmd) )
                    possible.add(key);

            if ( possible.size() > 0 ) {
                // We've got a command, so use it.
                Collections.sort(possible);
                cmd = possible.get(0);
            } else {
                t().red("No such command: ").reset(cmd).send(sender);
                return false;
            }
        }

        // See if the sender has the permission for that command.
        Permission perm = plugin.getServer().getPluginManager().getPermission("abyss.command." + cmd);
        if ( perm != null && !sender.hasPermission(perm) ) {
            t().red("You cannot make a wand for a command you are not allowed to use.").send(sender);
            return false;
        }

        // Check for a remaining uses count.
        int uses = 0;
        if ( args.size() > 0 ) {
            final String ustr = args.remove(0);
            try {
                uses = Integer.parseInt(ustr);
            } catch(NumberFormatException ex) {
                t().red("Invalid use count: ").reset(ustr).send(sender);
                return false;
            }
        }

        // Create the basic item.
        final ItemStack wand = new ItemStack(plugin.wandMaterial);
        final ItemMeta im = wand.getItemMeta();

        // Wand Name
        final ABCommand command = plugin.commands.get(cmd);
        final String name = command.color + WordUtils.capitalize(cmd.replaceAll("_", " "));
        im.setDisplayName(t().white(plugin.wandName).gray(" [").append(name).gray("]").toString());

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

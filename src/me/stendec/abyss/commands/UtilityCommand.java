package me.stendec.abyss.commands;

import me.stendec.abyss.ABCommand;
import me.stendec.abyss.ABPortal;
import me.stendec.abyss.AbyssPlugin;
import me.stendec.abyss.ModInfo;
import me.stendec.abyss.util.SafeLocation;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UtilityCommand extends ABCommand {

    public UtilityCommand(final AbyssPlugin plugin) {
        super(plugin, "utility");

        color = ChatColor.GREEN;
        require_portal = true;

        usage = "[index] <@block|+player|*player>";
        description = "Move the given portal modifier's utility block to a new location.";
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
            if ( entity instanceof ItemFrame) {
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

        // If we don't have an index, display the block locations for every block.
        if ( index == -1 ) {
            // TODO: Fix this.
            return true;
        }

        // Get the modifier info for that frame.
        final ModInfo info = portal.mods.get(index);
        if ( info == null ) {
            t().red("Error fetching modifier info for index %d.", index).send(sender);
            return false;
        }

        Block block = null;

        // Get the block location from the event.
        if ( event != null && event instanceof PlayerInteractEvent ) {
            final Block b = ((PlayerInteractEvent) event).getClickedBlock();
            block = (b != null) ? b : target;
        }

        // If we've got arguments, read the block location.
        if ( args.size() > 1 ) {
            t().red("Too many arguments.").send(sender);
            return false;

        } else if ( args.size() == 1 ) {
            final String arg = args.remove(0);

            try {
                block = parseBlock(sender, arg);
            } catch(IllegalArgumentException ex) {
                sender.sendMessage(ex.getMessage());
                return false;
            }
        }

        // If we've got the block, just set it.
        if ( block != null ) {
            final Location loc = block.getLocation();
            info.location = new SafeLocation(loc);

            // Play an effect to make it more noticeable.
            loc.getWorld().playEffect(loc, plugin.portalEffect, plugin.portalEffectData);

            t().darkgreen("The Utility Block of Modifier %02d of ", index).append(portal.getDisplayName()).
                darkgreen(" was set to: ").darkgray(ChatColor.RESET, "%d, %d, %d [%s]",
                    block.getX(), block.getY(), block.getZ(), block.getWorld().getName()).send(sender);

            if ( event != null && event instanceof PlayerInteractEvent ) {
                // Reset our metadata.
                clearTarget(((PlayerInteractEvent) event).getItem());
            }

            return true;
        }

        // If we've got an event, modify that wand!
        if ( event != null && event instanceof PlayerInteractEntityEvent ) {
            setTarget(((PlayerInteractEntityEvent) event).getPlayer(), ((PlayerInteractEntityEvent) event).getPlayer().getItemInHand(), portal, index);
            t().yellow("Right-click the block you'd like to set as the Utility Block for this modifier.").send(sender);

            // Make sure the wand *isn't* consumed this time.
            return false;
        }


        // Nothing? Just display the current location then.
        if ( info.location == null )
            info.updateLocation();

        block = info.location.getBlock();

        t().darkgreen("The Utility Block of Modifier %02d of ", index).append(portal.getDisplayName()).
                darkgreen(" is located at: ").darkgray(ChatColor.RESET, "%d, %d, %d [%s]",
                block.getX(), block.getY(), block.getZ(), block.getWorld().getName()).send(sender);
        return true;
    }

    private void clearTarget(final ItemStack wand) {
        final ItemMeta im = wand.getItemMeta();
        if ( ! im.hasLore() )
            return;

        List<String> lore = im.getLore();
        for(final Iterator<String> it = lore.iterator(); it.hasNext(); ) {
            String l = it.next();
            final String plain = ChatColor.stripColor(l).toLowerCase();
            if ( ! plain.startsWith("remaining uses: ") )
                it.remove();
        }

        im.setLore(lore);
        wand.setItemMeta(im);
    }

    private boolean setTarget(final Player player, final ItemStack wand, final ABPortal portal, final int index) {
        ItemStack rest = null;

        // If it's part of a stack, we'll want to split it up here.
        if ( wand.getAmount() > 1 ) {
            rest = wand.clone();
            rest.setAmount(wand.getAmount() - 1);

            wand.setAmount(1);
        }

        final ItemMeta im = wand.getItemMeta();
        List<String> lore = im.hasLore() ? im.getLore() : new ArrayList<String>();

        boolean result = false;
        if ( lore.size() < 4 ) {
            lore.add(portal.getName() + " " + index);
            im.setLore(lore);
            wand.setItemMeta(im);
        }

        // If we've got extra items, add them.
        if ( rest != null ) {
            for(final ItemStack dnf: player.getInventory().addItem(rest).values())
                player.getWorld().dropItemNaturally(player.getLocation(), dnf);
            player.updateInventory();
        }

        return result;
    }

}

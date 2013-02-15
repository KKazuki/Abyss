package me.stendec.abyss.modifiers;

import me.stendec.abyss.ABPortal;
import me.stendec.abyss.ModInfo;
import me.stendec.abyss.PortalModifier;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class EXPBottleModifier extends PortalModifier {

    public boolean onApply(final Player player, final ABPortal portal, final ModInfo info, final ItemStack item) {
        if ( !info.flags.containsKey("arrive") && !info.flags.containsKey("depart") )
            t().yellow("Be sure to set an ").bold("arrive").yellow(" or a ").bold("depart").
                    yellow(" cost flag on this modifier.").send(player);

        return true;
    }

    public boolean preTeleport(ABPortal from, ABPortal portal, ModInfo info, Entity entity, Location destination, Vector velocity) throws Message {
        // We only care about players coming from portals.
        if ( !(entity instanceof Player) || from == null )
            return true;

        Player player = (Player) entity;
        String cost = info.flags.get(info.getPortal().equals(from) ? "depart" : "arrive");
        if ( cost == null || cost.length() == 0 )
            return true;

        cost = cost.toLowerCase();

        boolean levels = cost.endsWith("l");
        if ( levels )
            cost = cost.substring(0, cost.length() - 1);

        int value = 0;
        try {
            value = Integer.parseInt(cost);
        } catch(NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid cost: " + cost);
        }

        // We only care about positive costs.
        if ( value <= 0 )
            return true;

        if ( levels && player.getLevel() < value ) {
            if ( info.flags.containsKey("silent") )
                return false;
            else if ( info.flags.containsKey("check only") )
                throw new Message(String.format("You must be at least level %d to use this portal.", value));
            else
                throw new Message(String.format("It costs %d levels to use this portal.", value));

        } else if ( player.getTotalExperience() < value ) {
            if ( info.flags.containsKey("silent") )
                return false;
            else
                throw new Message("You do not have enough experience to use this portal.");
        }

        return true;
    }

    public void postTeleport(final ABPortal from, final ABPortal portal, final ModInfo info, final Entity entity) {
        // We only care about players coming from portals.
        if ( !(entity instanceof Player) || from == null || info.flags.containsKey("check only") )
            return;

        Player player = (Player) entity;

        // If there's no cost, return.
        String cost = info.flags.get(info.getPortal().equals(from) ? "depart" : "arrive");
        if ( cost == null || cost.length() == 0 )
            return;

        cost = cost.toLowerCase();

        boolean levels = cost.endsWith("l");
        if ( levels )
            cost = cost.substring(0, cost.length() - 1);

        int value = 0;
        try {
            value = Integer.parseInt(cost);
        } catch(NumberFormatException ex) { return; }

        final boolean silent = info.flags.containsKey("silent");

        if ( value > 0 ) {
            if ( levels ) {
                player.setLevel(Math.max(0, player.getLevel() - value));
                if ( !silent )
                    t("You paid ").bold(value).reset(" levels to use this portal.").send(player);
            } else {
                player.setTotalExperience(Math.max(0, player.getTotalExperience() - value));
                if ( !silent )
                    t("You paid ").bold(value).reset(" experience to use this portal.").send(player);
            }
        } else {
            value = -value;

            if ( levels ) {
                player.setLevel(player.getLevel() + value);
                if ( !silent )
                    t("You received ").bold(value).reset(" levels for using this portal.").send(player);
            } else {
                player.setTotalExperience(player.getTotalExperience() + value);
                if ( !silent )
                    t("You received ").bold(value).reset(" experience for using this portal.").send(player);
            }
        }

    }

}

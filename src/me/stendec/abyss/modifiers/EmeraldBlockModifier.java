package me.stendec.abyss.modifiers;

import me.stendec.abyss.ABPortal;
import me.stendec.abyss.AbyssPlugin;
import me.stendec.abyss.ModInfo;
import me.stendec.abyss.PortalModifier;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.util.Vector;

public class EmeraldBlockModifier extends PortalModifier {

    private static Economy economy = null;

    public Economy getEconomy(final AbyssPlugin plugin) {
        if ( economy != null )
            return economy;

        Plugin vault = plugin.getServer().getPluginManager().getPlugin("Vault");
        if ( vault == null )
            return null;

        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if ( rsp == null )
            return null;

        economy = rsp.getProvider();
        return economy;
    }

    public boolean onApply(final Player player, final ABPortal portal, final ModInfo info, final ItemStack item) {
        // If we don't have the economy, throw a shoe.
        Economy econ = getEconomy(portal.getPlugin());

        // Don't apply it if we don't have the economy.
        if ( econ == null )
            throw new IllegalArgumentException("Vault is missing or has not provided an Economy.");

        if ( ! info.flags.containsKey("arrive") && !info.flags.containsKey("depart") )
            t().yellow("Be sure to set an ").bold("arrive").yellow(" or a ").bold("depart").
                yellow(" cost flag on this modifier.").send(player);

        return true;
    }

    public boolean preTeleport(ABPortal from, ABPortal portal, ModInfo info, Entity entity, Location destination, Vector velocity) throws Message {
        // We only care about players coming from portals.
        if ( ! (entity instanceof Player) || from == null )
            return true;

        Player player = (Player) entity;

        // If we can't get the economy, stop right now.
        Economy econ = getEconomy(portal.getPlugin());
        if ( econ == null )
            return false;

        // Try parsing the cost.
        final String cost = info.flags.get(info.getPortal().equals(from) ? "depart" : "arrive");
        if ( cost == null || cost.length() == 0 )
            return true;

        double value = 0;
        try {
            value = Double.parseDouble(cost);
        } catch(NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid cost: " + cost);
        }

        if ( value > 0 ) {
            double balance = econ.getBalance(player.getName());
            if ( balance < value ) {
                String name = econ.currencyNamePlural();
                if ( name == null || name.length() == 0 )
                    name = "money";

                if ( ! info.flags.containsKey("silent") )
                    throw new Message(String.format("You do not have enough %s to use this portal.", name));
                return false;
            }
        }

        return true;
    }

    public void postTeleport(final ABPortal from, final ABPortal portal, final ModInfo info, final Entity entity) {
        // We only care about players that came from a portal.
        if ( ! (entity instanceof Player) || from == null || info.flags.containsKey("check only"))
            return;

        // Get the player.
        Player player = (Player) entity;

        // If we can't get the economy, stop right now.
        Economy econ = getEconomy(portal.getPlugin());
        if ( econ == null )
            return;

        // Determine the cost.
        final String cost = info.flags.get(info.getPortal().equals(from) ? "depart" : "arrive");
        if ( cost == null || cost.length() == 0 )
            return;

        double value = 0;
        try {
            value = Double.parseDouble(cost);
        } catch(NumberFormatException ex) { return; }

        final boolean silent = info.flags.containsKey("silent");

        if ( value > 0 ) {
            final EconomyResponse resp = econ.withdrawPlayer(player.getName(), value);
            if ( !silent && resp.transactionSuccess() )
                t("You paid ").bold(econ.format(value)).reset(" to use this portal.").send(player);

        } else if ( value < 0 ) {
            value = -value;
            final EconomyResponse resp = econ.depositPlayer(player.getName(), value);
            if ( !silent && resp.transactionSuccess() )
                t("You received ").bold(econ.format(value)).reset(" for using this portal.").send(player);
        }
    }

}

package me.stendec.abyss.modifiers;

import me.stendec.abyss.ABPortal;
import me.stendec.abyss.ModInfo;
import me.stendec.abyss.PortalModifier;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SlimeModifier extends PortalModifier {

    public boolean onApply(final Player player, final ABPortal portal, final ModInfo info, final ItemStack item) {
        // Limit the velocity multiplier.
        if (portal.velocityMultiplier >= 5) {
            player.sendMessage(ChatColor.YELLOW + "This portal is already as fast as possible.");
            return false;
        }

        portal.velocityMultiplier += 1;
        return true;
    }

    public boolean onRemove(final Player player, final ABPortal portal, final ModInfo info, final ItemStack item) {
        // Always remove it, but never reduce the velocity below 1.
        if (portal.velocityMultiplier >= 2)
            portal.velocityMultiplier -= 1;

        return true;
    }

}

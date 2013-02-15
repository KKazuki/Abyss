package me.stendec.abyss.modifiers;

import me.stendec.abyss.ABPortal;
import me.stendec.abyss.ModInfo;
import me.stendec.abyss.PortalModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SlimeModifier extends PortalModifier {

    public boolean onApply(final Player player, final ABPortal portal, final ModInfo info, final ItemStack item) {
        portal.velocityMultiplier += portal.getPlugin().slimeBallStrength;
        return true;
    }

    public boolean onRemove(final Player player, final ABPortal portal, final ModInfo info, final ItemStack item) {
        // Always remove it, but never reduce the velocity below 1.
        portal.velocityMultiplier -= portal.getPlugin().slimeBallStrength;
        if ( portal.velocityMultiplier <= 1 )
            portal.velocityMultiplier = 1;

        return true;
    }

}

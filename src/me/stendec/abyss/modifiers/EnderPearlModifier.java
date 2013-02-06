package me.stendec.abyss.modifiers;

import me.stendec.abyss.ABPortal;
import me.stendec.abyss.ModInfo;
import me.stendec.abyss.PortalModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EnderPearlModifier extends PortalModifier {

    public boolean onApply(Player player, ABPortal portal, ModInfo info, ItemStack item) {
        // Just add 1 to the range multiplier.
        portal.rangeMultiplier += 1;

        return true;
    }

    public boolean onRemove(Player player, ABPortal portal, ModInfo info, ItemStack item) {
        if (portal.rangeMultiplier >= 2)
            portal.rangeMultiplier -= 1;

        return true;
    }
}

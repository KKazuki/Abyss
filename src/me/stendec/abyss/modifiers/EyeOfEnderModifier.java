package me.stendec.abyss.modifiers;

import me.stendec.abyss.ABPortal;
import me.stendec.abyss.ModInfo;
import me.stendec.abyss.PortalModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EyeOfEnderModifier extends PortalModifier {

    public boolean onApply(Player player, ABPortal portal, ModInfo info, ItemStack item) {
        portal.eyeCount++;
        portal.rangeMultiplier += 5;
        return true;
    }

    public boolean onRemove(Player player, ABPortal portal, ModInfo info, ItemStack item) {
        if ( portal.eyeCount > 0 )
            portal.eyeCount--;

        if ( portal.rangeMultiplier >= 6 )
            portal.rangeMultiplier -= 5;

        return true;
    }
}

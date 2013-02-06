package me.stendec.abyss.modifiers;

import me.stendec.abyss.ABPortal;
import me.stendec.abyss.ModInfo;
import me.stendec.abyss.PortalModifier;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class ObsidianModifier extends PortalModifier {

    public boolean preTeleport(final ABPortal from, final ABPortal portal, final ModInfo info, final Entity entity, final Location destination, final Vector velocity) {
        final ABPortal owner = info.getPortal();
        if ( owner == null )
            return true;

        if ( info.item.getType() == Material.OBSIDIAN )
            return owner.equals(from);
        else
            return owner.equals(portal);
    }

}

package me.stendec.abyss.modifiers;

import me.stendec.abyss.ABPortal;
import me.stendec.abyss.ModInfo;
import me.stendec.abyss.PortalModifier;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class CompassModifier extends PortalModifier {

    public void postTeleport(final ABPortal from, final ABPortal portal, final ModInfo info, final Entity entity) {
        // We only care about arriving players.
        final ABPortal owner = info.getPortal();
        if ( owner == null || owner.equals(from) || !(entity instanceof Player) )
            return;

        final Player player = (Player) entity;
        final boolean silent = info.flags.containsKey("silent");

        if (info.flags.containsKey("reset")) {
            player.setCompassTarget(null);
            if ( ! silent )
                player.sendMessage("Your compass has been reset.");

        } else if ( info.location != null ) {
            final Location loc = info.location.getLocation();
            if ( loc != null ) {
                player.setCompassTarget(loc);
                if ( ! silent )
                    player.sendMessage("Your compass has been updated.");
            }
        }
    }

}

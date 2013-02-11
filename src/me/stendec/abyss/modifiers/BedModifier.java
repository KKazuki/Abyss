package me.stendec.abyss.modifiers;

import me.stendec.abyss.ABPortal;
import me.stendec.abyss.ModInfo;
import me.stendec.abyss.PortalModifier;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BedModifier extends PortalModifier {

    private static boolean canSpawnAt(final Location loc) {
        return canSpawnAt(loc.getBlock());
    }

    private static boolean canSpawnAt(final Block block) {
        return block.getType() == Material.AIR && block.getRelative(BlockFace.UP).getType() == Material.AIR;
    }

    public boolean onApply(final Player player, final ABPortal portal, final ModInfo info, final ItemStack item) {
        // Make sure that the location is a valid spawning location.
        if (info.location == null)
            info.updateLocation();

        final Location loc = info.location.clone();
        final int maxHeight = loc.getWorld().getMaxHeight();

        // Move up.
        while (loc.getBlockY() <= maxHeight && !canSpawnAt(loc))
            loc.add(0, 1, 0);

        if (!canSpawnAt(loc))
            throw new IllegalArgumentException("Please set the Utility Block for this modifier to a valid spawn location before setting it.");

        if ( !info.location.equals(loc) && !info.flags.containsKey("*moved") ) {
            info.flags.put("*moved", String.format("%f;%f;%f", info.location.getX(), info.location.getY(), info.location.getZ()));
            info.location = loc;
        }

        return true;
    }

    public boolean onRemove(final Player player, final ABPortal portal, final ModInfo info, final ItemStack item) {
        // Clear the location if it was moved.
        final String moved = info.flags.get("*moved");
        if ( moved != null ) {
            if ( info.location != null && moved.contains(";") ) {
                final String[] parts = moved.split(";");
                final Location loc = info.location;
                loc.setX(Double.parseDouble(parts[0]));
                loc.setY(Double.parseDouble(parts[1]));
                loc.setZ(Double.parseDouble(parts[2]));

            } else {
                info.updateLocation();
                player.sendMessage("The modifier location was reset.");
            }

            info.flags.remove("*moved");
        }

        return true;
    }

    public void postTeleport(final ABPortal from, final ABPortal portal, final ModInfo info, final Entity entity) {
        // We only care about arriving players.
        final ABPortal owner = info.getPortal();
        if ( owner == null || owner.equals(from) || !(entity instanceof Player))
            return;

        // Make sure the location is valid before we set the spawn point.
        final Location loc = info.location;
        if (!canSpawnAt(loc))
            throw new IllegalArgumentException(String.format("Invalid spawn location: (%d, %d, %d)", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));

        final Player player = (Player) entity;
        player.setBedSpawnLocation(loc, true);

        // Let the player know about it.
        if (! info.flags.containsKey("silent"))
            player.sendMessage("Your spawn location has been updated.");
    }
}

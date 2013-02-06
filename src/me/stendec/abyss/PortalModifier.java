package me.stendec.abyss;

import me.stendec.abyss.util.ColorBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.util.Vector;

import java.util.HashMap;

public abstract class PortalModifier {

    ///////////////////////////////////////////////////////////////////////////
    // Modifier Storage
    ///////////////////////////////////////////////////////////////////////////

    private final static HashMap<Material, PortalModifier> modifiers = new HashMap<Material, PortalModifier>();

    public final static void register(final PortalModifier modifier, final Material... materials) {
        for(final Material m: materials)
            modifiers.put(m, modifier);
    }

    public final static PortalModifier get(final Material material) {
        return modifiers.get(material);
    }

    public final static PortalModifier get(final ItemStack item) {
        return (item != null) ? modifiers.get(item.getType()) : null;
    }

    public final static PortalModifier get(final Block block) {
        return (block != null) ? modifiers.get(block.getType()) : null;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Modifier Logic
    ///////////////////////////////////////////////////////////////////////////

    public boolean hasPermission(final Player player, final ABPortal portal, final ModInfo info, final ItemStack item) {
        final String key = item.getType().name().toLowerCase();
        Permission perm = portal.getPlugin().getServer().getPluginManager().getPermission("abyss.modifiers." + key);
        if ( perm == null ) {
            portal.getPlugin().getLogger().info("No permission: abyss.modifiers." + key);
            return true;
        }

        return player.hasPermission(perm);
    }

    public boolean onApply(final Player player, final ABPortal portal, final ModInfo info, final ItemStack item) {
        return true;
    }

    public boolean onRemove(final Player player, final ABPortal portal, final ModInfo info, final ItemStack item) {
        return true;
    }

    public boolean preTeleport(final ABPortal from, final ABPortal portal, final ModInfo info, final Entity entity, final Location destination, final Vector velocity) {
        return true;
    }

    public void postTeleport(final ABPortal from, final ABPortal portal, final ModInfo info, final Entity entity) {}

    ///////////////////////////////////////////////////////////////////////////
    // String Building Stuff
    ///////////////////////////////////////////////////////////////////////////

    protected static ColorBuilder t() { return new ColorBuilder(); }
    protected static ColorBuilder t(final String string) { return new ColorBuilder(string); }

}

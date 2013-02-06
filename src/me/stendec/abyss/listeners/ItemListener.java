package me.stendec.abyss.listeners;

import me.stendec.abyss.ABPortal;
import me.stendec.abyss.AbyssPlugin;
import me.stendec.abyss.FrameInfo;
import me.stendec.abyss.util.ColorBuilder;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dye;
import org.bukkit.material.Wool;

public class ItemListener implements Listener {

    private final AbyssPlugin plugin;

    public ItemListener(AbyssPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private static ColorBuilder t() {
        return new ColorBuilder();
    }

    private static ColorBuilder t(final String string) {
        return new ColorBuilder(string);
    }


    @EventHandler(priority =  EventPriority.LOW)
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        Hanging entity = event.getEntity();

        // If there's no portal, we don't care.
        final ABPortal portal = plugin.getManager().getByMetadata(event.getEntity());
        if (portal == null)
            return;

        // It is our event.
        event.setCancelled(true);

        // If the remover isn't a player, we don't care.
        Entity remover = event.getRemover();
        if (!(remover instanceof Player))
            return;

        Player player = (Player) remover;
        FrameInfo info = portal.frameIDs.get(entity.getUniqueId());
        ItemFrame frame = (ItemFrame) entity;

        if (!portal.canManipulate(player)) {
            t().red("Access Denied").send(player);
            return;
        }

        final ItemStack i = player.getItemInHand();
        final Material m = i.getType();
        DyeColor color = null;
        if ( m == Material.WOOL )
            color = ((Wool) i.getData()).getColor();
        else if ( m == Material.INK_SACK )
            color = ((Dye) i.getData()).getColor();


        try {
            if ( info.type == FrameInfo.Frame.NETWORK) {
                if ( portal.network.equals(plugin.defaultNetwork) ) {
                    // Portal Destruction! See if they've already clicked once.
                    final World w = portal.getCenter().getWorld();
                    if ( plugin.portalDestroyTime.containsKey(portal.uid) ) {
                        final long delta = w.getFullTime() - plugin.portalDestroyTime.get(portal.uid);

                        // If it's been less than a half-second, abort. This prevents accidental re-clicks.
                        if ( delta < 10L )
                            return;

                        // If it's been more than fifteen seconds, reset the timer.
                        if ( delta < 100L ) {
                            // Destroy the portal.
                            plugin.portalDestroyTime.remove(portal.uid);
                            ColorBuilder p = t().gold("Portal [").yellow(portal.getName()).gold("]");
                            if (!plugin.getManager().destroy(portal))
                                p.red(" could not be deleted.").send(player);
                            else
                                p.darkgreen(" was deleted successfully.").send(player);
                            return;
                        }
                    }

                    plugin.portalDestroyTime.put(portal.uid, w.getFullTime());
                    t().red().bold("Warning!! ").white("Clicking this frame again will ").bold("destroy").
                        white(" this portal.").send(player);

                }

                portal.setNetwork(plugin.defaultNetwork);

            } else if ( info.type == FrameInfo.Frame.COLOR) {
                if ( color == null )
                    portal.modColor(1);
                else
                    portal.setColor(color);

            } else if (info.type == FrameInfo.Frame.MOD) {
                ItemStack n = portal.removeMod(player, frame);
                if ( n.getType() == Material.AIR )
                    return;

                // Don't drop the item if the player is in creative mode.
                if ( player.getGameMode() == GameMode.CREATIVE )
                    return;

                frame.getWorld().dropItemNaturally(frame.getLocation(), n);

            } else if (info.type == FrameInfo.Frame.ID1) {
                if ( color == null )
                    portal.modID(1, true);
                else
                    portal.setPartialID(0, true);

            } else if (info.type == FrameInfo.Frame.ID2) {
                if ( color == null )
                    portal.modID(1, false);
                else
                    portal.setPartialID(0, false);

            } else if (info.type == FrameInfo.Frame.DEST1) {
                if ( color == null )
                    portal.modDestination(1, true);
                else
                    portal.setPartialDestination(0, true);

            } else if (info.type == FrameInfo.Frame.DEST2) {
                if ( color == null )
                    portal.modDestination(1, false);
                else
                    portal.setPartialDestination(0, false);
            }
        } catch(IllegalArgumentException ex) {
            t().red("Error Configuring Portal").lf().
                gray("    ").append(ex.getMessage()).send(player);
        }
    }

    @EventHandler
    public void onHangingBreak(HangingBreakEvent event) {
        // If there's no portal, we don't care.
        final ABPortal portal = plugin.getManager().getByMetadata(event.getEntity());
        if (portal == null)
            return;

        // Cancel it so it doesn't break.
        event.setCancelled(true);
    }

}

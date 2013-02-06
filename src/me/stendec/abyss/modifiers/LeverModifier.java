package me.stendec.abyss.modifiers;

import me.stendec.abyss.ABPortal;
import me.stendec.abyss.ModInfo;
import me.stendec.abyss.PortalModifier;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.material.Lever;
import org.bukkit.scheduler.BukkitScheduler;

public class LeverModifier extends PortalModifier {

    private class Flipper implements Runnable {

        final ModInfo info;
        final Block block;
        final boolean powered;

        Flipper(final ModInfo info, final Block block, final boolean powered) {
            this.info = info;
            this.block = block;
            this.powered = powered;
        }

        @Override
        public void run() {
            info.task = -1;

            // Make sure the block is still a lever.
            if (block.getType() != Material.LEVER)
                return;

            final BlockState bs = block.getState();
            final Lever lever = (Lever) bs.getData();
            if (lever.isPowered() == powered)
                return;

            lever.setPowered(powered);
            bs.setData(lever);
            bs.update();
        }
    }

    public void postTeleport(final ABPortal from, final ABPortal portal, final ModInfo info, final Entity entity) {
        final Block block = info.location.getBlock();
        if (block.getType() != Material.LEVER)
            return;

        final ABPortal owner = info.getPortal();
        if ( owner == null )
            return;

        final Material m = info.item.getType();
        final boolean player = entity instanceof Player;
        if ( owner.equals(portal) ) {
            // Entity Incoming
            if ((m == Material.STONE_PLATE && !player) || (m != Material.STONE_PLATE && m != Material.WOOD_PLATE))
                return;
        } else {
            // Entity Outgoing
            if ((m == Material.STONE_BUTTON && !player) || (m != Material.STONE_BUTTON && m != Material.WOOD_BUTTON))
                return;
        }

        final long duration = parseDuration(info.flags.get("duration"));
        final BukkitScheduler scheduler = portal.getPlugin().getServer().getScheduler();

        final BlockState bs = block.getState();
        final Lever lever = (Lever) bs.getData();
        final boolean powered = lever.isPowered();

        final Flipper flipper;
        if (info.task != -1) {
            flipper = new Flipper(info, block, !powered);
            scheduler.cancelTask(info.task);

        } else {
            flipper = new Flipper(info, block, powered);

            lever.setPowered(!powered);
            bs.setData(lever);
            bs.update();
        }

        info.task = scheduler.scheduleSyncDelayedTask(portal.getPlugin(), flipper, duration);
    }

    private static long parseDuration(final String duration) {
        if (duration == null || duration.length() == 0)
            return 20L;

        long out = -1;
        try {
            out = Long.parseLong(duration);
        } catch(NumberFormatException ex) {}

        if (out < 2)
            throw new IllegalArgumentException("duration must be at least 2 ticks");

        return out;
    }

}

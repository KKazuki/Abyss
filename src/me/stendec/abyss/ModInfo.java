package me.stendec.abyss;

import org.bukkit.Location;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;

import java.lang.ref.WeakReference;
import java.util.HashMap;

public class ModInfo {

    private WeakReference<ABPortal> portal;

    public ItemStack item;
    public Location location;
    public FrameInfo frame;
    public final HashMap<String, String> flags;
    public int task = -1;

    public ModInfo(final ABPortal portal) {
        setPortal(portal);
        flags = new HashMap<String, String>();
    }

    public void setPortal(final ABPortal portal) {
        this.portal = new WeakReference<ABPortal>(portal);
    }

    public ABPortal getPortal() {
        return portal.get();
    }

    public void updateLocation() {
        ItemFrame f = frame.getFrame(true);
        location = f.getLocation().getBlock().getRelative(f.getAttachedFace()).getRelative(f.getAttachedFace()).getLocation();
    }

}

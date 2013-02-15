package me.stendec.abyss.managers;

import me.stendec.abyss.ABPortal;
import me.stendec.abyss.AbyssPlugin;
import me.stendec.abyss.PortalManager;
import me.stendec.abyss.util.SafeLocation;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class BasicManager extends PortalManager {

    private HashMap<UUID, HashSet<UUID>> worldPortals;

    public BasicManager(final AbyssPlugin instance) {
        super(instance);

        worldPortals = new HashMap<UUID, HashSet<UUID>>();
    }

    ///////////////////////////////////////////////////////////////////////////
    // World Management
    ///////////////////////////////////////////////////////////////////////////

    public void worldLoad(final World world) {
        // Do nothing.
    }

    public void worldUnload(final World world) {
        // Do nothing here too!
    }

    ///////////////////////////////////////////////////////////////////////////
    // Portal Management
    ///////////////////////////////////////////////////////////////////////////

    public boolean spatial_add(final ABPortal portal) {
        // If we've got a location, add it to the worldPortals.
        final SafeLocation location = portal.getLocation();
        if ( location == null )
            return false;

        final UUID wid = location.getWorldId();
        HashSet<UUID> portals = worldPortals.get(wid);
        if ( portals == null ) {
            portals = new HashSet<UUID>();
            worldPortals.put(wid, portals);
        }

        portals.add(portal.uid);

        return true;
    }


    public void spatial_update(final ABPortal portal) {
        final SafeLocation location = portal.getLocation();
        final UUID wid = ( location != null ) ? location.getWorldId() : null;
        final UUID pid = portal.uid;

        for(final UUID key: worldPortals.keySet()) {
            HashSet<UUID> portals = worldPortals.get(key);
            if ( key.equals(wid) ) {
                if ( portals == null ) {
                    portals = new HashSet<UUID>();
                    worldPortals.put(key, portals);
                    portals.add(pid);
                } else
                    portals.add(pid);

            } else if ( portals != null && portals.contains(pid) )
                portals.remove(pid);
        }

        if ( ! worldPortals.containsKey(wid) ) {
            HashSet<UUID> portals = new HashSet<UUID>();
            worldPortals.put(wid, portals);
            portals.add(pid);
        }

    }


    public boolean spatial_remove(final ABPortal portal) {
        final SafeLocation location = portal.getLocation();
        if ( location == null )
            return true;

        final HashSet<UUID> portals = worldPortals.get(location.getWorldId());
        if ( portals != null && portals.contains(portal.uid) )
            portals.remove(portal.uid);

        return true;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Portal Location
    ///////////////////////////////////////////////////////////////////////////

    public ABPortal getByRoot(final Location location) {
        if (location == null)
            return null;

        final HashSet<UUID> portals = worldPortals.get(location.getWorld().getUID());
        if ( portals == null )
            return null;

        for(final UUID uid: portals) {
            final ABPortal portal = allPortals.get(uid);
            if ( portal.getLocation().equals(location) )
                return portal;
        }

        return null;
    }

    public ABPortal getAt(final Location location) {
        if (location == null)
            return null;

        final HashSet<UUID> portals = worldPortals.get(location.getWorld().getUID());
        if ( portals == null )
            return null;

        final World world = location.getWorld();
        final int x = location.getBlockX(), y = location.getBlockY(), z = location.getBlockZ();

        for(final UUID uid: portals) {
            final ABPortal portal = allPortals.get(uid);
            if ( portal.isInPortal(world, x, y, z) )
                return portal;
        }

        return null;
    }

    public ABPortal getUnder(final Location location) {
        if (location == null)
            return null;

        final HashSet<UUID> portals = worldPortals.get(location.getWorld().getUID());
        if ( portals == null )
            return null;

        final World world = location.getWorld();
        final int x = location.getBlockX(), y = location.getBlockY(), z = location.getBlockZ();

        for(final UUID uid: portals) {
            final ABPortal portal = allPortals.get(uid);
            if ( portal.isOverPortal(world, x, y, z) )
                return portal;
        }

        return null;
    }

    public ArrayList<ABPortal> getNear(final Location location) {
        if (location == null)
            return null;

        final HashSet<UUID> portals = worldPortals.get(location.getWorld().getUID());
        final ArrayList<ABPortal> out = new ArrayList<ABPortal>();
        if ( portals == null || portals.size() == 0 )
            return out;

        final World world = location.getWorld();
        final int x = location.getBlockX(), y = location.getBlockY(), z = location.getBlockZ();

        for(final UUID uid: portals) {
            final ABPortal portal = allPortals.get(uid);
            if ( portal.isNearPortal(world, x, y, z) )
                out.add(portal);
        }

        return out;
    }

    public ArrayList<ABPortal> getNear(final Location location, final double range) {
        if ( location == null )
            return null;

        final Location min = location.clone().subtract(range, range, range);
        final Location max = location.clone().add(range, range, range);

        return getWithin(min, max);
    }


    public ArrayList<ABPortal> getWithin(final Location min, final Location max) {

        if ( min == null || max == null )
            return null;

        if ( ! min.getWorld().equals(max.getWorld()) )
            return null;

        if ( min.equals(max) )
            return getNear(min);

        final HashSet<UUID> portals = worldPortals.get(min.getWorld().getUID());
        final ArrayList<ABPortal> out = new ArrayList<ABPortal>();
        if ( portals == null || portals.size() == 0 )
            return out;

        final double min_x = min.getX();
        final double max_x = max.getX();
        final double min_y = min.getY();
        final double max_y = max.getY();
        final double min_z = min.getZ();
        final double max_z = max.getZ();

        for(final UUID uid: portals) {
            final ABPortal portal = allPortals.get(uid);
            final SafeLocation pmin = portal.getMinimumLocation();
            final SafeLocation pmax = portal.getMaximumLocation();

            final double pmin_x = pmin.getX();
            final double pmax_x = pmax.getX();
            final double pmin_y = pmin.getY();
            final double pmax_y = pmax.getY();
            final double pmin_z = pmin.getZ();
            final double pmax_z = pmax.getZ();

            // This is Madness.
            if (( max_x >= pmin_x && min_x <= pmax_x ) &&
                ( max_y >= pmin_y && min_y <= pmax_y ) &&
                ( max_z >= pmin_z && min_z <= pmax_z ))
                    out.add(portal);
        }

        return out;
    }

}

package me.stendec.abyss.managers;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.stendec.abyss.ABPortal;
import me.stendec.abyss.AbyssPlugin;
import me.stendec.abyss.PortalManager;
import me.stendec.abyss.util.ParseUtils;
import me.stendec.abyss.util.SafeLocation;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import static com.sk89q.worldguard.bukkit.BukkitUtil.toVector;

public class WorldGuardManager extends PortalManager {

    private final WorldGuardPlugin wg;
    private final HashMap<UUID, HashSet<UUID>> worldPortals;

    public WorldGuardManager(final AbyssPlugin instance, final WorldGuardPlugin wg) {
        super(instance);
        this.wg = wg;

        worldPortals = new HashMap<UUID, HashSet<UUID>>();
    }

    ///////////////////////////////////////////////////////////////////////////
    // World Management
    ///////////////////////////////////////////////////////////////////////////

    public void worldLoad(final World world) {
        final UUID id = world.getUID();
        if ( ! worldPortals.containsKey(id) )
            return;

        for(final UUID pid: worldPortals.get(id)) {
            final ABPortal portal = allPortals.get(pid);
            if ( portal != null )
                spatial_add(portal);
        }
    }


    public void worldUnload(final World world) {
        final UUID id = world.getUID();
        if ( ! worldPortals.containsKey(id) )
            return;

        for(final UUID pid: worldPortals.get(id)) {
            final ABPortal portal = allPortals.get(pid);
            if ( portal != null )
                spatial_remove(portal);
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    // Portal Management
    ///////////////////////////////////////////////////////////////////////////

    public boolean spatial_add(final ABPortal portal) {
        final SafeLocation location = portal.getLocation();
        if ( location == null )
            return false;

        final UUID id = location.getWorldId();
        HashSet<UUID> portals = worldPortals.get(id);
        if ( portals == null ) {
            portals = new HashSet<UUID>();
            worldPortals.put(id, portals);
        }

        portals.add(portal.uid);

        final World world = location.getWorld();
        if ( world == null )
            return true;

        final RegionManager rm = wg.getRegionManager(world);
        if ( rm == null )
            return true;

        final String key = "abyss-" + portal.uid.toString();
        final BlockVector min = portal.getMinimumBlockVector();
        final BlockVector max = portal.getMaximumBlockVector();

        ProtectedCuboidRegion region;

        // If the region already exists, just update it.
        if ( rm.hasRegion(key) ) {
            ProtectedRegion r = rm.getRegion(key);
            if ( ! (r instanceof ProtectedCuboidRegion) )
                return false;

            region = (ProtectedCuboidRegion) r;
            region.setMinimumPoint(min);
            region.setMaximumPoint(max);

            return true;
        }

        // If not, make it.
        region = new ProtectedCuboidRegion(key, min, max);
        rm.addRegion(region);

        return true;
    }

    public void spatial_update(final ABPortal portal) {
        // Add also updates, so just do it.
        spatial_add(portal);
    }

    public boolean spatial_remove(final ABPortal portal) {
        final SafeLocation location = portal.getLocation();
        if ( location == null )
            return true;

        final HashSet<UUID> portals = worldPortals.get(location.getWorldId());
        if ( portals != null && portals.contains(portal.uid) )
            portals.remove(portal.uid);

        final World world = location.getWorld();
        if ( world == null )
            return true;

        final RegionManager rm = wg.getRegionManager(world);
        if ( rm == null )
            return true;

        final String key = "abyss-" + portal.uid.toString();
        if ( rm.hasRegion(key) )
            rm.removeRegion(key);

        return true;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Portal Location
    ///////////////////////////////////////////////////////////////////////////

    public ABPortal getByRoot(final Location location) {
        if ( location == null )
            return null;

        final RegionManager rm = wg.getRegionManager(location.getWorld());
        if ( rm == null )
            return null;

        for(ProtectedRegion region: rm.getApplicableRegions(location)) {
            final String key = region.getId();
            if ( ! key.startsWith("abyss-") )
                continue;

            final UUID uid = ParseUtils.tryUUID(key.substring(6));
            if ( uid == null )
                continue;

            final ABPortal portal = allPortals.get(uid);
            if ( portal != null && portal.getLocation().equals(location) )
                return portal;
        }

        return null;
    }

    public ABPortal getAt(final Location location) {
        if ( location == null )
            return null;

        final RegionManager rm = wg.getRegionManager(location.getWorld());
        if ( rm == null )
            return null;

        final World world = location.getWorld();
        final int x = location.getBlockX(), y = location.getBlockY(), z = location.getBlockZ();

        for(ProtectedRegion region: rm.getApplicableRegions(location)) {
            final String key = region.getId();
            if ( ! key.startsWith("abyss-") )
                continue;

            final UUID uid = ParseUtils.tryUUID(key.substring(6));
            if ( uid == null )
                continue;

            final ABPortal portal = allPortals.get(uid);
            if ( portal != null && portal.isInPortal(world, x, y, z) )
                return portal;
        }

        return null;
    }

    public ABPortal getUnder(final Location location) {
        if ( location == null )
            return null;

        final RegionManager rm = wg.getRegionManager(location.getWorld());
        if ( rm == null )
            return null;

        final World world = location.getWorld();
        final int x = location.getBlockX(), y = location.getBlockY(), z = location.getBlockZ();

        Location l;

        for(ProtectedRegion region: rm.getApplicableRegions(location)) {
            final String key = region.getId();
            if ( ! key.startsWith("abyss-") )
                continue;

            final UUID uid = ParseUtils.tryUUID(key.substring(6));
            if ( uid == null )
                continue;

            final ABPortal portal = allPortals.get(uid);
            if ( portal != null && portal.isOverPortal(world, x, y, z) )
                return portal;
        }

        return null;
    }

    public ArrayList<ABPortal> getNear(final Location location) {
        if ( location == null )
            return null;

        final RegionManager rm = wg.getRegionManager(location.getWorld());
        if ( rm == null )
            return null;

        final ArrayList<ABPortal> out = new ArrayList<ABPortal>();

        for(final ProtectedRegion region: rm.getApplicableRegions(location)) {
            final String key = region.getId();
            if ( ! key.startsWith("abyss-") )
                continue;

            final UUID uid = ParseUtils.tryUUID(key.substring(6));
            if ( uid == null )
                continue;

            final ABPortal portal = allPortals.get(uid);
            if ( portal != null )
                out.add(portal);
        }

        return out;
    }

    public ArrayList<ABPortal> getNear(final Location location, final double range) {
        if ( location == null )
            return null;

        final BlockVector min = new BlockVector(toVector(location).subtract(range, range, range));
        final BlockVector max = new BlockVector(toVector(location).add(range, range, range));

        return getWithin(location.getWorld(), min, max);
    }

    public ArrayList<ABPortal> getWithin(final Location min, final Location max) {
        return getWithin(min.getWorld(), new BlockVector(toVector(min)), new BlockVector(toVector(max)));
    }

    public ArrayList<ABPortal> getWithin(final World world, final BlockVector min, final BlockVector max) {
        final RegionManager rm = wg.getRegionManager(world);
        if ( rm == null )
            return null;

        ProtectedCuboidRegion region = new ProtectedCuboidRegion(null, min, max);

        final ArrayList<ABPortal> out = new ArrayList<ABPortal>();
        for (final ProtectedRegion r: rm.getApplicableRegions(region)) {
            final String key = r.getId();
            if ( !key.startsWith("abyss-") )
                continue;

            final UUID uid = ParseUtils.tryUUID(key.substring(6));
            if ( uid == null )
                continue;

            final ABPortal portal = allPortals.get(uid);
            if ( portal != null )
                out.add(portal);
        }

        return out;
    }

}

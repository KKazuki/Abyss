package me.stendec.abyss;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public abstract class PortalManager {

    protected final AbyssPlugin plugin;

    // Portal Storage
    protected HashMap<UUID, ABPortal> allPortals;
    protected HashMap<String, UUID> portalNames;

    protected HashMap<ItemStack, HashMap<DyeColor, ArrayList<UUID>>> networkPortals;
    protected HashMap<String, HashMap<DyeColor, ArrayList<UUID>>> playerPortals;

    protected HashMap<ItemStack, HashMap<DyeColor, HashMap<Short, UUID>>> networkPortalIds;
    protected HashMap<String, HashMap<DyeColor, HashMap<Short, UUID>>> playerPortalIds;

    protected HashMap<UUID, UUID> portalFrames;

    ///////////////////////////////////////////////////////////////////////////
    // Basic Initialization and Logic
    ///////////////////////////////////////////////////////////////////////////

    public PortalManager(final AbyssPlugin plugin) {
        this.plugin = plugin;

        // Initialize Generic Storage Structures
        allPortals = new HashMap<UUID, ABPortal>();
        portalNames = new HashMap<String, UUID>();
        portalFrames = new HashMap<UUID, UUID>();

        networkPortals = new HashMap<ItemStack, HashMap<DyeColor, ArrayList<UUID>>>();
        playerPortals = new HashMap<String, HashMap<DyeColor, ArrayList<UUID>>>();

        networkPortalIds = new HashMap<ItemStack, HashMap<DyeColor, HashMap<Short, UUID>>>();
        playerPortalIds = new HashMap<String, HashMap<DyeColor, HashMap<Short, UUID>>>();
    }


    public AbyssPlugin getPlugin() {
        return plugin;
    }


    public final ABPortal getById(final UUID id) {
        return allPortals.get(id);
    }

    public final ABPortal getByName(final String name) {
        final UUID uid = portalNames.get(name.toLowerCase());
        if ( uid == null )
            return null;

        return allPortals.get(uid);
    }

    public final ABPortal getByFrame(final Entity frame) {
        final UUID uid = portalFrames.get(frame.getUniqueId());
        if ( uid == null )
            return null;

        return allPortals.get(uid);
    }


    public boolean destroy(final ABPortal portal) {
        // See if we can remove the portal.
        if (!remove(portal))
            return false;

        // Now, make it self destruct.
        portal.destroyEntities(false);
        return true;
    }


    public final Set<Map.Entry<UUID, ABPortal>> entrySet() {
        return allPortals.entrySet();
    }

    public final Iterator<ABPortal> iterator() {
        return new PortalIterator(this);
    }

    public final void clear() {
        for(final ABPortal portal: allPortals.values()) {
            spatial_remove(portal);
        }

        allPortals.clear();
        portalNames.clear();
        portalFrames.clear();

        networkPortals.clear();
        networkPortalIds.clear();

        playerPortals.clear();
        playerPortalIds.clear();
    }

    public final int size() {
        return allPortals.size();
    }


    ///////////////////////////////////////////////////////////////////////////
    // Portal Network Access
    ///////////////////////////////////////////////////////////////////////////

    public final ABPortal getByNetworkId(final ItemStack key, final DyeColor color, final String owner, final short id) {
        final HashMap<Short, UUID> portals = getNetworkIds(key, color, owner);
        final UUID uid = portals.get(id);
        return ( uid == null ) ? null : getById(uid);
    }

    public final ArrayList<ABPortal> getNetworkForDestination(final ABPortal portal) {
        return getNetworkForDestination(portal, true);
    }

    public final ArrayList<ABPortal> getNetworkForDestination(final ABPortal portal, final boolean filter_invalid) {
        final ArrayList<UUID> network = getNetwork(portal.network, portal.color, portal.owner);
        if ( network == null )
            return null;

        final ArrayList<ABPortal> out = new ArrayList<ABPortal>();

        final int size = network.size();
        final int pind = network.indexOf(portal.uid);

        if ( size == 0 )
            return out;

        // Crazy For Loops!
        for(int index = (pind + 1) % size; index != pind; index = (index + 1) % size) {
            final ABPortal p = allPortals.get(network.get(index));
            if ( p != null && (!filter_invalid || p.valid) )
                out.add(p);
        }

        return out;
    }


    public final ArrayList<UUID> getNetwork(final ItemStack key, final DyeColor color, final String owner) {
        final boolean human = key.getType() == Material.SKULL_ITEM && key.getDurability() == 3;

        HashMap<DyeColor, ArrayList<UUID>> network;
        if (human) network = playerPortals.get(owner);
        else network = networkPortals.get(key);

        if ( network == null ) {
            network = new HashMap<DyeColor, ArrayList<UUID>>();
            if (human) playerPortals.put(owner, network);
            else networkPortals.put(key, network);
        }

        ArrayList<UUID> portals = network.get(color);
        if ( portals == null ) {
            portals = new ArrayList<UUID>();
            network.put(color, portals);
        }

        return portals;
    }


    public final HashMap<Short, UUID> getNetworkIds(final ItemStack key, final DyeColor color, final String owner) {
        final boolean human = key.getType() == Material.SKULL_ITEM && key.getDurability() == 3;

        HashMap<DyeColor, HashMap<Short, UUID>> network;
        if (human) network = playerPortalIds.get(owner);
        else network = networkPortalIds.get(key);

        if ( network == null ) {
            network = new HashMap<DyeColor, HashMap<Short, UUID>>();
            if (human) playerPortalIds.put(owner, network);
            else networkPortalIds.put(key, network);
        }

        HashMap<Short, UUID> portals = network.get(color);
        if ( portals == null ) {
            portals = new HashMap<Short, UUID>();
            network.put(color, portals);
        }

        return portals;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Network Manipulation
    ///////////////////////////////////////////////////////////////////////////

    public final boolean addToNetwork(final ABPortal portal) {
        final ArrayList<UUID> portals = getNetwork(portal.network, portal.color, portal.owner);
        if ( portals.contains(portal.uid) )
            return false;

        return portals.add(portal.uid);
    }

    public final boolean addToNetwork(final ABPortal portal, final int index) {
        final ArrayList<UUID> portals = getNetwork(portal.network, portal.color, portal.owner);
        if ( portals.contains(portal.uid) )
            return false;

        portals.add(index, portal.uid);
        return portals.contains(portal.uid);
    }


    public final boolean addToNetworkIds(final ABPortal portal) {
        final HashMap<Short, UUID> portals = getNetworkIds(portal.network, portal.color, portal.owner);
        if ( portals.containsKey(portal.id) )
            return false;

        portals.put(portal.id, portal.uid);
        return true;
    }


    public final int removeFromNetwork(final ABPortal portal) {
        final ArrayList<UUID> portals = getNetwork(portal.network, portal.color, portal.owner);
        final int index = portals.indexOf(portal.uid);
        if ( index != -1 )
            portals.remove(index);

        return index;
    }


    public final boolean removeFromNetworkIds(final ABPortal portal) {
        final HashMap<Short, UUID> portals = getNetworkIds(portal.network, portal.color, portal.owner);
        if (!portals.containsKey(portal.id))
            return false;

        portals.remove(portal.id);
        return true;
    }


    public final void addFrames(final ABPortal portal) {
        if ( ! allPortals.containsKey(portal.uid) )
            return;

        for(final UUID frame: portal.frameIDs.keySet())
            if ( ! portalFrames.containsKey(frame) )
                portalFrames.put(frame, portal.uid);
    }

    public final void addFrame(final ABPortal portal, final ItemFrame frame) {
        if ( ! allPortals.containsKey(portal.uid) )
            return;

        final UUID uid = frame.getUniqueId();
        if ( ! portalFrames.containsKey(uid) )
            portalFrames.put(uid, portal.uid);
    }

    public final void removeFrames(final ABPortal portal) {
        for(final UUID frame: portal.frameIDs.keySet())
            if ( portalFrames.containsKey(frame) )
                portalFrames.remove(frame);
    }

    public final void removeFrame(final ItemFrame frame) {
        final UUID uid = frame.getUniqueId();
        if ( portalFrames.containsKey(uid) )
            portalFrames.remove(uid);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Macro Portal Management
    ///////////////////////////////////////////////////////////////////////////

    public boolean add(final ABPortal portal) {
        if ( portal == null )
            return false;

        final UUID uid = portal.uid;

        // If it's already in allPortals, we can't add it again.
        if ( allPortals.containsKey(uid) )
            return false;

        // Try to add it to the spatial tracker.
        if (!spatial_add(portal))
            return false;

        // Since that succeeded, add it to allPortals and the networks.
        allPortals.put(uid, portal);
        portalNames.put(portal.getName().toLowerCase(), uid);
        addToNetwork(portal);
        addToNetworkIds(portal);
        addFrames(portal);

        return true;
    }


    public void update(final ABPortal portal) {
        if ( portal == null || !allPortals.containsKey(portal.uid) )
            return;

        spatial_update(portal);
    }


    public void updateName(final ABPortal portal, String oldName) {
        oldName = oldName.toLowerCase();
        if ( portalNames.containsKey(oldName) )
            portalNames.remove(oldName);

        portalNames.put(portal.getName().toLowerCase(), portal.uid);
    }


    public boolean remove(final ABPortal portal) {
        // If we don't have the portal, return true.
        if ( portal == null || !allPortals.containsKey(portal.uid) )
            return true;

        // See if we can remove it, spatially.
        if (!spatial_remove(portal))
            return false;

        // Remove it from allPortals and the networks.
        allPortals.remove(portal.uid);
        portalNames.remove(portal.getName().toLowerCase());
        removeFromNetwork(portal);
        removeFromNetworkIds(portal);

        return true;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Spatial Portal Management
    ///////////////////////////////////////////////////////////////////////////

    protected abstract boolean spatial_add(final ABPortal portal);
    protected abstract void spatial_update(final ABPortal portal);
    protected abstract boolean spatial_remove(final ABPortal portal);

    ///////////////////////////////////////////////////////////////////////////
    // Abstract Portal Location
    ///////////////////////////////////////////////////////////////////////////

    public ABPortal getByRoot(final Block block) {
        return (block != null) ? getByRoot(block.getLocation()) : null;
    }

    public ABPortal getAt(final Block block) {
        return (block != null) ? getAt(block.getLocation()) : null;
    }

    public ABPortal getUnder(final Block block) {
        return (block != null) ? getUnder(block.getLocation()) : null;
    }

    public ArrayList<ABPortal> getNear(final Block block) {
        return (block != null) ? getNear(block.getLocation()) : null;
    }

    public abstract ABPortal getByRoot(final Location location);
    public abstract ABPortal getAt(final Location location);
    public abstract ABPortal getUnder(final Location location);
    public abstract ArrayList<ABPortal> getNear(final Location location);
    public abstract ArrayList<ABPortal> getWithin(final Location minimum, final Location maximum);

    ///////////////////////////////////////////////////////////////////////////
    // Iterator
    ///////////////////////////////////////////////////////////////////////////

    public class PortalIterator implements Iterator<ABPortal> {

        private final PortalManager manager;
        private final ABPortal[] portals;
        private int index;
        private ABPortal last;

        public PortalIterator(final PortalManager manager) {
            this.manager = manager;

            Collection<ABPortal> p = manager.allPortals.values();
            portals = p.toArray(new ABPortal[p.size()]);

            index = -1;
            last = null;
        }

        public boolean hasNext() {
            return (index + 1) < portals.length;
        }

        public ABPortal next() {
            index++;
            last = portals[index];
            return last;
        }

        public void remove() {
            if ( last == null )
                throw new IllegalStateException();

            manager.remove(last);
            last = null;
        }
    }

}

package me.stendec.abyss.util;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.util.Vector;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SafeLocation implements ConfigurationSerializable {

    private UUID worldId;
    private double x, y, z;
    private float yaw, pitch;

    private WeakReference<Location> location;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public SafeLocation(final UUID world, final double x, final double y, final double z) {
        this(world, x, y, z, 0, 0);
    }

    public SafeLocation(final UUID world, final double x, final double y, final double z, final float yaw, final float pitch) {
        this.worldId = world;
        this.x = x; this.y = y; this.z = z;
        this.yaw = yaw; this.pitch = pitch;
    }

    public SafeLocation(final World world, final double x, final double y, final double z) {
        this(world.getUID(), x, y, z, 0, 0);
    }

    public SafeLocation(final Location location) {
        this.worldId = location.getWorld().getUID();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();

        this.location = new WeakReference<Location>(location);
    }

    public SafeLocation(final Block block) {
        this.worldId = block.getWorld().getUID();
        this.x = block.getX();
        this.y = block.getY();
        this.z = block.getZ();

        this.yaw = 0; this.pitch = 0;
    }

    public SafeLocation(final SafeLocation location) {
        this.worldId = location.worldId;
        this.x = location.x;
        this.y = location.y;
        this.z = location.z;
        this.yaw = location.yaw;
        this.pitch = location.pitch;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Serialization
    ///////////////////////////////////////////////////////////////////////////

    public static SafeLocation valueOf(Map<String, Object> value) {
        final UUID world = UUID.fromString((String) value.get("world"));
        final double x = (Double) value.get("x");
        final double y = (Double) value.get("y");
        final double z = (Double) value.get("z");

        final float yaw, pitch;
        if ( value.containsKey("yaw") )
             yaw = Float.parseFloat((String) value.get("yaw"));
        else yaw = 0f;

        if ( value.containsKey("pitch") )
             pitch = Float.parseFloat((String) value.get("pitch"));
        else pitch = 0f;

        return new SafeLocation(world, x, y, z, yaw, pitch);
    }


    public Map<String, Object> serialize() {
        Map<String, Object> out = new HashMap<String, Object>();

        out.put("world", worldId.toString());
        out.put("x", x);
        out.put("y", y);
        out.put("z", z);

        if ( yaw != 0 )
            out.put("yaw", String.valueOf(yaw));

        if ( pitch != 0 )
            out.put("pitch", String.valueOf(pitch));

        return out;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Comparison
    ///////////////////////////////////////////////////////////////////////////

    public boolean equals(Object o) {
        if (!(o instanceof SafeLocation))
            return false;

        SafeLocation i = (SafeLocation) o;
        return i.x == x && i.y == y && i.z == z && i.yaw == yaw && i.pitch == pitch && worldId.equals(i.worldId);
    }


    public boolean equals(final Location o) {
        final World world = o.getWorld();
        return world != null && world.getUID().equals(worldId) && o.getX() == x && o.getY() == y && o.getZ() == z && o.getPitch() == pitch && o.getYaw() == yaw;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Getters and Setters
    ///////////////////////////////////////////////////////////////////////////
    
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }

    public int getBlockX() { return (int) x; }
    public int getBlockY() { return (int) y; }
    public int getBlockZ() { return (int) z; }
    
    public void setX(final double value) {
        location = null;
        x = value;
    }

    public void setY(final double value) {
        location = null;
        y = value;
    }

    public void setZ(final double value) {
        location = null;
        z = value;
    }

    public void setYaw(final float value) {
        location = null;
        yaw = value;
    }

    public void setPitch(final float value) {
        location = null;
        pitch = value;
    }

    ///////////////////////////////////////////////////////////////////////////
    // World Properties
    ///////////////////////////////////////////////////////////////////////////

    public Chunk getChunk() {
        final World world = Bukkit.getWorld(worldId);
        return ( world != null ) ? world.getChunkAt((int) x, (int) z) : null;
    }

    public World getWorld() { return Bukkit.getWorld(worldId); }
    public UUID getWorldId() { return worldId; }

    public void setWorld(final UUID value) {
        location = null; worldId = value;
    }

    public void setWorld(final World value) {
        location = null; worldId = value.getUID();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Location Getter
    ///////////////////////////////////////////////////////////////////////////

    public Location getLocation() {
        Location loc = (location != null) ? location.get() : null;
        if ( loc == null ) {
            final World world = getWorld();
            if ( world != null ) {
                loc = new Location(world, x, y, z, yaw, pitch);
                location = new WeakReference<Location>(loc);
            }
        }

        return loc;
    }

    public Block getBlock() {
        final World world = Bukkit.getWorld(worldId);
        return ( world != null ) ? world.getBlockAt((int) x, (int) y, (int) z) : null;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Convenience Methods
    ///////////////////////////////////////////////////////////////////////////

    public double distance(final Location loc) {
        return Math.sqrt(distanceSquared(loc.getWorld().getUID(), loc.getX(), loc.getY(), loc.getZ()));
    }

    public double distance(final SafeLocation loc) {
        return Math.sqrt(distanceSquared(loc.worldId, loc.x, loc.y, loc.z));
    }

    public double distanceSquared(final Location loc) {
        return distanceSquared(loc.getWorld().getUID(), loc.getX(), loc.getY(), loc.getZ());
    }

    public double distanceSquared(final SafeLocation loc) {
        return distanceSquared(loc.worldId, loc.x, loc.y, loc.z);
    }

    public double distanceSquared(final UUID world, final double x, final double y, final double z) {
        if ( world == null || world != worldId )
            throw new IllegalArgumentException("Cannot measure distance in different worlds.");

        return Math.pow(this.x - x, 2) + Math.pow(this.y - y, 2) + Math.pow(this.z - z, 2);
    }

    public SafeLocation clone() {
        return new SafeLocation(this);
    }

    public SafeLocation clone(final double x, final double y, final double z) {
        // Clone and Add
        return new SafeLocation(this.worldId, this.x + x, this.y + y, this.z + z, this.yaw, this.pitch);
    }

    public SafeLocation add(final SafeLocation value) {
        Validate.isTrue(worldId == value.worldId, "Cannot add locations in different worlds.");
        return add(value.x, value.y, value.z);
    }

    public SafeLocation add(final Location value) {
        Validate.isTrue(worldId == value.getWorld().getUID(), "Cannot add locations in different worlds.");
        return add(value.getX(), value.getY(), value.getZ());
    }

    public SafeLocation add(final Vector value) {
        return add(value.getX(), value.getY(), value.getZ());
    }

    public SafeLocation add(final double x, final double y, final double z) {
        location = null;
        this.x += x; this.y += y; this.z += z;
        return this;
    }

    public SafeLocation subtract(final double x, final double y, final double z) {
        location = null;
        this.x -= x; this.y -= y; this.z -= z;
        return this;
    }

    public SafeLocation subtract(final SafeLocation value) {
        Validate.isTrue(worldId == value.worldId, "Cannot subtract locations in different worlds.");
        return subtract(value.x, value.y, value.z);
    }

    public SafeLocation subtract(final Location value) {
        Validate.isTrue(worldId == value.getWorld().getUID(), "Cannot subtract locations in different worlds.");
        return subtract(value.getX(), value.getY(), value.getZ());
    }

    public SafeLocation subtract(final Vector value) {
        return subtract(value.getX(), value.getY(), value.getZ());
    }


}

package me.stendec.abyss;

import com.sk89q.worldedit.BlockVector;
import me.stendec.abyss.util.ColorBuilder;
import org.apache.commons.lang.Validate;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.logging.Level;

public class ABPortal implements Comparable<ABPortal> {

    private final AbyssPlugin plugin;

    public UUID uid;
    public boolean valid;

    public short effect;
    public short effectSound;

    // The Portal Owner.
    private String name;
    public String owner;

    // Network Information
    public ItemStack network;
    public DyeColor color;

    // ID Information
    public short id;

    // Destination Information
    public short destination;

    // Portal Location Information
    private Location minimum;
    private Location maximum;

    private Location center;
    private Location location;
    private Rotation rotation;
    public short depth;
    private int size;

    // Modifiers
    public double velocityMultiplier;
    public double rangeMultiplier;

    public boolean mod_invalid;
    public int eyeCount;

    public ArrayList<ModInfo> mods;

    // Entities
    public final HashMap<FrameInfo.Frame, FrameInfo> frames;
    public final HashMap<UUID, FrameInfo> frameIDs;

    public ABPortal(final AbyssPlugin instance) {
        this(instance, UUID.randomUUID(), instance.minimumSize);
    }

    public ABPortal(final AbyssPlugin instance, final int size) {
        this(instance, UUID.randomUUID(), size);
    }

    public ABPortal(final AbyssPlugin instance, final UUID uid, final int size) {
        plugin = instance;

        frames = new HashMap<FrameInfo.Frame, FrameInfo>();
        frameIDs = new HashMap<UUID, FrameInfo>();

        // Create a new UUID.
        this.uid = uid;

        effect = 0;
        effectSound = 10;

        // Defaults
        network = plugin.defaultNetwork.clone();
        color = plugin.defaultColor;

        id = 0;
        destination = 0;

        this.size = size;
        rotation = Rotation.NONE;
        valid = true;

        mod_invalid = false;

        velocityMultiplier = 1;
        eyeCount = 0;
        rangeMultiplier = plugin.rangeMultiplier;
    }

    public final AbyssPlugin getPlugin() {
        return plugin;
    }

    public int compareTo(ABPortal other) {
        return getName().compareTo(other.getName());
    }

    // Loading and Saving

    public void load(final ConfigurationSection config) {
        owner = config.getString("owner", "Notch");
        name = config.getString("name");
        valid = config.getBoolean("valid", valid);

        network = config.getItemStack("network", network);
        color = DyeColor.valueOf(config.getString("color", color.name()));

        id = (short) config.getInt("id", id);
        destination = (short) config.getInt("destination", destination);

        depth = (short) config.getInt("depth", plugin.minimumDepth);
        size = config.getInt("size", size);

        effect = (short) config.getInt("effect", effect);
        effectSound = (short) config.getInt("effectSound", effectSound);

        velocityMultiplier = config.getDouble("velocityMultiplier", velocityMultiplier);
        rangeMultiplier = config.getDouble("rangeMultiplier", rangeMultiplier);

        try {
            rotation = Rotation.valueOf(config.getString("rotation", rotation.toString()));
        } catch(IllegalArgumentException ex) { rotation = Rotation.NONE; }

        location = loadLocation(config.getConfigurationSection("location"));
        center = location.clone().add(size / 2, 0, size / 2);
        minimum = location.clone().subtract(1, depth, 1);
        maximum = location.clone().add(size, 2, size);

        // Load all the FrameInfo objects.
        World world = location.getWorld();

        if (config.isConfigurationSection("frames")) {
            ConfigurationSection f = config.getConfigurationSection("frames");
            for(String key: f.getKeys(false)) {
                FrameInfo info = (FrameInfo) f.get(key);
                info.type = FrameInfo.Frame.valueOf(key);
                info.world = world;
                frames.put(info.type, info);
                frameIDs.put(info.id, info);
            }
        }

        mod_invalid = config.getBoolean("mod_invalid", mod_invalid);
        eyeCount = config.getInt("eye-count", eyeCount);

        // Handle the mod items.
        if (config.isConfigurationSection("mods")) {
            ConfigurationSection f = config.getConfigurationSection("mods");
            Set<String> keys = f.getKeys(false);

            if (mods == null)
                mods = new ArrayList<ModInfo>(keys.size());

            for(final String key: keys) {
                if (!f.isConfigurationSection(key))
                    continue;

                final ConfigurationSection ic = f.getConfigurationSection(key);
                final ModInfo info = new ModInfo(this);

                info.frame = (FrameInfo) ic.get("frame", null);
                if ( info.frame == null ) {
                    plugin.getLogger().warning("Error reading frame for: " + uid.toString() + "/" + key);
                } else {
                    info.frame.type = FrameInfo.Frame.MOD;
                    info.frame.world = world;
                }

                info.item = ic.getItemStack("item", null);
                if (ic.isConfigurationSection("location"))
                    info.location = loadLocation(ic.getConfigurationSection("location"));

                final List<String> flags = ic.getStringList("flags");
                if (flags != null)
                    for(final String value: flags) {
                        String[] pair;
                        if (!value.contains(":"))
                            pair = new String[]{value.trim(), ""};
                        else
                            pair = value.trim().split("\\s*:\\s*", 2);
                        info.flags.put(pair[0], pair[1]);
                    }

                mods.add(info);
                if ( info.frame != null )
                    frameIDs.put(info.frame.id, info.frame);
            }
        }
    }

    public static ABPortal fromConfig(final AbyssPlugin instance, final UUID uid, final ConfigurationSection config) {
        final ABPortal portal = new ABPortal(instance, uid, instance.minimumSize);
        portal.load(config);
        return portal;
    }

    public void save(ConfigurationSection config) {
        config.set("owner", owner);

        if ( name != null )
            config.set("name", name);

        if (!valid)
            config.set("valid", valid);

        config.set("network", network);
        config.set("color", color.name());

        config.set("effect", effect);
        config.set("effectSound", effectSound);

        if (id != 0)
            config.set("id", id);

        if (destination != 0)
            config.set("destination", destination);

        if (velocityMultiplier != 1)
            config.set("velocityMultiplier", velocityMultiplier);

        if (rangeMultiplier != plugin.rangeMultiplier)
            config.set("rangeMultiplier", rangeMultiplier);

        if ( eyeCount != 0 )
            config.set("eye-count", eyeCount);

        config.set("depth", depth);
        config.set("size", size);

        saveLocation(config.createSection("location"), location);

        if (rotation != Rotation.NONE)
            config.set("rotation", rotation.toString());

        // Store Frames
        ConfigurationSection f = config.createSection("frames");
        for(FrameInfo info: frames.values())
            f.set(info.type.toString(), info);

        if (mod_invalid)
            config.set("mod_invalid", mod_invalid);

        // Store Mods
        if (mods != null && mods.size() > 0) {
            f = config.createSection("mods");
            for(int x=0; x<mods.size(); x++) {
                ModInfo info = mods.get(x);

                ConfigurationSection m = f.createSection(Integer.toString(x));
                m.set("frame", info.frame);
                m.set("item", info.item);
                if (info.location != null)
                    saveLocation(m.createSection("location"), info.location);

                if(info.flags.size() > 0) {
                    ArrayList<String> flags = new ArrayList<String>(info.flags.size());
                    for(String key: info.flags.keySet())
                        flags.add(String.format("%s: %s", key, info.flags.get(key)));
                    m.set("flags", flags);
                }
            }
        }
    }

    // Permissions

    public boolean canManipulate(Player player) {
        return (player.isOp() || player.getName().equals(owner));
    }

    // Name

    public ColorBuilder getDisplayName() {
        return getDisplayName(false);
    }

    public ColorBuilder getDisplayName(final boolean bold) {
        String b = ( bold ) ? ChatColor.BOLD.toString() : "";
        return new ColorBuilder().gold(b).append("Portal [").yellow(getName()).gold(b).append("]");
    }

    public void setName(final String name) {
        Validate.notNull(name);

        if ( this.name != null && this.name.equals(name) )
            return;

        final String old_name = this.name;
        this.name = name;
        plugin.getManager().updateName(this, old_name);
    }


    public String getName() {
        if ( name != null )
            return name;

        if ( owner == null )
            throw new NullPointerException("cannot generate name without owner");

        final PortalManager manager = plugin.getManager();

        while(true) {
            final String lowner = owner.toLowerCase();
            name = String.format("%s-%d", lowner, plugin.getIdFor(lowner));
            if ( manager.getByName(name) == null )
                break;
        }

        return name;
    }


    // Configuration

    public void update() {
        boolean changed = false;
        final short new_depth = plugin.getDepthAt(location, size);
        final boolean new_valid = checkValid();

        if ( depth != new_depth ) {
            depth = new_depth;
            changed = true;
        }

        if ( valid != new_valid ) {
            valid = new_valid;
            changed = true;
        }

        // If we've changed, update with the portal manager.
        if ( changed )
            plugin.getManager().update(this);
    }

    private static boolean isIris(final Material mat) {
        switch(mat) {
            case AIR:
            case WALL_SIGN:
            case WOOD_PLATE:
            case STONE_PLATE:
            case STATIONARY_WATER:
            case WATER:
            case LAVA:
            case STATIONARY_LAVA:
            case LADDER:
            case WEB:
            case TORCH:
            case REDSTONE_TORCH_OFF:
            case REDSTONE_TORCH_ON:
            case FIRE:
            case REDSTONE_WIRE:
            case LEVER:
            case WOOD_BUTTON:
            case STONE_BUTTON:
            case PORTAL:
            case ENDER_PORTAL:
            case TRIPWIRE:
            case TRIPWIRE_HOOK:
            case SKULL:
                return false;
            default:
                return true;
        }
    }

    public boolean checkValid() {
        // See if this portal is a valid destination.
        if (mod_invalid)
            return false;

        // Make sure the two layers directly above the portal are air.
        for(double x=0; x<size; x++)
            for(double z=0; z<size; z++)
                if (isIris(location.clone().add(x,1,z).getBlock().getType()) ||
                        isIris(location.clone().add(x,2,z).getBlock().getType()))
                    return false;

        return true;
    }

    public ModInfo getModFromFrame(final ItemFrame frame) {
        if ( mods == null )
            return null;

        final UUID uid = frame.getUniqueId();
        for(final ModInfo info: mods) {
            if ( info.frame != null && uid.equals(info.frame.id) )
                return info;
        }

        return null;
    }

    public ItemStack removeMod(final Player player, final ItemFrame frame) {
        // Get the mod slot.
        final ModInfo info = getModFromFrame(frame);
        if (info == null || !mods.contains(info))
            return null;

        // Get the item.
        final ItemStack item = frame.getItem();

        // If the material is special, do stuff.
        final PortalModifier modifier = PortalModifier.get(item.getType());
        if (modifier != null && !modifier.onRemove(player, this, info, item))
            return null;

        // Clear the item.
        info.item = null;
        frame.setItem(null);

        // Clear the configuration.
        info.flags.clear();

        // And return the item.
        return item;
    }

    public boolean setMod(final Player player, final ItemFrame frame, final ItemStack item) {
        // Get the mod slot.
        final ModInfo info = getModFromFrame(frame);
        if (info == null || !mods.contains(info))
            return false;

        // If we've already got a modifier, remove it. If we can't, return false.
        if (info.item != null && removeMod(player, frame) == null)
            return false;

        // Get the modifier for this material and try to apply it.
        final PortalModifier modifier = PortalModifier.get(item.getType());
        if (modifier == null || !modifier.hasPermission(player, this, info, item) || !modifier.onApply(player, this, info, item))
            return false;

        // It worked. Set the item and return.
        info.item = item;
        frame.setItem(item);
        return true;
    }

    public void setNetwork(ItemStack item) {
        setNetwork(item, false);
    }

    public void setNetwork(ItemStack item, boolean allow_metadata) {
        if (item.getType() == Material.AIR || item.getAmount() == 0)
            throw new IllegalArgumentException("Network must be non-air and have a quantity of 1.");

        // First, copy our item.
        item = item.clone();

        // Now, remove any enchantments.
        for(Enchantment enchantment: item.getEnchantments().keySet())
            item.removeEnchantment(enchantment);

        // Make sure we've only got one.
        item.setAmount(1);

        // If metadata isn't allowed, clear it.
        if (!allow_metadata) {
            item.setItemMeta(null);
        }

        // Now, let's do this.
        final PortalManager manager = plugin.getManager();
        manager.removeFromNetwork(this);
        manager.removeFromNetworkIds(this);

        this.network = item;
        if (!frames.isEmpty())
            frames.get(FrameInfo.Frame.NETWORK).getFrame(true).setItem(item);

        manager.addToNetwork(this);
        manager.addToNetworkIds(this);
    }

    public void setColor(DyeColor color) {
        if (this.color == color)
            return;

        final PortalManager manager = plugin.getManager();
        manager.removeFromNetwork(this);
        manager.removeFromNetworkIds(this);


        this.color = color;
        if (!frames.isEmpty())
            frames.get(FrameInfo.Frame.COLOR).getFrame(true).
                    setItem(new ItemStack(Material.WOOL, 1, color.getWoolData()));

        manager.addToNetwork(this);
        manager.addToNetworkIds(this);
    }

    public void setID(short id) {
        if (this.id == id)
            return;

        final PortalManager manager = plugin.getManager();
        manager.removeFromNetworkIds(this);

        this.id = id;
        if (!frames.isEmpty())
            setIDFrames(id, frames.get(FrameInfo.Frame.ID1).getFrame(true),
                    frames.get(FrameInfo.Frame.ID2).getFrame(true));

        manager.addToNetworkIds(this);
    }

    public void setDestination(short dest) {
        if (this.destination == dest)
            return;

        this.destination = dest;
        if (!frames.isEmpty())
            setIDFrames(dest, frames.get(FrameInfo.Frame.DEST1).getFrame(true),
                    frames.get(FrameInfo.Frame.DEST2).getFrame(true));
    }

    public void modColor(int value) {
        setColor(DyeColor.getByWoolData((byte) (((color.getWoolData() + value) % 16 + 16) % 16)));
    }

    public void setPartialID(int value, boolean big) {
        if ( id < 0 || id > 288 )
            throw new IllegalArgumentException("Cannot modify id out of range.");

        int id1 = id / 17;
        int id2 = id % 17;

        if (big)
            id1 = (value % 17 + 17) % 17;
        else
            id2 = (value % 17 + 17) % 17;

        setID((short) ((id1 * 17) + id2));
    }

    public void modID(int value, boolean big) {
        if ( id < 0 || id > 288 )
            throw new IllegalArgumentException("Cannot modify id out of range.");

        int id1 = id / 17;
        int id2 = id % 17;

        if (big)
            id1 = ((id1 + value) % 17 + 17) % 17;
        else
            id2 = ((id2 + value) % 17 + 17) % 17;

        setID((short) ((id1 * 17) + id2));
    }

    public void modDestination(int value, boolean big) {
        if (destination < 0 || destination > 288 )
            throw new IllegalArgumentException("Cannot modify destination out of range.");

        int d1 = destination / 17;
        int d2 = destination % 17;

        if (big)
            d1 = ((d1 + value) % 17 + 17) % 17;
        else
            d2 = ((d2 + value) % 17 + 17) % 17;

        setDestination((short) ((d1 * 17) + d2));
    }

    public void setPartialDestination(int value, boolean big) {
        if (destination < 0 || destination > 288 )
            throw new IllegalArgumentException("Cannot modify destination out of range.");

        int d1 = destination / 17;
        int d2 = destination % 17;

        if (big)
            d1 = (value % 17 + 17) % 17;
        else
            d2 = (value % 17 + 17) % 17;

        setDestination((short) ((d1 * 17) + d2));
    }

    // Location Information

    public int getSize() {
        return size;
    }

    public Location getLocation() {
        return location.clone();
    }

    public Location getCenter() {
        return center.clone();
    }

    public boolean setLocation(Location loc) {
        return setLocation(loc, size);
    }

    public boolean setLocation(Location loc, int sz) {
        // Zero out the location.
        loc = loc.getBlock().getLocation();

        // Make sure we're actually moving.
        if ( location != null && location.equals(loc) && sz == size  )
            return true;

        // See if we can move before we do. This also finds the right block to
        // associate the portal with.
        loc = plugin.findRoot(loc, sz);
        if (loc == null || !plugin.validateLocation(loc, sz))
            return false;

        // Set our new depth.
        depth = plugin.getDepthAt(loc, sz);

        destroyEntities(true);

        location = loc;
        size = sz;
        center = location.clone().add(size / 2, 0, size / 2);
        minimum = location.clone().subtract(1, depth, 1);
        maximum = location.clone().add(size, 2, size);

        createEntities();

        return true;
    }

    public Rotation getRotation() {
        return rotation;
    }

    public void setRotation(Rotation rot) {
        if ( rotation.equals(rot) )
            return;

        boolean entities = !frames.isEmpty();

        if (entities)
            destroyEntities(true);

        rotation = rot;

        if (entities)
            createEntities();
    }

    // Entity Code

    public void destroyEntities(boolean temporary) {
        if (frames.isEmpty())
            return;

        // Iterate through all the ModInfo instances.
        if (mods != null && mods.size() > 0)
            for(ModInfo info: mods) {
                if (info.frame == null)
                    continue;

                ItemFrame frame = info.frame.getFrame(true);
                if (frame != null) {
                    if (info.item != null && info.item.getType() != Material.AIR && !temporary) {
                        frame.getWorld().dropItemNaturally(frame.getLocation(), frame.getItem());
                        frame.setItem(null);
                        info.item = null;
                    }
                    frame.remove();
                }

                info.frame = null;
            }

        // Iterate through all the FrameInfo instances.
        for(FrameInfo info: frames.values()) {
            ItemFrame frame = info.getFrame(true);

            if (frame != null)
                frame.remove();
        }

        // Remove from the manager.
        plugin.getManager().removeFrames(this);

        // Now, clear out the instances.
        frames.clear();
        frameIDs.clear();
    }

    public static ItemFrame spawnFrame(Location loc, BlockFace face) {
        // Get the block that we need to spawn the entity at.
        Block block = loc.getBlock();
        Block spawn = block.getRelative(face.getOppositeFace());

        BlockState north = null;
        BlockState south = null;
        BlockState east = null;
        BlockState west = null;

        // Make sure there's exactly 1 air block for the frame to spawn into.
        Block b = spawn.getRelative(BlockFace.NORTH);
        if (face != BlockFace.NORTH && b.getType() == Material.AIR) {
            north = b.getState();
            b.setType(Material.STONE);
        } else if (face == BlockFace.NORTH && b.getType() != Material.AIR) {
            north = b.getState();
            b.setType(Material.AIR);
        }

        b = spawn.getRelative(BlockFace.EAST);
        if (face != BlockFace.EAST && b.getType() == Material.AIR) {
            east = b.getState();
            b.setType(Material.STONE);
        } else if (face == BlockFace.EAST && b.getType() != Material.AIR) {
            east = b.getState();
            b.setType(Material.AIR);
        }

        b = spawn.getRelative(BlockFace.SOUTH);
        if (face != BlockFace.SOUTH && b.getType() == Material.AIR) {
            south = b.getState();
            b.setType(Material.STONE);
        } else if (face == BlockFace.SOUTH && b.getType() != Material.AIR) {
            south = b.getState();
            b.setType(Material.AIR);
        }

        b = spawn.getRelative(BlockFace.WEST);
        if (face != BlockFace.WEST && b.getType() == Material.AIR) {
            west = b.getState();
            b.setType(Material.STONE);
        } else if (face == BlockFace.WEST && b.getType() != Material.AIR) {
            west = b.getState();
            b.setType(Material.AIR);
        }

        // Now spawn the entity.
        ItemFrame entity = null;

        try {
            entity = spawn.getWorld().spawn(spawn.getLocation(), ItemFrame.class);
        } catch(IllegalArgumentException ex) {}
        finally {
            // Now replace the blocks we replaced.
            if (north != null) north.update(true);
            if (east != null) east.update(true);
            if (south != null) south.update(true);
            if (west != null) west.update(true);
        }

        return entity;
    }

    public ItemFrame setFrame(FrameInfo.Frame type, ItemFrame frame) {
        // If we've got an existing frame of that type, remove it.
        FrameInfo info = frames.get(type);
        if (info != null) {
            // Remove the existing frame if it's not this frame.
            ItemFrame old = info.getFrame(true);
            if (old.equals(frame))
                return frame;

            old.remove();
            frames.remove(type);
        }

        // Create the new info and store it.
        info = new FrameInfo(type, frame);

        frames.put(type, info);
        frameIDs.put(info.id, info);
        plugin.getManager().addFrame(this, frame);

        return frame;
    }

    public ItemFrame setFrame(FrameInfo.Frame type, Location loc, BlockFace face) {
        return setFrame(type, spawnFrame(loc, face));
    }

    public ItemFrame setModFrame(ModInfo info, ItemFrame frame) {
        FrameInfo f = new FrameInfo(FrameInfo.Frame.MOD, frame);

        // Store the new FrameInfo.
        info.frame = f;
        info.updateLocation();

        frameIDs.put(f.id, f);
        plugin.getManager().addFrame(this, frame);

        // Make sure the frame is showing the right item.
        frame.setItem(info.item);

        return frame;
    }

    public ItemFrame setModFrame(ModInfo info, Location loc, BlockFace face) {
        return setModFrame(info, spawnFrame(loc, face));
    }

    private Location l() {
        return location.clone(); }

    private Location l(final int x, final int y, final int z) {
        return location.clone().add(x, y, z); }

    public void createEntities() {
        // Don't do this if we've got existing frames.
        if (!frames.isEmpty())
            return;

        ItemFrame if_network;
        ItemFrame if_color;
        ItemFrame if_id1;
        ItemFrame if_id2;
        ItemFrame if_dest1;
        ItemFrame if_dest2;

        // Make sure we have mods.
        if (mods == null) {
            int mod_count = Math.min(size, plugin.maximumMods);
            mods = new ArrayList<ModInfo>(mod_count);
            for(int index=0; index < mod_count; index++) {
                mods.add(new ModInfo(this));
            }
        }

        // Get the center two spots of each wall.
        int half = (size / 2) - 1;
        int odd = size % 2;

        if ( rotation == Rotation.CLOCKWISE ) {
            if_network = setFrame(FrameInfo.Frame.NETWORK, l(size - 1, 0, half), BlockFace.WEST);
            if_color = setFrame(FrameInfo.Frame.COLOR, l(size - 1, 0, half + 1 + odd), BlockFace.WEST);

            if_id1 = setFrame(FrameInfo.Frame.ID1, l(size-2, 0, 0), BlockFace.SOUTH);
            if_id2 = setFrame(FrameInfo.Frame.ID2, l(size-1, 0, 0), BlockFace.SOUTH);

            if_dest1 = setFrame(FrameInfo.Frame.DEST1, l(size-1, 0, size-1), BlockFace.NORTH);
            if_dest2 = setFrame(FrameInfo.Frame.DEST2, l(size-2, 0, size-1), BlockFace.NORTH);

            // Iterate through the mods, making frames.
            Location ml = l(size - 1, -1, (size - mods.size()) / 2);
            for(final ModInfo info: mods) {
                setModFrame(info, ml, BlockFace.WEST);
                ml.add(0, 0, 1);
            }

        } else if ( rotation == Rotation.FLIPPED ) {
            if_network = setFrame(FrameInfo.Frame.NETWORK, l(half + 1 + odd, 0, size - 1), BlockFace.NORTH);
            if_color = setFrame(FrameInfo.Frame.COLOR, l(half, 0, size - 1), BlockFace.NORTH);

            if_id1 = setFrame(FrameInfo.Frame.ID1, l(size-1, 0, size-2), BlockFace.WEST);
            if_id2 = setFrame(FrameInfo.Frame.ID2, l(size-1, 0, size-1), BlockFace.WEST);

            if_dest1 = setFrame(FrameInfo.Frame.DEST1, l(0, 0, size-1), BlockFace.EAST);
            if_dest2 = setFrame(FrameInfo.Frame.DEST2, l(0, 0, size-2), BlockFace.EAST);

            // Iterate through the mods, making frames.
            Location ml = l(size - (1 + ((size - mods.size()) / 2)), -1, size-1);
            for(final ModInfo info: mods) {
                setModFrame(info, ml, BlockFace.NORTH);
                ml.add(-1, 0, 0);
            }

        } else if ( rotation == Rotation.COUNTER_CLOCKWISE ) {
            if_network = setFrame(FrameInfo.Frame.NETWORK, l(0, 0, half+1+odd), BlockFace.EAST);
            if_color = setFrame(FrameInfo.Frame.COLOR, l(0, 0, half), BlockFace.EAST);

            if_id1 = setFrame(FrameInfo.Frame.ID1, l(1, 0, size-1), BlockFace.NORTH);
            if_id2 = setFrame(FrameInfo.Frame.ID2, l(0, 0, size-1), BlockFace.NORTH);

            if_dest1 = setFrame(FrameInfo.Frame.DEST1, l(), BlockFace.SOUTH);
            if_dest2 = setFrame(FrameInfo.Frame.DEST2, l(1,0,0), BlockFace.SOUTH);

            // Iterate through the mods, making frames.
            Location ml = location.clone().add(0, -1, size - (1 + ((size - mods.size()) / 2)));
            for(ModInfo info: mods) {
                setModFrame(info, ml, BlockFace.EAST);
                ml.add(0, 0, -1);
            }

        } else {
            if_network = setFrame(FrameInfo.Frame.NETWORK, l(half, 0, 0), BlockFace.SOUTH);
            if_color = setFrame(FrameInfo.Frame.COLOR, l(half+1+odd, 0, 0), BlockFace.SOUTH);

            if_id1 = setFrame(FrameInfo.Frame.ID1, l(0, 0, 1), BlockFace.EAST);
            if_id2 = setFrame(FrameInfo.Frame.ID2, l(), BlockFace.EAST);

            if_dest1 = setFrame(FrameInfo.Frame.DEST1, l(size-1, 0, 0), BlockFace.WEST);
            if_dest2 = setFrame(FrameInfo.Frame.DEST2, l(size-1, 0, 1), BlockFace.WEST);

            // Iterate through the mods, making frames.
            Location ml = location.clone().add((size - mods.size()) / 2, -1, 0);
            for(ModInfo info: mods) {
                setModFrame(info, ml, BlockFace.SOUTH);
                ml.add(1, 0, 0);
            }
        }

        // Now that we've got the frames, update them all.
        if_network.setItem(network);
        if_color.setItem(new ItemStack(Material.WOOL, 1, color.getWoolData()));

        setIDFrames(id, if_id1, if_id2);
        setIDFrames(destination, if_dest1, if_dest2);


        // Add this portal's frames to the manager.
        plugin.getManager().addFrames(this);
    }

    private void old() {
        // Don't do this if we've got existing frames.
        if (!frames.isEmpty())
            return;

        ItemFrame if_network;
        ItemFrame if_color;
        ItemFrame if_id1;
        ItemFrame if_id2;
        ItemFrame if_dest1;
        ItemFrame if_dest2;

        // Make sure we have mods.
        if (mods == null) {
            int mod_count = Math.min(size, plugin.maximumMods);
            mods = new ArrayList<ModInfo>(mod_count);
            for(int index=0; index < mod_count; index++) {
                mods.add(new ModInfo(this));
            }
        }

        // Get the center two spots of each wall.
        int half = (size / 2) - 1;

        Location nc1 = location.clone().add(half, 0, 0);
        Location nc2 = location.clone().add(half + 1, 0, 0);

        Location ec1 = location.clone().add(size - 1, 0, half);
        Location ec2 = location.clone().add(size - 1, 0, half + 1);

        Location wc1 = location.clone().add(0, 0, half + 1);
        Location wc2 = location.clone().add(0, 0, half);

        Location sc1 = location.clone().add(half + 1, 0, size - 1);
        Location sc2 = location.clone().add(half, 0, size - 1);

        if ( rotation == Rotation.CLOCKWISE ) {
            if_network = setFrame(FrameInfo.Frame.NETWORK, ec1, BlockFace.WEST);
            if_color = setFrame(FrameInfo.Frame.COLOR, ec2, BlockFace.WEST);

            if_id1 = setFrame(FrameInfo.Frame.ID1, nc1, BlockFace.SOUTH);
            if_id2 = setFrame(FrameInfo.Frame.ID2, nc2, BlockFace.SOUTH);

            if_dest1 = setFrame(FrameInfo.Frame.DEST1, sc1, BlockFace.NORTH);
            if_dest2 = setFrame(FrameInfo.Frame.DEST2, sc2, BlockFace.NORTH);

            // Iterate through the mods, making frames.
            Location ml = location.clone().add(size - 1, -1, (size - mods.size()) / 2);
            for(ModInfo info: mods) {
                setModFrame(info, ml, BlockFace.WEST);
                ml.add(0, 0, 1);
            }

        } else if ( rotation == Rotation.FLIPPED ) {
            if_network = setFrame(FrameInfo.Frame.NETWORK, sc1, BlockFace.NORTH);
            if_color = setFrame(FrameInfo.Frame.COLOR, sc2, BlockFace.NORTH);

            if_id1 = setFrame(FrameInfo.Frame.ID1, ec1, BlockFace.WEST);
            if_id2 = setFrame(FrameInfo.Frame.ID2, ec2, BlockFace.WEST);

            if_dest1 = setFrame(FrameInfo.Frame.DEST1, wc1, BlockFace.EAST);
            if_dest2 = setFrame(FrameInfo.Frame.DEST2, wc2, BlockFace.EAST);

            // Iterate through the mods, making frames.
            Location ml = location.clone().add(size - (1 + ((size - mods.size()) / 2)), -1, size - 1);
            for(ModInfo info: mods) {
                setModFrame(info, ml, BlockFace.NORTH);
                ml.add(-1, 0, 0);
            }

        } else if ( rotation == Rotation.COUNTER_CLOCKWISE ) {
            if_network = setFrame(FrameInfo.Frame.NETWORK, wc1, BlockFace.EAST);
            if_color = setFrame(FrameInfo.Frame.COLOR, wc2, BlockFace.EAST);

            if_id1 = setFrame(FrameInfo.Frame.ID1, sc1, BlockFace.NORTH);
            if_id2 = setFrame(FrameInfo.Frame.ID2, sc2, BlockFace.NORTH);

            if_dest1 = setFrame(FrameInfo.Frame.DEST1, nc1, BlockFace.SOUTH);
            if_dest2 = setFrame(FrameInfo.Frame.DEST2, nc2, BlockFace.SOUTH);

            // Iterate through the mods, making frames.
            Location ml = location.clone().add(0, -1, size - (1 + ((size - mods.size()) / 2)));
            for(ModInfo info: mods) {
                setModFrame(info, ml, BlockFace.EAST);
                ml.add(0, 0, -1);
            }

        } else {
            if_network = setFrame(FrameInfo.Frame.NETWORK, nc1, BlockFace.SOUTH);
            if_color = setFrame(FrameInfo.Frame.COLOR, nc2, BlockFace.SOUTH);

            if_id1 = setFrame(FrameInfo.Frame.ID1, wc1, BlockFace.EAST);
            if_id2 = setFrame(FrameInfo.Frame.ID2, wc2, BlockFace.EAST);

            if_dest1 = setFrame(FrameInfo.Frame.DEST1, ec1, BlockFace.WEST);
            if_dest2 = setFrame(FrameInfo.Frame.DEST2, ec2, BlockFace.WEST);

            // Iterate through the mods, making frames.
            Location ml = location.clone().add((size - mods.size()) / 2, -1, 0);
            for(ModInfo info: mods) {
                setModFrame(info, ml, BlockFace.SOUTH);
                ml.add(1, 0, 0);
            }
        }

        // Now that we've got the frames, update them all.
        if_network.setItem(network);
        if_color.setItem(new ItemStack(Material.WOOL, 1, color.getWoolData()));

        setIDFrames(id, if_id1, if_id2);
        setIDFrames(destination, if_dest1, if_dest2);


        // Add this portal's frames to the manager.
        plugin.getManager().addFrames(this);
    }

    private void setIDFrames(short value, ItemFrame frame1, ItemFrame frame2) {
        // Try to avoid null pointer exceptions, shall we?
        if (frame1 == null || frame2 == null)
            return;

        if (value < 0 || value > 288) {
            ItemStack bedrock = new ItemStack(Material.BEDROCK, 1);
            frame1.setItem(bedrock);
            frame2.setItem(bedrock);
            return;
        }

        int f1 = (value / 17) - 1;
        int f2 = (value % 17) - 1;

        frame1.setItem((f1 == -1) ? null : new ItemStack(Material.WOOL, 1, (short) f1));
        frame2.setItem((f2 == -1) ? null : new ItemStack(Material.WOOL, 1, (short) f2));
    }

    // Portal Location Check

    public double getRange() {
        return plugin.baseRange + (rangeMultiplier * depth);
    }

    public double checkRange(final ABPortal portal) {
        return checkRange(portal, getDistance(portal));
    }

    public double checkRange(final ABPortal portal, double distance) {
        return distance - (getRange() + portal.getRange());
    }

    public double getDistance(final ABPortal portal) {
        // Get the distance to the given portal.
        if ( portal == null || portal.location == null || location == null )
            return Double.POSITIVE_INFINITY;

        final Location from = location.clone();
        final Location to = portal.getLocation();

        final World fromWorld = from.getWorld();
        final World toWorld = to.getWorld();

        final World.Environment fromEnv = fromWorld.getEnvironment();
        final World.Environment toEnv = toWorld.getEnvironment();

        // Ready to be uncommented in a time of great need.
        // ColorBuilder out = new ColorBuilder().append("Distance Calculation").lf().
        //         append("    From: %d, %d, %d [%s:%s]", from.getBlockX(), from.getBlockY(), from.getBlockZ(), fromWorld.getName(), fromEnv.name()).lf().
        //         append("      To: %d, %d, %d [%s:%s]", to.getBlockX(), to.getBlockY(), to.getBlockZ(), toWorld.getName(), toEnv.name()).lf();

        // If we're coming from the nether, adjust from coordinates.
        if ( fromEnv == World.Environment.NETHER && toEnv != World.Environment.NETHER ) {
            to.setX(to.getX() / 8);
            to.setZ(to.getZ() / 8);
        }

        else if ( fromEnv != World.Environment.NETHER && toEnv == World.Environment.NETHER ) {
            from.setX(from.getX() / 8);
            from.setZ(from.getZ() / 8);
        }

        // Get the base distance.
        from.setWorld(toWorld);
        double distance = from.distance(to);

        // out.append("    Base: %f", distance).lf();

        final double fromEyes = 1 + eyeCount;
        final double toEyes = 1 + portal.eyeCount;

        // If we're not in the same world, impose multipliers based on the types.
        if ( fromWorld.equals(toWorld) ) {
            final double eyes = (eyeCount + portal.eyeCount);
            if ( fromEnv == World.Environment.NORMAL )
                distance /= (eyes * 0.5) + 1;

            else if ( fromEnv == World.Environment.NETHER )
                distance /= (eyes * 1.6) + 1;

            else if ( fromEnv == World.Environment.THE_END )
                distance /= (eyes * 0.2) + 1;

        } else {
            if ( fromEnv == World.Environment.NORMAL)
                distance *= 3.75 / fromEyes;

            else if ( fromEnv == World.Environment.NETHER )
                distance *= 4.5 / fromEyes;

            else if ( fromEnv == World.Environment.THE_END )
                distance *= 16 / fromEyes;

            if ( toEnv == World.Environment.NORMAL )
                distance *= 3.75 / toEyes;

            else if ( toEnv == World.Environment.NETHER )
                distance *= 4.5 / toEyes;

            else if ( toEnv == World.Environment.THE_END )
                distance *= 16 / toEyes;
        }

        // out.append("   Final: %f", distance).send(plugin.getLogger(), Level.INFO);

        return distance;
    }

    public Location getMinimumLocation() {
        return minimum.clone();
    }

    public BlockVector getMinimumBlockVector() {
        return new BlockVector(minimum.getBlockX(), minimum.getBlockY(), minimum.getBlockZ());
    }

    public Location getMaximumLocation() {
        return maximum.clone();
    }

    public BlockVector getMaximumBlockVector() {
        return new BlockVector(maximum.getBlockX(), maximum.getBlockY(), maximum.getBlockZ());
    }


    public boolean isNearPortal(final Location loc) {
        return (loc != null) && isNearPortal(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public boolean isNearPortal(final World world, final int x, final int y, final int z) {
        // Determine if the given location is within this portal or its frame.
        if ( location == null || world == null || !location.getWorld().equals(world) )
            return false;

        // Make sure it's vertically within the portal, under it, or above it.
        final int dy = location.getBlockY() - y;
        if ( dy < -2 || dy > (depth + 1) )
            return false;

        // Check X
        final int dx = x - location.getBlockX();
        if ( dx < -1 || dx > size )
            return false;

        // Check Z.
        final int dz = z - location.getBlockZ();
        return !(dz < -1 || dz > size );
    }


    public boolean isOverPortal(final Location loc) {
        return (loc != null) && isOverPortal(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public boolean isOverPortal(final World world, final int x, final int y, final int z) {
        // Determine if the given location is directly over this portal.
        if ( location == null || world == null || !location.getWorld().equals(world) )
            return false;

        // Make sure it's vertically over the portal.
        final int dy = y - location.getBlockY();
        if ( dy <= 0 || dy > 2 )
            return false;

        // Check X.
        final int dx = x - location.getBlockX();
        if ( dx < 0 || dx >= size )
            return false;

        // And Z.
        final int dz = z - location.getBlockZ();
        return !( dz < 0 || dz >= size );
    }


    public boolean isInPortal(final Block block) {
        if ( block == null )
            return false;

        final World world;
        try { world = block.getWorld(); }
        catch(NullPointerException ex) { return false; }

        return isInPortal(world, block.getX(), block.getY(), block.getZ());
    }

    public boolean isInPortal(final Location loc) {
        return (loc != null) && isInPortal(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public boolean isInPortal(final World world, final int x, final int y, final int z) {
        // Determine if the given location is within this portal.
        if ( location == null || world == null || !location.getWorld().equals(world) )
            return false;

        // Make sure it's vertically within the portal.
        final int dy = location.getBlockY() - y;
        if ( dy < 0 || dy > depth )
            return false;

        // Check X.
        final int dx = x - location.getBlockX();
        if ( dx < 0 || dx >= size )
            return false;

        // And Z.
        final int dz = z - location.getBlockZ();
        return !( dz < 0 || dz >= size );
    }


    public boolean isInFrame(final World world, final int x, final int y, final int z) {
        // Determine if the given location is within the important two layers
        // of the portal.
        if ( location == null || world == null || !location.getWorld().equals(world) )
            return false;

        // Vertical Check
        final int dy = location.getBlockY() - y;
        if ( dy < 0 || dy > 1 )
            return false;

        // Check X
        final int dx = x - location.getBlockX();
        if ( dx < -1 || dx > size )
            return false;

        // And Z.
        final int dz = z - location.getBlockZ();
        return !( dz < -1 || dz > size );
    }


    private static void saveLocation(final ConfigurationSection config, final Location loc) {
        config.set("world", loc.getWorld().getUID().toString());
        config.set("x", loc.getX());
        config.set("y", loc.getY());
        config.set("z", loc.getZ());
        config.set("yaw", Float.toString(loc.getYaw()));
        config.set("pitch", Float.toString(loc.getPitch()));
    }

    private static Location loadLocation(final ConfigurationSection c) {
        final World w = Bukkit.getWorld(UUID.fromString(c.getString("world")));
        if (w == null)
            throw new IllegalArgumentException("Non-existent world.");

        return new Location(w, c.getDouble("x"), c.getDouble("y"), c.getDouble("z"),
                Float.parseFloat(c.getString("yaw")), Float.parseFloat(c.getString("pitch")));
    }

}

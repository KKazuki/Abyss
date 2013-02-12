package me.stendec.abyss;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FrameInfo implements ConfigurationSerializable {

    public enum Frame {
        NETWORK, COLOR, ID1, ID2, DEST1, DEST2, MOD
    }

    public World world;

    private int chunk_x;
    private int chunk_z;

    public UUID id;
    public Frame type;

    private WeakReference<ItemFrame> instance;

    FrameInfo() {}

    public FrameInfo(Frame type, ItemFrame frame) {
        this.type = type;
        id = frame.getUniqueId();

        Location loc = frame.getLocation();
        Chunk chunk = loc.getChunk();

        world = chunk.getWorld();
        chunk_x = chunk.getX();
        chunk_z = chunk.getZ();

        instance = new WeakReference<ItemFrame>(frame);
    }

    public ItemFrame getFrame() {
        return getFrame(false);
    }

    public ItemFrame getFrame(boolean force) {
        ItemFrame frame = (instance != null) ? instance.get() : null;
        if (frame == null || !frame.isValid()) {
            if (world != null) {
                Chunk chunk = world.getChunkAt(chunk_x, chunk_z);
                if (!chunk.isLoaded() && force)
                    chunk.load();

                if (chunk.isLoaded())
                    for(Entity entity: chunk.getEntities())
                        if (entity.getUniqueId().equals(id)) {
                            frame = (ItemFrame) entity;
                            instance = new WeakReference<ItemFrame>(frame);
                            break;
                        }
            }
        }

        return frame;
    }

    public static FrameInfo valueOf(Map<String, Object> value) {
        FrameInfo out = new FrameInfo();

        out.chunk_x = (Integer) value.get("x");
        out.chunk_z = (Integer) value.get("z");
        out.id = UUID.fromString((String) value.get("id"));

        return out;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<String, Object>();

        map.put("id", id.toString());
        map.put("x", chunk_x);
        map.put("z", chunk_z);

        return map;
    }

}

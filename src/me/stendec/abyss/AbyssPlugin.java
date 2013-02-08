package me.stendec.abyss;

import com.google.common.base.Joiner;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import me.stendec.abyss.commands.*;
import me.stendec.abyss.events.AbyssPreTeleportEvent;
import me.stendec.abyss.listeners.*;

import me.stendec.abyss.managers.BasicManager;
import me.stendec.abyss.managers.WorldGuardManager;
import me.stendec.abyss.modifiers.*;
import me.stendec.abyss.util.ColorBuilder;
import me.stendec.abyss.util.EntityUtils;
import me.stendec.abyss.util.IteratorChain;
import me.stendec.abyss.util.ParseUtils;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AbyssPlugin extends JavaPlugin {

    // Storage Files
    private File portalFile;


    // Portal Wand Configuration
    public Material wandMaterial;
    public String wandName;

    // Default Network Configuration
    public ItemStack defaultNetwork;
    public DyeColor defaultColor;

    // Portal Dimensions
    public short minimumDepth;
    public short minimumSize;
    public short maximumSize;
    public short maximumMods;

    // Portal Configuration
    public double minimumVelocity;
    public double maximumVelocity;
    public long cooldownTicks;

    public boolean limitDistance;
    public double rangeMultiplier;
    public double baseRange;


    // Portal Storage
    private PortalManager manager;
    public HashMap<UUID, UUID> entityLastPortal;
    public HashMap<UUID, Long> entityLastTime;
    public HashMap<String, Integer> lastId;
    public HashMap<UUID, Long> portalDestroyTime;

    private boolean useWorldGuard;

    // Portal Effect Task
    private BukkitTask task;

    // Command Storage
    public HashMap<String, ABCommand> commands;
    public HashMap<String, String> aliases;


    static {
        ConfigurationSerialization.registerClass(FrameInfo.class);

        PortalModifier.register(new BedModifier(), Material.BED);
        PortalModifier.register(new SlimeModifier(), Material.SLIME_BALL);
        PortalModifier.register(new DispenserModifier(), Material.DISPENSER);
        PortalModifier.register(new LeverModifier(), Material.STONE_PLATE, Material.STONE_BUTTON, Material.WOOD_PLATE, Material.WOOD_BUTTON);
        PortalModifier.register(new ObsidianModifier(), Material.OBSIDIAN, Material.SOUL_SAND);
        PortalModifier.register(new EnderPearlModifier(), Material.ENDER_PEARL);
        PortalModifier.register(new BookModifier(), Material.BOOK, Material.BOOK_AND_QUILL, Material.WRITTEN_BOOK);
        PortalModifier.register(new CompassModifier(), Material.COMPASS);
        PortalModifier.register(new EyeOfEnderModifier(), Material.EYE_OF_ENDER);
    }


    @Override
    public void onLoad() {
        // Bare Basics
        saveDefaultConfig();
        portalFile = new File(getDataFolder(), "portals.yml");

        // Command Creation
        commands = new HashMap<String, ABCommand>();
        aliases = new HashMap<String, String>();

        commands.put("info", new InfoCommand(this));
        commands.put("wand", new WandCommand(this));
        commands.put("create", new CreateCommand(this));
        commands.put("delete", new DeleteCommand(this));
        commands.put("list", new ListCommand(this));
        commands.put("teleport", new TeleportCommand(this));
        commands.put("configure", new ConfigureCommand(this));

        aliases.put("remove", "delete");
        aliases.put("tp", "teleport");
        aliases.put("config", "configure");
    }


    @Override
    public void onEnable() {

        // Initialize Storage Structures
        entityLastPortal = new HashMap<UUID, UUID>();
        entityLastTime = new HashMap<UUID, Long>();
        lastId = new HashMap<String, Integer>();
        portalDestroyTime = new HashMap<UUID, Long>();


        // Load Configuration
        configure();


        // If we can get the right command map, add aliases to our command.
        CommandMap map = getCommandMap();
        final Command cmd = getCommand("abyss");
        if ( cmd != null && map != null && cmd.unregister(map) ) {
            // Add our dynamic aliases, and update the usage string.
            List<String> als = cmd.getAliases();

            for(final String key: commands.keySet()) {
                final String ckey = "ab" + key;
                if ( ! als.contains(ckey) )
                    als.add(ckey);
            }

            for(final String key: aliases.keySet()) {
                final String ckey = "ab" + key;
                if ( ! als.contains(ckey) )
                    als.add(ckey);
            }

            // Modify the usage string.
            if ( commands.size() > 0 )
                cmd.setUsage(cmd.getUsage().replace("[subcommand]", "[" + Joiner.on('|').join(commands.keySet()) + "]"));

            // Re-register the command.
            cmd.setAliases(als);
            map.register("abyss", cmd);
        }


        // Create the PortalManager.
        WorldGuardPlugin worldGuard = useWorldGuard ? getWorldGuard() : null;
        if ( worldGuard == null ) {
            getLogger().fine("Using BasicManager");
            manager = new BasicManager(this);

        } else {
            getLogger().fine("Using WorldGuardManager");
            manager = new WorldGuardManager(this, worldGuard);
        }


        // Load Portals
        loadPortals();


        // Create all the necessary listeners.
        new BlockListener(this);
        new ItemListener(this);
        new PlayerListener(this);
        new VehicleListener(this);
        //new WorldListener(this);


        // Start the Task
        task = getServer().getScheduler().runTaskTimer(this, new PortalEffect(this), 20L, 40L);

    }


    @Override
    public void onDisable() {
        // Destroy the task.
        task.cancel();
        task = null;

        // Save the portals before we leave.
        savePortals();

        // Destroy the manager before we leave.
        manager = null;

        // Destroy the local storage arrays.
        entityLastPortal = null;
        entityLastTime = null;
        lastId = null;
        portalDestroyTime = null;

    }


    ///////////////////////////////////////////////////////////////////////////
    // Configuration Storage
    ///////////////////////////////////////////////////////////////////////////

    public void writeConfig() {
        final FileConfiguration config = getConfig();

        // Portal Wand Configuration
        config.set("wand-name", wandName);
        config.set("wand-material", wandMaterial.name());

        // Network Configuration
        config.set("network-material", String.format("%s:%d", defaultNetwork.getType().name(), defaultNetwork.getDurability()));
        config.set("network-color", defaultColor.name());

        // Portal Dimensions
        config.set("minimum-depth", minimumDepth);
        config.set("minimum-size", minimumSize);
        config.set("maximum-size", maximumSize);
        config.set("maximum-modifiers", maximumMods);

        // Range Configuration
        config.set("limit-distance", limitDistance);
        config.set("base-range", baseRange);
        config.set("range-multiplier", rangeMultiplier);

        // Portal Configuration
        config.set("minimum-velocity", minimumVelocity);
        config.set("maximum-velocity", maximumVelocity);
        config.set("cooldown-ticks", cooldownTicks);
        config.set("use-worldguard", useWorldGuard);

        // Now, save the configuration to disk.
        saveConfig();
    }


    public void configure() {
        // Reload the configuration from disk.
        reloadConfig();

        final FileConfiguration config = getConfig();
        final Configuration defaults = config.getDefaults();
        final Logger log = getLogger();

        // Portal Wand
        wandName = config.getString("wand-name");

        String string = config.getString("wand-material");
        wandMaterial = Material.matchMaterial(string);
        if ( wandMaterial == null ) {
            string = defaults.getString("wand-material");
            log.warning("Invalid wand-material. Using: " + string);
            wandMaterial = Material.matchMaterial(string);
        }


        // Default Network
        Object network = config.get("network-key");
        defaultNetwork = null;
        if (network instanceof ItemStack)
            defaultNetwork = (ItemStack) network;
        else if (network instanceof String)
            defaultNetwork = ParseUtils.matchItem((String) network);

        if ( defaultNetwork == null ) {
            string = defaults.getString("network-key");
            log.warning("Invalid network-key. Using: " + string);
            defaultNetwork = ParseUtils.matchItem(string);
        }

        defaultColor = ParseUtils.matchColor(config.getString("network-color"));
        if ( defaultColor == null ) {
            string = defaults.getString("network-color");
            log.warning("Invalid network-color. Using: " + string);
            defaultColor = ParseUtils.matchColor(string);
        }


        // Portal Dimensions
        minimumDepth = (short) config.getInt("minimum-depth", defaults.getInt("minimum-depth"));
        minimumSize = (short) config.getInt("minimum-size", defaults.getInt("minimum-size"));
        maximumSize = (short) config.getInt("maximum-size", defaults.getInt("maximum-size"));
        maximumMods = (short) config.getInt("maximum-modifiers", defaults.getInt("maximum-modifiers", maximumSize));

        if ( minimumSize < 2 ) {
            log.warning("Invalid minimum-size. Must be at least 2.");
            minimumSize = 2;
        }

        if ( minimumDepth < 2 ) {
            log.warning("Invalid minimum-depth. Must be at least 2.");
            minimumDepth = 2;
        }

        if ( maximumMods > maximumSize )
            log.log(Level.CONFIG, "maximum-mods will be limited to maximum-size");


        // Portal Configuration
        minimumVelocity = config.getDouble("minimum-velocity", defaults.getDouble("minimum-velocity"));
        maximumVelocity = config.getDouble("maximum-velocity", defaults.getDouble("maximum-velocity"));
        cooldownTicks = config.getLong("cooldown-ticks", defaults.getLong("cooldown-ticks"));
        useWorldGuard = config.getBoolean("use-worldguard", defaults.getBoolean("use-worldguard"));

        // Range Configuration
        limitDistance = config.getBoolean("limit-distance", defaults.getBoolean("limit-distance"));
        rangeMultiplier = config.getDouble("depth-multiplier", defaults.getDouble("depth-multiplier"));
        baseRange = config.getDouble("base-range", defaults.getDouble("base-range"));

    }


    ///////////////////////////////////////////////////////////////////////////
    // Portal Loader
    ///////////////////////////////////////////////////////////////////////////

    public void savePortals() {
        YamlConfiguration config = new YamlConfiguration();

        for(Map.Entry<UUID, ABPortal> entry: manager.entrySet())
            entry.getValue().save(config.createSection(entry.getKey().toString()));

        if ( ! lastId.isEmpty() ) {
            final ConfigurationSection ids = config.createSection("last-ids");
            for(Map.Entry<String, Integer> entry: lastId.entrySet())
                ids.set(entry.getKey(), entry.getValue());
        }

        try {
            config.save(portalFile);
        } catch(IOException ex) {
            getLogger().log(Level.SEVERE, "Unable to save portal configuration.");
        }
    }


    public void loadPortals() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(portalFile);
        if (config != null) {
            for(String key: config.getKeys(false)) {
                // Skip any non-section keys, non-portal keys.
                if (!config.isConfigurationSection(key) || key.equals("last-ids") )
                    continue;

                final UUID uid;
                try {
                    uid = UUID.fromString(key);
                } catch(IllegalArgumentException ex) {
                    getLogger().warning("Invalid Portal UID: " + key);
                    continue;
                }

                // Load the portal and store it.
                final ABPortal portal = ABPortal.fromConfig(this, uid, config.getConfigurationSection(key));
                manager.add(portal);
            }

            if ( config.isConfigurationSection("last-ids") ) {
                ConfigurationSection ids = config.getConfigurationSection("last-ids");
                for(final String key: ids.getKeys(false))
                    lastId.put(key, ids.getInt(key));
            }

        }
    }


    ///////////////////////////////////////////////////////////////////////////
    // Strange Getters
    ///////////////////////////////////////////////////////////////////////////

    private WorldGuardPlugin getWorldGuard() {
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
        if ( plugin == null || !(plugin instanceof WorldGuardPlugin) )
            return null;

        return (WorldGuardPlugin) plugin;
    }


    private CommandMap getCommandMap() {
        final PluginManager m = getServer().getPluginManager();
        if (! (m instanceof SimplePluginManager) )
            return null;

        try {
            final Field f = SimplePluginManager.class.getDeclaredField("commandMap");
            f.setAccessible(true);

            Object map = f.get(m);
            if ( map instanceof CommandMap )
                return (CommandMap) map;

        } catch(final Exception e) { }

        return null;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Portal Wand Code
    ///////////////////////////////////////////////////////////////////////////

    public String validatePortalWand(final ItemStack item) {
        if (item == null || item.getType() != wandMaterial)
            return null;

        final ItemMeta im = item.getItemMeta();
        if (! im.hasDisplayName() )
            return null;

        String key = im.getDisplayName();
        final int length = key.length();

        // Strip color codes from the name for easier processing.
        key = ChatColor.stripColor(key).toLowerCase();

        // No color was removed. A player may have made this on an anvil, so
        // we can't accept this.
        if ( length == key.length() )
            return null;

        if (!key.startsWith(wandName.toLowerCase()))
            return null;

        int index = key.lastIndexOf("[");
        if (index == -1)
            return null;

        int ind2 = key.indexOf("]", index);
        if (ind2 == -1)
            return null;

        return key.substring(index + 1, ind2);
    }


    ///////////////////////////////////////////////////////////////////////////
    // Commands
    ///////////////////////////////////////////////////////////////////////////

    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] arguments) {
        // We only support our command.
        if ( ! command.getName().equals("abyss") )
            return false;

        // Convert the arguments to a list.
        final ArrayList<String> args = new ArrayList<String>(Arrays.asList(arguments));

        // Remove empty strings.
        for(Iterator<String> it = args.iterator(); it.hasNext(); )
            if ( it.next().trim().length() == 0 )
                it.remove();

        // See if this is a help command.
        boolean help = false;
        if ( args.size() > 0 && args.get(0).equalsIgnoreCase("help") ) {
            help = true;
            args.remove(0);
        }

        // Determine the sub-command.
        String cmdkey = null;
        if ( ! label.equals("abyss") ) {
            cmdkey = label.substring(2);
        } else if ( args.size() > 0 ) {
            // Take the first argument.
            cmdkey = args.remove(0).toLowerCase();
        }

        // If we don't have a command key, fail.
        if ( cmdkey == null )
            return false;

        // Handle aliases first.
        if ( aliases.containsKey(cmdkey) )
            cmdkey = aliases.get(cmdkey);

        // Try getting a command from that key.
        ABCommand cmd = commands.get(cmdkey);
        if ( cmd == null ) {
            final ArrayList<String> possible = new ArrayList<String>();
            for(final String key: commands.keySet())
                if ( key.startsWith(cmdkey) )
                    possible.add(key);

            if ( possible.size() > 0 ) {
                // We've got a command, so use it.
                Collections.sort(possible);
                cmdkey = possible.get(0);
                cmd = commands.get(cmdkey);
            }
        }

        // If we still don't have one, return.
        if ( cmd == null )
            return false;

        // Make sure we've got permission to do this.
        final PluginManager pm = getServer().getPluginManager();
        final Permission perm = pm.getPermission("abyss.command." + cmdkey);
        if ( perm != null && !sender.hasPermission(perm) ) {
            t().red("Permission Denied").send(sender);
            return true;
        }


        // Check for a block.
        Block block = null;
        if ( args.size() > 0 ) {
            final String key = args.get(0);
            if ( key.length() > 0 && key.charAt(0) == '@' ) {
                args.remove(0);
                Location loc = null;

                // First, try getting a player with that name.
                Player player = getServer().getPlayer(key.substring(1));
                if ( player != null )
                    loc = player.getLocation();

                if ( loc == null && key.contains(",") ) {
                    final World world = (sender instanceof Player) ? ((Player) sender).getWorld() : null;
                    loc = ParseUtils.matchLocation(key.substring(1), world);
                }

                if ( loc == null ) {
                    t().red("Invalid player name or block location: ").reset(key).send(sender);
                    return true;
                }

                block = loc.getBlock();
            }
        }


        // If we have arguments, and the command needs a portal, parse the next argument as that.
        ABPortal portal = (block != null) ? manager.getAt(block) : null;
        if ( cmd.require_portal && portal == null ) {
            if ( block != null ) {
                t().red("No portal at: ").darkgray(ChatColor.WHITE, "%d, %d, %d [%s]", block.getX(), block.getY(), block.getZ(), block.getWorld().getName()).send(sender);
                return true;
            }

            if ( args.size() > 0 ) {
                final String key = args.remove(0);

                // First, see if it's a UUID. If not, try using it as a name.
                try {
                    portal = manager.getById(UUID.fromString(key));
                } catch(IllegalArgumentException ex) { }

                if ( portal == null )
                    portal = manager.getByName(key);

                // If it's not a valid name, try treating it as a location.
                if ( portal == null ) {
                    final World world = (sender instanceof Player) ? ((Player) sender).getWorld() : null;
                    final Location loc = ParseUtils.matchLocation(key, world);
                    if ( loc != null )
                        portal = manager.getAt(loc);
                }

                if ( portal == null) {
                    t().red("Invalid portal: ").reset(key).send(sender);
                    return true;
                }
            }
        }


        // If we're not getting help, run the command.
        if ( cmd.require_portal && portal == null )
            help = true;

        String usage = null;

        if ( ! help ) {
            try {
                cmd.run(sender, null, block, portal, args);
            } catch(ABCommand.NeedsHelp ex) {
                help = true;
                final String msg = ex.getMessage();
                if ( msg != null && msg.length() > 0 )
                    usage = msg;
            }
        }

        // If we need help, display it.
        if ( help ) {
            ColorBuilder out = t(sender instanceof Player ? "/" : "").append(label).append(" ");

            if ( label.equals("abyss") )
                out.append(cmdkey).append(" ");

            if ( usage != null )
                out.append(usage);
            else
                out.append(cmd.usage != null ? cmd.usage : (cmd.require_portal ? "[@block|@player|portal]" : "<@block|@player|portal>"));

            out.send(sender);
        }

        return true;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Portal Placement Code
    ///////////////////////////////////////////////////////////////////////////

    public int getIdFor(final String key) {
        final int id = (lastId.containsKey(key) ? lastId.get(key) : 0) + 1;
        lastId.put(key, id);
        return id;
    }


    public boolean validLayer(final Location location, final int size) {
        /* Determine if there's a valid layer at the location. This is assuming
           that the provided location is the lowest X,Z pair of the inside of
           the portal. */

        if ( location == null )
            return false;

        // Check the portal blocks.
        for(double x=0; x < size; x++) {
            for(double z=0; z < size; z++) {
                if (!validLiquid(location.clone().add(x, 0, z)))
                    return false;
            }
        }

        // Now, check the frame blocks.
        for(double x = -1; x < (size + 1); x++) {
            if (!validFrameBlock(location.clone().add(x, 0, -1)))
                return false;

            if (!validFrameBlock(location.clone().add(x, 0, size)))
                return false;
        }

        for(double z=0; z < size; z++) {
            if (!validFrameBlock(location.clone().add(-1, 0, z)))
                return false;

            if (!validFrameBlock(location.clone().add(size, 0, z)))
                return false;
        }

        return true;
    }


    public static boolean validFrameBlock(final Location location) {
        return (location != null) && validFrameBlock(location.getBlock());
    }


    public static boolean validFrameBlock(final Block block) {
        final Material mat = block.getType();
        return (block != null) && (mat.isOccluding() || mat == Material.ICE);
    }


    public static boolean validLiquid(final Location location) {
        return (location != null) && validLiquid(location.getBlock());
    }


    public static boolean validLiquid(final Block block) {
        if (block == null)
            return false;

        // Accept lava in the Nether.
        if ( block.getBiome() == Biome.HELL )
            return block.isLiquid();

        final Material m = block.getType();
        return m == Material.WATER || m == Material.STATIONARY_WATER;
    }


    public Location findRoot(Location location, final int size) {
        if (location == null)
            return null;

        // Get the block at that location, and then get the block's
        // location to ensure we've got the exact coordinates.
        final Block block = location.getBlock();
        location = block.getLocation();

        // If the block isn't a valid liquid, stop right now.
        if (!validLiquid(block))
            return null;

        // Find the lowest X and Y water block for this portal hole.
        int i = size - 1;
        while(validLiquid(location.subtract(1, 0, 0))) {
            i -= 1;
            if ( i < 0 )
                return null;
        }
        location.add(1, 0, 0);

        i = size - 1;
        while(validLiquid(location.subtract(0, 0, 1))) {
            i -= 1;
            if ( i < 0 )
                return null;
        }
        location.add(0, 0, 1);

        // Rise to the top.
        while(validLiquid(location))
            location.add(0, 1, 0);
        location.subtract(0, 1, 0);

        return location;
    }


    public boolean validateLocation(final Location location, final int size) {
        if (location == null)
            return false;

        // Assume we've been given the root location already. Make sure
        // there's not an existing portal.
        final ABPortal portal = manager.getByRoot(location);
        if ( portal != null )
            return false;

        // Now, check the depth of the portal. If it has any depth, it's a
        // valid portal location.
        return ( getDepthAt(location, size) >= minimumDepth );
    }


    public short getDepthAt(final Location location, final int size) {
        if (location == null)
            return 0;

        // Get the depth of available portal, starting at the given location.
        Location loc = location.clone();

        short depth = 0;
        while(validLayer(loc, size)) {
            loc.subtract(0, 1, 0);
            depth += 1;
        }

        return depth;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Portal Selection Code
    ///////////////////////////////////////////////////////////////////////////

    public ArrayList<ABPortal> getDestinations(final ABPortal portal) {
        if ( portal == null )
            return null;

        final ArrayList<ABPortal> out;

        if ( portal.destination != 0 ) {
            // Use the specific destination exclusively.
            out = new ArrayList<ABPortal>();
            final ABPortal destination = manager.getByNetworkId(portal.network, portal.color, portal.owner, portal.destination);
            if ( destination != null && destination.valid )
                out.add(destination);
        } else {
            out = manager.getNetworkForDestination(portal);
        }

        return out;
    }


    public PortalManager getManager() {
        return manager;
    }

    private static ArrayList<ABPortal> empty = new ArrayList<ABPortal>();

    public ArrayList<ABPortal> protectBlock(final Block block) {
        final Location loc = block.getLocation();
        final int y = loc.getBlockY();

        // Get all the portals near this block.
        final ArrayList<ABPortal> portals = manager.getNear(loc);
        if ( portals == null )
            return empty;

        // Iterate through all the portals, making sure we can do this.
        for(final ABPortal portal: portals) {
            final int py = portal.getLocation().getBlockY();
            if ( y <= py && y > py - 2 )
                // Protect the important layers of the frame.
                return null;
        }

        return portals;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Portal Use Code
    ///////////////////////////////////////////////////////////////////////////

    public Location usePortal(final Entity entity, final Location from, Location to) {
        // Don't handle invalid entities, or those in vehicles.
        if ( entity.isInsideVehicle() || !entity.isValid() )
            return null;

        // Only handle events within the world.
        final World world = to.getWorld();
        final int y = to.getBlockY();
        if ( y < 0 || y > world.getMaxHeight() - 1)
            return null;

        // If the entity didn't move into a liquid, don't handle it.
        if ( validLiquid(from.getBlock()) || !validLiquid(to.getBlock()) )
            return null;

        // Calculate the entity's velocity.
        final Vector fv = from.toVector();
        final Vector tv = to.toVector();

        Vector delta = tv.subtract(fv);
        delta.setY(-delta.getY());

        // If the entity hasn't moved, or if the entity isn't falling, then
        // we don't care about this.
        if ( fv.equals(tv) || delta.getY() <= 0 )
            return null;

        // Try to find a portal at this location.
        final ABPortal portal = manager.getAt(to);
        if ( portal == null )
            return null;

        // Make sure the portal is valid.
        if ( !portal.valid ) {
            if ( entity instanceof Player )
                t().red("The portal is obstructed.").send((Player) entity);
            return null;
        }

        // If the entity is in cooldown, we don't want to teleport yet.
        final UUID eid = entity.getUniqueId();
        if ( portal.uid.equals(entityLastPortal.get(eid)) ) {
            final Long time = entityLastTime.get(eid);
            final long now = world.getFullTime();
            if ( time != null && now < time ) {
                // Make sure the cooldown period only works once.
                entityLastPortal.remove(eid);
                return null;
            }
        }

        // Get a list of destination portals.
        final ArrayList<ABPortal> destinations = getDestinations(portal);
        if ( destinations == null || destinations.size() == 0 ) {
            if ( entity instanceof Player ) {
                final ColorBuilder cb = t();
                if ( portal.destination != 0 )
                    cb.red("The destination is unreachable.");
                else if ( limitDistance )
                    cb.red("No valid destinations within range.");
                else
                    cb.red("No valid destinations.");
                cb.send((Player) entity);
            }

            return null;
        }

        // Iterate through the destinations, trying to teleport until it succeeds.
        if ( destinations.size() > 0 ) {
            for(final ABPortal destination: destinations) {
                final Location loc = doTeleport(entity, portal, destination, to, delta);
                if ( loc != null )
                    return loc;
            }
        }

        if ( entity instanceof Player ) {
            final ColorBuilder cb = t();
            if ( portal.destination != 0 )
                cb.red("The destination is unreachable.");
            else
                cb.red("No valid destinations.");
            cb.send((Player) entity);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public Location doTeleport(final Entity entity, final ABPortal from, final ABPortal to, final Location pos, Vector delta) {
        // Get the destination location.
        Location dest = to.getCenter();
        delta = delta.clone();

        // Apply the position's yaw and pitch to destination.
        dest.setYaw(pos.getYaw());
        dest.setPitch(pos.getPitch());

        // If we have a source portal, do a bunch of stuff.
        if ( from != null ) {
            Location fc = from.getCenter();
            final Vector offset = pos.clone().subtract(fc).toVector();

            // If either rangeMultiplier is infinite, don't deal with it.
            if ( limitDistance && !Double.isInfinite(from.rangeMultiplier) && !Double.isInfinite(to.rangeMultiplier) ) {

                double distance;

                // Handle different worlds.
                if ( ! fc.getWorld().equals(dest.getWorld())) {
                    fc.setWorld(dest.getWorld());

                    double multiplier = 100;
                    if ( from.eyeCount > 0 && to.eyeCount > 0 )
                        multiplier = multiplier / Math.pow(4, from.eyeCount + to.eyeCount);

                    distance = dest.distance(fc) * multiplier;

                } else {
                    distance = dest.distance(fc);
                }

                // Apply portal depth to the problem.
                distance -= from.depth * from.rangeMultiplier;
                distance -= to.depth * to.rangeMultiplier;

                getLogger().info("Distance: " + distance);

                // If distance is still greater than zero, quit right now.
                if ( distance > 0 )
                    return null;
            }

            // Adjust for relative size.
            final int to_size = to.getSize();
            final int from_size = from.getSize();

            if ( from_size != to_size ) {
                final float multiplier = ((float) to_size) / from_size;
                offset.setX(offset.getX() * multiplier);
                offset.setZ(offset.getZ() * multiplier);
            }

            // Determine the relative rotation.
            // 1 = 90, -3 = -270, 2 = 180, -2 = -180, 3 = 270, -1 = -90, 0 = 0
            final Rotation from_rot = from.getRotation();
            final int rotation = to.getRotation().ordinal() - from_rot.ordinal();
            final int opposite_rot = (rotation >= 0) ? (rotation + 2) % 4 : (rotation - 2) % 4;

            if ( opposite_rot != 0 ) {
                // Apply the rotation to the destination rotation.
                dest.setYaw(dest.getYaw() + (opposite_rot * 90));

                // Apply the rotation to the velocity.
                double dx, dz;

                if ( opposite_rot == 0 ) {
                    dx = delta.getX();
                    dz = delta.getZ();

                } else if ( opposite_rot == 1 || opposite_rot == -3 ) {
                    dx = -delta.getZ();
                    dz = delta.getX();

                } else if ( opposite_rot == 2 || opposite_rot == -2 ) {
                    dx = -delta.getX();
                    dz = -delta.getZ();

                } else {
                    dx = delta.getZ();
                    dz = -delta.getX();
                }

                delta.setX(dx);
                delta.setZ(dz);
            }

            // Apply the rotation to the offset.
            double off_x = offset.getX(), off_z = offset.getZ();

            if ( from_rot == Rotation.CLOCKWISE || from_rot == Rotation.COUNTER_CLOCKWISE )
                off_z = -off_z;
            else
                off_x = -off_x;

            if ( rotation != 0 ) {
                final double rads = Math.toRadians(rotation * 90);
                final double c = Math.cos(rads), s = Math.sin(rads);

                offset.setX(off_x * c - off_z * s);
                offset.setZ(off_x * s + off_z * c);

            } else {
                offset.setX(off_x);
                offset.setZ(off_z);
            }

            // Apply the offset to the destination coordinates, and add a slight
            // height to make things work better.
            offset.setY(1);
            dest.add(offset);
            dest.add(0, 0.5, 0);
        }

        // Apply the destination portal's velocity multiplier.
        if ( to.velocityMultiplier != 1 )
            delta.multiply(to.velocityMultiplier);

        // Assure a minimum and maximum velocity.
        double length = delta.length();
        if ( length < minimumVelocity ) {
            delta.multiply(minimumVelocity / length);
            length = delta.length();
        }

        if ( length > maximumVelocity )
            delta.multiply(maximumVelocity / length);

        // If we're dealing with a Minecart, check the destination for tracks.
        if ( entity instanceof Minecart ) {
            BlockFace bf = EntityUtils.toBlockFace(delta.clone().setY(0));
            if ( bf == BlockFace.SELF )
                bf = EntityUtils.toBlockFace(to.getRotation()).getOppositeFace();

            Block block = dest.getBlock().getRelative(bf);
            while ( to.isNearPortal(block.getLocation()) ) {
                if ( isRail(block.getType()) ) {
                    // We've got a rail! Move to it and be done.
                    dest = block.getLocation();
                    delta = EntityUtils.toVector(bf, delta.length());
                    break;
                }

                block = block.getRelative(bf);
            }
        }

        // Run pre-teleportation modifiers.
        boolean okay = true;

        Iterator<ModInfo> it = to.mods.iterator();
        if ( from != null )
            it = new IteratorChain<ModInfo>(from.mods.iterator(), it);

        for(; it.hasNext(); ) {
            final ModInfo info = it.next();
            if ( info.item == null )
                continue;

            // Get the PortalModifier for this item type.
            final PortalModifier modifier = PortalModifier.get(info.item);
            if ( modifier == null )
                continue;

            // Make sure the modifier's location is set.
            if ( info.location == null )
                info.updateLocation();

            // Now, try to execute the modifier.
            try {
                okay = modifier.preTeleport(from, to, info, entity, dest, delta);
            } catch(IllegalArgumentException ex) {
                if ( ! info.flags.containsKey("silent") && entity instanceof Player )
                    t().red("Portal Modifier ").darkgray("[").darkred(info.item.getType().name()).
                            darkgray("]").red("Configuration Error").lf().
                        gray("    ").append(ex.getMessage()).send((Player) entity);
            } catch(Exception ex) {
                getLogger().log(Level.SEVERE, "Exception in Portal Modifier [" + info.item.getType().name() + "] Pre-Teleportation Check", ex);
            }

            // If it's not okay, abandon ship.
            if ( !okay )
                return null;
        }

        // Fire a pre-teleport event.
        final AbyssPreTeleportEvent event = new AbyssPreTeleportEvent(from, to, entity, dest, delta);
        getServer().getPluginManager().callEvent(event);

        // If it's cancelled, abandon ship.
        if ( event.isCancelled() )
            return null;

        // Update the destination and delta from the event.
        dest = event.getDestination();
        delta = event.getVelocity();

        // If the entity has a passenger, we have to teleport them separately.
        Entity passenger = null;
        if ( !entity.isEmpty() ) {
            passenger = entity.getPassenger();

            // If we can't eject the passenger, abort.
            if ( ! entity.eject() )
                return null;

            // If we can't teleport the passenger, abort.
            if ( doTeleport(passenger, from, to, passenger.getLocation(), delta) == null ) {
                // Put the passenger back first.
                entity.setPassenger(passenger);
                return null;
            }
        }

        // Teleport the entity. This is the last point that can return null.
        if ( ! entity.teleport(dest, PlayerTeleportEvent.TeleportCause.PLUGIN) ) {
            // If we can't go... too bad for the passenger!
            return null;
        }

        // Set the entity's velocity and fall distance.
        entity.setVelocity(delta);
        entity.setFallDistance(0);

        // Recover our passenger, after a slight delay.
        if ( passenger != null ) {
            final Entity p = passenger;
            getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                @Override
                public void run() {
                    entity.setPassenger(p);
                }
            }, 5L);
        }

        // Play an effect.
        // TODO: Make this configurable.

        final World pw = pos.getWorld();
        final World dw = dest.getWorld();

        pw.playEffect(pos, Effect.MOBSPAWNER_FLAMES, 0);
        dw.playEffect(dest, Effect.MOBSPAWNER_FLAMES, 0);

        pw.playSound(pos, Sound.ENDERMAN_TELEPORT, 1, 1);
        dw.playSound(dest, Sound.ENDERMAN_TELEPORT, 1, 1);

        // Post-Teleportation Modifiers
        it = to.mods.iterator();
        if ( from != null )
            it = new IteratorChain<ModInfo>(from.mods.iterator(), it);

        for(; it.hasNext(); ) {
            final ModInfo info = it.next();
            if ( info.item == null )
                continue;

            // Get the PortalModifier for this item type.
            final PortalModifier modifier = PortalModifier.get(info.item);
            if ( modifier == null )
                continue;

            // Make sure the modifier's location is set.
            if ( info.location == null )
                info.updateLocation();

            // Now, try to execute the modifier.
            try {
                modifier.postTeleport(from, to, info, entity);
            } catch(IllegalArgumentException ex) {
                if ( ! info.flags.containsKey("silent") && entity instanceof Player )
                    t().red("Portal Modifier ").darkgray("[").darkred(info.item.getType().name()).
                            darkgray("]").red("Configuration Error").lf().
                            gray("    ").append(ex.getMessage()).send((Player) entity);
            } catch(Exception ex) {
                getLogger().log(Level.SEVERE, "Exception in Portal Modifier [" + info.item.getType().name() + "] Post-Teleportation Event", ex);
            }
        }

        // Set cooldown information.
        final UUID eid = entity.getUniqueId();
        entityLastPortal.put(eid, to.uid);
        entityLastTime.put(eid, entity.getWorld().getFullTime() + cooldownTicks);

        // Return the destination.
        return dest;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Pretty Portal Effects
    ///////////////////////////////////////////////////////////////////////////

    private static ColorBuilder t() {
        return new ColorBuilder();
    }

    private static ColorBuilder t(final String string) {
        return new ColorBuilder(string);
    }

    private class PortalEffect implements Runnable {

        private final AbyssPlugin plugin;
        private final Random random;

        PortalEffect(final AbyssPlugin instance) {
            plugin = instance;
            random = new Random();
        }

        @Override
        public void run() {
            for(final Map.Entry<UUID, ABPortal> entry: plugin.manager.entrySet()) {
                final ABPortal portal = entry.getValue();
                if ( portal == null )
                    continue;

                final Location center = portal.getCenter();
                if (! center.getChunk().isLoaded() )
                    continue;

                portal.effect--;
                if (portal.effect > 0)
                    continue;

                portal.effect = (short) random.nextInt(5);

                final World w = center.getWorld();
                center.subtract(0, portal.depth / 2, 0);

                w.playEffect(center, Effect.MOBSPAWNER_FLAMES, 1);

                portal.effectSound--;
                if (portal.effectSound > 0)
                    continue;

                portal.effectSound = (short) (random.nextInt(5) + 6);
                w.playSound(center, Sound.PORTAL, 0.2f, 1);
            }
        }
    }


    public static boolean isRail(final Material mat) {
        switch(mat) {
            case RAILS:
            case POWERED_RAIL:
            case DETECTOR_RAIL:
                return true;
            default:
                return false;
        }
    }


}

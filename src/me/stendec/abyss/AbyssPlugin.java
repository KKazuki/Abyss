package me.stendec.abyss;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import me.stendec.abyss.commands.*;
import me.stendec.abyss.events.AbyssPreTeleportEvent;
import me.stendec.abyss.listeners.BlockListener;
import me.stendec.abyss.listeners.ItemListener;
import me.stendec.abyss.listeners.PlayerListener;
import me.stendec.abyss.listeners.VehicleListener;
import me.stendec.abyss.managers.BasicManager;
import me.stendec.abyss.managers.WorldGuardManager;
import me.stendec.abyss.modifiers.*;
import me.stendec.abyss.util.*;
import org.apache.commons.lang.Validate;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
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

    // Auto Updater
    public byte autoUpdate;
    public Updater updater;
    public String[] updateMessage;
    public ArrayList<String> updateCheckers;


    // Portal Wand Configuration
    public Material wandMaterial;
    public String wandName;
    public int wandRange;

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

    // Frame Configuration
    public boolean frameSingleMaterial;
    public HashSet<Material> frameMaterials;
    public int frameCornerDepth;

    // Rails
    public boolean smartRails;

    // Effect Configuration
    public boolean usePortalEffect;

    public Effect portalEffect;
    public int portalEffectData;
    public Sound portalSound;
    public float portalSoundVolume;
    public float portalSoundPitch;

    public boolean useStaticEffect;
    public boolean staticEffectCentered;
    public boolean staticEffectFullHeight;

    public Effect staticEffect;
    public int staticEffectData;
    public Sound staticSound;
    public float staticSoundVolume;
    public float staticSoundPitch;


    // Portal Storage
    private PortalManager manager;
    public HashMap<UUID, UUID> entityLastPortal;
    public HashMap<UUID, Long> entityLastTime;
    public HashMap<String, Integer> lastId;
    public HashMap<UUID, Long> portalDestroyTime;

    private boolean useWorldGuard;

    // Portal Effect Task
    private BukkitTask task;

    // Entity Whitelist
    public HashSet<EntityType> entityTypeWhitelist;

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

        // Update Stuff
        updateCheckers = new ArrayList<String>();

        // Command Creation
        commands = new HashMap<String, ABCommand>();
        aliases = new HashMap<String, String>();

        new InfoCommand(this);
        new WandCommand(this);
        new CreateCommand(this);
        new DeleteCommand(this);
        new ListCommand(this);
        new TeleportCommand(this);
        new ConfigureCommand(this);
        new ModifierCommand(this);
        new DestinationsCommand(this);
        new ReloadCommand(this);
        new UtilityCommand(this);
        new UpdateCommand(this);

        aliases.put("remove", "delete");
        aliases.put("tp", "teleport");
        aliases.put("config", "configure");
        aliases.put("mod", "modifier");
        aliases.put("dest", "destinations");
        aliases.put("info", "information");
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


        // Start the Task
        task = getServer().getScheduler().runTaskTimer(this, new PortalEffect(this), 20L, 40L);


        // Try to update.
        updateMessage = null;
        if ( autoUpdate != 0 )
            startUpdater(autoUpdate);

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
    // Automatic Updates
    ///////////////////////////////////////////////////////////////////////////

    public void startUpdater(final byte autoUpdate) {
        Updater.UpdateType mode = ( autoUpdate == 1 ) ? Updater.UpdateType.NO_DOWNLOAD : Updater.UpdateType.DEFAULT;
        updater = new Updater(this, "abyss", getFile(), mode, false, new UpdaterCallback());
    }


    private class UpdaterCallback implements Runnable {
        public void run() {
            // Get the current status.
            Updater.UpdateResult result = updater.getResult(false);

            ColorBuilder out = null;

            // Set a message if we've got something important.
            if ( result == Updater.UpdateResult.DOWNLOADING ) {
                out = t("Downloading an update for Abyss...");

            } else if ( result == Updater.UpdateResult.UPDATE_AVAILABLE ) {
                out = t("An update for Abyss is available: ").append(updater.getLatestVersionString()).
                        gray(" (").reset(ParseUtils.humanReadableByteCount(updater.getFileSize(), true)).
                        gray(" )");

            } else if ( result == Updater.UpdateResult.SUCCESS ) {
                out = t("An update for Abyss has been downloaded. Restart the server to apply it.");

            } else if ( result == Updater.UpdateResult.NO_UPDATE && updateCheckers.size() > 0 ) {
                String message = "There are no updates available for Abyss.";
                for(final String name: updateCheckers) {
                    Player player = getServer().getPlayer(name);
                    if ( player != null && player.hasPermission("abyss.update") )
                        player.sendMessage(message);
                }

                updateCheckers.clear();
            }

            if ( out != null ) {
                updateMessage = out.toString().split("\n");
                for(final Player player: getServer().getOnlinePlayers())
                    if ( player.hasPermission("abyss.update") )
                        player.sendMessage(updateMessage);

                // Show it to the console too.
                getServer().getConsoleSender().sendMessage(updateMessage);
            }
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    // Configuration Storage
    ///////////////////////////////////////////////////////////////////////////

    public void configure() {
        // Reload the configuration from disk.
        reloadConfig();

        final FileConfiguration config = getConfig();
        final Logger log = getLogger();

        // Copy defaults so we can save them if missing.
        config.options().copyDefaults(true);

        // Entity Type Whitelist
        entityTypeWhitelist = new HashSet<EntityType>();
        if ( config.contains("entity-type-whitelist") ) {
            for(final String string: config.getStringList("entity-type-whitelist")) {
                final EntityType en = ParseUtils.matchEntityType(string);
                if ( en != null ) {
                    entityTypeWhitelist.add(en);
                } else
                    log.warning("Invalid entity type in whitelist: " + string);
            }
        }

        // Auto Update
        String string = config.getString("auto-update", "true");
        Byte result = ParseUtils.matchUpdate(string);
        if ( result == null ) {
            autoUpdate = 2;
            log.warning("Invalid auto-update. Using: true");
        } else {
            autoUpdate = result;
        }


        // Portal Wand
        wandName = config.getString("wand-name", "Portal Wand");
        wandRange = config.getInt("wand-range", 256);

        string = config.getString("wand-material");
        wandMaterial = Material.matchMaterial(string);
        if ( wandMaterial == null ) {
            wandMaterial = Material.STICK;
            log.warning("Invalid wand-material. Using: STICK");
        }


        // Default Network
        Object network = config.get("network-key");
        defaultNetwork = null;
        if (network instanceof ItemStack)
            defaultNetwork = (ItemStack) network;
        else if (network instanceof String)
            defaultNetwork = ParseUtils.matchItem((String) network);

        if ( defaultNetwork == null ) {
            defaultNetwork = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            log.warning("Invalid network-key. Using: SKULL ITEM:3");
        }

        defaultColor = ParseUtils.matchColor(config.getString("network-color"));
        if ( defaultColor == null ) {
            defaultColor = DyeColor.WHITE;
            log.warning("Invalid network-color. Using: WHITE");
        }


        // Portal Dimensions
        minimumDepth = (short) config.getInt("minimum-depth", 2);
        minimumSize = (short) config.getInt("minimum-size", 2);
        maximumSize = (short) config.getInt("maximum-size", 4);

        // Wrap this in a contains check so we don't set it if we don't have to.
        if ( config.contains("maximum-modifiers") )
            maximumMods = (short) config.getInt("maximum-modifiers", maximumSize);
        else
            maximumMods = maximumSize;

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
        minimumVelocity = config.getDouble("minimum-velocity", 0.15);
        maximumVelocity = config.getDouble("maximum-velocity", 10);
        cooldownTicks = config.getLong("cooldown-ticks", 40);
        useWorldGuard = config.getBoolean("use-worldguard", true);

        // Rails
        smartRails = config.getBoolean("smart-rails", true);

        // Frame Configuration
        frameSingleMaterial = config.getBoolean("single-material", true);
        frameCornerDepth = config.getInt("corner-depth", 2);

        frameMaterials = new HashSet<Material>();
        List<String> mats = config.isList("frame-materials") ? config.getStringList("frame-materials") : ImmutableList.of(config.getString("frame-materials", "occluding"));
        if ( mats != null )
            for(String mat: mats) {
                boolean negative = mat.length() > 1 && mat.charAt(0) == '-';
                if ( negative )
                    mat = mat.substring(1);

                if ( mat.equalsIgnoreCase("solid") || mat.equalsIgnoreCase("occluding") ) {
                    boolean occluding = ! mat.equalsIgnoreCase("solid");
                    for(final Material m: Material.values()) {
                        if ( occluding ? m.isOccluding() : m.isSolid() ) {
                            if ( negative )
                                frameMaterials.remove(m);
                            else
                                frameMaterials.add(m);
                        }
                    }

                    continue;
                }

                final Material m = Material.matchMaterial(mat);
                if ( m == null ) {
                    log.warning("Invalid material in frame-materials: " + mat);
                    continue;
                }

                if ( negative )
                    frameMaterials.remove(m);
                else
                    frameMaterials.add(m);
            }

        if ( frameMaterials.size() == 0 ) {
            log.warning("frame-materials is an empty list. Using: occluding");
            for(final Material mat: Material.values())
                if ( mat.isOccluding() )
                    frameMaterials.add(mat);
        }


        // Range Configuration
        limitDistance = config.getBoolean("limit-distance", true);
        rangeMultiplier = config.getDouble("depth-multiplier", 25);
        baseRange = config.getDouble("base-range", 50);


        // Effect Configuration
        usePortalEffect = config.getBoolean("use-portal-effect", true);
        useStaticEffect = config.getBoolean("use-static-effect", true);
        staticEffectCentered = config.getBoolean("static-effect-centered", false);
        staticEffectFullHeight = config.getBoolean("static-effect-full-height", true);

        if ( config.isConfigurationSection("portal-effect") ) {
            ConfigurationSection c = config.getConfigurationSection("portal-effect");
            portalEffect = ParseUtils.matchEffect(c.getString("name"));
            if ( portalEffect == null ) {
                log.warning("Invalid portal-effect. Defaulting to: MOBSPAWNER_FLAMES");
                portalEffect = Effect.MOBSPAWNER_FLAMES;
                portalEffectData = 0;
            } else {
                portalEffectData = c.getInt("data", 0);
            }
        } else {
            portalEffect = ParseUtils.matchEffect(config.getString("portal-effect"));
            if ( portalEffect == null ) {
                if ( config.contains("portal-effect") )
                    log.warning("Invalid portal-effect. Defaulting to: MOBSPAWNER_FLAMES");
                portalEffect = Effect.MOBSPAWNER_FLAMES;
            }
            portalEffectData = 0;
        }

        if ( config.isConfigurationSection("static-effect") ) {
            ConfigurationSection c = config.getConfigurationSection("static-effect");
            staticEffect = ParseUtils.matchEffect(c.getString("name"));
            if ( staticEffect == null ) {
                log.warning("Invalid static-effect. Defaulting to portal-effect.");
                staticEffect = portalEffect;
                staticEffectData = portalEffectData;
            } else {
                staticEffectData = c.getInt("data", 0);
            }
        } else {
            staticEffect = ParseUtils.matchEffect(config.getString("static-effect"));
            if ( staticEffect == null ) {
                if ( config.contains("static-effect") )
                    log.warning("Invalid static-effect. Defaulting to portal-effect.");
                staticEffect = portalEffect;
                staticEffectData = portalEffectData;
            }
        }

        if ( config.isConfigurationSection("portal-sound") ) {
            ConfigurationSection c = config.getConfigurationSection("portal-sound");
            portalSound = ParseUtils.matchSound(c.getString("name"));
            if ( portalSound == null ) {
                log.warning("Invalid portal-sound. Defaulting to: ENDERMAN_TELEPORT");
                portalSound = Sound.ENDERMAN_TELEPORT;
                portalSoundPitch = 1;
                portalSoundVolume = 1;
            } else {
                portalSoundPitch = c.getInt("pitch", 1);
                portalSoundVolume = c.getInt("volume", 1);
            }
        } else {
            portalSound = ParseUtils.matchSound(config.getString("portal-sound"));
            if ( portalSound == null ) {
                if ( config.contains("portal-sound") )
                    log.warning("Invalid portal-sound. Defaulting to: ENDERMAN_TELEPORT");
                portalSound = Sound.ENDERMAN_TELEPORT;
            }
            portalSoundPitch = 1;
            portalSoundVolume = 1;
        }

        if ( config.isConfigurationSection("static-sound") ) {
            ConfigurationSection c = config.getConfigurationSection("static-sound");
            staticSound = ParseUtils.matchSound(c.getString("name"));
            if ( staticSound == null ) {
                log.warning("Invalid static-sound. Defaulting to portal-sound.");
                staticSound = portalSound;
                staticSoundPitch = portalSoundPitch;
                staticSoundVolume = portalSoundVolume;
            } else {
                staticSoundPitch = c.getInt("pitch", 1);
                staticSoundVolume = c.getInt("volume", 1);
            }
        } else {
            staticSound = ParseUtils.matchSound(config.getString("static-sound"));
            if ( staticSound == null ) {
                if ( config.contains("static-sound") )
                    log.warning("Invalid static-sound. Defaulting to portal-sound.");
                staticSound = portalSound;
                staticSoundPitch = portalSoundPitch;
                staticSoundVolume = portalSoundVolume;
            } else {
                staticSoundVolume = 1;
                staticSoundPitch = 1;
            }
        }

        // Save with the defaults we just set. This destroys formatting, but I
        // don't care all that much.
        saveConfig();
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
        if ( item == null || item.getType() != wandMaterial )
            return null;

        final ItemMeta im = item.getItemMeta();
        if ( ! im.hasDisplayName() )
            return null;

        String key = im.getDisplayName();
        final int length = key.length();

        // Strip color codes from the name for easier processing.
        key = ChatColor.stripColor(key);

        // Ensure that the string has been shortened, and that it now starts
        // with the wand name.
        if ( length == key.length() || !key.startsWith(wandName) )
            return null;

        int index = key.lastIndexOf("[");
        if (index == -1)
            return null;

        int ind2 = key.indexOf("]", index);
        if (ind2 == -1)
            return null;

        return key.substring(index + 1, ind2).toLowerCase();
    }


    ///////////////////////////////////////////////////////////////////////////
    // Commands
    ///////////////////////////////////////////////////////////////////////////

    public String getABCommand(final String label, final List<String> args, boolean complete) {
        String key = null;
        String help = null;

        // If we need help, move that *after* the sub-command.
        if ( label.equals("abyss") && args.size() > 0 && args.get(0).equalsIgnoreCase("help") ) {
            if ( args.size() > 1 ) {
                help = args.remove(0);
            } else
                return null;
        }

        if ( ! label.equals("abyss") )
            key = label.substring(2);
        else if ( args.size() > 0 )
            key = args.remove(0).toLowerCase();

        if ( key == null ) {
            if ( help != null )
                args.add(0, help);
            return null;
        }

        if ( aliases.containsKey(key) )
            key = aliases.get(key);

        // We don't want to tab-complete if this isn't the last value.
        if ( complete && args.size() != 0 )
            complete = false;

        if ( ! complete && ! commands.containsKey(key) ) {
            // If we don't have an exact match, try iterating the alphabetized
            // list of all commands for a partial match.
            final List<String> possible = new ArrayList<String>(commands.keySet());
            Collections.sort(possible);
            for(final String cmdName: possible)
                if ( cmdName.startsWith(key) ) {
                    key = cmdName;
                    break;
                }
        }

        if ( help != null )
            args.add(0, help);

        return key;
    }


    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] arguments) {
        Validate.notNull(sender, "sender cannot be null");
        Validate.notNull(command, "command cannot be null");
        Validate.notNull(label, "label cannot be null");
        Validate.notNull(arguments, "arguments cannot be null");

        // Convert the arguments to a list with no empty strings.
        final ArrayList<String> args = new ArrayList<String>(Arrays.asList(arguments));
        for(Iterator<String> it = args.iterator(); it.hasNext(); )
            if ( it.next().trim().length() == 0 )
                it.remove();

        // Determine the sub-command.
        final String cmdkey = getABCommand(label, args, false);
        if ( cmdkey == null )
            return false;

        // Try getting a command from that key.
        ABCommand cmd = commands.get(cmdkey);
        if ( cmd == null ) {
            t().red("No such sub-command: ").reset(cmdkey).send(sender);
            return true;
        }

        // Make sure we've got permission to do this.
        final PluginManager pm = getServer().getPluginManager();
        final Permission perm = pm.getPermission("abyss.command." + cmdkey);
        if ( perm != null && !sender.hasPermission(perm) ) {
            t().red("Permission Denied").send(sender);
            return true;
        }

        // Use the command executor.
        return cmd.onCommand(sender, command, label, args);
    }

    public List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] arguments) {
        Validate.notNull(sender, "sender cannot be null");
        Validate.notNull(command, "command cannot be null");
        Validate.notNull(label, "label cannot be null");
        Validate.notNull(arguments, "arguments cannot be null");

        // Convert the arguments to a list.
        final ArrayList<String> args = new ArrayList<String>(Arrays.asList(arguments));

        // Determine the sub-command.
        final String cmdkey = getABCommand(label, args, true);
        if ( cmdkey == null ) {
            // No sub-command? Auto-complete it!
            List<String> out = new ArrayList<String>(commands.keySet());
            out.add("help");
            return out;
        }

        // Try getting a command.
        ABCommand cmd = commands.get(cmdkey);
        if ( cmd == null ) {
            List<String> out = new ArrayList<String>(commands.keySet());
            for(final Iterator<String> it = out.iterator(); it.hasNext(); )
                if ( ! it.next().startsWith(cmdkey) )
                    it.remove();

            if ( "help".startsWith(cmdkey) )
                out.add("help");
            return out;
        }

        // Pass it on.
        return cmd.onTabComplete(sender, command, label, args);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Portal Placement Code
    ///////////////////////////////////////////////////////////////////////////

    public int getIdFor(final String key) {
        final int id = (lastId.containsKey(key) ? lastId.get(key) : 0) + 1;
        lastId.put(key, id);
        return id;
    }


    public HashSet<Material> validLayer(final Location location, final short depth, final int size) {
        /* Determine if there's a valid layer at the location. This is assuming
           that the provided location is the lowest X,Z pair of the inside of
           the portal. */

        if ( location == null )
            return null;

        // Check the portal blocks.
        for(double x=0; x < size; x++) {
            for(double z=0; z < size; z++) {
                if (!validLiquid(location.clone().add(x, 0, z)))
                    return null;
            }
        }

        // Make a hashset to store our materials in.
        HashSet<Material> out = new HashSet<Material>();

        // Now, check the frame blocks.
        final double min, max;
        if ( depth < frameCornerDepth ) {
            min = -1; max = size + 1;
        } else {
            min = 0; max = size;
        }

        for(double x = min; x < max; x++) {
            Block block = location.clone().add(x, 0, -1).getBlock();
            if ( !validFrameBlock(block) )
                return null;

            out.add(block.getType());

            block = location.clone().add(x, 0, size).getBlock();
            if ( !validFrameBlock(block) )
                return null;

            out.add(block.getType());
        }

        for(double z=0; z < size; z++) {
            Block block = location.clone().add(-1, 0, z).getBlock();
            if ( !validFrameBlock(block) )
                return null;

            out.add(block.getType());

            block = location.clone().add(size, 0, z).getBlock();
            if ( !validFrameBlock(block) )
                return null;

            out.add(block.getType());
        }

        // Limit ourselves to one material?
        if ( frameSingleMaterial && out.size() > 1 )
            return null;

        return out;
    }


    public boolean validFrameBlock(final Location location) {
        return (location != null) && validFrameBlock(location.getBlock());
    }

    public boolean validFrameBlock(final Block block) {
        return block != null && frameMaterials.contains(block.getType());
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
        HashSet<Material> materials = new HashSet<Material>();

        while(loc.getBlockY() > 0) {
            HashSet<Material> out = validLayer(loc, depth, size);
            if ( out == null )
                break;

            materials.addAll(out);
            if ( frameSingleMaterial && materials.size() > 1 )
                break;

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
        final int x = loc.getBlockX();
        final int z = loc.getBlockZ();
        final int y = loc.getBlockY();

        // Get all the portals near this block.
        final ArrayList<ABPortal> portals = manager.getNear(loc);
        if ( portals == null )
            return empty;

        // Iterate through all the portals, making sure we can do this.
        for(final ABPortal portal: portals) {
            final int py = portal.getLocation().getBlockY();
            if ( y <= py && y > py - frameCornerDepth )
                // Protect the important layers of the frame.
                return null;

            final Location min = portal.getMinimumLocation();
            final Location max = portal.getMaximumLocation();

            if ( y <= py && y > py - 2 && !((x == min.getBlockX() || x == max.getBlockX()) && (z == min.getBlockZ() || z == max.getBlockZ())) )
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
            if ( time != null && entity.getWorld().getFullTime() < time ) {
                // Make sure the cooldown period only works once.
                entityLastPortal.remove(eid);
                entityLastTime.remove(eid);
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
                final Entity ent = doTeleport(entity, portal, destination, to, delta);
                if ( ent != null )
                    return (ent.equals(entity)) ? ent.getLocation() : null;
            }
        }

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

    @SuppressWarnings("unchecked")
    public Entity doTeleport(Entity entity, final ABPortal from, final ABPortal to, final Location pos, Vector delta) {
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
                // If there's any unaccounted for range, stop right now.
                if ( to.checkRange(from) > 0 )
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

            // Useful for later flipping!
            boolean x_major = Math.abs(delta.getX()) > Math.abs(delta.getZ());

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

            if ( rotation != 0 ) {
                final double rads = Math.toRadians(rotation * 90);
                final double c = Math.cos(rads), s = Math.sin(rads);

                offset.setX(off_x * c - off_z * s);
                offset.setZ(off_x * s + off_z * c);

                off_x = offset.getX();
                off_z = offset.getZ();
            }

            // Flip the offset.
            if ( rotation == 0 || rotation == 2 || rotation == -2 )
                x_major = ! x_major;

            if ( x_major )
                offset.setX(-off_x);
            else
                offset.setZ(-off_z);

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
            BlockFace bf = BlockUtils.toBlockFace(delta.clone().setY(0));
            if ( bf == BlockFace.SELF )
                bf = BlockUtils.toBlockFace(to.getRotation()).getOppositeFace();

            Block block = dest.getBlock().getRelative(bf);
            while ( to.isNearPortal(block.getLocation()) ) {
                if ( isRail(block.getType()) ) {
                    // We've got a rail! Move to it and be done.
                    dest = block.getLocation();
                    delta = BlockUtils.toVector(bf, delta.length());
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

        // Make sure the area we're teleporting to is loaded.
        if ( ! dest.getChunk().isLoaded() )
            dest.getChunk().load();

        // If the entity has a passenger, we have to teleport them separately.
        Entity passenger = null;
        if ( !entity.isEmpty() ) {
            passenger = entity.getPassenger();

            // If we can't eject the passenger, abort.
            if ( ! entity.eject() )
                return null;

            // If we can't teleport the passenger, abort.
            Entity ent = doTeleport(passenger, from, to, passenger.getLocation(), delta);
            if ( ent == null ) {
                // Put the passenger back first.
                entity.setPassenger(passenger);
                return null;
            } else {
                passenger = ent;
            }
        }

        // Teleport the entity. This is the last point that can return null.
        // This has a chance of cloning the entity and returning a new one,
        // because Minecraft entities are weird.
        Entity after_port = EntityUtils.teleport(entity, dest, PlayerTeleportEvent.TeleportCause.PLUGIN, entityTypeWhitelist);
        if ( after_port == null ) {
            // If we can't go... too bad for the passenger!
            return null;
        } else {
            // Check to see if the old entity is in the cooldown list, and
            // remove it to keep things tidy.
            final UUID uid = entity.getUniqueId();
            if ( entityLastPortal.containsKey(uid) )
                entityLastPortal.remove(uid);

            if ( entityLastTime.containsKey(uid) )
                entityLastTime.remove(uid);

            entity = after_port;
        }

        // Set the entity's velocity and fall distance.
        entity.setVelocity(delta);
        entity.setFallDistance(0);

        // Recover our passenger, after a slight delay.
        if ( passenger != null ) {
            final Entity p = passenger;
            final Entity e = entity;
            getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                @Override
                public void run() {
                    e.setPassenger(p);
                }
            }, 5L);
        }

        // Play an effect.
        final World pw = pos.getWorld();
        final World dw = dest.getWorld();

        pw.playEffect(pos, portalEffect, portalEffectData);
        dw.playEffect(dest, portalEffect, portalEffectData);
        pw.playSound(pos, portalSound, portalSoundVolume, portalSoundPitch);
        dw.playSound(dest, portalSound, portalSoundVolume, portalSoundPitch);

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
        if ( cooldownTicks > 0 ) {
            final UUID eid = entity.getUniqueId();
            entityLastPortal.put(eid, to.uid);
            entityLastTime.put(eid, entity.getWorld().getFullTime() + cooldownTicks);
        }

        // Return the destination.
        return entity;
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
                int min_y = portal.getMinimumLocation().getBlockY();
                final double size = ((double) portal.getSize() / 2) - 0.5;

                if ( ! plugin.staticEffectFullHeight ) {
                    center.subtract(0, (portal.depth / 2), 0);
                    min_y = center.getBlockY() - 1;
                }

                while ( center.getBlockY() > min_y ) {
                    center.subtract(0, 2, 0);
                    final Location l;
                    if ( plugin.staticEffectCentered ) {
                        l = center;
                    } else {
                        l = center.clone().add((random.nextDouble() - 0.5) * size, 1, (random.nextDouble() - 0.5) * size);
                    }

                    w.playEffect(l, plugin.staticEffect, plugin.staticEffectData);
                }

                portal.effectSound--;
                if (portal.effectSound > 0)
                    continue;

                portal.effectSound = (short) (random.nextInt(5) + 6);
                w.playSound(portal.getCenter(), plugin.staticSound, plugin.staticSoundVolume, plugin.staticSoundPitch);
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

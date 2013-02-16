## Version 7: February 16, 2013 ##

 * **Note:** This is a release candidate, baring any bug reports from users.
 * [Source Code][v7]

### Added ###
 * Metrics using MCStats.
 * ``use-metrics`` configuration option for enabling metric collection, defaults to true.

### Fixed ###
 * Bug when creating irregularly sized portals when the two size ranges don't match.
 * The configuration descriptions now use WikiCreole, ensuring that a table of contents is available on dev.bukkit.org.

&nbsp;


## Version 6: February 14, 2013 ##

 * [Source Code][v6]

### Added ###
 * Portals may be any dimensions and not simply square.
 * Abyss can now load world scale and blacklist information from Multiverse.
 * Emerald Block Portal Modifier, which uses Vault to interact with economy mods.
 * Bottle 'o Enchanting Portal Modifier, which lets you make portals cost experience to use, or require a minimum experience level.
 * ``require-square`` configuration option for enforcing portal square-ness, defaults to true.
 * ``use-multiverse`` configuration option for enabling Multiverse support, defaults to true.
 * ``slime-ball-strength`` configuration option for modifying slime ball strength, defaults to 1.0.

### Fixed ###
 * Portals now use SafeLocations, which can safely exist even when the world they're within isn't loaded.
 * Exceptions during startup are now trapped, ensuring problems with the plugins file won't prevent the plugin from loading.
 * ``use-static-effect`` actually does something now.

&nbsp;


## Version 5: February 13, 2013 ##

 * [Source Code][v5]

### Added ###
 * Rails will now automatically connect to the edge of portals, making it easier to make compact rail systems using portals.
 * ``smart-rails`` configuration option for enable new rail logic, defaults to true.

### Fixed ###
 * Configuration not saving new values into user's configuration files.
 * Fixed the piston extend event not working properly with the ``corner-depth`` configuration option.

&nbsp;


## Version 4: February 12, 2013 ##

 * [Source Code][v4]

### Added ###
 * Auto-updater that fetches the latest version from dev.bukkit.org.
 * Update Command for manually starting an update.
 * ``single-material`` configuration option for portal frames, defaults to false.
 * ``corner-depth`` configuration option for portal frames, defaults to 2.
 * ``frame-materials`` configuration option for portal frames. defaults to [occluding, ice]

### Fixed ###
 * Removed old, unused code from ABPortal.

&nbsp;


## Version 3: February 11, 2013 ##

 * [Source Code][v3]

### Added ###
 * Tab completion for many commands.
 * ``wand-range`` configuration option, defaults to 256 blocks.
 * ``abyss.command.teleport_other`` permission for teleporting other players with the Teleport command.
 * Portal modifier indices to the Information command's output.
 * The Utility command for moving a portal modifier's Utility Block.

### Fixed ###
 * Entity position upon teleporting is now properly offset for the new portal rotation.
 * The Modifier command can now be used as a regular command.
 * The Portal Wand can be used beyond the range a player may normally click a block.
 * Players must have the ``abyss.use`` permission to modify portals.
 * Right-clicking with a Portal Wand takes precedent over right-clicking a portal's item frames.

### API ###

 * Added: ``getNear(Location location, double range)`` to get all portals in a 2*range cube around location.
 * Fixed: Portal Wand logic is now in the ABCommand class where it belongs.

&nbsp;


## Version 2: February 10, 2013 ##

 * Initial release on dev.bukkit.org.

### Added ###
 * A command system.
 * Entities can now travel to other worlds through portals.
 * ``entity-type-whitelist`` configuration option, for controlling which entities
   can travel through portals to other worlds.

&nbsp;


## Version 1: Initial release. ##

 * No changes.


[v7]: https://github.com/stendec/Abyss/tree/v7
[v6]: https://github.com/stendec/Abyss/tree/v6
[v5]: https://github.com/stendec/Abyss/tree/v5
[v4]: https://github.com/stendec/Abyss/tree/v4
[v3]: https://github.com/stendec/Abyss/tree/v3

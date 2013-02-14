## Version 5: February 13, 2013 ##

### Added ###
 * Rails will now automatically connect to the edge of portals, making it easier to make compact rail systems using portals.
 * ``smart-rails`` configuration option for enable new rail logic, defaults to true.

### Fixed ###
 * Configuration not saving new values into user's configuration files.
 * Fixed the piston extend event not working properly with the ``corner-depth`` configuration option.


## Version 4: February 12, 2013 ##

### Added ###
 * Auto-updater that fetches the latest version from dev.bukkit.org.
 * Update Command for manually starting an update.
 * ``single-material`` configuration option for portal frames, defaults to false.
 * ``corner-depth`` configuration option for portal frames, defaults to 2.
 * ``frame-materials`` configuration option for portal frames. defaults to [occluding, ice]

### Fixed ###
 * Removed old, unused code from ABPortal.


## Version 3: February 11, 2013 ##

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


## Version 2: Initial release on dev.bukkit.org. ##

### Added ###
 * A command system.
 * Entities can now travel to other worlds through portals.
 * ``entity-type-whitelist`` configuration option, for controlling which entities
   can travel through portals to other worlds.


## Version 1: Initial release. ##

 * No changes.
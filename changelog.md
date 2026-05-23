# Quark 4.1-480 For Neoforge 1.21.1

# Fixes
- *Properly* Fixed #5537: Stripped wood blocks (Azalea, Ashen, and Trumpet) have wrong tag (thanks copygirl) 
- Fixed #5437: Netherite repair not hidden properly with Diamond Repair enabled in JEI
- Fixed #5538: can't pet players even when added to the allowlist
- Fixed #5544: Can only craft chests using vanilla oak planks
- Fixed #5553: Neoforge 1.21.1 Custom Pathfinder Quill Color doesn't seem to work
- Fixed #5560: "Improved Tooltip" Module's Enchanted Book Tooltips is hardcoded
- Fixed #5564: [479 and more] A .cache folder is packed into the JAR
  - this fix decreases jar size by at least 10 KB
- Fixed #5570: [1.21.1] Matrix Enchanting "Info" recipe is still present when Matrix Enchanting is disabled with EMI+JEI installed.
- Fixed the flamarang smithing recipe being enabled if the pickarang module or Enable Flamerang config is disabled
- Fix inconsistent sorting for items with the same items with different components
- Fix a bug relating to sorting not showing on held shulker boxes
- Fixed the Bent Recipes cookie recipe cheating you out of 7 cookies
- Fix crates not saving properly when they contained more than 256 stacks (thanks Ocelot5836)
- Fixed certain hints being added for disabled content

# Changes
- Quark now requires at least NeoForge 21.1.230
- You can now craft vanilla chests with modded woods if they have no variant chest. 
  - This was a longstanding (3+ years) issue Siuolplex found a cursed workaround for!
- Trowel -> Iron Nugget smelting recipe now checks if the Trowel module is enabled
- Updated Ukrainian translation (StarmanMine142)
- Updated Japanese translation (Abbage230)
- Improved Tooltips' Enchanting Tooltips bow checks for the Stored Enchantments item component instead of Enchanted Books/Ancient Tomes
- Add some variance to the fairy rings
- The Blossom and Ancient Trees modules now contain descriptions with their corresponding in-game names (the items had their names changed previously, but the module and internal names did not)
- The Pathfinder Maps' custom maps config comment now says to use int colors instead of hex colors
  - this feature seems to have never worked as the previous comment in the config file described

# Additions
- Added a Trowel -> Iron Nugget blasting recipe

## Code Related
- Add some fallback code for AttributeTooltips.
- Fixed a bug in our unused api
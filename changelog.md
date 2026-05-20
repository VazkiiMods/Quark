# Quark 4.1-480 For Neoforge 1.21.1

# Fixes
- *Properly* Fixed #5537: Stripped wood blocks (Azalea, Ashen, and Trumpet) have wrong tag (thanks copygirl) 
- Fixed #5544: Can only craft chests using vanilla oak planks
- Fixed #5560: "Improved Tooltip" Module's Enchanted Book Tooltips is hardcoded
- Fixed #5564: [479 and more] A .cache folder is packed into the JAR
  - this fix decreases jar size by at least 10 KB
- Fixed the flamarang smithing recipe being enabled if the pickarang module or Enable Flamerang config is disabled
- Fix inconsistent sorting for items with the same
- Fix a bug relating to sorting not showing on held shulker boxes
- Fixed the Bent Recipes cookie recipe cheating you out of 7 cookies

# Changes
- You can now craft vanilla chests with modded woods if they have no variant chest. 
  - This was a longstanding (3+ years) issue siuolplex found a cursed workaround for
- Trowel -> Iron Nugget smelting recipe now checks if the Trowel module is enabled
- Updated Ukrainian translation (StarmanMine142)
- Updated Japanese translation (Abbage230)
- Improved Tooltips' Enchanting Tooltips bow checks for the Stored Enchantments item component instead of Enchanted Books/Ancient Tomes
- Add some variance to the fairy rings

# Additions
- Added a Trowel -> Iron Nugget blasting recipe

# Code Related
- Add some fallback code for AttributeTooltips.
- Fixed a bug in our unused api

# Misc
[wip note, might not include this on release]
Due to an influx of poor quality Pull Requests, the Quark GitHub repository now contains an AI policy; see CLAUDE.md or AGENTS.md.

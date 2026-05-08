# Quark 4.1-479 For Neoforge 1.21.1

Fixed the trowel thanks to LeoBeliik, and also snuck in an experimental module.

# Fixes
- Fixed #5513 (and all related issues): Trowel Crash: Item must not be minecraft:air
- Fixed #5540: Redstone Randomizer Makes Incorrect Place and Break sounds
- Fixed #5541: Automatic Tool Restock can swap the trowel when it shouldn't
- Fixed #5545: Ladders missing #c:ladders item and block tag
- Fixed #5547: Jasper, Limestone, Shale, Myalite, Permafrost, and their polished varients, along with polished calcite, have incorrect stone tag

# Changes
- Added a Conditional Tags datapack for Variant Ladders
- Polished stone blocks are no longer under the #c:stones item/block tags

# Additions
- Added Vanilla Stone Clusters module, disabled by default, under the Experimental category. This is an attempt to restore part of the pre-1.18 version of Quark's Big Stone Clusters, where Granite, Diorite, and Andesite only spawn in biome-dependent clusters.
  - If this module is enabled, a datapack (quark_vdo_vanilla_stone_clusters) will be loaded that sets the `ore_granite` etc. ConfiguredFeatures to size 0. 
  - This module uses the BigStoneClusters module's blocksToReplace config.
  - This is under Experimental because I don't know if the datapack will cause issues, and the default biomes probably need to be changed, and if enabled, triggers the "Experimental Settings" screen when loading a world and I don't know why!
  - Despite this module's name, Calcite is still generated from the BigStoneClusters module.
  - Please try it out and let me (Partonetrain) know what you think of this on the Forum or on Discord!
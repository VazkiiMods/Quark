# Quark 4.1-481 For Neoforge 1.21.1

# Fixes
- Fixed #5572: Foxhound Faces are swapped from its tamed variant
- Fixed #5577: Undead Mobs do not have Undead Properties 
- Fixed #5579: Chains should be detected differently
- Fixed #5582: You can place things you shouldn't with scaffolding replacement
- Fixed #5584: (1.21.1) "Pat the dogs" config options don't work for most modded entities

# Changes
- Replace Scaffolding now works with modded scaffold blocks (i.e. Create's Copper Scaffolding). 
  - Placing multiple different types of scaffolding blocks in a pillar and trying to replace any scaffolds will only replace the type of scaffolding that was right-clicked.
- Multi-block blocks like doors can no longer replace scaffolding
- Wood Posts will now connect to Hedges immediately above them
- Chains Connect Blocks now works with modded chains
- Tamed Foxhounds now have a maximum health of 30. This is to give them a slight disadvantage to normal wolves

# Additions
- Rotation Lock now works with Replace Scaffolding; blocks will be replaced according to your rotation lock
- Added a `quark:cannot_replace_scaffolding` block tag for blocks that shouldn't replace scaffolding
- Added the `quark:pathfinders_quill` loot function, which sets items to 

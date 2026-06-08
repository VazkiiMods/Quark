# Quark 4.1-481 For Neoforge 1.21.1

# Fixes
- Fix #5577: Undead Mobs do not have Undead Properties 
- Fix #5582: You can place things you shouldn't with scaffolding replacement

# Changes
- Replace Scaffolding now works with modded scaffold blocks (i.e. Create's Copper Scaffolding). 
  - Placing multiple different types of scaffolding blocks in a pillar and trying to replace any scaffolds will only replace the type of scaffolding that was right-clicked.
- Multi-block blocks like doors can no longer replace scaffolding

# Additions
- Rotation Lock now works with Replace Scaffolding; blocks will be replaced according to your rotation lock
- Added a `quark:cannot_replace_scaffolding` block tag for blocks that shouldn't replace scaffolding

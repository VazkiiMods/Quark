# Quark 4.1-477 For Neoforge 1.21.1

## Fixes
- [WIP not working] Fix #5500: Variant chests still visible in recipes even when turned off.
- Fix #5504: Disabling Utility Recipes deletes recipes for stone tools
- [WIP, not all modules fixed yet] Fix 5496: Blocks & items are not compostable

## Changes
- [WIP not working] Variant Chests are now no longer loaded into the `c:chests` item tag if the module is disabled ([since Neoforge changes the trapped chest recipe](https://github.com/neoforged/NeoForge/blob/1.21.11/src/generated/resources/data/minecraft/recipe/trapped_chest.json)) 
    - Similarly,  `lootr:chests` and `lootr:trapped_chests`
- [WIP] `quark:glow_shroom_feedables` item tag no longer loads if Glimmering Weald is disabled
- Variant Chests, Variant Bookshelves, and Variant Ladders modules now have anti-overlap with Carved Wood and will disable themselves if that mod is installed
## Additions

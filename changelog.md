# Quark 4.1-477 For Neoforge 1.21.1

## Fixes
- Fix #5500: Variant chests still visible in recipes even when turned off.
- Fix #5504: Disabling Utility Recipes deletes recipes for stone tools

## Changes
- Variant Chests are now no longer loaded into the `c:chests` item tag if the module is disabled ([since Neoforge changes the trapped chest recipe](https://github.com/neoforged/NeoForge/blob/1.21.11/src/generated/resources/data/minecraft/recipe/trapped_chest.json)) 

## Additions

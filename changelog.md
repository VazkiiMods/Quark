# Quark 4.1-477 For Neoforge 1.21.1

Fixes and a couple of changes to improve the out-of-the-box experience.

## Fixes
- Fix #5504: Disabling Utility Recipes deletes recipes for stone tools
- Fix #5496: Blocks & items are not compostable
- Fix #5507: Potential incorrect tagging on framed glass blocks
- Fix #5215: Leaving "Back" key unbound causes all unknown keys to trigger it.
- Fix #5500: Variant chests still visible in recipes even when turned off.

## Changes
- Variant Chests, Variant Bookshelves, and Variant Ladders modules now have anti-overlap with Carved Wood and will disable themselves if that mod is installed
- Updated Simplified Chinese translation (qznfbnj)
- Updated Japanese translation (Abbage230)
- Updated Russian translation (AstardGrimoire)
- Updated German translation (GulutGames)
- Updated various translation files using unused Twitter and Reddit translation keys, replaced with Forum and Bluesky keys. 
- 
## Additions
- Added a system to add tags to items/blocks depending on config via *additional* datapacks. Unfortunately this is a kind of band-aid fix that doesn't solve the root issue of there being no way to conditionally add items to tags. 
    - Added Conditional Tag datapacks for: Variant Chests, Variant Bookshelves, and Framed Glass
    - This system does not account for multi-condition items, for example, Ashen Chests will still be in the `c:chests` tag if Variant Chests is enabled but Ancient Wood is not
    - More tag contents may be moved to additional conditional datapacks as needed for future updates. We recommend that modpack makers check if disabled content being in present tags creates issues (and removing said items from the tag with CraftTweaker or similar) instead of relying on the config.
    - This will end up bloating the `/datapack list` command, but that's a small price to pay to fix certain essential tags containing disabled content.
# Quark 4.1-488 For Neoforge 1.21.1

Fixes, changes to mobs and AI goals, and a nerf to Forgotten Hat.

# Fixes
- Fixed #5683: Librarian villager ancient tome trade is not awarded when using enchanted books created in anvil (thanks midori-0)
- Partially fixed #5680: foxhounds prefer cobblestone furnace 
- Fixed #5687: quark:PetTime not being set on Foxhound 
- Partially fixed #5688: Dogs Want Love option lobotomizes foxhounds/wolves
- Fixed wild and tamed foxhounds tilting their head when you held a bone. 
- Fixed wild foxhounds tilting their head when you held meat, as well as them letting you feed them (it didn't do anything if you did).

# Changes
- Forgotten Hat no longer provides 1 armor when worn.
- Changed foxhounds setting you on fire to better distinguish the cause:
  - Petting them now sets you on fire for 1 second instead of 5. 
  - Getting to close to your tamed foxhound that needs pets (only if Pat the Dog's module dogsWantLove option is enabled) will now set you on fire for 3 seconds instead of 5.
- Foxhounds now target smokers and blast furnaces to sleep on. They could already speed them up, but would not select them for a place to sleep.
  - This will also affect modded furnaces (it uses AbstractFurnaceBlockEntity instead of FurnaceBlockEntity)
- Stoneling, Wraith, and Oretoise now use the `minecraft:fall_damage_immune` entity type tag instead of being hardcoded to be fall damage immune.
- Reorganized various mobs' AI goals and their priorities.
- `quark:PetTime` NBT data is no longer set on wolves and shibas if PatTheDogs' dogsWantLove is disabled (set to -1).
- Glowshroom generation is now more data-driven (see [this PR](https://github.com/VazkiiMods/Quark/pull/5633) for details) (thanks EltrutLikes)
- Oretoises no longer have unused breeding-related AI goals.
- Updated Brazilian Portuguese translation (thanks Brun333rp)

# Additions
- Made Expanded Item Interactions' armor interaction exceptions data-driven instead of hardcoded:
  - Added `counts_as_armor_for_expanded_item_interactions` item tag and sub-tags (head, chest, etc) for items that should be equippable to armor slots but aren't `ArmorItem`s.
  - Pumpkins and skulls can now be equipped via Expanded Item Interactions.
- Rotation Lock module now has an option to display what key to press to disable it alongside its crosshair indicator, enabled by default.
- Chutes work with modded item transfers now (thanks Klisz)
- Attempting to sort a container that uses SlotItemHandler will now show a warning in chat (once per launch) that links to issue [5675](https://github.com/VazkiiMods/Quark/issues/5675)
  - If you know how to address this in a compatible way (i.e., not breaking container GUIs that restrict what items can go in certain slots) let us know!
- Added Czech translation (thanks RomanPlayer22)

P.S., if you're in the United States, remember to vote if you haven't already!
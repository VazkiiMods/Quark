# Quark 4.1-488 For Neoforge 1.21.1

# Fixes

- Fixed #5683: Librarian villager ancient tome trade is not awarded when using enchanted books created in anvil
- Partially fixed #5680: foxhounds prefer cobblestone furnace 
- Fixed #5687: quark:PetTime not being set on Foxhound 

# Changes
- Foxhounds now target smokers and blast furnaces to sleep on. They could already speed them up, but would not select them for a place to sleep.
  - This will also affect modded furnaces (it uses AbstractFurnaceBlockEntity instead of FurnaceBlockEntity) 
- `quark:PetTime` NBT data is no longer set on wolves and shibas if PatTheDogs' dogsWantLove is disabled (set to -1).
- Petting tamed foxhounds sets you on fire for 1 second instead of 5. Tamed foxhounds that need pets (only if dogsWantLove is enabled) will now ignite their owners if they come close for 3 seconds instead of 5.

# Additions
- Merged #5677: Chute ItemHandler capability (Thanks Klisz)
  - Chutes work with modded item transfers now.
- Merged #5684: Fixed librarian ancient tome trade rejecting anvil made books (thanks midori-0)
- Attempting to sort a container that uses SlotItemhandler will now show a warning in chat (once per launch) that links to issue [5675](https://github.com/VazkiiMods/Quark/issues/5675)
  - If you know how to address this in a compatible way (i.e., not breaking container GUIs that restrict what items can go in certain slots) let us know!

P.S., if you're in the United States remember to vote!
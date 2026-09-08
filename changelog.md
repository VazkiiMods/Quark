# Quark 4.1-483 For Neoforge 1.21.1

# Fixes
- Fixed #5638: Several Enchantments (Modded and Vanilla) Not Functioning with "Golden Tools Have Fortune" Config [1.21.1]
    - Also, mutual exclusions are now applied correctly to built-in and applied enchantments.
    - This fix undoes a previous change, and as a results compatibility with Kilt is potentially reduced, but this is untested and not something we are explicitly supporting.
- Potentially fixed #5647: Make Azalea Tree Change be not hard coded
# Changes
- Updated Finnish translation (Thanks footwanterfin)
- Azalea Wood Module, if disabled manually or by anti-overlap, will no longer force the vanilla azalea tree ConfiguredFeature to use Oak Logs

# Additions
- Added a config option to Oddities' Totem of Holding to drop all items on hit (Thanks Lightning323) 
- Backpacks have item handler capabilities now; modded items and tools can potentially use items from the backpack
- Feed trough has item handler capability now; you should be able to insert into it with modded pipes (Thanks Klisz)


# Quark 4.1-487 For Neoforge 1.21.1

# Fixes
- Partial fix for #5667: [Bug] NPE in TinyPotatoModel.isCustomRenderer during ModifyBakingResult (Map.compute inserts null entry)
    - This does not fix the root issue but *should* prevent a crash with Continuity+Connector installed
# Changes

# Additions
- Merged #5673 [1.21] Give Quark mobs the ability to wear hats (thanks hedgehog1029!)
  - If Create is installed, Quark's mobs can now wear conductor hats
- Expanded Item Interactions now has an option to invert clicks (use left-clicks for the interaction instead of right-clicks). 
  - Similar to 1.21.2+ bundles
  - Some JEI hints may be incorrect if this is enabled
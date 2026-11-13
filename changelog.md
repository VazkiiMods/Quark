Quark 4.1-489 (... and Zeta 1.1-41)

# Changes

- (Internal only) Moved some worldgen logic around, removed WorldGenRegionMixin 

# Fixes

- Usage ticker module animation is now interpolated correctly
- Cloud block (the one placed by Cloud in a Bottle) has a smoother animation (fixed interpolation), and is ever so slightly bigger, starting at 10 pixels

# Additions

- Cloud block has a pixel consistent new unique texture (no more white concrete) & is translucent
- Cloud block emits slightly less particles & has different place and break sounds
- Improved Tooltips module has a new tag `quark:enchanting_tooltip_items"`, meant for other mods to use
- Cloud block has same exact rendering as the game clouds, changing with rain and night
- The particles it emits are also custom now & have matching colors too, plus water rain particles during rain

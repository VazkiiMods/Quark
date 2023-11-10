package org.violetmoon.quark.content.experimental.item;

import javax.annotation.Nonnull;

import org.violetmoon.quark.base.item.QuarkItem;
import org.violetmoon.quark.content.experimental.module.VariantSelectorModule;
import org.violetmoon.quark.content.tweaks.module.LockRotationModule;
import org.violetmoon.zeta.module.ZetaModule;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class HammerItem extends QuarkItem {

	public HammerItem(ZetaModule module) {
		super("hammer", module, new Item.Properties()
				.stacksTo(1)
				.tab(CreativeModeTab.TAB_TOOLS));
	}
	
	@Nonnull
	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockState state = level.getBlockState(pos);
		Block block = state.getBlock();

		if(player != null) {
			String variant = VariantSelectorModule.getSavedVariant(player);
			Block variantBlock = VariantSelectorModule.getVariantOrOriginal(block, variant);
			if(variantBlock != null) {
				level.removeBlock(pos, false);

				BlockPlaceContext bpc = new YungsBetterBlockPlaceContext(context);
				BlockState place = variantBlock.getStateForPlacement(bpc);

				place = LockRotationModule.fixBlockRotation(place, bpc);

				if(place != null && !place.equals(state) && !level.isClientSide) {
					level.removeBlock(pos, false);
					level.setBlock(pos, place, 1 | 2);
					player.swing(context.getHand());

					level.playSound(null, pos, place.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
				} else {
					level.setBlock(pos, state, 0);
				}

				return InteractionResult.SUCCESS;
			}
		}
		
		return InteractionResult.PASS;
	}
	
	private static class YungsBetterBlockPlaceContext extends BlockPlaceContext {

		public YungsBetterBlockPlaceContext(UseOnContext ctx) {
			super(ctx);
		}
		
		// vanilla BlockPlaceContext offsets the original clicked pos if replaceClicked is false
		// so that the block is placed on the edge, but in this case we want to place it in the
		// same blockpos that was clicked so we do this nonsense

		@Nonnull
		@Override
		public BlockPos getClickedPos() {
			boolean oldRepl = replaceClicked;

			replaceClicked = true;
			BlockPos pos = super.getClickedPos();
			
			replaceClicked = oldRepl;
			return pos;
		}
		
	}

}

package org.violetmoon.quark.content.building.module;

import java.util.Map;

import org.violetmoon.quark.content.building.block.CutVineBlock;
import org.violetmoon.zeta.client.AlikeColorHandler;
import org.violetmoon.zeta.client.event.load.ZAddBlockColorHandlers;
import org.violetmoon.zeta.client.event.load.ZAddItemColorHandlers;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.event.play.entity.player.ZRightClickBlock;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.Hint;
import org.violetmoon.zeta.util.MiscUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

@ZetaLoadModule(category = "building")
public class ShearVinesModule extends ZetaModule {

	public static Block cut_vine;

	@Hint
	Item vine = Items.VINE;

	@LoadEvent
	public final void register(ZRegister event) {
		cut_vine = new CutVineBlock(this);
	}

	@PlayEvent
	public void onRightClick(ZRightClickBlock event) {
		ItemStack stack = event.getItemStack();
		if(zeta().itemExtensions.get(stack).canShearZeta(stack)) {
			BlockPos pos = event.getPos();
			Level world = event.getLevel();
			BlockState state = world.getBlockState(pos);

			if(state.getBlock() == Blocks.VINE) {
				BlockState newState = cut_vine.defaultBlockState();
				Map<Direction, BooleanProperty> map = VineBlock.PROPERTY_BY_DIRECTION;
				for(Direction d : map.keySet()) {
					BooleanProperty prop = map.get(d);
					newState = newState.setValue(prop, state.getValue(prop));
				}

				world.setBlockAndUpdate(pos, newState);

				BlockPos testPos = pos.below();
				BlockState testState = world.getBlockState(testPos);
				while(testState.is(Blocks.VINE) || testState.is(cut_vine)) {
					world.removeBlock(testPos, false);
					testPos = testPos.below();
					testState = world.getBlockState(testPos);
				}

				Player player = event.getPlayer();
				world.playSound(player, pos, SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 0.5F, 1F);
				if(!player.getAbilities().instabuild)
					MiscUtil.damageStack(player, event.getHand(), stack, 1);

				event.setCancellationResult(InteractionResult.sidedSuccess(world.isClientSide));
				event.setCanceled(true);
			}
		}
	}

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends ShearVinesModule {

		private final AlikeColorHandler LIKE_VINE = new AlikeColorHandler(Blocks.VINE.defaultBlockState());

		@LoadEvent
		public void blockColors(ZAddBlockColorHandlers event) {
			event.registerNamed(zeta(), b -> LIKE_VINE, "vine");
		}

		@LoadEvent
		public void itemColors(ZAddItemColorHandlers event) {
			event.registerNamed(zeta(), i -> LIKE_VINE, "vine");
		}
	}
}

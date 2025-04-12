package org.violetmoon.quark.content.automation.block;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.violetmoon.zeta.block.ZetaButtonBlock;
import org.violetmoon.zeta.module.ZetaModule;

/**
 * @author WireSegal
 *         Created at 9:14 PM on 10/8/19.
 */
public class MetalButtonBlock extends ZetaButtonBlock {

	public MetalButtonBlock(String regname, @Nullable ZetaModule module, int speed) {
		super(BlockSetType.IRON, speed, regname, module,
				Block.Properties.of()
						.mapColor(MapColor.NONE)
						.noCollission()
						.strength(0.5F)
						.sound(SoundType.METAL)
						.pushReaction(PushReaction.DESTROY));
	}

	@NotNull
	@Override
	protected SoundEvent getSound(boolean powered) {
		return powered ? SoundEvents.STONE_BUTTON_CLICK_ON : SoundEvents.STONE_BUTTON_CLICK_OFF;
	}
}

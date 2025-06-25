package org.violetmoon.quark.content.tools.module;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BeaconBlockEntity.BeaconBeamSection;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.world.block.CorundumClusterBlock;
import org.violetmoon.quark.content.world.module.CorundumModule;
import org.violetmoon.zeta.advancement.ManualTrigger;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.event.load.ZGatherHints;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.Hint;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

@ZetaLoadModule(category = "tools")
public class BeaconRedirectionModule extends ZetaModule {

	private static final TagKey<Block> BEACON_TRANSPARENT = Quark.asTagKey(Registries.BLOCK,"beacon_transparent");

	@Config
	public static int horizontalMoveLimit = 64;

	@Config(flag = "tinted_glass_dims")
	public static boolean allowTintedGlassTransparency = true;

	@Hint("tinted_glass_dims")
	Item tinted_glass = Items.TINTED_GLASS;

	public static boolean staticEnabled;

	public static ManualTrigger redirectTrigger;

	@LoadEvent
	public final void register(ZRegister event) {
		redirectTrigger = event.getAdvancementModifierRegistry().registerManualTrigger("redirect_beacon");
	}

	@LoadEvent
	public final void configChanged(ZConfigChanged event) {
		staticEnabled = isEnabled();
	}

	@LoadEvent
	public void addAdditionalHints(ZGatherHints event) {
		final String redirectHint = "beacon_redirect_item";
		String type = "amethyst";

		if(!Quark.ZETA.modules.isEnabled(CorundumModule.class))
			event.hintItem(Items.AMETHYST_CLUSTER, redirectHint, zeta());
		else
			type = "corundum";

		Component comp = Component.translatable("quark.jei.hint.beacon_redirection", Component.translatable("quark.jei.hint.beacon_" + type));
		event.accept(Items.BEACON, comp);
	}

	// The value that comes out of this is fed onto a constant for the FOR loop that
	// computes the beacon segments, so we return 0 to run that code, or MAX_VALUE to not
	public static int tickBeacon(BeaconBlockEntity beacon, int original) {
		if(!staticEnabled)
			return original;

		Level world = beacon.getLevel();
		BlockPos beaconPos = beacon.getBlockPos();
		BlockPos currPos = beaconPos;

		int horizontalMoves = horizontalMoveLimit;
		int targetHeight = world.getHeight(Heightmap.Types.WORLD_SURFACE, beaconPos.getX(), beaconPos.getZ());

		boolean broke = false;
		boolean didRedirection = false;

		beacon.checkingBeamSections.clear();

		int currColor = 16777215; // (255 << 16) + (255 << 8) + 255
		float alpha = 1F;

		Direction lastDir = null;
		ExtendedBeamSegment currSegment = new ExtendedBeamSegment(Direction.UP, Vec3i.ZERO, currColor, alpha);

		Collection<BlockPos> seenPositions = new HashSet<>();
		boolean check = true;
		boolean hardColorSet = false;

		while(world.isInWorldBounds(currPos) && horizontalMoves > 0) {
			if(currSegment.dir == Direction.UP && currSegment.dir != lastDir) {
				int heightmapVal = world.getHeight(Heightmap.Types.WORLD_SURFACE, currPos.getX(), currPos.getZ());
				if(heightmapVal == (currPos.getY() + 1)) {
					currSegment.setHeight(heightmapVal + 1000);
					break;
				}

				lastDir = currSegment.dir;
			}

			currPos = currPos.relative(currSegment.dir);
			if(currSegment.dir.getAxis().isHorizontal())
				horizontalMoves--;
			else
				horizontalMoves = horizontalMoveLimit;

			BlockState blockstate = world.getBlockState(currPos);
			Block block = blockstate.getBlock();
			Integer targetColor = blockstate.getBeaconColorMultiplier(world, currPos, beaconPos);
			float targetAlpha = -1;

			if(allowTintedGlassTransparency) {
				if(block == Blocks.TINTED_GLASS)
					targetAlpha = (alpha < 0.3F ? 0F : (alpha / 2F));
			}

			if(isRedirectingBlock(block)) {
				Direction dir = blockstate.getValue(BlockStateProperties.FACING);
				if(dir == currSegment.dir)
					currSegment.increaseHeight();
				else {
					check = true;
					beacon.checkingBeamSections.add(currSegment);

					targetColor = getTargetColor(block);
					if(targetColor >> 16 == 255 && targetColor >> 8 == 255 && targetColor == 255)
						targetColor = currColor;

                    currColor = (((currColor >> 16) + (targetColor >> 16) * 3) / 4) + (((currColor >> 8) + (targetColor >> 8) * 3) / 4) + ((currColor + targetColor * 3) / 4);
					alpha = 1F;
					didRedirection = true;
					lastDir = currSegment.dir;
					currSegment = new ExtendedBeamSegment(dir, currPos.subtract(beaconPos), currColor, alpha);
				}
			} else if(targetColor != null || targetAlpha != -1) {
				if (targetColor.equals(currColor) && targetAlpha == alpha)
					currSegment.increaseHeight();
				else {
					check = true;
					beacon.checkingBeamSections.add(currSegment);

					int mixedColor = currColor;
					if(targetColor != null) {
						mixedColor = (((currColor >> 16) + (targetColor >> 16)) / 2) + (((currColor >> 8) + (targetColor >> 8)) / 2) + ((currColor + targetColor) / 2);

						if(!hardColorSet) {
							mixedColor = targetColor;
							hardColorSet = true;
						}

						currColor = mixedColor;
					}

					if(targetAlpha != -1)
						alpha = targetAlpha;

					lastDir = currSegment.dir;
					currSegment = new ExtendedBeamSegment(currSegment.dir, currPos.subtract(beaconPos), mixedColor, alpha);
				}
			} else {
				boolean bedrock = blockstate.is(BEACON_TRANSPARENT); //Bedrock blocks don't stop beacon beams

				if(!bedrock && blockstate.getLightBlock(world, currPos) >= 15) {
					broke = true;
					break;
				}

				currSegment.increaseHeight();

				if(bedrock)
					continue;
			}

			if(check) {
				boolean added = seenPositions.add(currPos);
				if(!added) {
					broke = true;
					break;
				}

			}
		}

		if(horizontalMoves == 0 || currPos.getY() <= world.getMinBuildHeight())
			broke = true;

		final String tag = "quark:redirected";

		if(!broke) {
			beacon.checkingBeamSections.add(currSegment);
			beacon.lastCheckY = targetHeight + 1;
		} else {
			beacon.getPersistentData().putBoolean(tag, false);

			beacon.checkingBeamSections.clear();
			beacon.lastCheckY = targetHeight;
		}

		if(!beacon.getPersistentData().getBoolean(tag) && didRedirection && !beacon.checkingBeamSections.isEmpty()) {
			beacon.getPersistentData().putBoolean(tag, true);

			int i = beaconPos.getX();
			int j = beaconPos.getY();
			int k = beaconPos.getZ();
			for(ServerPlayer serverplayer : beacon.getLevel().getEntitiesOfClass(ServerPlayer.class, (new AABB((double) i, (double) j, (double) k, (double) i, (double) (j - 4), (double) k)).inflate(10.0D, 5.0D, 10.0D)))
				redirectTrigger.trigger(serverplayer);
		}

		return Integer.MAX_VALUE;
	}

	private static boolean isRedirectingBlock(Block block) {
		return CorundumModule.staticEnabled ? block instanceof CorundumClusterBlock : block == Blocks.AMETHYST_CLUSTER;
	}

	private static int getTargetColor(Block block) {
		return block instanceof CorundumClusterBlock cc ? cc.base.color : 16777216;
	}

	public static class ExtendedBeamSegment extends BeaconBeamSection {

		public final Direction dir;
		public final Vec3i offset;
		public final float alpha;

		private boolean isTurn = false;

		public ExtendedBeamSegment(Direction dir, Vec3i offset, int colorsIn, float alpha) {
			super(colorsIn);
			this.offset = offset;
			this.dir = dir;
			this.alpha = alpha;
		}

		public void makeTurn() {
			isTurn = true;
		}

		public boolean isTurn() {
			return isTurn;
		}

		@Override
		public void increaseHeight() { // increase visibility
			super.increaseHeight();
		}

		public void setHeight(int target) {
			height = target;
		}

	}

}

package vazkii.quark.content.tweaks.client.item;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.zeta.util.ItemNBTHelper;
import vazkii.quark.content.tweaks.module.CompassesWorkEverywhereModule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class CompassAngleGetter {

	private static final String TAG_CALCULATED = "quark:compass_calculated";
	private static final String TAG_WAS_IN_NETHER = "quark:compass_in_nether";
	private static final String TAG_POSITION_SET = "quark:compass_position_set";
	private static final String TAG_NETHER_TARGET_X = "quark:nether_x";
	private static final String TAG_NETHER_TARGET_Z = "quark:nether_z";

	public static void tickCompass(Player player, ItemStack stack) {
		boolean calculated = isCalculated(stack);
		boolean nether = player.level.dimension().location().equals(LevelStem.NETHER.location());

		if(calculated) {
			boolean wasInNether = ItemNBTHelper.getBoolean(stack, TAG_WAS_IN_NETHER, false);
			BlockPos pos = player.blockPosition();
			boolean isInPortal = player.level.getBlockState(pos).getBlock() == Blocks.NETHER_PORTAL;

			if(nether && !wasInNether && isInPortal) {
				ItemNBTHelper.setInt(stack, TAG_NETHER_TARGET_X, pos.getX());
				ItemNBTHelper.setInt(stack, TAG_NETHER_TARGET_Z, pos.getZ());
				ItemNBTHelper.setBoolean(stack, TAG_WAS_IN_NETHER, true);
				ItemNBTHelper.setBoolean(stack, TAG_POSITION_SET, true);
			} else if(!nether && wasInNether) {
				ItemNBTHelper.setBoolean(stack, TAG_WAS_IN_NETHER, false);
				ItemNBTHelper.setBoolean(stack, TAG_POSITION_SET, false);
			}
		} else {
			ItemNBTHelper.setBoolean(stack, TAG_CALCULATED, true);
			ItemNBTHelper.setBoolean(stack, TAG_WAS_IN_NETHER, nether);
		}
	}

	static boolean isCalculated(ItemStack stack) {
		return stack.hasTag() && ItemNBTHelper.getBoolean(stack, TAG_CALCULATED, false);
	}

	@OnlyIn(Dist.CLIENT)
	public static class Impl implements ItemPropertyFunction {

		private final Angle normalAngle = new Angle();
		private final Angle unknownAngle = new Angle();

		@Override
		@OnlyIn(Dist.CLIENT)
		public float call(@Nonnull ItemStack stack, @Nullable ClientLevel worldIn, @Nullable LivingEntity entityIn, int id) {
			if(entityIn == null && !stack.isFramed())
				return 0F;

			if(CompassesWorkEverywhereModule.enableCompassNerf && (!stack.hasTag() || !ItemNBTHelper.getBoolean(stack, TAG_CALCULATED, false)))
				return 0F;

			boolean carried = entityIn != null;
			Entity entity = carried ? entityIn : stack.getFrame();

			if (entity == null)
				return 0;

			if(worldIn == null && entity != null && entity.level instanceof ClientLevel level)
				worldIn = level;

			double angle;

			boolean calculate = false;
			BlockPos target = new BlockPos(0, 0, 0);

			ResourceLocation dimension = worldIn.dimension().location();
			boolean isLodestone = CompassItem.isLodestoneCompass(stack);
			BlockPos lodestonePos = isLodestone ? this.getLodestonePosition(worldIn, stack.getOrCreateTag()) : null;

			if(lodestonePos != null) {
				calculate = true;
				target = lodestonePos;
			} else if (!isLodestone) {
				if (dimension.equals(LevelStem.END.location()) && CompassesWorkEverywhereModule.enableEnd)
					calculate = true;
				else if (dimension.equals(LevelStem.NETHER.location()) && isCalculated(stack) && CompassesWorkEverywhereModule.enableNether) {
					boolean set = ItemNBTHelper.getBoolean(stack, TAG_POSITION_SET, false);
					if (set) {
						int x = ItemNBTHelper.getInt(stack, TAG_NETHER_TARGET_X, 0);
						int z = ItemNBTHelper.getInt(stack, TAG_NETHER_TARGET_Z, 0);
						calculate = true;
						target = new BlockPos(x, 0, z);
					}
				} else if (worldIn.dimensionType().natural()) {
					calculate = true;
					target = getWorldSpawn(worldIn);
				}
			}

			long gameTime = worldIn.getGameTime();
			if(calculate && target != null) {
				double d1 = carried ? entity.getYRot() : getFrameRotation((ItemFrame)entity);
				d1 = Mth.positiveModulo(d1 / 360.0D, 1.0D);
				double d2 = getAngleToPosition(entity, target) / (Math.PI * 2D);

				if(carried) {
					if(normalAngle.needsUpdate(gameTime))
						normalAngle.wobble(gameTime, 0.5D - (d1 - 0.25D));
						angle = d2 + normalAngle.rotation;
				} else angle = 0.5D - (d1 - 0.25D - d2);
			} else {
				if(unknownAngle.needsUpdate(gameTime))
					unknownAngle.wobble(gameTime, Math.random());

				angle = unknownAngle.rotation + shift(id);
			}

			return Mth.positiveModulo((float) angle, 1.0F);
		}


		private double getFrameRotation(ItemFrame frame) {
			return Mth.wrapDegrees(180 + frame.getDirection().toYRot());
		}

		private double getAngleToPosition(Entity entity, BlockPos blockpos) {
			Vec3 pos = entity.position();
			return Math.atan2(blockpos.getZ() - pos.z, blockpos.getX() - pos.x);
		}

		// Magic number cribbed from vanilla
		private float shift(int id) {
			return (id * 1327217883) / (float) Integer.MAX_VALUE;
		}

		// vanilla copy from here on out

		@Nullable
		private BlockPos getLodestonePosition(Level world, CompoundTag tag) {
			boolean flag = tag.contains("LodestonePos");
			boolean flag1 = tag.contains("LodestoneDimension");
			if (flag && flag1) {
				Optional<ResourceKey<Level>> optional = CompassItem.getLodestoneDimension(tag);
				if (optional.isPresent() && world.dimension().equals(optional.get())) {
					return NbtUtils.readBlockPos(tag.getCompound("LodestonePos"));
				}
			}

			return null;
		}

		@Nullable
		private BlockPos getWorldSpawn(ClientLevel world) {
			return world.dimensionType().natural() ? world.getSharedSpawnPos() : null;
		}

		@OnlyIn(Dist.CLIENT)
		private static class Angle {
			private double rotation;
			private double rota;
			private long lastUpdateTick;

			private boolean needsUpdate(long tick) {
				return lastUpdateTick != tick;
			}

			private void wobble(long gameTime, double angle) {
				lastUpdateTick = gameTime;
				double d0 = angle - rotation;
				d0 = Mth.positiveModulo(d0 + 0.5D, 1.0D) - 0.5D;
				rota += d0 * 0.1D;
				rota *= 0.8D;
				rotation = Mth.positiveModulo(rotation + rota, 1.0D);
			}
		}

	}




}

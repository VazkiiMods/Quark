package vazkii.quark.content.automation.module;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import vazkii.zeta.module.ZetaLoadModule;
import vazkii.zeta.module.ZetaModule;
import vazkii.quark.base.module.config.Config;
import vazkii.zeta.event.ZCommonSetup;
import vazkii.zeta.event.bus.LoadEvent;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@ZetaLoadModule(category = "automation")
public class DispensersPlaceBlocksModule extends ZetaModule {

	@Config public static List<String> blacklist = Lists.newArrayList("minecraft:water", "minecraft:lava", "minecraft:fire");

	@Config(description = "Set to false to refrain from registering any behaviors for blocks that have optional dispense behaviors already set.\n"
			+ "An optional behavior is one that will defer to the generic dispense item behavior if its condition fails.\n"
			+ "e.g. the Shulker Box behavior is optional, because it'll throw out the item if it fails, whereas TNT is not optional.\n"
			+ "If true, it'll attempt to use the previous behavior before trying to place the block in the world.\n"
			+ "Requires a game restart to re-apply.")
	public static boolean wrapExistingBehaviors = true;

	@LoadEvent
	public void setup(ZCommonSetup e) {
		if(!enabled)
			return;

		BlockBehavior baseBehavior = new BlockBehavior();

		e.enqueueWork(() -> {
			Map<Item, DispenseItemBehavior> registry = DispenserBlock.DISPENSER_REGISTRY;

			for(Block b : Registry.BLOCK) {
				ResourceLocation res = Registry.BLOCK.getKey(b);
				if(!blacklist.contains(Objects.toString(res))) {
					Item item = b.asItem();
					if(item instanceof BlockItem) {
						DispenseItemBehavior original = registry.get(item);
						boolean exists = original != null && original.getClass() != DefaultDispenseItemBehavior.class;
						if(original instanceof BlockBehavior) continue; //some blocks map to same item (mob heads)
						if(exists) {
							if(wrapExistingBehaviors && original instanceof OptionalDispenseItemBehavior opt)
								registry.put(item, new BlockBehavior(opt));
						}
						else
							registry.put(item, baseBehavior);
					}

				}
			}
		});
	}

	public static class BlockBehavior extends OptionalDispenseItemBehavior {

		private final OptionalDispenseItemBehavior wrapped;

		public BlockBehavior() {
			this(null);
		}

		public BlockBehavior(OptionalDispenseItemBehavior wrapped) {
			this.wrapped = wrapped;
		}

		@Nonnull
		@Override
		public ItemStack execute(BlockSource source, ItemStack stack) {
			if(wrapped != null) {
				ItemStack wrappedResult = wrapped.dispense(source, stack);
				if(wrapped.isSuccess()) {
					setSuccess(true);
					return wrappedResult;
				}
			}

			setSuccess(false);

			Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
			Direction against = direction;
			BlockPos pos = source.getPos().relative(direction);

			if (stack.getItem() instanceof BlockItem item) {
				Block block = item.getBlock();
				if (block instanceof StairBlock && direction.getAxis() != Axis.Y)
					direction = direction.getOpposite();
				else if (block instanceof SlabBlock)
					against = Direction.UP;

				setSuccess(item.place(new NotStupidDirectionalPlaceContext(source.getLevel(), pos, direction, stack, against))
					.consumesAction());
			}

			return stack;
		}

	}

	// DirectionPlaceContext results in infinite loops when using slabs
	private static class NotStupidDirectionalPlaceContext extends DirectionalPlaceContext {

		protected boolean replaceClicked;
		protected Direction direction;

		public NotStupidDirectionalPlaceContext(Level worldIn, BlockPos pos, Direction facing, ItemStack stack, Direction against) {
			super(worldIn, pos, facing, stack, against);
			replaceClicked = worldIn.getBlockState(getHitResult().getBlockPos()).canBeReplaced(this); // getHitResult = getRayTraceResult
			this.direction = facing;
		}

		@Override
		public boolean canPlace() {
			return replaceClicked;
		}

		@Nonnull
		@Override
		public Direction getNearestLookingDirection() {
			return direction.getOpposite();
		}

	}

}

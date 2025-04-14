package org.violetmoon.quark.content.automation.module;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.apache.commons.lang3.tuple.Pair;

import org.violetmoon.zeta.api.IIndirectConnector;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.Hint;

import java.util.function.Predicate;

@ZetaLoadModule(category = "automation")
public class ChainsConnectBlocksModule extends ZetaModule {

	@Hint
	Item chain = Items.CHAIN;

	public static boolean staticEnabled;

	@LoadEvent
	public final void register(ZRegister event) {
		IIndirectConnector.INDIRECT_STICKY_BLOCKS.add(Pair.of(ChainConnection.PREDICATE, ChainConnection.INSTANCE));
	}

	@LoadEvent
	public final void configChanged(ZConfigChanged event) {
		staticEnabled = isEnabled();
	}

	public static class ChainConnection implements IIndirectConnector {

		public static ChainConnection INSTANCE = new ChainConnection();
		public static Predicate<BlockState> PREDICATE = s -> s.getBlock() == Blocks.CHAIN;

		@Override
		public boolean isEnabled() {
			return ChainsConnectBlocksModule.staticEnabled;
		}

		@Override
		public boolean canConnectIndirectly(Level world, BlockPos ourPos, BlockPos sourcePos, BlockState ourState, BlockState sourceState) {
			Axis axis = ourState.getValue(ChainBlock.AXIS);

			switch(axis) {
			case X:
				if(ourPos.getX() == sourcePos.getX())
					return false;
				break;
			case Y:
				if(ourPos.getY() == sourcePos.getY())
					return false;
				break;
			case Z:
				if(ourPos.getZ() == sourcePos.getZ())
					return false;
			}

			if(sourceState.getBlock() == ourState.getBlock()) {
				Axis otherAxis = sourceState.getValue(ChainBlock.AXIS);
				return axis == otherAxis;
			}

			return true;
		}

	}

}

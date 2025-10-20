package org.violetmoon.quark.content.building.module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.registries.Registries;
import org.violetmoon.quark.api.ICrawlSpaceBlock;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.building.block.HollowLogBlock;
import org.violetmoon.zeta.advancement.ManualTrigger;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZCommonSetup;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.event.play.entity.player.ZPlayerTick;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.Hint;
import org.violetmoon.zeta.util.VanillaWoods;
import org.violetmoon.zeta.util.VanillaWoods.Wood;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

@ZetaLoadModule(category = "building")
public class HollowLogsModule extends ZetaModule {

	public static List<Block> hollowLogs = new ArrayList<>();
	private static final String TAG_TRYING_TO_CRAWL = "quark:trying_crawl";

	public static ManualTrigger crawlTrigger;

	@Config(flag = "hollow_log_auto_crawl")
	public static boolean enableAutoCrawl = true;

	@Hint(key = "hollow_logs", value = "hollow_log_auto_crawl")
	public static TagKey<Block> hollowLogsTag;

	public static boolean staticEnabled;
	public static Map<Block, Block> logMap = new HashMap<>();
	
	@LoadEvent
	public final void configChanged(ZConfigChanged event) {
		staticEnabled = isEnabled();
	}
	
	@LoadEvent
	public final void register(ZRegister event) {
		for(Wood wood : VanillaWoods.ALL_WITH_LOGS) {
			Block block = new HollowLogBlock(wood.log(), this, !wood.nether());
			hollowLogs.add(block);
		}

		crawlTrigger = event.getAdvancementModifierRegistry().registerManualTrigger("hollow_log_crawl");
	}

	@LoadEvent
	public final void setup(ZCommonSetup event) {
		hollowLogsTag = Quark.asTagKey(Registries.BLOCK, "hollow_logs");
	}

	@PlayEvent
	public void playerTick(ZPlayerTick.Start event) {
		if(enableAutoCrawl) {
			Player player = event.getPlayer();
			BlockPos playerPos = player.blockPosition();
			boolean isTrying = player.isVisuallyCrawling() ||
					(player.isCrouching() && !player.isColliding(playerPos, player.level().getBlockState(playerPos)));
			boolean wasTrying = player.getPersistentData().getBoolean(TAG_TRYING_TO_CRAWL);

			if(!player.isVisuallyCrawling()) {
				if(isTrying && !wasTrying) {
					Direction dir = player.getDirection();
					Direction opp = dir.getOpposite();
					if(dir.getAxis() != Axis.Y) {
						BlockPos pos = playerPos.relative(dir);

						if(!tryClimb(player, opp, playerPos)) // Crawl out
							if(!tryClimb(player, opp, playerPos.above())) // Crawl out
								if(!tryClimb(player, dir, pos)) // Crawl into
									tryClimb(player, dir, pos.above()); // Crawl into
					}
				}
			}

			if(isTrying != wasTrying)
				player.getPersistentData().putBoolean(TAG_TRYING_TO_CRAWL, isTrying);
		}
	}

	private boolean tryClimb(Player player, Direction dir, BlockPos pos) {
		BlockState state = player.level().getBlockState(pos);
		Block block = state.getBlock();

		if(block instanceof ICrawlSpaceBlock crawlSpace) {
			if(crawlSpace.canCrawl(player.level(), state, pos, dir)) {
				player.setPose(Pose.SWIMMING);
				player.setSwimming(true);

				double x = pos.getX() + 0.5 - (dir.getStepX() * 0.3);
				double y = pos.getY() + crawlSpace.crawlHeight(player.level(), state, pos, dir);
				double z = pos.getZ() + 0.5 - (dir.getStepZ() * 0.3);

				player.setPos(x, y, z);

				if(player instanceof ServerPlayer sp && crawlSpace.isLog(sp, state, pos, dir))
					crawlTrigger.trigger(sp);

				return true;
			}
		}

		return false;
	}
}

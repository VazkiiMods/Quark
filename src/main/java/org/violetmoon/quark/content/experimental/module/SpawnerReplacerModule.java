package org.violetmoon.quark.content.experimental.module;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@ZetaLoadModule(category = "experimental", enabledByDefault = false)
public class SpawnerReplacerModule extends ZetaModule {

	@Config(
		description = "Mobs to be replaced with other mobs.\n" +
				"Format is: \"mob1,mob2\", i.e. \"minecraft:spider,minecraft:skeleton\""
	)
	public static List<String> replaceMobs = Lists.newArrayList();

	private static boolean staticEnabled;

	private static final Map<EntityType<?>, EntityType<?>> spawnerReplacements = Maps.newHashMap();

	@LoadEvent
	public final void configChanged(ZConfigChanged event) {
		staticEnabled = isEnabled();

		spawnerReplacements.clear();

		for(String replaceKey : replaceMobs) {
			String[] split = replaceKey.split(",");
			if(split.length == 2) {
				Optional<EntityType<?>> before = EntityType.byString(split[0]);
				Optional<EntityType<?>> after = EntityType.byString(split[1]);
				if(before.isPresent() && after.isPresent()) {
					spawnerReplacements.put(before.get(), after.get());
				}
			}
		}
	}

	public static void spawnerUpdate(Level level, BlockPos pos, BlockState state, SpawnerBlockEntity be) {
		if(!staticEnabled || level.isClientSide())
			return;

		BaseSpawner spawner = be.getSpawner();
		Entity example = spawner.getOrCreateDisplayEntity(level, pos);
		if(example != null) {
			EntityType<?> present = example.getType();
			if(spawnerReplacements.containsKey(present)) {
				spawner.setEntityId(spawnerReplacements.get(present), level, level.getRandom(), pos);

				be.setChanged();
				level.sendBlockUpdated(pos, state, state, 3);
			}
		}
	}

}

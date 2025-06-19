package org.violetmoon.quark.content.world.module;

import java.util.ArrayList;

import net.minecraft.resources.ResourceKey;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.util.QuarkWorldGenWeights;
import org.violetmoon.quark.content.world.block.MonsterBoxBlock;
import org.violetmoon.quark.content.world.block.be.MonsterBoxBlockEntity;
import org.violetmoon.quark.content.world.gen.MonsterBoxGenerator;
import org.violetmoon.quark.mixin.mixins.accessor.AccessorLivingEntity;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.config.type.DimensionConfig;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZCommonSetup;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.event.play.entity.living.ZLivingDrops;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.world.WorldGenHandler;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

@ZetaLoadModule(category = "world")
public class MonsterBoxModule extends ZetaModule {

	public static final String TAG_MONSTER_BOX_SPAWNED = "quark:monster_box_spawned";
	public static final ResourceLocation MONSTER_BOX_LOOT_TABLE = Quark.asResource("misc/monster_box");
	public static final ResourceLocation MONSTER_BOX_SPAWNS_LOOT_TABLE = Quark.asResource("misc/monster_box_spawns");

	public static BlockEntityType<MonsterBoxBlockEntity> blockEntityType;

	@Config(description = "The chance for the monster box generator to try and place one in a chunk. 0 is 0%, 1 is 100%\nThis can be higher than 100% if you want multiple per chunk.")
	public static double chancePerChunk = 0.2;

	@Config
	public static int minY = -50;
	@Config
	public static int maxY = 0;
	@Config
	public static int minMobCount = 5;
	@Config
	public static int maxMobCount = 8;
	@Config
	public static DimensionConfig dimensions = DimensionConfig.overworld(false);
	@Config
	public static boolean enableExtraLootTable = true;
	@Config
	public static double activationRange = 2.5;

	@Config(description = "How many blocks to search vertically from a position before trying to place a block. Higher means you'll get more boxes in open spaces.")
	public static int searchRange = 15;

	public static Block monster_box = null;

	@LoadEvent
	public final void register(ZRegister event) {
		monster_box = new MonsterBoxBlock(this);

		blockEntityType = BlockEntityType.Builder.of(MonsterBoxBlockEntity::new, monster_box).build(null);
		Quark.ZETA.registry.register(blockEntityType, "monster_box", Registries.BLOCK_ENTITY_TYPE);
	}

	@LoadEvent
	public final void setup(ZCommonSetup event) {
		WorldGenHandler.addGenerator(this, new MonsterBoxGenerator(dimensions), Decoration.UNDERGROUND_DECORATION, QuarkWorldGenWeights.MONSTER_BOXES);
	}

	@PlayEvent
	public void onDrops(ZLivingDrops event) {
		LivingEntity entity = event.getEntity();
		if(enableExtraLootTable && entity.level() instanceof ServerLevel serverLevel
				&& entity.getPersistentData().getBoolean(TAG_MONSTER_BOX_SPAWNED)
				&& entity.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)
				&& ((AccessorLivingEntity) entity).quark$lastHurtByPlayerTime() > 0) {
			LootTable loot = serverLevel.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, MONSTER_BOX_LOOT_TABLE));
			ObjectArrayList<ItemStack> generatedLoot = loot.getRandomItems(getLootParamsBuilder(entity, true, event.getSource()).create(LootContextParamSets.ENTITY));
			entity.captureDrops(new ArrayList<>());
			for(ItemStack stack : generatedLoot)
				entity.spawnAtLocation(stack);
			event.getDrops().addAll(entity.captureDrops(null));
		}
	}

	private LootParams.Builder getLootParamsBuilder(LivingEntity entity, boolean bl, DamageSource damageSource) {
		LootParams.Builder builder = new LootParams.Builder((ServerLevel) entity.level())
				.withParameter(LootContextParams.THIS_ENTITY, entity)
				.withParameter(LootContextParams.ORIGIN, entity.position())
				.withParameter(LootContextParams.DAMAGE_SOURCE, damageSource)
				.withOptionalParameter(LootContextParams.ATTACKING_ENTITY, damageSource.getEntity())
				.withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, damageSource.getDirectEntity());

		if(bl && ((AccessorLivingEntity) entity).quark$lastHurtByPlayer() != null) {
			builder = builder.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, ((AccessorLivingEntity) entity).quark$lastHurtByPlayer()).withLuck(((AccessorLivingEntity) entity).quark$lastHurtByPlayer().getLuck());
		}

		return builder;
	}
}

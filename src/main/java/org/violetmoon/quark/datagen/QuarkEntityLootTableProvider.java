package org.violetmoon.quark.datagen;

import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SmeltItemFunction;
import net.minecraft.world.level.storage.loot.predicates.*;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.content.mobs.entity.Stoneling;
import org.violetmoon.quark.content.mobs.module.*;
import org.violetmoon.quark.content.tools.module.PathfinderMapsModule;
import org.violetmoon.quark.content.world.module.GlimmeringWealdModule;

import java.util.List;
import java.util.stream.Stream;

public class QuarkEntityLootTableProvider extends EntityLootSubProvider {
    //TODO config conditions
    //and pool names possibly? not sure if they are convention

    static CompoundTag stonelingNotMade = new CompoundTag();
    static {
        stonelingNotMade.putBoolean(Stoneling.TAG_PLAYER_MADE, false);
    }

    protected QuarkEntityLootTableProvider(HolderLookup.Provider registries) {
        super(FeatureFlags.REGISTRY.allFlags(), FeatureFlags.REGISTRY.allFlags(), registries);
    }


    @Override
    public void generate() {
        add(CrabsModule.crabType, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(CrabsModule.crab_shell))
                    .when(LootItemKilledByPlayerCondition.killedByPlayer())
                    .when(LootItemRandomChanceWithEnchantedBonusCondition.randomChanceAndLootingBoost(this.registries, 0.25F, 0.03F))
                )
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(CrabsModule.crab_leg))
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(0, 1)))
                        .apply(SmeltItemFunction.smelted().when(this.shouldSmeltLoot()))
                        .apply(EnchantedCountIncreaseFunction.lootingMultiplier(this.registries, UniformGenerator.between(0F, 1F)))
                )
        );

        add(ForgottenModule.forgottenType, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(Items.ARROW))
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(4, 8)))
                        .apply(EnchantedCountIncreaseFunction.lootingMultiplier(this.registries, UniformGenerator.between(1F, 2F)))
                ).withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(Items.BONE))
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 3)))
                        .apply(EnchantedCountIncreaseFunction.lootingMultiplier(this.registries, UniformGenerator.between(0F, 1F)))
                ).withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(ForgottenModule.forgotten_hat))
                        .when(LootItemKilledByPlayerCondition.killedByPlayer())
                )
        );

        add(FoxhoundModule.foxhoundType, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(Items.LEATHER))
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(0, 3)))
                        .apply(EnchantedCountIncreaseFunction.lootingMultiplier(this.registries, UniformGenerator.between(0F, 1F)))
                ).withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(Items.COAL))
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2)))
                        .apply(EnchantedCountIncreaseFunction.lootingMultiplier(this.registries, UniformGenerator.between(0F, 2F)))
                )
        );

        add(StonelingsModule.stonelingType, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(StonelingsModule.diamondHeart))
                        //condition: stoneling_drop_diamond_heart config
                ).withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(PathfinderMapsModule.pathfinders_quill))
                        //condition: pathfinder_maps
                        //condition: glimmering_weald
                        //condition: stoneling_weald_pathfinder
                        .when(LootItemKilledByPlayerCondition.killedByPlayer())
                        .when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().nbt(new NbtPredicate((stonelingNotMade)))))
                        .when(LootItemRandomChanceWithEnchantedBonusCondition.randomChanceAndLootingBoost(this.registries, 0.08F, 0.02F))
                        //.when(LocationCheck.checkLocation(LocationPredicate.Builder.inBiome(Holder.Reference.createStandAlone(new HolderOwner<>() {}, GlimmeringWealdModule.BIOME_KEY))))

                        //.when(InvertedLootItemCondition.invert(biomeCheck.)) //vanilla location predicate supports biome, but vanilla datagen has no biome check
                        //there is a quark:in_biome condition but maybe we should be using the vanilla one, just make our own biomeCheck - Train
                        // I found it but I need to actually bootstrap the biomes for this and Im too lazy to right now. Ill revisit shortly
                )
        );

        add(ToretoiseModule.toretoiseType, LootTable.lootTable()); //empty

        add(WraithModule.wraithType, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .add(LootItem.lootTableItem(WraithModule.soul_bead))
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1)))
                                .apply(EnchantedCountIncreaseFunction.lootingMultiplier(this.registries, UniformGenerator.between(0F, 1F)))
                )
        );
    }

    @Override
    protected @NotNull Stream<EntityType<?>> getKnownEntityTypes() {
        List<EntityType<?>> list = List.of(CrabsModule.crabType, ForgottenModule.forgottenType, FoxhoundModule.foxhoundType, StonelingsModule.stonelingType, ToretoiseModule.toretoiseType, WraithModule.wraithType);
        return list.stream();
    }
}

package org.violetmoon.quark.content.tweaks.module;

import org.violetmoon.quark.base.Quark;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.play.entity.ZEntityJoinLevel;
import org.violetmoon.zeta.event.play.entity.living.ZBabyEntitySpawn;
import org.violetmoon.zeta.event.play.entity.living.ZLivingTick;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.MiscUtil;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

@ZetaLoadModule(category = "tweaks")
public class PigLittersModule extends ZetaModule {

	private static final String GOLDEN_CARROT_TAG = "quark:AteGoldenCarrot";

	@Config
	@Config.Min(1)
	public static int minPigLitterSize = 2;
	@Config
	@Config.Min(1)
	public static int maxPigLitterSize = 3;

	@Config
	public static boolean pigsEatGoldenCarrots = true;

	@Config
	@Config.Min(0)
	public static int minGoldenCarrotBoost = 0;
	@Config
	@Config.Min(0)
	public static int maxGoldenCarrotBoost = 2;

	public static boolean canEat(ItemStack stack) {
		return Quark.ZETA.modules.isEnabled(PigLittersModule.class) && pigsEatGoldenCarrots && stack.is(Items.GOLDEN_CARROT);
	}

	public static void onEat(Animal animal, ItemStack stack) {
		if(animal instanceof Pig && canEat(stack))
			animal.getPersistentData().putBoolean(GOLDEN_CARROT_TAG, true);
	}

	private static int getNumberBetween(RandomSource random, int boundA, int boundB) {
		int min = Math.min(boundA, boundB);
		int max = Math.max(boundA, boundB);

		return min + random.nextInt(max - min + 1);
	}

	@PlayEvent
	public void onPigAppear(ZEntityJoinLevel event) {
		if(pigsEatGoldenCarrots && event.getEntity() instanceof Pig pig) {
			boolean alreadySetUp = pig.goalSelector.getAvailableGoals().stream()
					.anyMatch(goal -> goal.getGoal() instanceof TemptGoal tempt && tempt.items.test(new ItemStack(Items.GOLDEN_CARROT)));

			if(!alreadySetUp) {
				int priority = pig.goalSelector.getAvailableGoals().stream()
						.filter(goal -> goal.getGoal() instanceof TemptGoal)
						.findFirst()
						.map(WrappedGoal::getPriority)
						.orElse(-1);

				if(priority >= 0)
					MiscUtil.addGoalJustAfterLatestWithPriority(pig.goalSelector, 4, new TemptGoal(pig, 1.2D, Ingredient.of(Items.GOLDEN_CARROT), false));
			}
		}
	}

	@PlayEvent
	public void onEntityUpdate(ZLivingTick event) {
		if(event.getEntity() instanceof Animal animal && !animal.isInLove())
			animal.getPersistentData().remove(GOLDEN_CARROT_TAG);
	}

	@PlayEvent
	public void onPigBreed(ZBabyEntitySpawn.Lowest event) {
		AgeableMob mob = event.getChild();
		Mob mobA = event.getParentA();
		Mob mobB = event.getParentB();
		if(mob instanceof Pig) {
			Level lvl = mob.level();
			if(lvl instanceof ServerLevel level &&
					mobA instanceof Animal parentA &&
					mobB instanceof Animal parentB) {
				int litterSize = getNumberBetween(level.random, minPigLitterSize, maxPigLitterSize);

				if(mobA.getPersistentData().getBoolean(GOLDEN_CARROT_TAG))
					litterSize += getNumberBetween(level.random, minGoldenCarrotBoost, maxGoldenCarrotBoost);

				if(mobB.getPersistentData().getBoolean(GOLDEN_CARROT_TAG))
					litterSize += getNumberBetween(level.random, minGoldenCarrotBoost, maxGoldenCarrotBoost);

				if(litterSize > 1) {
					for(int i = 1; i < litterSize; i++) {
						AgeableMob newChild = parentA.getBreedOffspring(level, parentB);
						if(newChild != null) {
							Player cause = event.getCausedByPlayer();
							if(cause instanceof ServerPlayer player) {
								player.awardStat(Stats.ANIMALS_BRED);
								CriteriaTriggers.BRED_ANIMALS.trigger(player, parentA, parentB, newChild);
							}

							newChild.setBaby(true);
							newChild.moveTo(parentA.getX(), parentA.getY(), parentA.getZ(), 0.0F, 0.0F);
							level.addFreshEntityWithPassengers(newChild);
						}
					}
				}
			}
		}
	}
}

package org.violetmoon.quark.content.automation.module;

import com.google.common.collect.ImmutableSet;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.sensing.TemptingSensor;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.automation.block.FeedingTroughBlock;
import org.violetmoon.quark.content.automation.block.be.FeedingTroughBlockEntity;
import org.violetmoon.quark.content.automation.client.screen.TroughScreen;
import org.violetmoon.quark.mixin.mixins.accessor.AccessorTemptingSensor;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.event.play.entity.ZEntityJoinLevel;
import org.violetmoon.zeta.event.play.entity.living.ZBabyEntitySpawn;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.Hint;

import java.util.*;
import java.util.function.Predicate;

/**
 * @author WireSegal
 * Created at 9:48 AM on 9/20/19.
 */
@ZetaLoadModule(category = "automation")
public class FeedingTroughModule extends ZetaModule {

    //using a ResourceKey because they're interned, and Holder.Reference#is leverages this for a very efficient implementation
    private static final ResourceKey<PoiType> FEEDING_TROUGH_POI_KEY = Quark.asResourceKey(Registries.POINT_OF_INTEREST_TYPE, "feeding_trough");
    private static final Set<FakePlayer> FREE_FAKE_PLAYERS = new HashSet<>();
    //fake players created are either stored here above or in the cache below.
    //this way each animal has its own player which is needed as they are moved in diff pos
    private static final WeakHashMap<Animal, TroughPointer> NEARBY_TROUGH_CACHE = new WeakHashMap<>();
    private static final ThreadLocal<Boolean> breedingOccurred = ThreadLocal.withInitial(() -> false);
    private static int fakePlayersCount = 0;

    public static BlockEntityType<FeedingTroughBlockEntity> blockEntityType;
    public static MenuType<DispenserMenu> menuType;
    @Hint
    Block feeding_trough;

    @Config(description = "How long, in game ticks, between animals being able to eat from the trough")
    @Config.Min(1)
    public static int cooldown = 30;

    @Config(description = "The maximum amount of animals allowed around the trough's range for an animal to enter love mode")
    public static int maxAnimals = 32;

    @Config(description = "The chance (between 0 and 1) for an animal to enter love mode when eating from the trough")
    @Config.Min(value = 0.0, exclusive = true)
    @Config.Max(1.0)
    public static double loveChance = 0.333333333;

    @Config
    public static double range = 10;

    @Config(description = "Chance that an animal decides to look for a through. Closer it is to 1 the more performance it will take. Decreasing will make animals take longer to find one")
    public static double lookChance = 0.015;

    //TODO: not sure this works properly. it only cancels the first orb
    @PlayEvent
    public void onBreed(ZBabyEntitySpawn.Lowest event) {
        if (event.getCausedByPlayer() == null && event.getParentA().level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT))
            breedingOccurred.set(true);
    }

    @PlayEvent
    public void onOrbSpawn(ZEntityJoinLevel event) {
        if (event.getEntity() instanceof ExperienceOrb && breedingOccurred.get()) {
            event.setCanceled(true);
            breedingOccurred.remove();
        }
    }

    // Both TempingSensor and TemptGoal work by keeping track of a nearby player who is holding food.
    // The Feeding Trough causes mobs to pathfind to it by injecting a fakeplayer into these AI goals, who stands at the
    // location of the Trough and holds food they like.

    // The "realPlayer" parameter represents a real player located by existing TemptingSensor/TemptGoal code.
    // If there is a real player, and they are holding food, we don't swap them for a fakeplayer, so that animals path to
    // real players before they consider pathing to the Trough.
    // We now only call these if a valid realPlayer is not there, hence why we don't need that parameter anymore

    public static @Nullable Player modifyTemptingSensor(TemptingSensor sensor, Animal animal, ServerLevel level) {
        return modifyTempt(level, animal, ((AccessorTemptingSensor) sensor).quark$getTemptations());
    }

    public static @Nullable Player modifyTemptGoal(TemptGoal goal, Animal animal, ServerLevel level) {
        return modifyTempt(level, animal, (Ingredient) goal.items);
    }

    private static @Nullable Player modifyTempt(ServerLevel level, Animal animal, Predicate<ItemStack> temptations) {
        //early-exit conditions
        if (!Quark.ZETA.modules.isEnabled(FeedingTroughModule.class) ||
                !animal.canFallInLove() ||
                animal.getAge() != 0
        ) {
            return null;
        }

        //do we already know about a nearby trough?
        var iterator = NEARBY_TROUGH_CACHE.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            TroughPointer pointer = entry.getValue();
            if (!pointer.valid(entry.getKey())) {
                iterator.remove();
                // add fake player back to the list
                FREE_FAKE_PLAYERS.add(pointer.fakePlayer);
            }
        }
        TroughPointer pointer = NEARBY_TROUGH_CACHE.get(animal);

        //There's no cached trough nearby.
        //Randomize whether we actually look for a new trough, to hopefully not eat all the tick time.
        if (pointer == null && level.random.nextFloat() <= lookChance*20) {
            pointer = TroughPointer.find(level, animal, temptations);
            if (pointer != null){
                NEARBY_TROUGH_CACHE.put(animal, pointer);
                // remove from free players list
                FREE_FAKE_PLAYERS.remove(pointer.fakePlayer);
            }
        }

        //did we find one?
        if (pointer != null) {
            pointer.tryEatingOrTickCooldown(animal);

            if (!pointer.isOnCooldown()) {

                //if the animal can see it, direct the animal to this trough's fakeplayer
                BlockPos location = pointer.pos;
                Vec3 eyesPos = animal.position().add(0, animal.getEyeHeight(), 0);
                Vec3 targetPos = new Vec3(location.getX(), location.getY(), location.getZ()).add(0.5, 0.0625, 0.5);
                BlockHitResult ray = level.clip(new ClipContext(eyesPos, targetPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, animal));
                if (ray.getType() == HitResult.Type.BLOCK && ray.getBlockPos().equals(location)) {
                    return pointer.fakePlayer;
                }
            }
        }

        return null;
    }


    @LoadEvent
    public final void register(ZRegister event) {
        feeding_trough = new FeedingTroughBlock("feeding_trough", this,
                Block.Properties.of().mapColor(MapColor.WOOD).ignitedByLava().strength(0.6F).sound(SoundType.WOOD));

        blockEntityType = BlockEntityType.Builder.of(FeedingTroughBlockEntity::new, feeding_trough).build(null);
        event.getRegistry().register(blockEntityType, "feeding_trough", Registries.BLOCK_ENTITY_TYPE);

        PoiType feedingTroughPoi = new PoiType(ImmutableSet.copyOf(feeding_trough.getStateDefinition().getPossibleStates()), 1, 32);
        event.getRegistry().register(feedingTroughPoi, FEEDING_TROUGH_POI_KEY.location(), Registries.POINT_OF_INTEREST_TYPE);

        menuType = IMenuTypeExtension.create((windowId, inv, data) -> new DispenserMenu(windowId, inv));
        event.getRegistry().register(menuType, "feeding_trough", Registries.MENU);
    }


    private static final class TroughPointer {
        private final BlockPos pos;
        private final FakePlayer fakePlayer;
        private final Predicate<ItemStack> temptations;
        private int eatCooldown = 0; //Ideally cooldown should be per entity... Assuming troughs don't change much this is fine
        private int giveUpCooldown = 20 * 20; //max seconds till we give up

        private TroughPointer(BlockPos pos, FakePlayer player, Predicate<ItemStack> temptations) {
            this.pos = pos;
            this.fakePlayer = player;
            this.temptations = temptations;
        }

        // This is a bit ugly. 0 = new pointer, 1 = end of life, other = ticking cooldown
        // Once a through is found and an animal is fed, its considered valid until cooldown runs out.
        // Then its invalidated so animals can find possibly closer ones
        boolean valid(Animal animal) {
            if (animal.isRemoved() || !animal.isAlive() || fakePlayer.level() != animal.level() || pos.distSqr(animal.blockPosition()) > range * range) {
                return false;
            }
            if (eatCooldown == 1){
                return false;
            }
            if (giveUpCooldown <= 0){
                return false;
            }
            if (eatCooldown != 0) return true;

            //check if it has food and tile is valid
            if(animal.level().getBlockEntity(pos) instanceof FeedingTroughBlockEntity trough){
                //this should be called in tick, but we save one tile call by doing this...
                trough.updateFoodHolder(animal, temptations, fakePlayer);
                //if it still has food
                return !fakePlayer.getMainHandItem().isEmpty();
            }
            return false;
        }

        void tryEatingOrTickCooldown(Animal animal) {
            giveUpCooldown--;
            if (eatCooldown == 0) {
                float feedDistance = 0.5f +animal.getBbWidth()*1.8f;
                if (pos.distToCenterSqr(animal.position()) < (feedDistance * feedDistance)) {
                    if (animal.level().getBlockEntity(pos) instanceof FeedingTroughBlockEntity trough) {
                        switch (trough.tryFeedingAnimal(animal)) {
                            case FED -> eatCooldown = cooldown; // just fed. set normal cooldown
                            case SECS -> eatCooldown = 1; // remove immediately, as it will use animal own love cooldown for feeding again
                        }
                    }
                }
            } else eatCooldown--;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (TroughPointer) obj;
            return Objects.equals(this.pos, that.pos) &&
                    Objects.equals(this.fakePlayer, that.fakePlayer);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pos, fakePlayer);
        }

        // If animal cant eat.
        // Pointer won't be erased
        // until cooldown is 0. Maybe would have been better with entity nbt data like it was so it persisted
        public boolean isOnCooldown() {
            return eatCooldown != 0;
        }


        @Nullable
        static TroughPointer find(ServerLevel level, Animal animal, Predicate<ItemStack> temptations) {
            // this is an expensive part
            BlockPos position = animal.getOnPos();
            Optional<BlockPos> opt = level.getPoiManager().findClosest(
                    holder -> holder.is(FEEDING_TROUGH_POI_KEY), p -> p.distSqr(position) <= range * range,
                    position, (int) range, PoiManager.Occupancy.ANY);
            if (opt.isPresent()) {
                BlockPos pos = opt.get();

                if (level.getBlockEntity(pos) instanceof FeedingTroughBlockEntity trough) {
                    //only returns if it has the right food
                    FakePlayer foodHolder = getOrCreateFakePlayer(level);
                    if (foodHolder != null) {
                        trough.updateFoodHolder(animal, temptations, foodHolder);
                        // if it has a food item
                        if (!foodHolder.getMainHandItem().isEmpty()) {
                            return new TroughPointer(pos, foodHolder, temptations);
                        }
                    }
                    return null;
                }
            }
            return null;
        }
    }

    private static FakePlayer getOrCreateFakePlayer(ServerLevel serverLevel){
        Optional<FakePlayer> any = FREE_FAKE_PLAYERS.stream().findAny();
        if(any.isEmpty()){
            GameProfile dummyProfile = new GameProfile(UUID.randomUUID(), "[FeedingTrough-"+ ++fakePlayersCount+"]");
            var p = FakePlayerFactory.get(serverLevel, dummyProfile);
            FREE_FAKE_PLAYERS.add(p);
            return p;
        }
        else {
            return any.get();
        }
    }

    // TODO: Replace with Zeta event
    @EventBusSubscriber
    public static final class Client {

        @SubscribeEvent
        public void clientSetup(RegisterMenuScreensEvent event) {
            event.register(menuType, TroughScreen::new);
        }
    }

    /*
    @ZetaLoadModule(clientReplacement = true)
    public static final class Client extends FeedingTroughModule{

        @LoadEvent
        public void clientSetup(ZClientSetup event) {
            event.enqueueWork(() -> MenuScreens.register(menuType, TroughScreen::new));
        }
    }
     */
}

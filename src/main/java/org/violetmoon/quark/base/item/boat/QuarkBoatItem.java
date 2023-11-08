package org.violetmoon.quark.base.item.boat;

import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.violetmoon.quark.base.handler.WoodSetHandler;
import org.violetmoon.quark.base.item.QuarkItem;
import org.violetmoon.zeta.module.ZetaModule;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Predicate;

public class QuarkBoatItem extends QuarkItem {

	private static final Predicate<Entity> ENTITY_PREDICATE = EntitySelector.NO_SPECTATORS.and(Entity::isPickable);

	public final String type;
	private final boolean chest;

	public QuarkBoatItem(String type, ZetaModule module, boolean chest) {
		super(type + (chest ? "_chest" : "") + "_boat", module,
				(new Item.Properties()).stacksTo(1).tab(CreativeModeTab.TAB_TRANSPORTATION));

		this.type = type;
		this.chest = chest;
	}

	// Vanilla copy
	@Nonnull
	@Override
	public InteractionResultHolder<ItemStack> use(@Nonnull Level world, Player player, @Nonnull InteractionHand hand) {
		ItemStack itemstack = player.getItemInHand(hand);
		HitResult hitresult = getPlayerPOVHitResult(world, player, ClipContext.Fluid.ANY);
		if (hitresult.getType() == HitResult.Type.MISS) {
			return InteractionResultHolder.pass(itemstack);
		} else {
			Vec3 view = player.getViewVector(1.0F);
			List<Entity> list = world.getEntities(player, player.getBoundingBox().expandTowards(view.scale(5.0D)).inflate(1.0D), ENTITY_PREDICATE);
			if (!list.isEmpty()) {
				Vec3 eyes = player.getEyePosition();

				for(Entity entity : list) {
					AABB aabb = entity.getBoundingBox().inflate(entity.getPickRadius());
					if (aabb.contains(eyes)) {
						return InteractionResultHolder.pass(itemstack);
					}
				}
			}

			if (hitresult.getType() == HitResult.Type.BLOCK) {
				Boat boat =
						chest ? new QuarkChestBoat(world, hitresult.getLocation().x, hitresult.getLocation().y, hitresult.getLocation().z)
								: new QuarkBoat(world, hitresult.getLocation().x, hitresult.getLocation().y, hitresult.getLocation().z);

				((IQuarkBoat) boat).setQuarkBoatTypeObj(WoodSetHandler.getQuarkBoatType(type));
				boat.setYRot(player.getYRot());
				if (!world.noCollision(boat, boat.getBoundingBox())) {
					return InteractionResultHolder.fail(itemstack);
				} else {
					if (!world.isClientSide) {
						world.addFreshEntity(boat);
						world.gameEvent(player, GameEvent.ENTITY_PLACE, new BlockPos(hitresult.getLocation()));
						if (!player.getAbilities().instabuild) {
							itemstack.shrink(1);
						}
					}

					player.awardStat(Stats.ITEM_USED.get(this));
					return InteractionResultHolder.sidedSuccess(itemstack, world.isClientSide());
				}
			} else {
				return InteractionResultHolder.pass(itemstack);
			}
		}
	}

}

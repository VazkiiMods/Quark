package org.violetmoon.quark.addons.oddities.module;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.violetmoon.quark.addons.oddities.client.render.entity.TotemOfHoldingRenderer;
import org.violetmoon.quark.addons.oddities.entity.TotemOfHoldingEntity;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.tweaks.compat.TotemOfHoldingCuriosCompat;
import org.violetmoon.zeta.client.event.load.ZAddModels;
import org.violetmoon.zeta.client.event.load.ZClientSetup;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.event.play.entity.living.ZLivingDrops;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;

import java.util.Collection;
import java.util.Objects;

/**
 * @author WireSegal
 *         Created at 1:21 PM on 3/30/20.
 */
@ZetaLoadModule(category = "oddities")
public class TotemOfHoldingModule extends ZetaModule {
	private static final String TAG_LAST_TOTEM = "quark:lastTotemOfHolding";

	private static final String TAG_DEATH_X = "quark:deathX";
	private static final String TAG_DEATH_Z = "quark:deathZ";
	private static final String TAG_DEATH_DIM = "quark:deathDim";

	public static EntityType<TotemOfHoldingEntity> totemType;

	@Config(description = "Set this to false to remove the behaviour where totems destroy themselves if the player dies again.")
	public static boolean darkSoulsMode = true;

	@Config(name = "Spawn Totem on PVP Kill", description = "Totem will always spawn if the player killer is themselves.")
	public static boolean enableOnPK = false;

	@Config(description = "Set this to true to make it so that if a totem is destroyed, the items it holds are destroyed alongside it rather than dropped")
	public static boolean destroyLostItems = false;

	@Config(description = "Set this to false to only allow the owner of a totem to collect its items rather than any player")
	public static boolean allowAnyoneToCollect = true;

	@LoadEvent
	public final void register(ZRegister event) {
		totemType = EntityType.Builder.of(TotemOfHoldingEntity::new, MobCategory.MISC)
				.sized(0.5F, 1F)
				.updateInterval(128) // update interval
				.fireImmune()
				.setShouldReceiveVelocityUpdates(false)
				.build("totem");
		Quark.ZETA.registry.register(totemType, "totem", Registries.ENTITY_TYPE);
	}

	@PlayEvent
	public void onPlayerDrops(ZLivingDrops.Lowest event) {
		LivingEntity entity = event.getEntity();
		if(!(entity instanceof Player player))
			return;

		Collection<ItemEntity> drops = event.getDrops();

		if(!event.isCanceled() && (enableOnPK || !(event.getSource().getEntity() instanceof Player) || entity == event.getSource().getEntity())) {
			CompoundTag data = player.getPersistentData();
			CompoundTag persistent = data.getCompound(Player.PERSISTED_NBT_TAG);

			if(!drops.isEmpty()) {
				TotemOfHoldingEntity totem = new TotemOfHoldingEntity(totemType, player.level());
				totem.setPos(player.getX(), Math.max(player.level().getMinBuildHeight() + 3, player.getY() + 1), player.getZ());
				totem.setOwner(player);
				totem.setCustomName(player.getDisplayName());
				drops.stream()
						.filter(Objects::nonNull)
						.map(ItemEntity::getItem)
						.filter(stack -> !stack.isEmpty())
						.forEach(totem::addItem);
				if (zeta().isModLoaded("curios"))
					TotemOfHoldingCuriosCompat.saveCurios(player, totem);
				if(!player.level().isClientSide)
					player.level().addFreshEntity(totem);

				persistent.putString(TAG_LAST_TOTEM, totem.getUUID().toString());

				event.setCanceled(true);
			} else
				persistent.putString(TAG_LAST_TOTEM, "");

			BlockPos pos = player.blockPosition(); // getPosition
			persistent.putInt(TAG_DEATH_X, pos.getX());
			persistent.putInt(TAG_DEATH_Z, pos.getZ());
			persistent.putString(TAG_DEATH_DIM, player.level().dimension().location().toString());

			if(!data.contains(Player.PERSISTED_NBT_TAG))
				data.put(Player.PERSISTED_NBT_TAG, persistent);
		}
	}

	public static String getTotemUUID(Player player) {
		CompoundTag cmp = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
		if(cmp.contains(TAG_LAST_TOTEM))
			return cmp.getString(TAG_LAST_TOTEM);

		return "";
	}

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends TotemOfHoldingModule {
		@LoadEvent
		public final void clientSetup(ZClientSetup event) {
			EntityRenderers.register(totemType, TotemOfHoldingRenderer::new);
		}

		@LoadEvent
		public void registerAdditionalModels(ZAddModels event) {
			event.register(ModelResourceLocation.standalone(Quark.asResource("extra/totem_of_holding")));
		}
	}
}

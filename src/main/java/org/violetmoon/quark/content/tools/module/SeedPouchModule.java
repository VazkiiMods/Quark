package org.violetmoon.quark.content.tools.module;

import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.tools.client.tooltip.SeedPouchClientTooltipComponent;
import org.violetmoon.quark.content.tools.item.SeedPouchItem;
import org.violetmoon.zeta.client.event.load.ZClientSetup;
import org.violetmoon.zeta.client.event.load.ZTooltipComponents;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.event.play.entity.ZItemEntityPickup;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.Hint;

import java.util.List;

@ZetaLoadModule(category = "tools")
public class SeedPouchModule extends ZetaModule {

	@Hint
	public static Item seed_pouch;

	public static final TagKey<Item> seedPouchHoldableTag = ItemTags.create(Quark.asResource("seed_pouch_holdable"));
	public static final TagKey<Item> seedPouchFertilizersTag = ItemTags.create(Quark.asResource("seed_pouch_fertilizers"));
	public static final TagKey<Item> seedPouchVerticalPlacementTag = ItemTags.create(Quark.asResource("seed_pouch_vertical_placement"));

	@Config
	public static int maxItems = 640;
	@Config
	public static boolean showAllVariantsInCreative = true;
	@Config
	public static int shiftRange = 3;

	@Config(description = "Allow putting bone meal into the Seed Pouch (or anything else in the tag 'quark:seed_pouch_fertilizers')")
	public static boolean allowFertilizer = true;
	@Config
	public static int fertilizerShiftRange = 3;

	@LoadEvent
	public final void register(ZRegister event) {
		seed_pouch = new SeedPouchItem(this);
	}

	@PlayEvent
	public void onItemPickup(ZItemEntityPickup event) {
		Player player = event.getPlayer();
		ItemStack toPickup = event.getItem().getItem();

		for(ItemStack pouch : List.of(player.getMainHandItem(), player.getOffhandItem())) {
			if(pouch.getItem() != seed_pouch || pouch.getCount() != 1)
				continue;

			if(SeedPouchItem.mutateContents(pouch, contents ->
				!contents.isEmpty() &&
				contents.absorb(toPickup) // <- mutates 'toPickup' if the items fit
			)) {
				if(player.level() instanceof ServerLevel slevel)
					slevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BUNDLE_INSERT,
						SoundSource.PLAYERS, 0.2F, (slevel.random.nextFloat() - slevel.random.nextFloat()) * 1.4F + 2.0F);
				break;
			}
		}
	}

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends SeedPouchModule {
		@LoadEvent
		public void clientSetup(ZClientSetup e) {
			e.enqueueWork(() ->
				ItemProperties.register(seed_pouch, Quark.asResource("pouch_items"), (ClampedItemPropertyFunction) (pouch, level, entityIn, pSeed) -> {
					SeedPouchItem.PouchContents contents = SeedPouchItem.getContents(pouch);

					if(entityIn instanceof Player player && contents.canFit(player.containerMenu.getCarried()))
						return 0F; //Ensure the pouch appears open

					int count = contents.getCount();
					return count == 0 ? 0F : count / (float) SeedPouchModule.maxItems;
				}));
		}

		@LoadEvent
		public void registerClientTooltipComponentFactories(ZTooltipComponents event) {
			event.register(SeedPouchItem.Tooltip.class, t -> new SeedPouchClientTooltipComponent(t.stack()));
		}
	}
}

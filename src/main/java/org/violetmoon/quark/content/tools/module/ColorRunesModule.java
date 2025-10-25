package org.violetmoon.quark.content.tools.module;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.components.QuarkDataComponents;
import org.violetmoon.quark.base.network.message.UpdateTridentMessage;
import org.violetmoon.quark.content.tools.base.RuneColor;
import org.violetmoon.quark.content.tools.client.render.GlintRenderTypes;
import org.violetmoon.quark.content.tools.item.RuneItem;
import org.violetmoon.quark.content.tools.recipe.SmithingRuneRecipe;
import org.violetmoon.quark.mixin.mixins.accessor.AccessorAbstractArrow;
import org.violetmoon.quark.mixin.mixins.accessor.AccessorArmorTrim;
import org.violetmoon.zeta.advancement.ManualTrigger;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.event.play.entity.player.ZPlayerTick;
import org.violetmoon.zeta.event.play.loading.ZLootTableLoad;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.Hint;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author WireSegal
 *         Hacked by svenhjol
 *         Created at 1:52 PM on 8/17/19.
 */
@ZetaLoadModule(category = "tools")
public class ColorRunesModule extends ZetaModule {

	private static final ThreadLocal<RuneColor> targetColor = new ThreadLocal<>();
	@Hint
	public static Item rune;

	@Config
	public static int dungeonWeight = 10;
	@Config
	public static int netherFortressWeight = 8;
	@Config
	public static int jungleTempleWeight = 8;
	@Config
	public static int desertTempleWeight = 8;
	@Config
	public static int itemQuality = 0;

	public static ManualTrigger fullRainbowTrigger;

	public static void setTargetStack(ItemStack stack) {
		setTargetColor(getStackColor(stack));
	}

	public static void setTargetColor(RuneColor color) {
		targetColor.set(color);
	}

	public static RuneColor changeColor() {
		return targetColor.get();
	}

	@Nullable
	public static RuneColor getStackColor(ItemStack target) {
		if(target == null)
			return null;

		RuneColor manualColor = getAppliedStackColor(target);
		if (manualColor != null)
			return manualColor;

		return RuneColor.byName(target.get(QuarkDataComponents.RUNE_COLOR));

	}

	@Nullable
	public static RuneColor getAppliedStackColor(ItemStack target) {
		if(target == null) return null;
		return RuneColor.byName(target.get(QuarkDataComponents.RUNE_COLOR));
	}

	private static final Map<ThrownTrident, ItemStack> TRIDENT_STACK_REFERENCES = new WeakHashMap<>();

	public static void syncTrident(Consumer<CustomPacketPayload> packetConsumer, ThrownTrident trident, boolean force) {
		ItemStack stack = ((AccessorAbstractArrow)trident).quark$getPickupItem();
		ItemStack prev = TRIDENT_STACK_REFERENCES.get(trident);
		if(force || prev == null || ItemStack.isSameItemSameComponents(stack, prev))
			packetConsumer.accept(new UpdateTridentMessage(trident.getId(), stack));
		else
			TRIDENT_STACK_REFERENCES.put(trident, stack);
	}

	public static ItemStack withRune(ItemStack stack, @Nullable RuneColor color) {
		if (color != null) {
			stack.set(QuarkDataComponents.RUNE_COLOR, color.getSerializedName());
		}
		return stack;
	}

	@LoadEvent
	public final void register(ZRegister event) {
		event.getRegistry().register(SmithingRuneRecipe.SERIALIZER, "smithing_rune", Registries.RECIPE_SERIALIZER);

		rune = new RuneItem("smithing_template_rune", this);

		fullRainbowTrigger = event.getAdvancementModifierRegistry().registerManualTrigger("full_rainbow");
	}

	@PlayEvent
	public void onLootTableLoad(ZLootTableLoad event) {
		int weight = 0;

		if(event.getName().equals(BuiltInLootTables.SIMPLE_DUNGEON))
			weight = dungeonWeight;
		else if(event.getName().equals(BuiltInLootTables.NETHER_BRIDGE))
			weight = netherFortressWeight;
		else if(event.getName().equals(BuiltInLootTables.JUNGLE_TEMPLE))
			weight = jungleTempleWeight;
		else if(event.getName().equals(BuiltInLootTables.DESERT_PYRAMID))
			weight = desertTempleWeight;

		if(weight > 0) {
			LootPoolEntryContainer entry = LootItem.lootTableItem(rune)
					.setWeight(weight)
					.setQuality(itemQuality)
					.build();
			event.add(entry);
		}
	}

	@PlayEvent
	public void onPlayerTick(ZPlayerTick.Start event) {
		final String tag = "quark:what_are_you_gay_or_something";
		Player player = event.getPlayer();

		boolean wasRainbow = player.getPersistentData().getBoolean(tag);
		boolean rainbow = isPlayerRainbow(player);

		if(wasRainbow != rainbow) {
			player.getPersistentData().putBoolean(tag, rainbow);
			if(rainbow && player instanceof ServerPlayer sp)
				fullRainbowTrigger.trigger(sp);
		}
	}

	private boolean isPlayerRainbow(Player player) {
		Set<EquipmentSlot> checks = ImmutableSet.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);

		for(EquipmentSlot slot : checks) {
			ItemStack stack = player.getItemBySlot(slot);
			if(stack.isEmpty() || getStackColor(stack) != RuneColor.RAINBOW)
				return false;
		}

		return true;
	}

	public static boolean canHaveRune(ItemStack stack) {
		return stack.isEnchanted() || (stack.getItem() == Items.COMPASS && stack.has(DataComponents.LODESTONE_TRACKER)); // isLodestoneCompass = is lodestone compass
	}

	public static Component extremeRainbow(Component component) {
		String emphasis = component.getString();

		float time = Quark.proxy.getVisualTime();

		MutableComponent emphasized = Component.empty();
		for (int i = 0; i < emphasis.length(); i++) {
			emphasized.append(rainbow(Component.literal("" + emphasis.charAt(i)), i, time));
		}

		return emphasized;
	}

	private static MutableComponent rainbow(MutableComponent component, int shift, float time) {
		return component.withStyle((s) -> s.withColor(
			TextColor.fromRgb(Mth.hsvToRgb((time + shift) * 2 % 360 / 360F, 1F, 1F))));
	}

    public static void appendRuneTooltip(ItemStack stack, List<Component> components) {
        ArmorTrim trim = stack.get(DataComponents.TRIM);
		RuneColor color = ColorRunesModule.getAppliedStackColor(stack);

        if (trim != null && !((AccessorArmorTrim) trim).showInTooltip()) {
			//There IS a trim on this item and showInTooltip is false
            return;
        }
		else {
			//there is NOT a trim on this item, or there is a trim but showInTooltip is true
			if (color != null) {
				if (!components.contains(AccessorArmorTrim.getUPGRADE_TITLE())) {
					components.add(AccessorArmorTrim.getUPGRADE_TITLE());
				}
				//only add Upgrade: if it's not already there

				MutableComponent baseComponent = Component.translatable("rune.quark." + color.getName());
				if (color == RuneColor.RAINBOW) {
					components.add(CommonComponents.space().append(ColorRunesModule.extremeRainbow(baseComponent)));
				} else {
					components.add(CommonComponents.space().append(baseComponent.withStyle((style) -> style.withColor(color.getTextColor()))));
				}
			}
		}
    }

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends ColorRunesModule {

		public static RenderType getGlint() {
			return renderType(GlintRenderTypes.glint, RenderType::glint);
		}

		public static RenderType getGlintTranslucent() {
			return renderType(GlintRenderTypes.glintTranslucent, RenderType::glintTranslucent);
		}

		public static RenderType getEntityGlint() {
			return renderType(GlintRenderTypes.entityGlint, RenderType::entityGlint);
		}

		public static RenderType getGlintDirect() {
			return renderType(GlintRenderTypes.glintDirect, RenderType::entityGlintDirect);
		}

		public static RenderType getEntityGlintDirect() {
			return renderType(GlintRenderTypes.entityGlintDirect, RenderType::entityGlintDirect);
		}

		public static RenderType getArmorGlint() {
			return renderType(GlintRenderTypes.armorGlint, RenderType::armorEntityGlint);
		}

		public static RenderType getArmorEntityGlint() {
			return renderType(GlintRenderTypes.armorEntityGlint, RenderType::armorEntityGlint);
		}

		private static RenderType renderType(Map<RuneColor, RenderType> map, Supplier<RenderType> vanilla) {
			RuneColor color = changeColor();
			return color != null ? map.get(color) : vanilla.get();
		}
	}
}

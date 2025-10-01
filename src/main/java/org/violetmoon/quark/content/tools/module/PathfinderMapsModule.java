package org.violetmoon.quark.content.tools.module;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.components.QuarkDataComponents;
import org.violetmoon.quark.catnip.animation.AnimationTickHolder;
import org.violetmoon.quark.content.tools.item.PathfindersQuillItem;
import org.violetmoon.quark.content.tools.loot.InBiomeCondition;
import org.violetmoon.zeta.advancement.ManualTrigger;
import org.violetmoon.zeta.client.event.load.ZAddItemColorHandlers;
import org.violetmoon.zeta.client.event.load.ZClientSetup;
import org.violetmoon.zeta.client.event.play.ZRenderGuiOverlay;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.config.type.IConfigType;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.event.play.entity.living.ZLivingTick;
import org.violetmoon.zeta.event.play.entity.player.ZPlayerTick;
import org.violetmoon.zeta.event.play.loading.ZVillagerTrades;
import org.violetmoon.zeta.event.play.loading.ZWandererTrades;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.Hint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@ZetaLoadModule(category = "tools")
public class PathfinderMapsModule extends ZetaModule {

	private static final String TAG_CHECKED_FOR_PATHFINDER = "quark:checked_pathfinder";

	private static final Object mutex = new Object();

	public static List<TradeInfo> builtinTrades = new ArrayList<>();
	public static List<TradeInfo> customTrades = new ArrayList<>();
	public static List<TradeInfo> tradeList = new ArrayList<>();

	@Config(description = """
			In this section you can add custom Pathfinder Maps. This works for both vanilla and modded biomes.
			Each custom map must be on its own line.
			The format for a custom map is as follows:
			<id>,<level>,<min_price>,<max_price>,<color>,<name>

			With the following descriptions:
			 - <id> being the biome's ID NAME. You can find vanilla names here - https://minecraft.wiki/w/Biome#Biome_IDs
			 - <level> being the Cartographer villager level required for the map to be unlockable
			 - <min_price> being the cheapest (in Emeralds) the map can be
			 - <max_price> being the most expensive (in Emeralds) the map can be
			 - <color> being a hex color (without the #) for the map to display. You can generate one here - https://htmlcolorcodes.com/

			Here's an example of a map to locate Ice Mountains:
			minecraft:ice_mountains,2,8,14,FF7FE4FF""")
	private final List<String> customs = new ArrayList<>();

    public static LootItemConditionType inBiomeConditionType;

	public static ManualTrigger pathfinderMapTrigger;

	@Hint
	public static Item pathfinders_quill;

	@Config(description = "Set to false to make it so the default quark Pathfinder Map Built-In don't get added, and only the custom ones do")
	public static boolean applyDefaultTrades = true;

	@Config(description = "How many steps in the search should the Pathfinder's Quill do per tick? The higher this value, the faster it'll find a result, but the higher chance it'll lag the game while doing so")
	public static int pathfindersQuillSpeed = 32;

	@Config(description = "Experimental. Determines if quills should be multithreaded instead. Will ignore quill speed. This could drastically improve performance as it execute the logic off the main thread ideally causing no lag at all")
	public static boolean multiThreaded = true;

	@Config(description = "Allows retrying after a pathfinder quill fails to find a biome nearby. Turn off if you think its op")
	public static boolean allowRetrying = true;

	@Config
	public static int searchRadius = 6400;
	@Config
	public static int xpFromTrade = 5;

	@Config
	public static boolean addToCartographer = true;
	@Config
	public static boolean addToWanderingTraderForced = true;
	@Config
	public static boolean addToWanderingTraderGeneric = false;
	@Config
	public static boolean addToWanderingTraderRare = false;

	@Config
	public static boolean drawHud = true;
	@Config
	public static boolean hudOnTop = false;

	@LoadEvent
	public final void register(ZRegister event) {
		loadTradeInfo(Biomes.SNOWY_PLAINS, true, 4, 8, 14, 0xFF7FE4FF);
		loadTradeInfo(Biomes.WINDSWEPT_HILLS, true, 4, 8, 14, 0xFF8A8A8A);
		loadTradeInfo(Biomes.DARK_FOREST, true, 4, 8, 14, 0xFF00590A);
		loadTradeInfo(Biomes.DESERT, true, 4, 8, 14, 0xFFCCB94E);
		loadTradeInfo(Biomes.SAVANNA, true, 4, 8, 14, 0xFF9BA562);
		loadTradeInfo(Biomes.SWAMP, true, 4, 12, 18, 0xFF22370F);
		loadTradeInfo(Biomes.MANGROVE_SWAMP, true, 4, 12, 18, 0xFF22370F);
		loadTradeInfo(Biomes.OLD_GROWTH_PINE_TAIGA, true, 4, 12, 18, 0xFF5B421F);

		loadTradeInfo(Biomes.FLOWER_FOREST, true, 5, 16, 22, 0xFFCE46E2);
		loadTradeInfo(Biomes.JUNGLE, true, 5, 16, 22, 0xFF22B600);
		loadTradeInfo(Biomes.BAMBOO_JUNGLE, true, 5, 16, 22, 0xFF3DE217);
		loadTradeInfo(Biomes.BADLANDS, true, 5, 16, 22, 0xFFC67F22);
		loadTradeInfo(Biomes.MUSHROOM_FIELDS, true, 5, 20, 26, 0xFF4D4273);
		loadTradeInfo(Biomes.ICE_SPIKES, true, 5, 20, 26, 0xFF1EC0C9);
		loadTradeInfo(Biomes.CHERRY_GROVE, true, 5, 20, 26, 0xFFE9A9E8);

		inBiomeConditionType = new LootItemConditionType(InBiomeCondition.CODEC);
		Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, Quark.asResource("in_biome"), inBiomeConditionType);

		pathfinderMapTrigger = event.getAdvancementModifierRegistry().registerManualTrigger("pathfinder_map_center");

		pathfinders_quill = new PathfindersQuillItem(this);
	}

	@PlayEvent
	public void onTradesLoaded(ZVillagerTrades event) {
		if(event.getType() == VillagerProfession.CARTOGRAPHER && addToCartographer)
			synchronized (mutex) {
				Int2ObjectMap<List<ItemListing>> trades = event.getTrades();
				for(TradeInfo info : tradeList)
					if(info != null)
						trades.get(info.level).add(new PathfinderQuillTrade(info, true));
			}
	}

	@PlayEvent
	public void onWandererTradesLoaded(ZWandererTrades event) {
		if(!addToWanderingTraderForced && (addToWanderingTraderGeneric || addToWanderingTraderRare))
			synchronized (mutex) {
				if(!tradeList.isEmpty()) {
					List<PathfinderQuillTrade> quillTrades = tradeList.stream().map(info -> new PathfinderQuillTrade(info, false)).collect(Collectors.toList());

					MultiTrade mt = new MultiTrade(quillTrades);
					if(addToWanderingTraderGeneric)
						event.getGenericTrades().add(mt);
					if(addToWanderingTraderRare)
						event.getRareTrades().add(mt);
				}
			}
	}

	@PlayEvent
	public void livingTick(ZLivingTick event) {
		if(event.getEntity() instanceof WanderingTrader wt && addToWanderingTraderForced && !wt.getPersistentData().getBoolean(TAG_CHECKED_FOR_PATHFINDER) && !event.getEntity().level().isClientSide) {
            boolean hasPathfinder = false;
            MerchantOffers offers = wt.getOffers();

			for(MerchantOffer offer : offers) {
				if(offer.getResult().is(pathfinders_quill)) {
					hasPathfinder = true;
					break;
				}
			}

			if(!hasPathfinder && !tradeList.isEmpty()) {
				TradeInfo info = tradeList.get(wt.level().random.nextInt(tradeList.size()));

				PathfinderQuillTrade trade = new PathfinderQuillTrade(info, false);
				MerchantOffer offer = trade.getOffer(wt, wt.level().random);
				if(offer != null) {
					offers.addFirst(offer);
				}
			}

			wt.getPersistentData().putBoolean(TAG_CHECKED_FOR_PATHFINDER, true);
		}
	}

	@PlayEvent
	public void playerTick(ZPlayerTick.Start event) {
		Player player = event.getPlayer();
		if (!(player instanceof ServerPlayer)) return;
		if (!tryCheckCenter(player, InteractionHand.MAIN_HAND))
            tryCheckCenter(player, InteractionHand.OFF_HAND);
	}

	private boolean tryCheckCenter(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

		if (stack.getItem() == Items.FILLED_MAP && stack.getOrDefault(QuarkDataComponents.IS_PATHFINDER, false)) {
            MapDecorations mapDecorations = stack.get(DataComponents.MAP_DECORATIONS);
            if (mapDecorations == null) return false;

            for (Map.Entry<String, MapDecorations.Entry> entry : mapDecorations.decorations().entrySet()) {
                if (entry.getKey().equals("+")) {
                    Vec3 pos = player.position();
                    double playerX = pos.x;
                    double playerZ = pos.z;
                    double x = entry.getValue().x();
                    double z = entry.getValue().z();
                    double distSq = (playerX - x) * (playerX - x) + (playerZ - z) * (playerZ - z);

                    if (distSq < 200) {
                        pathfinderMapTrigger.trigger((ServerPlayer) player);
                        return true;
                    }
                }
            }
		}
        return false;
	}

	@LoadEvent
	public final void configChanged(ZConfigChanged event) {
		synchronized (mutex) {
			tradeList.clear();
			customTrades.clear();

			loadCustomMaps(customs);

			if(applyDefaultTrades)
				tradeList.addAll(builtinTrades);

			tradeList.addAll(customTrades);
		}
	}

	private void loadTradeInfo(ResourceKey<Biome> biome, boolean enabled, int level, int minPrice, int maxPrice, int color) {
		builtinTrades.add(new TradeInfo(biome.location(), enabled, level, minPrice, maxPrice, color));
	}

	private void loadCustomTradeInfo(ResourceLocation biome, boolean enabled, int level, int minPrice, int maxPrice, int color) {
		customTrades.add(new TradeInfo(biome, enabled, level, minPrice, maxPrice, color));
	}

	private void loadCustomTradeInfo(String line) throws IllegalArgumentException {
		String[] tokens = line.split(",");
		if(tokens.length != 5 && tokens.length != 6) // Silently ignore old name format
			throw new IllegalArgumentException("Wrong number of parameters " + tokens.length + " (expected 5)");

		ResourceLocation biomeName = ResourceLocation.parse(tokens[0]);
		int level = Integer.parseInt(tokens[1]);
		int minPrice = Integer.parseInt(tokens[2]);
		int maxPrice = Integer.parseInt(tokens[3]);
		int color = Integer.parseInt(tokens[4], 16);

		loadCustomTradeInfo(biomeName, true, level, minPrice, maxPrice, color);
	}

	private void loadCustomMaps(Iterable<String> lines) {
		for(String s : lines)
			try {
				loadCustomTradeInfo(s);
			} catch (IllegalArgumentException e) {
				Quark.LOG.warn("[Custom Pathfinder Maps] Error while reading custom map string \"{}\"", s);
				Quark.LOG.warn("[Custom Pathfinder Maps] - {}", e.getMessage());
			}
	}

	private record MultiTrade(List<? extends ItemListing> parents) implements ItemListing {

		@Override
		public MerchantOffer getOffer(Entity entity, RandomSource random) {
			int idx = random.nextInt(parents.size());
			return parents.get(idx).getOffer(entity, random);
		}
	}

	private record PathfinderQuillTrade(TradeInfo info, boolean hasCompass) implements ItemListing {

		@Override
		public MerchantOffer getOffer(@NotNull Entity entity, @NotNull RandomSource random) {
			if(!info.enabled)
				return null;

			int i = random.nextInt(info.maxPrice - info.minPrice + 1) + info.minPrice;

			ItemStack itemstack = PathfindersQuillItem.forBiome(info.biome.toString(), info.color);
			if(itemstack.isEmpty())
				return null;

			int xp = xpFromTrade * Math.max(1, (info.level - 1));

			if(hasCompass)
				return new MerchantOffer(new ItemCost(Items.EMERALD, i), Optional.of(new ItemCost(Items.COMPASS)), itemstack, 12, xp, 0.2F);
			return new MerchantOffer(new ItemCost(Items.EMERALD, i), itemstack, 12, xp, 0.2F);
		}
	}

	public static class TradeInfo implements Predicate<Holder<Biome>>, IConfigType {

		public final ResourceLocation biome;
		public final int color;

		@Config
		public boolean enabled;
		@Config
		public final int level;
		@Config
		public final int minPrice;
		@Config
		public final int maxPrice;

		TradeInfo(ResourceLocation biome, boolean enabled, int level, int minPrice, int maxPrice, int color) {
			this.biome = biome;
			this.enabled = enabled;
			this.level = level;
			this.minPrice = minPrice;
			this.maxPrice = maxPrice;
			this.color = color;
		}

		@Override
		public boolean test(Holder<Biome> biomeHolder) {
			return biomeHolder.is(biome);
		}
	}

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends PathfinderMapsModule {
		@LoadEvent
		public void clientSetup(ZClientSetup e) {
			e.enqueueWork(() ->
                    ItemProperties.register(pathfinders_quill, Quark.asResource("has_biome"), (stack, level, entity, i) ->
                            (PathfindersQuillItem.getTargetBiome(stack) != null) ? 1 : 0));
		}

		@PlayEvent
		public void drawHUD(ZRenderGuiOverlay.Post event) {
			if (drawHud && event.getLayerName().equals(VanillaGuiLayers.HOTBAR) && !Minecraft.getInstance().options.hideGui) {
                Minecraft mc = Minecraft.getInstance();
				GuiGraphics guiGraphics = event.getGuiGraphics();

				if (mc.screen != null || mc.player == null) return;

                ItemStack quill = PathfindersQuillItem.getActiveQuill(mc.player);

                if (quill != null) {
					int textX = 5;
					int textY = PathfinderMapsModule.hudOnTop ? 20 : (event.getWindow().getGuiScaledHeight() - 15);

					guiGraphics.drawString(mc.font, PathfindersQuillItem.getSearchingComponent(), textX, textY, 0xFFFFFFFF, true);

					int quillX = textX;
					int quillY = textY - 15;
					float speed = 0.1F;
					float total = AnimationTickHolder.getTicks() * speed;

					float offsetX = (float) (Math.sin(total) + 1) * 20;
					float offsetY = (float) (Math.sin(total * 8) - 1);

					if (Math.cos(total) < 0)
						offsetY = 0;

					quillX += (int) offsetX;
					quillY += (int) offsetY;

					guiGraphics.renderItem(quill, quillX, quillY);
				}
			}
		}

		@LoadEvent
		public void registerItemColors(ZAddItemColorHandlers event) {
			ItemColor color = (stack, tintIndex) -> tintIndex == 0 ? 0xFFFFFFFF : PathfindersQuillItem.getOverlayColor(stack);
			event.register(color, pathfinders_quill);
		}
	}
}

package org.violetmoon.quark.content.tweaks.module;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.core.registries.Registries;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.tweaks.block.DirtyGlassBlock;
import org.violetmoon.zeta.block.ZetaBlock;
import org.violetmoon.zeta.block.ZetaInheritedPaneBlock;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZCommonSetup;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.item.ZetaItem;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.CreativeTabManager;
import org.violetmoon.zeta.util.MiscUtil;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

/**
 * @author WireSegal
 *         Created at 12:26 PM on 8/24/19.
 */
@ZetaLoadModule(category = "tweaks")
public class GlassShardModule extends ZetaModule {

	public static ZetaBlock dirtyGlass;
	public static ZetaInheritedPaneBlock dirtyGlassPane;

	public static TagKey<Item> shardTag;

	public static Item clearShard;
	public static Item dirtyShard;

	public static final Map<DyeColor, Item> shardColors = new EnumMap<>(DyeColor.class);

	@LoadEvent
	public final void register(ZRegister event) {
		dirtyGlass = new DirtyGlassBlock("dirty_glass", this,
				Block.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(0.3F).sound(SoundType.GLASS));

		dirtyGlassPane = (ZetaInheritedPaneBlock) new ZetaInheritedPaneBlock(dirtyGlass).setCreativeTab(CreativeModeTabs.COLORED_BLOCKS, Blocks.WHITE_STAINED_GLASS_PANE, true);

        CreativeTabManager.startChain(CreativeModeTabs.INGREDIENTS, false, false, Items.PINK_DYE);
        clearShard = new ZetaItem("clear_shard", this, new Item.Properties()).setCreativeTab(CreativeModeTabs.INGREDIENTS, Items.PINK_DYE, false);
		dirtyShard = new ZetaItem("dirty_shard", this, new Item.Properties()).setCreativeTab(CreativeModeTabs.INGREDIENTS);

		for(DyeColor color : MiscUtil.CREATIVE_COLOR_ORDER)
			shardColors.put(color, new ZetaItem(color.getSerializedName() + "_shard", this, new Item.Properties()).setCreativeTab(CreativeModeTabs.INGREDIENTS));
		CreativeTabManager.endChain();
	}

	@LoadEvent
	public final void setup(ZCommonSetup event) {
		shardTag = Quark.asTagKey(Registries.ITEM,"shards");
	}
}

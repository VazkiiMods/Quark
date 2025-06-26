package org.violetmoon.quark.base.handler;

import net.neoforged.fml.common.EventBusSubscriber;

import java.util.HashMap;
import java.util.Map;

//@EventBusSubscriber
public class QuarkRemapHandler {
	//datafixers could have also been used here but good luck figuring them out
	// TODO: Replace with Registry#addAlias/DataFixers

	private static final Map<String, String> REMAP = new HashMap<>();

	static {
		REMAP.put("quark:crafter", "minecraft:crafter");
		REMAP.put("quark:polished_tuff", "minecraft:polished_tuff");
		REMAP.put("quark:bamboo_planks_slab", "minecraft:bamboo_planks_slab");
		REMAP.put("quark:bamboo_planks_stairs", "minecraft:bamboo_planks_stairs");
		REMAP.put("quark:bamboo_fence", "minecraft:bamboo_fence");
		REMAP.put("quark:bamboo_fence_gate", "minecraft:bamboo_fence_gate");
		REMAP.put("quark:bamboo_door", "minecraft:bamboo_door");
		REMAP.put("quark:bamboo_trapdoor", "minecraft:bamboo_trapdoor");
		REMAP.put("quark:bamboo_button", "minecraft:bamboo_button");
		REMAP.put("quark:bamboo_pressure_plate", "minecraft:bamboo_pressure_plate");
		REMAP.put("quark:bamboo_bookshelf", "minecraft:bamboo_bookshelf");
		REMAP.put("quark:bamboo_sign", "minecraft:bamboo_sign");
		REMAP.put("quark:bamboo_mosaic", "minecraft:bamboo_mosaic");
		REMAP.put("quark:bamboo_block", "minecraft:bamboo_block");
		REMAP.put("quark:stripped_bamboo_block", "minecraft:stripped_bamboo_block");
		REMAP.put("quark:egg_parrot_grey", "quark:egg_parrot_gray");
	}


	/*
	@SubscribeEvent
	public static void onRemapBlocks(MissingMappingsEvent event) {
		remapAll(event, BuiltInRegistries.BLOCK);
		remapAll(event, BuiltInRegistries.ITEM);
	}

	private static <T> void remapAll(MissingMappingsEvent event, DefaultedRegistry<T> block) {
		for (var v : event.getMappings(block.key(), Quark.MOD_ID)) {
			String rem = REMAP.get(v.getKey().toString());
			if (rem != null) {
				var b = block.getOptional(ResourceLocation.parse(rem));
				b.ifPresent(v::remap);
			} else v.ignore();
		}
	}
	 */
}

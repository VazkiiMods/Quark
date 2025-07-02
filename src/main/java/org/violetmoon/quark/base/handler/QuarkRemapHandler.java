package org.violetmoon.quark.base.handler;

import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZRegister;

import java.util.HashMap;
import java.util.Map;

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




	//todo: Probably doesnt need to be a registry event. Perhaps we handle this on the Zeta end as well? A Zeta "RemapEvent" could work.

	@LoadEvent
	public static void onRemapBlocks(ZRegister event) {
		for (Map.Entry<String, String> entry : REMAP.entrySet()) {
			BuiltInRegistries.BLOCK.addAlias(ResourceLocation.parse(entry.getKey()), ResourceLocation.parse(entry.getValue()));
			BuiltInRegistries.ITEM.addAlias(ResourceLocation.parse(entry.getKey()), ResourceLocation.parse(entry.getValue()));
		}
	}
}

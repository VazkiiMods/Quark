package org.violetmoon.quark.api.event;

import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

import java.util.Set;

public class GatherToolClassesEvent extends Event {

	public final ItemStack stack;
	public final Set<String> classes;

	public GatherToolClassesEvent(ItemStack stack, Set<String> classes) {
		this.stack = stack;
		this.classes = classes;
	}

}

package org.violetmoon.quark.api.event;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class QuarkModuleEvent extends Event implements ICancellableEvent {

	public final String eventName;

	public QuarkModuleEvent(String eventName) {
		this.eventName = eventName;
	}

}

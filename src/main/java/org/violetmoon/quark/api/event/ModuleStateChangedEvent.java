package org.violetmoon.quark.api.event;

/**
 * Fired when a module's state (enabled/disabled) is changed.
 * Cancel the event to force the module disabled.
 */

public class ModuleStateChangedEvent extends QuarkModuleEvent {

	public final boolean enabled;

	public ModuleStateChangedEvent(String eventName, boolean enabled) {
		super(eventName);
		this.enabled = enabled;
	}

}

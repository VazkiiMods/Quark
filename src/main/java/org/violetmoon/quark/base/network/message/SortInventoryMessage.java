package org.violetmoon.quark.base.network.message;

import org.violetmoon.quark.base.handler.SortingHandler;
import org.violetmoon.zeta.network.IZetaMessage;
import org.violetmoon.zeta.network.IZetaNetworkEventContext;

import java.io.Serial;

public class SortInventoryMessage implements IZetaMessage {

	@Serial
	private static final long serialVersionUID = -4340505435110793951L;

	public boolean forcePlayer;

	public SortInventoryMessage() {}

	public SortInventoryMessage(boolean forcePlayer) {
		this.forcePlayer = forcePlayer;
	}

	@Override
	public boolean receive(IZetaNetworkEventContext context) {
		context.enqueueWork(() -> SortingHandler.sortInventory(context.getSender(), forcePlayer));
		return true;
	}

}

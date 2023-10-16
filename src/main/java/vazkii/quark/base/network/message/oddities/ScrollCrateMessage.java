package vazkii.quark.base.network.message.oddities;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

import vazkii.quark.addons.oddities.inventory.CrateMenu;
import vazkii.zeta.network.IZetaMessage;
import vazkii.zeta.network.IZetaNetworkEventContext;

import java.io.Serial;

public class ScrollCrateMessage implements IZetaMessage {

	@Serial
	private static final long serialVersionUID = -921358009630134620L;

	public boolean down;

	public ScrollCrateMessage() {}

	public ScrollCrateMessage(boolean down) {
		this.down = down;
	}

	@Override
	public boolean receive(IZetaNetworkEventContext context) {
		context.enqueueWork(() -> {
			ServerPlayer player = context.getSender();
			AbstractContainerMenu container = player.containerMenu;

			if(container instanceof CrateMenu crate)
				crate.scroll(down, false);
		});

		return true;
	}

}

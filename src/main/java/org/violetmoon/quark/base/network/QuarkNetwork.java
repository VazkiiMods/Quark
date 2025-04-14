package org.violetmoon.quark.base.network;

import java.time.Instant;

import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.network.message.ChangeHotbarMessage;
import org.violetmoon.quark.base.network.message.DoEmoteMessage;
import org.violetmoon.quark.base.network.message.DoubleDoorMessage;
import org.violetmoon.quark.base.network.message.HarvestMessage;
import org.violetmoon.quark.base.network.message.InventoryTransferMessage;
import org.violetmoon.quark.base.network.message.RequestEmoteMessage;
import org.violetmoon.quark.base.network.message.ScrollOnBundleMessage;
import org.violetmoon.quark.base.network.message.SetLockProfileMessage;
import org.violetmoon.quark.base.network.message.ShareItemC2SMessage;
import org.violetmoon.quark.base.network.message.ShareItemS2CMessage;
import org.violetmoon.quark.base.network.message.SortInventoryMessage;
import org.violetmoon.quark.base.network.message.UpdateTridentMessage;
import org.violetmoon.quark.base.network.message.experimental.PlaceVariantRestoreMessage;
import org.violetmoon.quark.base.network.message.experimental.PlaceVariantUpdateMessage;
import org.violetmoon.quark.base.network.message.oddities.HandleBackpackMessage;
import org.violetmoon.quark.base.network.message.oddities.MatrixEnchanterOperationMessage;
import org.violetmoon.quark.base.network.message.oddities.ScrollCrateMessage;
import org.violetmoon.quark.content.tweaks.module.LockRotationModule;

import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.MessageSignature;

public final class QuarkNetwork {

	// bump when you change messages
	public static final int PROTOCOL_VERSION = 4;

	public static void init() {
		ZetaNetworkHandler network = Quark.ZETA.createNetworkHandler(PROTOCOL_VERSION);
		Quark.ZETA.network = network;

		network.getSerializer().mapHandlers(Instant.class, (buf, field) -> buf.readInstant(), (buf, field, instant) -> buf.writeInstant(instant));
		network.getSerializer().mapHandlers(MessageSignature.class, (buf, field) -> new MessageSignature(buf.accessByteBufWithCorrectSize()), (buf, field, signature) -> MessageSignature.write(buf, signature));
		network.getSerializer().mapHandlers(LastSeenMessages.Update.class, (buf, field) -> new LastSeenMessages.Update(buf), (buf, field, update) -> update.write(buf));

		// Base Quark
		network.register(SortInventoryMessage.class, ZetaNetworkDirection.PLAY_TO_SERVER);
		network.register(InventoryTransferMessage.class, ZetaNetworkDirection.PLAY_TO_SERVER);
		network.register(DoubleDoorMessage.class, ZetaNetworkDirection.PLAY_TO_SERVER);
		network.register(HarvestMessage.class, ZetaNetworkDirection.PLAY_TO_SERVER);
		network.register(RequestEmoteMessage.class, ZetaNetworkDirection.PLAY_TO_SERVER);
		network.register(ChangeHotbarMessage.class, ZetaNetworkDirection.PLAY_TO_SERVER);
		network.register(SetLockProfileMessage.class, ZetaNetworkDirection.PLAY_TO_SERVER);
		network.register(ShareItemC2SMessage.class, ZetaNetworkDirection.PLAY_TO_SERVER);
		network.register(ScrollOnBundleMessage.class, ZetaNetworkDirection.PLAY_TO_SERVER);
		network.getSerializer().mapHandlers(LockRotationModule.LockProfile.class, LockRotationModule.LockProfile::readProfile, LockRotationModule.LockProfile::writeProfile);

		// Oddities
		network.register(HandleBackpackMessage.class, ZetaNetworkDirection.PLAY_TO_SERVER);
		network.register(MatrixEnchanterOperationMessage.class, ZetaNetworkDirection.PLAY_TO_SERVER);
		network.register(ScrollCrateMessage.class, ZetaNetworkDirection.PLAY_TO_SERVER);

		// Experimental
		network.register(PlaceVariantUpdateMessage.class, ZetaNetworkDirection.PLAY_TO_SERVER);
		network.register(PlaceVariantRestoreMessage.class, ZetaNetworkDirection.PLAY_TO_CLIENT);

		// Clientbound
		network.register(DoEmoteMessage.class, ZetaNetworkDirection.PLAY_TO_CLIENT);
		network.register(UpdateTridentMessage.class, ZetaNetworkDirection.PLAY_TO_CLIENT);
		network.register(ShareItemS2CMessage.class, ZetaNetworkDirection.PLAY_TO_CLIENT);
	}

}

package org.violetmoon.quark.base;

import org.violetmoon.quark.integration.lootr.client.ClientLootrIntegration;
import org.violetmoon.quark.integration.lootr.client.IClientLootrIntegration;
import org.violetmoon.zeta.client.ZetaClient;
import org.violetmoon.zeta.util.ZetaSide;
import org.violetmoon.zetaimplforge.client.ForgeZetaClient;

public class QuarkClient {

	static {
		if (Quark.ZETA.side == ZetaSide.SERVER)
			throw new IllegalAccessError("SOMEONE LOADED QuarkClient ON THE SERVER!!!! DON'T DO THAT!!!!!!");
	}

	public static final ZetaClient ZETA_CLIENT = new ForgeZetaClient(Quark.ZETA);

	public static final String MISC_GROUP = "quark.gui.keygroup.misc";
	public static final String INV_GROUP = "quark.gui.keygroup.inv";
	public static final String EMOTE_GROUP = "quark.gui.keygroup.emote";

	public static final IClientLootrIntegration LOOTR_INTEGRATION = Quark.ZETA.modIntegration("lootr",
			() -> ClientLootrIntegration::new,
			() -> IClientLootrIntegration.Dummy::new);

}

package org.violetmoon.quark.content.tweaks.client.emote;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.player.Player;

import net.neoforged.fml.loading.FMLLoader;
import org.violetmoon.quark.base.Quark;

import aurelienribon.tweenengine.Timeline;

public class TemplateSourcedEmote extends EmoteBase {

	private static final boolean DEV_ENV = !FMLLoader.isProduction();

	public TemplateSourcedEmote(EmoteDescriptor desc, Player player, HumanoidModel<?> model, HumanoidModel<?> armorModel, HumanoidModel<?> armorLegsModel) {
		super(desc, player, model, armorModel, armorLegsModel);

		if(shouldLoadTimelineOnLaunch()) {
			Quark.LOG.debug("Loading emote " + desc.getTranslationKey());
			desc.template.readAndMakeTimeline(desc, player, model);
		}
	}

	public boolean shouldLoadTimelineOnLaunch() {
		return DEV_ENV;
	}

	@Override
	public Timeline getTimeline(Player player, HumanoidModel<?> model) {
		return desc.template.getTimeline(desc, player, model);
	}

	@Override
	public boolean usesBodyPart(int part) {
		return desc.template.usesBodyPart(part);
	}

}

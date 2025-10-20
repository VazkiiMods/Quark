package org.violetmoon.quark.content.tweaks.client.emote;

import net.minecraft.resources.ResourceLocation;

import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.tweaks.module.EmotesModule;

public class CustomEmoteDescriptor extends EmoteDescriptor {

	public CustomEmoteDescriptor(String name, String regName, int index) {
		super(CustomEmote.class, name, regName, index, getSprite(name), new CustomEmoteTemplate(name));
	}

	public static ResourceLocation getSprite(String name) {
		ResourceLocation customRes = ResourceLocation.fromNamespaceAndPath(EmoteHandler.CUSTOM_EMOTE_NAMESPACE, name);
		//if(EmotesModule.Client.resourcePack.hasResource(PackType.CLIENT_RESOURCES, customRes))
		if(EmotesModule.Client.resourcePack.hasResource(name))
			return customRes;

		return Quark.asResource("textures/emote/custom.png");
	}

	@Override
	public String getTranslationKey() {
		return ((CustomEmoteTemplate) template).getName();
	}

	@Override
	public String getLocalizedName() {
		return getTranslationKey();
	}

}

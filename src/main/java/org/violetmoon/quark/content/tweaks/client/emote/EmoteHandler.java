/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Quark Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Quark
 *
 * Quark is Open Source and distributed under the
 * CC-BY-NC-SA 3.0 License: https://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB
 *
 * File Created @ [26/03/2016, 21:37:17 (GMT)]
 */
package org.violetmoon.quark.content.tweaks.client.emote;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EmoteHandler {

	public static final String CUSTOM_EMOTE_NAMESPACE = "quark_custom";
	public static final String CUSTOM_PREFIX = "custom:";

	public static final Map<String, EmoteDescriptor> emoteMap = new LinkedHashMap<>();
	private static final Map<String, EmoteBase> playerEmotes = new HashMap<>();

	private static int count;

	public static void clearEmotes() {
		emoteMap.clear();
	}

	public static void addEmote(String name, Class<? extends EmoteBase> clazz) {
		EmoteDescriptor desc = new EmoteDescriptor(clazz, name, name, count++);
		emoteMap.put(name, desc);
	}

	public static void addEmote(String name) {
		addEmote(name, TemplateSourcedEmote.class);
	}

	public static void addCustomEmote(String name) {
		String reg = CUSTOM_PREFIX + name;
		EmoteDescriptor desc = new CustomEmoteDescriptor(name, reg, count++);
		emoteMap.put(reg, desc);
	}

	public static void putEmote(Entity player, String emoteName, int tier) {
		if(player instanceof AbstractClientPlayer clientPlayer && emoteMap.containsKey(emoteName)) {
			putEmote(clientPlayer, emoteMap.get(emoteName), tier);
		}
	}

	private static void putEmote(AbstractClientPlayer player, EmoteDescriptor desc, int tier) {
		String name = player.getGameProfile().getName();
		if(desc == null)
			return;

		if(desc.getTier() > tier)
			return;

		HumanoidModel<?> model = getPlayerModel(player);
		HumanoidModel<?> armorModel = getPlayerArmorModel(player);
		HumanoidModel<?> armorLegModel = getPlayerArmorLegModel(player);

		if(model != null && armorModel != null && armorLegModel != null) {
			resetPlayer(player);
			EmoteBase emote = desc.instantiate(player, model, armorModel, armorLegModel);
			emote.startAllTimelines();
			playerEmotes.put(name, emote);
		}
	}

	public static void updateEmotes(Entity e) {
		if(e instanceof AbstractClientPlayer player) {
			String name = player.getGameProfile().getName();

			if(player.getPose() == Pose.STANDING) {
				if(playerEmotes.containsKey(name)) {
					resetPlayer(player);

					EmoteBase emote = playerEmotes.get(name);
					boolean done = emote.isDone();

					if(!done)
						emote.update();
				}
			}
		}
	}

	public static void preRender(PoseStack stack, Player player) {
		EmoteBase emote = getPlayerEmote(player);
		if(emote != null) {
			stack.pushPose();
			emote.rotateAndOffset(stack);
		}
	}

	public static void postRender(PoseStack stack, Player player) {
		EmoteBase emote = getPlayerEmote(player);
		if(emote != null) {
			stack.popPose();
		}
	}

	public static void onRenderTick(Minecraft mc) {
		Level world = mc.level;
		if(world == null)
			return;

		for(Player player : world.players())
			updatePlayerStatus(player);
	}

	private static void updatePlayerStatus(Player e) {
		if(e instanceof AbstractClientPlayer player) {
			String name = player.getGameProfile().getName();

			if(playerEmotes.containsKey(name)) {
				EmoteBase emote = playerEmotes.get(name);
				boolean done = emote.isDone();
				if(done) {
					playerEmotes.remove(name);
					resetPlayer(player);
				} else
					emote.update();
			}
		}
	}

	public static EmoteBase getPlayerEmote(Player player) {
		return playerEmotes.get(player.getGameProfile().getName());
	}

	private static PlayerRenderer getRenderPlayer(AbstractClientPlayer player) {
		Minecraft mc = Minecraft.getInstance();
		EntityRenderDispatcher manager = mc.getEntityRenderDispatcher();

		EntityRenderer<? extends Player> render = manager.getSkinMap().get(player.getSkin().model());
		return (render instanceof PlayerRenderer playerRenderer) ? playerRenderer : null;
	}

	private static HumanoidModel<?> getPlayerModel(AbstractClientPlayer player) {
		PlayerRenderer render = getRenderPlayer(player);
		return (render != null) ? render.getModel() : null;
	}

	private static HumanoidModel<?> getPlayerArmorModel(AbstractClientPlayer player) {
		return getPlayerArmorModelForSlot(player, EquipmentSlot.CHEST);
	}

	private static HumanoidModel<?> getPlayerArmorLegModel(AbstractClientPlayer player) {
		return getPlayerArmorModelForSlot(player, EquipmentSlot.LEGS);
	}

	private static HumanoidModel<?> getPlayerArmorModelForSlot(AbstractClientPlayer player, EquipmentSlot slot) {
		PlayerRenderer render = getRenderPlayer(player);
		if(render == null) return null;

		List<RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>> list = render.layers;
		for(RenderLayer<?, ?> r : list) {
			if(r instanceof HumanoidArmorLayer)
				return ((HumanoidArmorLayer<?, ?, ?>) r).getArmorModel(slot);
		}

		return null;
	}

	private static void resetPlayer(AbstractClientPlayer player) {
		resetModel(getPlayerModel(player));
		resetModel(getPlayerArmorModel(player));
		resetModel(getPlayerArmorLegModel(player));
	}

	private static void resetModel(HumanoidModel<?> model) {
		if(model != null) {
			resetPart(model.head);
			resetPart(model.hat);
			resetPart(model.body);
			resetPart(model.leftArm);
			resetPart(model.rightArm);
			resetPart(model.leftLeg);
			resetPart(model.rightLeg);
			if(model instanceof PlayerModel<?> pmodel) {
				resetPart(pmodel.jacket);
				resetPart(pmodel.leftSleeve);
				resetPart(pmodel.rightSleeve);
				resetPart(pmodel.leftPants);
				resetPart(pmodel.rightPants);
			}

			ModelAccessor.INSTANCE.resetModel(model);
		}
	}

	private static void resetPart(ModelPart part) {
		if(part != null)
			part.zRot = 0F;
	}
}

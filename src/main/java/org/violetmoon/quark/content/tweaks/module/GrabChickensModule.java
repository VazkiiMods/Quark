package org.violetmoon.quark.content.tweaks.module;

import java.util.List;

import org.violetmoon.quark.base.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.event.play.entity.ZEntityInteract;
import org.violetmoon.zeta.event.play.entity.player.ZPlayerTick;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;

import net.minecraft.client.model.ChickenModel;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayer;

@ZetaLoadModule(category = "tweaks")
public class GrabChickensModule extends ZetaModule {
	
	@Config
	private static boolean needsNoHelmet = true;
	
	@Config(description = "Set to 0 to disable")
	private static int slownessLevel = 1;

	private static boolean staticEnabled;
	
	@LoadEvent
	public final void configChanged(ZConfigChanged event) {
		staticEnabled = enabled;
	}
	
	@PlayEvent
	public void playerInteract(ZEntityInteract event) {
		Entity target = event.getTarget();
		Player player = event.getEntity();
		Level level = event.getLevel();
		
		if(staticEnabled && event.getHand() == InteractionHand.MAIN_HAND 
				&& !player.isCrouching() 
				&& !(player instanceof FakePlayer)
				&& player.getMainHandItem().isEmpty()
				&& canPlayerHostChicken(player) 
				&& target.getType() == EntityType.CHICKEN
				&& !((Chicken) target).isBaby()) {
			List<Entity> passengers = player.getPassengers();
			
			boolean changed = false;
			
			if(passengers.contains(target)) {
				if(!level.isClientSide)
					target.stopRiding();
				
				changed = true;
			} else if(passengers.isEmpty()) {
				if(!level.isClientSide)
					target.startRiding(player, false);
				
				changed = true;
			}
			
			if(changed) {
				if(level instanceof ServerLevel slevel)
					slevel.getChunkSource().chunkMap.broadcast(target, new ClientboundSetPassengersPacket(player));
				else player.swing(InteractionHand.MAIN_HAND);
			}
		}
	}
	
	@PlayEvent
	public void playerTick(ZPlayerTick.Start event) {
		Player player = event.getPlayer();
		Level level = player.level;
		
		if(player.hasPassenger(e -> e.getType() == EntityType.CHICKEN)) {
			if(!canPlayerHostChicken(player)) {
				player.ejectPassengers();
				
				if(level instanceof ServerLevel slevel)
					slevel.getChunkSource().chunkMap.broadcast(player, new ClientboundSetPassengersPacket(player));
			} else if(!player.hasEffect(MobEffects.SLOW_FALLING)) {
				player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 5, 0, true, false));
				
				if(slownessLevel > 0 && !player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN))
					player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, slownessLevel - 1, true, false));
			}
		}
	}
	
	private boolean canPlayerHostChicken(Player player) {
		return (!needsNoHelmet || player.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) && !player.isUnderWater();
	}

	public static class Client {

		//not client-replacement module since it's just somewhere to stick this method
		public static void setRenderChickenFeetStatus(Chicken entity, ChickenModel<Chicken> model) {
			if(!staticEnabled)
				return;

			boolean should = entity.getVehicle() == null || entity.getVehicle().getType() != EntityType.PLAYER;
			model.leftLeg.visible = should;
			model.rightLeg.visible = should;
		}

	}
	
}

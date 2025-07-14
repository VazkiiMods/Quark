package org.violetmoon.quark.content.tools.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;

import org.violetmoon.quark.base.components.QuarkDataComponents;
import org.violetmoon.zeta.item.ZetaItem;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.CreativeTabManager;

public class SlimeInABucketItem extends ZetaItem {

	public static final String TAG_ENTITY_DATA = "slime_nbt";
	public static final String TAG_EXCITED = "excited";

	public SlimeInABucketItem(ZetaModule module) {
		super("slime_in_a_bucket", module,
				new Item.Properties()
						.stacksTo(1)
						.craftRemainder(Items.BUCKET));
		CreativeTabManager.addToCreativeTabNextTo(CreativeModeTabs.TOOLS_AND_UTILITIES, this, Items.TADPOLE_BUCKET, false);
	}

	@Override
	public void inventoryTick(@NotNull ItemStack stack, @NotNull Level world, @NotNull Entity entity, int itemSlot, boolean isSelected) {
		if(world instanceof ServerLevel serverLevel) {
			Vec3 pos = entity.position();
			int x = Mth.floor(pos.x);
			int z = Mth.floor(pos.z);
			boolean slime = isSlimeChunk(serverLevel, x, z);
			boolean excited = Boolean.TRUE.equals(stack.get(QuarkDataComponents.EXCITED));
			if(excited != slime)
				stack.set(QuarkDataComponents.EXCITED, slime);
		}
	}

	@NotNull
	@Override
	public InteractionResult useOn(UseOnContext context) {
		BlockPos pos = context.getClickedPos();
		Direction facing = context.getClickedFace();
		Level worldIn = context.getLevel();
		Player playerIn = context.getPlayer();
		InteractionHand hand = context.getHand();

		double x = pos.getX() + 0.5 + facing.getStepX();
		double y = pos.getY() + 0.5 + facing.getStepY();
		double z = pos.getZ() + 0.5 + facing.getStepZ();

		if(!worldIn.isClientSide) {
			Slime slime = new Slime(EntityType.SLIME, worldIn);

			if((playerIn.getItemInHand(hand).has(QuarkDataComponents.SLIME_NBT))){
				CompoundTag data = playerIn.getItemInHand(hand).get(QuarkDataComponents.SLIME_NBT).copyTag();
				slime.load(data);
				}
			else {
				slime.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1.0);
				slime.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.3);
				slime.setHealth(slime.getMaxHealth());
			}

			slime.setPos(x, y, z);

			worldIn.gameEvent(playerIn, GameEvent.ENTITY_PLACE, slime.position());
			worldIn.addFreshEntity(slime);
			playerIn.swing(hand);
		}

		worldIn.playSound(playerIn, pos, SoundEvents.BUCKET_EMPTY, SoundSource.NEUTRAL, 1.0F, 1.0F);

		if(!playerIn.getAbilities().instabuild)
			playerIn.setItemInHand(hand, new ItemStack(Items.BUCKET));

		return InteractionResult.sidedSuccess(worldIn.isClientSide);
	}

	@NotNull
	@Override
	public Component getName(@NotNull ItemStack stack) {
		if(stack.has(QuarkDataComponents.SLIME_NBT)) {
			CompoundTag cmp = stack.get(QuarkDataComponents.SLIME_NBT).copyTag();
			if(cmp != null && cmp.contains("CustomName")) {

				Component custom = Component.Serializer.fromJson(cmp.getString("CustomName"), RegistryAccess.EMPTY);
				return Component.translatable("item.quark.slime_in_a_bucket.named", custom);
			}
		}

		return super.getName(stack);
	}

	public static boolean isSlimeChunk(ServerLevel world, int x, int z) {
		ChunkPos chunkpos = new ChunkPos(new BlockPos(x, 0, z));
		return WorldgenRandom.seedSlimeChunk(chunkpos.x, chunkpos.z, world.getSeed(), 987234911L).nextInt(10) == 0;
	}

}

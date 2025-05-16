package org.violetmoon.quark.integration.lootr.client;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import noobanidus.mods.lootr.common.client.ClientHooks;
import noobanidus.mods.lootr.neoforge.config.ConfigManager;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.building.module.VariantChestsModule;
import org.violetmoon.quark.integration.lootr.LootrVariantChestBlockEntity;

import java.util.UUID;

public class LootrVariantChestRenderer<T extends LootrVariantChestBlockEntity> extends ChestRenderer<T> {

	private UUID playerIdCache = null;
	protected final boolean isTrap;

	public LootrVariantChestRenderer(BlockEntityRendererProvider.Context context, boolean isTrap) {
		super(context);
		this.isTrap = isTrap;
	}

	@Override
	public Material getMaterial(T tile, ChestType type) {
		if(!(tile.getBlockState().getBlock() instanceof VariantChestsModule.IVariantChest v))
			return null;

		//lazy-init pattern
		if(playerIdCache == null) {
			Player player = ClientHooks.getPlayer();
			if(player != null)
				playerIdCache = player.getUUID();
		}

		boolean opened = tile.isClientOpened() || (tile.getClientOpeners() != null && tile.getClientOpeners().contains(playerIdCache));

		//apply the texture naming convention
		StringBuilder tex = new StringBuilder(v.getTextureFolder())
				.append('/')
				.append(v.getTexturePath())
				.append('/');
		if(isTrap) {
			if(ConfigManager.isVanillaTextures())
				tex.append("trap");
			else if(opened)
				tex.append("lootr_trap_opened");
			else
				tex.append("lootr_trap");
		} else {
			if(ConfigManager.isVanillaTextures())
				tex.append("normal");
			else if(opened)
				tex.append("lootr_opened");
			else
				tex.append("lootr_normal");
		}

		return new Material(Sheets.CHEST_SHEET, Quark.asResource(tex.toString()));
	}

	@Override
	public AABB getRenderBoundingBox(T blockEntity) {
		BlockPos pos1 = blockEntity.getBlockPos().offset(-1, 0, -1);
		BlockPos pos2 = blockEntity.getBlockPos().offset(2, 2, 2);
		return new AABB(new Vec3(pos1.getX(), pos1.getY(), pos1.getZ()), new Vec3(pos2.getX(), pos2.getY(), pos2.getZ()));
	}
}

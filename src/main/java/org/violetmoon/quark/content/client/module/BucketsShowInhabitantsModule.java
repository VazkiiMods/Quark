package org.violetmoon.quark.content.client.module;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.QuarkClient;
import org.violetmoon.quark.base.config.Config;
import org.violetmoon.quark.content.mobs.entity.Crab;
import org.violetmoon.quark.content.mobs.module.CrabsModule;
import org.violetmoon.quark.content.tools.item.SlimeInABucketItem;
import org.violetmoon.quark.content.tools.module.SlimeInABucketModule;
import org.violetmoon.zeta.client.event.load.ZAddItemColorHandlers;
import org.violetmoon.zeta.client.event.load.ZClientSetup;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.ItemNBTHelper;

import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.IntUnaryOperator;

@ZetaLoadModule(category = "client")
public class BucketsShowInhabitantsModule extends ZetaModule {

	@Config
	public boolean showAxolotls = true;
	@Config
	public boolean showCrabs = true;
	@Config
	public boolean showTropicalFish = true;
	@Config
	public boolean showShinySlime = true;

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends BucketsShowInhabitantsModule {

		@LoadEvent
		public void clientSetup(ZClientSetup e) {
			e.enqueueWork(() -> {
				ItemProperties.register(Items.AXOLOTL_BUCKET, new ResourceLocation(Quark.MOD_ID, "variant"),
					new MobBucketVariantProperty(Axolotl.Variant.BY_ID.length, () -> showAxolotls));
				ItemProperties.register(CrabsModule.crab_bucket, new ResourceLocation(Quark.MOD_ID, "variant"),
					new MobBucketVariantProperty(Crab.COLORS, () -> showCrabs));

				ItemProperties.register(SlimeInABucketModule.slime_in_a_bucket, new ResourceLocation(Quark.MOD_ID, "shiny"),
					new ShinyMobBucketProperty(() -> showShinySlime && VariantAnimalTexturesModule.staticEnabled && VariantAnimalTexturesModule.enableShinySlime));

				ItemProperties.register(Items.TROPICAL_FISH_BUCKET, new ResourceLocation(Quark.MOD_ID, "base"),
					new TropicalFishBucketVariantProperty(TropicalFish::getBaseVariant, () -> showTropicalFish));
				ItemProperties.register(Items.TROPICAL_FISH_BUCKET, new ResourceLocation(Quark.MOD_ID, "pattern"),
					new TropicalFishBucketVariantProperty(TropicalFish::getPatternVariant, () -> showTropicalFish));
			});
		}

		@LoadEvent
		public void registerItemColors(ZAddItemColorHandlers evt) {
			ItemColor parent = QuarkClient.ZETA_CLIENT.getItemColor(evt.getItemColors(), Items.TROPICAL_FISH_BUCKET);
			evt.register(new TropicalFishBucketColor(parent, () -> showTropicalFish), Items.TROPICAL_FISH_BUCKET);
		}

		private class MobBucketVariantProperty implements ItemPropertyFunction {

			private final int maxVariants;
			private final BooleanSupplier featureEnabled;

			public MobBucketVariantProperty(int maxVariants, BooleanSupplier featureEnabled) {
				this.maxVariants = maxVariants;
				this.featureEnabled = featureEnabled;
			}

			@Override
			public float call(@Nonnull ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int id) {
				if(!enabled || !featureEnabled.getAsBoolean())
					return 0;

				return ItemNBTHelper.getInt(stack, Axolotl.VARIANT_TAG, 0) % maxVariants;
			}
		}

		private class ShinyMobBucketProperty implements ItemPropertyFunction {

			private final BooleanSupplier featureEnabled;

			public ShinyMobBucketProperty(BooleanSupplier featureEnabled) {
				this.featureEnabled = featureEnabled;
			}

			@Override
			public float call(@Nonnull ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int id) {
				if(!enabled || !featureEnabled.getAsBoolean())
					return 0;

				CompoundTag data = ItemNBTHelper.getCompound(stack, SlimeInABucketItem.TAG_ENTITY_DATA, true);
				if(data != null && data.hasUUID("UUID")) {
					UUID uuid = data.getUUID("UUID");
					if(VariantAnimalTexturesModule.Client.isShiny(uuid))
						return 1;
				}

				return 0;
			}
		}

		private class TropicalFishBucketVariantProperty implements ItemPropertyFunction {

			private final IntUnaryOperator extractor;
			private final BooleanSupplier featureEnabled;

			public TropicalFishBucketVariantProperty(IntUnaryOperator extractor, BooleanSupplier featureEnabled) {
				this.extractor = extractor;
				this.featureEnabled = featureEnabled;
			}

			@Override
			public float call(@Nonnull ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int id) {
				if(!enabled || !featureEnabled.getAsBoolean())
					return 0;

				CompoundTag tag = stack.getTag();
				if(tag != null && tag.contains(TropicalFish.BUCKET_VARIANT_TAG, Tag.TAG_INT)) {
					int variant = tag.getInt(TropicalFish.BUCKET_VARIANT_TAG);
					return extractor.applyAsInt(variant) + 1;
				}

				return 0;
			}
		}

		private class TropicalFishBucketColor implements ItemColor {

			@Nullable
			private final ItemColor parent;
			private final BooleanSupplier featureEnabled;

			public TropicalFishBucketColor(@Nullable ItemColor parent, BooleanSupplier featureEnabled) {
				this.parent = parent;
				this.featureEnabled = featureEnabled;
			}

			@Override
			public int getColor(@Nonnull ItemStack stack, int layer) {
				if(enabled && featureEnabled.getAsBoolean() && (layer == 1 || layer == 2)) {
					CompoundTag tag = stack.getTag();
					if(tag != null && tag.contains(TropicalFish.BUCKET_VARIANT_TAG, Tag.TAG_INT)) {
						int variant = tag.getInt(TropicalFish.BUCKET_VARIANT_TAG);

						DyeColor dyeColor = layer == 1 ? TropicalFish.getBaseColor(variant) : TropicalFish.getPatternColor(variant);
						float[] colorComponents = dyeColor.getTextureDiffuseColors();

						return ((int) (colorComponents[0] * 255) << 16) |
							((int) (colorComponents[1] * 255) << 8) |
							(int) (colorComponents[2] * 255);
					}
				}

				return parent != null ? parent.getColor(stack, layer) : -1;
			}
		}

	}

}

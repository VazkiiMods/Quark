package org.violetmoon.quark.content.tools.module;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.tools.client.render.entity.PickarangRenderer;
import org.violetmoon.quark.content.tools.config.PickarangType;
import org.violetmoon.quark.content.tools.entity.rang.AbstractPickarang;
import org.violetmoon.quark.content.tools.entity.rang.Flamerang;
import org.violetmoon.quark.content.tools.entity.rang.Pickarang;
import org.violetmoon.quark.content.tools.item.PickarangItem;
import org.violetmoon.zeta.advancement.ManualTrigger;
import org.violetmoon.zeta.client.event.load.ZClientSetup;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.BooleanSuppliers;
import org.violetmoon.zeta.util.Hint;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

@ZetaLoadModule(category = "tools")
public class PickarangModule extends ZetaModule {

    @Config(name = "pickarang")
    public static PickarangType<Pickarang> pickarangType = new PickarangType<>(Items.DIAMOND, Items.DIAMOND_PICKAXE, 20, 3, 800, 20.0, 2, 10);

    @Config(name = "flamerang")
    public static PickarangType<Flamerang> flamerangType = new PickarangType<>(Items.NETHERITE_INGOT, Items.NETHERITE_PICKAXE, 20, 4, 1040, 20.0, 3, 10);

    @Config(flag = "flamerang")
    public static boolean enableFlamerang = true;

    @Config(description = "Set this to true to use the recipe without the Heart of Diamond, even if the Heart of Diamond is enabled.", flag = "pickarang_never_uses_heart")
    public static boolean neverUseHeartOfDiamond = false;

    @Hint
    public static Item pickarang;
    @Hint("flamerang")
    public static Item flamerang;

    private static final List<PickarangType<?>> knownTypes = new ArrayList<>();
    private static boolean isEnabled;

    public static final TagKey<Block> pickarangImmuneTag = Quark.asTagKey(Registries.BLOCK,"pickarang_immune");

    public static final ResourceKey<DamageType> pickarangDamageType = Quark.asResourceKey(Registries.DAMAGE_TYPE,"pickarang");

    public static ManualTrigger throwPickarangTrigger;
    public static ManualTrigger useFlamerangTrigger;

    private static final ThreadLocal<DamageSource> ACTIVE_PICKARANG_DAMAGE = new ThreadLocal<>();



    @LoadEvent
    public final void register(ZRegister event) {
        pickarang = makePickarang(pickarangType, "pickarang", Pickarang::new, Pickarang::new, BooleanSuppliers.TRUE).setCreativeTab(CreativeModeTabs.TOOLS_AND_UTILITIES, Items.DIAMOND_HOE, false);
        flamerang = makePickarang(flamerangType, "flamerang", Flamerang::new, Flamerang::new, () -> enableFlamerang).setCreativeTab(CreativeModeTabs.TOOLS_AND_UTILITIES, Items.NETHERITE_HOE, false);

        throwPickarangTrigger = event.getAdvancementModifierRegistry().registerManualTrigger("throw_pickarang");
        useFlamerangTrigger = event.getAdvancementModifierRegistry().registerManualTrigger("use_flamerang");
    }

    private <T extends AbstractPickarang<T>> PickarangItem makePickarang(PickarangType<T> type, String name,
                                                                         EntityType.EntityFactory<T> entityFactory,
                                                                         PickarangType.PickarangConstructor<T> thrownFactory,
                                                                         BooleanSupplier condition) {

        EntityType<T> entityType = EntityType.Builder.of(entityFactory, MobCategory.MISC)
                .sized(0.4F, 0.4F)
                .clientTrackingRange(4)
                .updateInterval(10)
                .build(name);
        Quark.ZETA.registry.register(entityType, name, Registries.ENTITY_TYPE);

        knownTypes.add(type);
        type.setEntityType(entityType, thrownFactory);
        return (PickarangItem) new PickarangItem(name, this, propertiesFor(type), type).setCondition(condition);
    }

    private <T extends AbstractPickarang<T>> Item.Properties propertiesFor(PickarangType<T> type) {
        Item.Properties properties = new Item.Properties()
                .stacksTo(1);

        if (type.durability > 0)
            properties.durability(type.durability);

        if (type.isFireResistant())
            properties.fireResistant();

        properties.attributes(PickarangItem.createAttributes(type));

        return properties;
    }

    @LoadEvent
    public final void configChanged(ZConfigChanged event) {
        // Pass over to a static reference for easier computing the coremod hook
        isEnabled = this.isEnabled();
    }

    public static boolean getIsFireResistant(boolean vanillaVal, Entity entity) {
        if (!isEnabled || vanillaVal)
            return vanillaVal;

        Entity riding = entity.getVehicle();
        if (riding instanceof AbstractPickarang<?> pick)
            return pick.getPickarangType().isFireResistant();

        return false;
    }

    @ZetaLoadModule(clientReplacement = true)
    public static class Client extends PickarangModule {

        @LoadEvent
        public final void clientSetup(ZClientSetup event) {
            knownTypes.forEach(t -> EntityRenderers.register(t.getEntityType(), PickarangRenderer::new));
        }

    }

    public static DamageSource getDamageSource(Entity causingEntity, @Nullable Entity directEntity) {
        var type = causingEntity.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(pickarangDamageType);
        return new DamageSource(type, causingEntity, directEntity);
    }


    public static void setActivePickarangDamage(DamageSource damage) {
        ACTIVE_PICKARANG_DAMAGE.set(damage);
    }

    public static DamageSource getActiveDamage() {
        return ACTIVE_PICKARANG_DAMAGE.get();
    }

}

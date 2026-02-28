package org.violetmoon.quark.content.mobs.client.render.entity;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.mobs.client.model.BudgieModel;
import org.violetmoon.quark.content.mobs.entity.Budgie;

public class BudgieRenderer extends MobRenderer<Budgie, BudgieModel<Budgie>> {

    public static final ModelLayerLocation BUDGIE_LAYER = new ModelLayerLocation(Quark.asResource("budgie"), "main");

    public BudgieRenderer(EntityRendererProvider.Context context) {
        super(context, new BudgieModel<>(context.bakeLayer(BUDGIE_LAYER)), 0.3f);
    }

    @Override
    public ResourceLocation getTextureLocation(Budgie entity) {
        return switch (entity.getVariant()) {
            case 1 -> Quark.asResource("textures/model/entity/budgie/blue.png");
            case 2 -> Quark.asResource("textures/model/entity/budgie/yellow.png");
            case 3 -> Quark.asResource("textures/model/entity/budgie/white.png");
            default -> Quark.asResource("textures/model/entity/budgie/green.png");
        };
    }
}

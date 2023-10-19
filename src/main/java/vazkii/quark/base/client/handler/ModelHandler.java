package vazkii.quark.base.client.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.quark.addons.oddities.client.model.BackpackModel;
import vazkii.quark.base.Quark;
import vazkii.quark.base.client.render.QuarkArmorModel;
import vazkii.quark.content.mobs.client.model.CrabModel;
import vazkii.quark.content.mobs.client.model.ForgottenHatModel;
import vazkii.quark.content.mobs.client.model.FoxhoundModel;
import vazkii.quark.content.mobs.client.model.ShibaModel;
import vazkii.quark.content.mobs.client.model.StonelingModel;
import vazkii.quark.content.mobs.client.model.ToretoiseModel;
import vazkii.quark.content.mobs.client.model.WraithModel;

public class ModelHandler {

	private static final Map<ModelLayerLocation, Layer> layers = new HashMap<>();
	private static final Map<Pair<ModelLayerLocation, EquipmentSlot>, QuarkArmorModel> cachedArmors = new HashMap<>();

	public static ModelLayerLocation shiba;
	public static ModelLayerLocation foxhound;
	public static ModelLayerLocation stoneling;
	public static ModelLayerLocation crab;
	public static ModelLayerLocation toretoise;
	public static ModelLayerLocation wraith;
	
	public static ModelLayerLocation quark_boat;
	public static ModelLayerLocation quark_boat_chest;

	public static ModelLayerLocation forgotten_hat;
	public static ModelLayerLocation backpack;

	private static boolean modelsInitted = false;

	private static void initModels() {
		if(modelsInitted)
			return;

		shiba = addModel("shiba", ShibaModel::createBodyLayer, ShibaModel::new);
		foxhound = addModel("foxhound", FoxhoundModel::createBodyLayer, FoxhoundModel::new);
		stoneling = addModel("stoneling", StonelingModel::createBodyLayer, StonelingModel::new);
		crab = addModel("crab", CrabModel::createBodyLayer, CrabModel::new);
		toretoise = addModel("toretoise", ToretoiseModel::createBodyLayer, ToretoiseModel::new);
		wraith = addModel("wraith", WraithModel::createBodyLayer, WraithModel::new);
		
		quark_boat = addModel("quark_boat", () -> BoatModel.createBodyModel(false), r -> new BoatModel(r, false));
		quark_boat_chest = addModel("quark_boat_chest", () -> BoatModel.createBodyModel(true), r -> new BoatModel(r, true));

		forgotten_hat = addArmorModel("forgotten_hat", ForgottenHatModel::createBodyLayer);
		backpack = addArmorModel("backpack", BackpackModel::createBodyLayer);

		modelsInitted = true;
	}

	private static ModelLayerLocation addModel(String name, Supplier<LayerDefinition> supplier, Function<ModelPart, EntityModel<?>> modelConstructor) {
		return addLayer(name, new Layer(supplier, modelConstructor));
	}

	private static ModelLayerLocation addArmorModel(String name, Supplier<LayerDefinition> supplier) {
		return addLayer(name, new Layer(supplier, QuarkArmorModel::new));
	}

	private static ModelLayerLocation addLayer(String name, Layer layer) {
		ModelLayerLocation loc = new ModelLayerLocation(new ResourceLocation(Quark.MOD_ID, name), "main");
		layers.put(loc, layer);
		return loc;
	}

	@SubscribeEvent
	public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
		initModels();

		for(ModelLayerLocation location : layers.keySet()) {
			Quark.LOG.info("Registering model layer " + location);
			event.registerLayerDefinition(location, layers.get(location).definition);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends Mob, M extends EntityModel<T>> M model(ModelLayerLocation location) {
		initModels();

		Layer layer = layers.get(location);
		Minecraft mc = Minecraft.getInstance();

		return (M) layer.modelConstructor.apply(mc.getEntityModels().bakeLayer(location));
	}

	public static QuarkArmorModel armorModel(ModelLayerLocation location, EquipmentSlot slot) {
		Pair<ModelLayerLocation, EquipmentSlot> key = Pair.of(location, slot);
		if(cachedArmors.containsKey(key))
			return cachedArmors.get(key);

		initModels();

		Layer layer = layers.get(location);
		Minecraft mc = Minecraft.getInstance();
		QuarkArmorModel model = layer.armorModelConstructor.apply(mc.getEntityModels().bakeLayer(location), slot);
		cachedArmors.put(key, model);

		return model;
	}

	private static class Layer {

		final Supplier<LayerDefinition> definition;
		final Function<ModelPart, EntityModel<?>> modelConstructor;
		final BiFunction<ModelPart, EquipmentSlot, QuarkArmorModel> armorModelConstructor;

		public Layer(Supplier<LayerDefinition> definition, Function<ModelPart, EntityModel<?>> modelConstructor) {
			this.definition = definition;
			this.modelConstructor = modelConstructor;
			this.armorModelConstructor = null;
		}

		public Layer(Supplier<LayerDefinition> definition, BiFunction<ModelPart, EquipmentSlot, QuarkArmorModel> armorModelConstructor) {
			this.definition = definition;
			this.modelConstructor = null;
			this.armorModelConstructor = armorModelConstructor;
		}

	}

}

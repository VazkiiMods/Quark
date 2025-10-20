package org.violetmoon.quark.content.tools.client.render;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.tools.base.RuneColor;

import java.util.Map;
import java.util.function.Function;

public class GlintRenderTypes extends RenderType {
	private GlintRenderTypes(String name, VertexFormat vf, VertexFormat.Mode mode, int bufSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setup, Runnable clean) {
		super(name, vf, mode, bufSize, affectsCrumbling, sortOnUpload, setup, clean);
		throw new UnsupportedOperationException("Don't instantiate this");
	}

	public static Map<RuneColor, RenderType> glint = newRenderMap(GlintRenderTypes::buildGlintRenderType);
	public static Map<RuneColor, RenderType> glintTranslucent = newRenderMap(GlintRenderTypes::buildGlintTranslucentRenderType);
	public static Map<RuneColor, RenderType> entityGlint = newRenderMap(GlintRenderTypes::buildEntityGlintRenderType);
	public static Map<RuneColor, RenderType> glintDirect = newRenderMap(GlintRenderTypes::buildGlintDirectRenderType);
	public static Map<RuneColor, RenderType> entityGlintDirect = newRenderMap(GlintRenderTypes::buildEntityGlintDriectRenderType);
	public static Map<RuneColor, RenderType> armorGlint = newRenderMap(GlintRenderTypes::buildArmorGlintRenderType);
	public static Map<RuneColor, RenderType> armorEntityGlint = newRenderMap(GlintRenderTypes::buildArmorEntityGlintRenderType);

	public static void addGlintTypes(Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder> map) {
		addGlintTypes(map, glint);
		addGlintTypes(map, glintTranslucent);
		addGlintTypes(map, entityGlint);
		addGlintTypes(map, glintDirect);
		addGlintTypes(map, entityGlintDirect);
		addGlintTypes(map, armorGlint);
		addGlintTypes(map, armorEntityGlint);
	}

	private static Map<RuneColor, RenderType> newRenderMap(Function<String, RenderType> func) {
		Map<RuneColor, RenderType> map = new Object2ObjectOpenHashMap<>();

		for(RuneColor color : RuneColor.values())
			map.put(color, func.apply(color.getSerializedName()));

		return map;
	}

	private static void addGlintTypes(Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder> map, Map<RuneColor, RenderType> typeMap) {
		for(RenderType renderType : typeMap.values())
			if(!map.containsKey(renderType))
				map.put(renderType, new ByteBufferBuilder(renderType.bufferSize()));
	}

	private static RenderType buildGlintRenderType(String name) {
		return RenderType.create("glint_" + name, DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, false, false, CompositeState.builder()
				.setShaderState(RenderStateShard.RENDERTYPE_GLINT_SHADER)
				.setTextureState(new TextureStateShard(texture(name), true, false))
				.setWriteMaskState(RenderStateShard.COLOR_WRITE)
				.setCullState(RenderStateShard.NO_CULL)
				.setDepthTestState(RenderStateShard.EQUAL_DEPTH_TEST)
				.setTransparencyState(RenderStateShard.GLINT_TRANSPARENCY)
				.setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
				.setTexturingState(RenderStateShard.GLINT_TEXTURING)
				.createCompositeState(false));
	}

	private static RenderType buildGlintTranslucentRenderType(String name) {
		return RenderType.create("glint_translucent_" + name, DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, false, false, CompositeState.builder()
				.setShaderState(RenderStateShard.RENDERTYPE_GLINT_TRANSLUCENT_SHADER)
				.setTextureState(new TextureStateShard(texture(name), true, false))
				.setWriteMaskState(RenderStateShard.COLOR_WRITE)
				.setCullState(RenderStateShard.NO_CULL)
				.setDepthTestState(RenderStateShard.EQUAL_DEPTH_TEST)
				.setTransparencyState(RenderStateShard.GLINT_TRANSPARENCY)
				.setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
				.setTexturingState(RenderStateShard.GLINT_TEXTURING)
				.createCompositeState(false));
	}

	private static RenderType buildEntityGlintRenderType(String name) {
		return RenderType.create("entity_glint_" + name, DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, false, false, CompositeState.builder()
				.setShaderState(RenderStateShard.RENDERTYPE_ENTITY_GLINT_SHADER)
				.setTextureState(new TextureStateShard(texture(name), true, false))
				.setWriteMaskState(RenderStateShard.COLOR_WRITE)
				.setCullState(RenderStateShard.NO_CULL)
				.setDepthTestState(RenderStateShard.EQUAL_DEPTH_TEST)
				.setTransparencyState(RenderStateShard.GLINT_TRANSPARENCY)
				.setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
				.setTexturingState(RenderStateShard.ENTITY_GLINT_TEXTURING)
				.createCompositeState(false));
	}

	private static RenderType buildGlintDirectRenderType(String name) {
		return RenderType.create("glint_direct_" + name, DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, false, false, CompositeState.builder()
				.setShaderState(RenderStateShard.RENDERTYPE_ENTITY_GLINT_DIRECT_SHADER)
				.setTextureState(new TextureStateShard(texture(name), true, false))
				.setWriteMaskState(RenderStateShard.COLOR_WRITE)
				.setCullState(RenderStateShard.NO_CULL)
				.setDepthTestState(RenderStateShard.EQUAL_DEPTH_TEST)
				.setTransparencyState(RenderStateShard.GLINT_TRANSPARENCY)
				.setTexturingState(RenderStateShard.GLINT_TEXTURING)
				.createCompositeState(false));
	}

	private static RenderType buildEntityGlintDriectRenderType(String name) {
		return RenderType.create("entity_glint_direct_" + name, DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, false, false, CompositeState.builder()
				.setShaderState(RenderStateShard.RENDERTYPE_ENTITY_GLINT_DIRECT_SHADER)
				.setTextureState(new TextureStateShard(texture(name), true, false))
				.setWriteMaskState(RenderStateShard.COLOR_WRITE)
				.setCullState(RenderStateShard.NO_CULL)
				.setDepthTestState(RenderStateShard.EQUAL_DEPTH_TEST)
				.setTransparencyState(RenderStateShard.GLINT_TRANSPARENCY)
				.setTexturingState(RenderStateShard.ENTITY_GLINT_TEXTURING)
				.createCompositeState(false));
	}

	private static RenderType buildArmorGlintRenderType(String name) {
		return RenderType.create("armor_glint_" + name, DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, false, false, CompositeState.builder()
				.setShaderState(RenderStateShard.RENDERTYPE_ARMOR_ENTITY_GLINT_SHADER)
				.setTextureState(new TextureStateShard(texture(name), true, false))
				.setWriteMaskState(RenderStateShard.COLOR_WRITE)
				.setCullState(RenderStateShard.NO_CULL)
				.setDepthTestState(RenderStateShard.EQUAL_DEPTH_TEST)
				.setTransparencyState(RenderStateShard.GLINT_TRANSPARENCY)
				.setTexturingState(RenderStateShard.ENTITY_GLINT_TEXTURING)
				.setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
				.createCompositeState(false));
	}

	private static RenderType buildArmorEntityGlintRenderType(String name) {
		return RenderType.create("armor_entity_glint_" + name, DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS, 256, false, false, CompositeState.builder()
				.setShaderState(RenderStateShard.RENDERTYPE_ARMOR_ENTITY_GLINT_SHADER)
				.setTextureState(new TextureStateShard(texture(name), true, false))
				.setWriteMaskState(RenderStateShard.COLOR_WRITE)
				.setCullState(RenderStateShard.NO_CULL)
				.setDepthTestState(RenderStateShard.EQUAL_DEPTH_TEST)
				.setTransparencyState(RenderStateShard.GLINT_TRANSPARENCY)
				.setTexturingState(RenderStateShard.ENTITY_GLINT_TEXTURING)
				.setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
				.createCompositeState(false));
	}

	private static ResourceLocation texture(String name) {
		return Quark.asResource("textures/glint/enchanted_item_glint_" + name + ".png");
	}
}

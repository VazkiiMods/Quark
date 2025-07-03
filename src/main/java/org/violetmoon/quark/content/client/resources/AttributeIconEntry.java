package org.violetmoon.quark.content.client.resources;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;

import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public record AttributeIconEntry(
		Map<AttributeSlot, AttributeDisplayType> displayTypes,
		ResourceLocation texture,
		CompareType comparison) {

	public static class Serializer implements JsonDeserializer<AttributeIconEntry>, JsonSerializer<AttributeIconEntry> {
		public static Serializer INSTANCE = new Serializer();

		@Override
		public AttributeIconEntry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			JsonObject obj = GsonHelper.convertToJsonObject(json, "attribute icon");
			JsonObject displayObj = GsonHelper.getAsJsonObject(obj, "display");
			Map<AttributeSlot, AttributeDisplayType> display = new HashMap<>();
			for(AttributeSlot slot : AttributeSlot.values()) {
				String key = slot.name().toLowerCase(Locale.ROOT);
				String displayType = GsonHelper.getAsString(displayObj, key).toUpperCase(Locale.ROOT);
				AttributeDisplayType trueType = null;
				for(AttributeDisplayType type : AttributeDisplayType.values()) {
					if(type.name().equals(displayType)) {
						trueType = type;
						break;
					}
				}
				if(trueType == null)
					throw new JsonSyntaxException("Display type " + displayType + " is not valid");

				display.put(slot, trueType);
			}

			String texturePath = GsonHelper.getAsString(obj, "texture");
			ResourceLocation truncatedPath = ResourceLocation.parse(texturePath);
			ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(truncatedPath.getNamespace(), "textures/" + truncatedPath.getPath() + ".png");

			String compareStr = GsonHelper.getAsString(obj, "compare", "no_compare");
			CompareType type = CompareType.valueOf(compareStr.toUpperCase(Locale.ROOT));

			return new AttributeIconEntry(display, texture, type);
		}

		@Override
		public JsonElement serialize(AttributeIconEntry src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject obj = new JsonObject();
			JsonObject display = new JsonObject();
			for(AttributeSlot slot : AttributeSlot.values()) {
				display.addProperty(slot.name().toLowerCase(Locale.ROOT), src.displayTypes.get(slot).name().toLowerCase(Locale.ROOT));
			}
			obj.add("display", display);
			obj.addProperty("texture", src.texture.getNamespace() + ":" + snipTexturePath(src.texture.getNamespace()));
			return null;
		}

		private static String snipTexturePath(String texture) {
			if(texture.startsWith("textures/"))
				texture = texture.substring(9);
			if(texture.endsWith(".png"))
				texture = texture.substring(0, texture.length() - 4);
			return texture;
		}
	}

	public static enum CompareType {
		NO_COMPARE((a, b) -> 0),
		LOWER_BETTER((a, b) -> sign(a, b)),
		HIGHER_BETTER((a, b) -> sign(b, a));

		private Comparator<Double> comparator;

		private CompareType(Comparator<Double> comparator) {
			this.comparator = comparator;
		}

		private static int sign(double a, double b) {
			double diff = a - b;
			if(diff < 0)
				return -1;
			if(diff > 0)
				return 1;
			return 0;
		}

		public ChatFormatting getColor(double ours, double equipped) {
			int val = comparator.compare(equipped, ours);
			if(val == 0)
				return ChatFormatting.WHITE;
			if(val < 0)
				return ChatFormatting.RED;
			return ChatFormatting.GREEN;
		}

	}
}

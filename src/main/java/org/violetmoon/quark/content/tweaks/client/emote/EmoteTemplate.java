package org.violetmoon.quark.content.tweaks.client.emote;

import com.google.common.collect.Lists;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import org.violetmoon.quark.base.Quark;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import aurelienribon.tweenengine.*;

public class EmoteTemplate {

	private static final Map<String, Integer> parts = new HashMap<>();
	private static final Map<String, Integer> tweenables = new HashMap<>();
	private static final Map<String, Function> functions = new HashMap<>();
	private static final Map<String, TweenEquation> equations = new HashMap<>();

	static {
		functions.put("name", (em, model, player, timeline, tokens) -> name(em, timeline, tokens));
		functions.put("use", (em, model, player, timeline, tokens) -> use(em, timeline, tokens));
		functions.put("unit", (em, model, player, timeline, tokens) -> unit(em, timeline, tokens));
		functions.put("animation", (em, model, player, timeline, tokens) -> animation(em, timeline, tokens));
		functions.put("section", (em, model, player, timeline, tokens) -> section(em, timeline, tokens));
		functions.put("end", (em, model, player, timeline, tokens) -> end(em, timeline, tokens));
		functions.put("move", (em, model, player, timeline, tokens) -> move(em, model, timeline, tokens));
		functions.put("reset", (em, model, player, timeline, tokens) -> reset(em, model, timeline, tokens));
		functions.put("pause", (em, model, player, timeline, tokens) -> pause(em, timeline, tokens));
		functions.put("yoyo", (em, model, player, timeline, tokens) -> yoyo(em, timeline, tokens));
		functions.put("repeat", (em, model, player, timeline, tokens) -> repeat(em, timeline, tokens));
		functions.put("tier", (em, model, player, timeline, tokens) -> tier(em, timeline, tokens));
		functions.put("sound", (em, model, player, timeline, tokens) -> sound(em, player, timeline, tokens));

		Class<?> clazz = ModelAccessor.class;
		Field[] fields = clazz.getDeclaredFields();
		for(Field f : fields) {
			if(f.getType() != int.class)
				continue;

			int modifiers = f.getModifiers();
			if(Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)) {
				try {
					int val = f.getInt(null);
					String name = f.getName().toLowerCase(Locale.ROOT);
					if(name.matches("^.+?_[xyz]$"))
						tweenables.put(name, val);
					else
						parts.put(name, val);
				} catch (Exception e) {
					Quark.LOG.error("Failed while attempting to set up model parameters for emotes", e);
				}
			}
		}

		clazz = TweenEquations.class;
		fields = clazz.getDeclaredFields();
		for(Field f : fields) {
			String name = f.getName().replaceAll("[A-Z]", "_$0").substring(5).toLowerCase(Locale.ROOT);
			try {
				TweenEquation eq = (TweenEquation) f.get(null);
				equations.put(name, eq);
				if(name.equals("none"))
					equations.put("linear", eq);
			} catch (Exception e) {
				Quark.LOG.error("Failed while attempting to set up tween equations for emotes", e);
			}
		}
	}

	public final String file;

	public List<String> readLines;
	public List<Integer> usedParts;
	public Stack<Timeline> timelineStack;
	public float speed;
	public int tier;
	public boolean compiled = false;
	public boolean compiledOnce = false;

	private List<EmoteSound> activeSounds = Lists.newArrayList();

	public EmoteTemplate(String file) {
		this.file = file;
		readAndMakeTimeline(null, null, null);
	}

	public Timeline getTimeline(EmoteDescriptor desc, Player player, HumanoidModel<?> model) {
		compiled = false;
		speed = 1;
		tier = 0;

		if(readLines == null)
			return readAndMakeTimeline(desc, player, model);
		else {
			Timeline timeline = null;
			timelineStack = new Stack<>();

			int i = 0;
			try {
				for(; i < readLines.size() && !compiled; i++)
					timeline = handle(model, player, timeline, readLines.get(i));
			} catch (Exception e) {
				logError(e, i);
				return Timeline.createSequence();
			}

			if(timeline == null)
				return Timeline.createSequence();

			return timeline;
		}
	}

	public Timeline readAndMakeTimeline(EmoteDescriptor desc, Player player, HumanoidModel<?> model) {
		Timeline timeline = null;
		usedParts = new ArrayList<>();
		timelineStack = new Stack<>();
		int lines = 0;

		tier = 0;

		compiled = compiledOnce = false;
		readLines = new ArrayList<>();
		try (BufferedReader reader = createReader()) {

			try {
				String s;
				while((s = reader.readLine()) != null && !compiled) {
					lines++;
					readLines.add(s);
					timeline = handle(model, player, timeline, s);
				}
			} catch (Exception e) {
				logError(e, lines);
				return fallback();
			}

			if(timeline == null)
				return fallback();

			return timeline;
		} catch (IOException e) {
			Quark.LOG.warn("Failed to load emote " + desc, e);
			return fallback();
		} finally {
			compiledOnce = true;
			if(desc != null)
				desc.updateTier(this);
		}
	}

	protected BufferedReader createReader() throws FileNotFoundException {
		return new BufferedReader(new InputStreamReader(Quark.class.getResourceAsStream("/assets/quark/emotes/" + file)));
	}

	protected Timeline fallback() {
		return Timeline.createSequence();
	}

	private void logError(Exception e, int line) {
		Quark.LOG.error("[Custom Emotes] Error loading line " + (line + 1) + " of emote " + file);
		if(!(e instanceof IllegalArgumentException)) {
			Quark.LOG.error("[Custom Emotes] This is an Internal Error, and not one in the emote file, please report it", e);
		} else
			Quark.LOG.error("[Custom Emotes] " + e.getMessage());
	}

	private Timeline handle(HumanoidModel<?> model, Player player, Timeline timeline, String s) throws IllegalArgumentException {
		s = s.trim();
		if(s.startsWith("#") || s.isEmpty())
			return timeline;

		String[] tokens = s.trim().split(" ");
		String function = tokens[0];

		if(functions.containsKey(function))
			return functions.get(function).invoke(this, model, player, timeline, tokens);

		throw new IllegalArgumentException("Illegal function name " + function);
	}

	protected void setName(String[] tokens) {}

	private static Timeline name(EmoteTemplate em, Timeline timeline, String[] tokens) throws IllegalArgumentException {
		em.setName(tokens);
		return timeline;
	}

	private static Timeline use(EmoteTemplate em, Timeline timeline, String[] tokens) throws IllegalArgumentException {
		if(em.compiledOnce)
			return timeline;

		assertParamSize(tokens, 2);

		String part = tokens[1];

		if(parts.containsKey(part))
			em.usedParts.add(parts.get(part));
		else
			throw new IllegalArgumentException("Illegal part name for function use: " + part);

		return timeline;
	}

	private static Timeline unit(EmoteTemplate em, Timeline timeline, String[] tokens) throws IllegalArgumentException {
		assertParamSize(tokens, 2);
		em.speed = Float.parseFloat(tokens[1]);
		return timeline;
	}

	private static Timeline tier(EmoteTemplate em, Timeline timeline, String[] tokens) throws IllegalArgumentException {
		assertParamSize(tokens, 2);
		em.tier = Integer.parseInt(tokens[1]);
		return timeline;
	}

	private static Timeline animation(EmoteTemplate em, Timeline timeline, String[] tokens) throws IllegalArgumentException {
		if(timeline != null)
			throw new IllegalArgumentException("Illegal use of function animation, animation already started");

		assertParamSize(tokens, 2);

		String type = tokens[1];

		Timeline newTimeline = switch(type) {
		case "sequence" -> Timeline.createSequence();
		case "parallel" -> Timeline.createParallel();
		default -> throw new IllegalArgumentException("Illegal animation type: " + type);
		};

		newTimeline.addCallback(TweenCallback.START, (tween) -> {
			EmoteSound.endAll(em.activeSounds);
			em.activeSounds = Lists.newArrayList();
		});
		return newTimeline;
	}

	private static Timeline section(EmoteTemplate em, Timeline timeline, String[] tokens) throws IllegalArgumentException {
		if(timeline == null)
			throw new IllegalArgumentException("Illegal use of function section, animation not started");
		assertParamSize(tokens, 2);

		String type = tokens[1];
		Timeline newTimeline = switch(type) {
		case "sequence" -> Timeline.createSequence();
		case "parallel" -> Timeline.createParallel();
		default -> throw new IllegalArgumentException("Illegal section type: " + type);
		};

		em.timelineStack.push(timeline);
		return newTimeline;
	}

	private static Timeline end(EmoteTemplate em, Timeline timeline, String[] tokens) throws IllegalArgumentException {
		if(timeline == null)
			throw new IllegalArgumentException("Illegal use of function end, animation not started");
		assertParamSize(tokens, 1);

		if(em.timelineStack.isEmpty()) {
			em.compiled = true;
			return timeline;
		}

		Timeline poppedLine = em.timelineStack.pop();
		poppedLine.push(timeline);
		return poppedLine;
	}

	private static Timeline move(EmoteTemplate em, HumanoidModel<?> model, Timeline timeline, String[] tokens) throws IllegalArgumentException {
		if(timeline == null)
			throw new IllegalArgumentException("Illegal use of function move, animation not started");
		if(tokens.length < 4)
			throw new IllegalArgumentException(String.format(Locale.ROOT, "Illegal parameter amount for function move: %d (at least 4 are required)", tokens.length));

		String partStr = tokens[1];
		int part;
		if(tweenables.containsKey(partStr))
			part = tweenables.get(partStr);
		else
			throw new IllegalArgumentException("Illegal part name for function move: " + partStr);

		float time = Float.parseFloat(tokens[2]) * em.speed;
		float target = Float.parseFloat(tokens[3]);

		Tween tween = null;
		boolean valid = model != null;
		if(valid)
			tween = Tween.to(model, part, time).target(target);
		if(tokens.length > 4) {
			int index = 4;
			while(index < tokens.length) {
				String cmd = tokens[index++];
				int times;
				float delay;
				switch(cmd) {
				case "delay" -> {
					assertParamSize("delay", tokens, 1, index);
					delay = Float.parseFloat(tokens[index++]) * em.speed;
					if(valid)
						tween = tween.delay(delay);
				}
				case "yoyo" -> {
					assertParamSize("yoyo", tokens, 2, index);
					times = Integer.parseInt(tokens[index++]);
					delay = Float.parseFloat(tokens[index++]) * em.speed;
					if(valid)
						tween = tween.repeatYoyo(times, delay);
				}
				case "repeat" -> {
					assertParamSize("repeat", tokens, 2, index);
					times = Integer.parseInt(tokens[index++]);
					delay = Float.parseFloat(tokens[index++]) * em.speed;
					if(valid)
						tween = tween.repeat(times, delay);
				}
				case "ease" -> {
					assertParamSize("ease", tokens, 1, index);
					String easeType = tokens[index++];
					if(equations.containsKey(easeType)) {
						if(valid)
							tween.ease(equations.get(easeType));
					} else
						throw new IllegalArgumentException("Easing type " + easeType + " doesn't exist");
				}
				default -> throw new IllegalArgumentException(String.format(Locale.ROOT, "Invalid modifier %s for move function", cmd));
				}
			}
		}

		if(valid)
			return timeline.push(tween);
		return timeline;
	}

	private static Timeline sound(EmoteTemplate em, Player player, Timeline timeline, String[] tokens) throws IllegalArgumentException {
		if(timeline == null)
			throw new IllegalArgumentException("Illegal use of function sound, animation not started");
		if(tokens.length < 2)
			throw new IllegalArgumentException("Expected action (continuous, instant, stop) for function sound");

		String playType = tokens[1];
		if(playType.equals("stop")) {

			List<BaseTween<?>> children = timeline.getChildren();
			BaseTween<?> callbackTween = timeline;
			int tweenEvent = TweenCallback.START;
			if(!children.isEmpty()) {
				tweenEvent = TweenCallback.COMPLETE;
				callbackTween = children.get(children.size() - 1);
			}

			callbackTween.addCallback(tweenEvent, (tween) -> EmoteSound.endAll(em.activeSounds));
		} else {

			boolean repeating = playType.equals("continuous");
			if(!repeating && !playType.equals("instant"))
				throw new IllegalArgumentException(String.format(Locale.ROOT, "Invalid modifier %s for sound function", playType));

			assertParamSize(tokens, 4, 6);

			String endCondition = tokens[2];
			boolean endWithSequence = endCondition.equals("section");
			if(!endWithSequence && !endCondition.equals("emote"))
				throw new IllegalArgumentException(String.format(Locale.ROOT, "Invalid modifier %s for sound function", endCondition));

			String type = tokens[3];
			float volume = 1f;
			float pitch = 1f;

			try {
				if(tokens.length >= 5)
					volume = Math.min(Float.parseFloat(tokens[4]), 1.5f);

				if(tokens.length >= 6)
					pitch = Float.parseFloat(tokens[5]);
			} catch (NumberFormatException ex) {
				throw new IllegalArgumentException("Illegal number in function sound", ex);
			}

			ResourceLocation soundEvent = ResourceLocation.parse(type);

			List<BaseTween<?>> children = timeline.getChildren();

			List<EmoteSound> sounds = Lists.newArrayList();

			final float finalVolume = volume;
			final float finalPitch = pitch;

			BaseTween<?> callbackTween = timeline;
			int tweenEvent = TweenCallback.START;
			if(!children.isEmpty()) {
				tweenEvent = TweenCallback.COMPLETE;
				callbackTween = children.get(children.size() - 1);
			}

			callbackTween.addCallback(tweenEvent,
					(tween) -> EmoteSound.add(em.activeSounds, sounds, player, em,
							soundEvent, finalVolume, finalPitch,
							repeating, endWithSequence));

			timeline.addCallback(TweenCallback.COMPLETE,
					(tween) -> EmoteSound.endSection(sounds));
		}

		return timeline;
	}

	private static Timeline reset(EmoteTemplate em, HumanoidModel<?> model, Timeline timeline, String[] tokens) throws IllegalArgumentException {
		if(tokens.length < 4)
			throw new IllegalArgumentException(String.format(Locale.ROOT, "Illegal parameter amount for function reset: %d (at least 4 are required)", tokens.length));

		String part = tokens[1];
		boolean allParts = part.equals("all");
		if(!allParts && !parts.containsKey(part))
			throw new IllegalArgumentException("Illegal part name for function reset: " + part);

		String type = tokens[2];
		boolean all = type.equals("all");
		boolean rot = all || type.equals("rotation");
		boolean off = all || type.equals("offset");

		if(!rot && !off)
			throw new IllegalArgumentException("Illegal reset type: " + type);

		int partInt = allParts ? 0 : parts.get(part);
		float time = Float.parseFloat(tokens[3]) * em.speed;

		if(model != null) {
			Timeline parallel = Timeline.createParallel();
			int lower = allParts ? 0 : partInt + (rot ? 0 : 3);
			int upper = allParts ? ModelAccessor.STATE_COUNT : partInt + (off ? ModelAccessor.STATE_COUNT : 3);

			for(int i = lower; i < upper; i++) {
				int piece = (i / ModelAccessor.MODEL_PROPS) * ModelAccessor.MODEL_PROPS;
				if(em.usedParts.contains(piece))
					parallel.push(Tween.to(model, i, time));
			}

			timeline.push(parallel);
		}

		return timeline;
	}

	private static Timeline pause(EmoteTemplate em, Timeline timeline, String[] tokens) throws IllegalArgumentException {
		assertParamSize(tokens, 2);
		float ms = Float.parseFloat(tokens[1]) * em.speed;
		return timeline.pushPause(ms);
	}

	private static Timeline yoyo(EmoteTemplate em, Timeline timeline, String[] tokens) throws IllegalArgumentException {
		assertParamSize(tokens, 3);
		int times = Integer.parseInt(tokens[1]);
		float delay = Float.parseFloat(tokens[2]) * em.speed;
		return timeline.repeatYoyo(times, delay);
	}

	private static Timeline repeat(EmoteTemplate em, Timeline timeline, String[] tokens) throws IllegalArgumentException {
		assertParamSize(tokens, 3);
		int times = Integer.parseInt(tokens[1]);
		float delay = Float.parseFloat(tokens[2]) * em.speed;
		return timeline.repeat(times, delay);
	}

	private static void assertParamSize(String[] tokens, int expect) throws IllegalArgumentException {
		if(tokens.length != expect)
			throw new IllegalArgumentException(String.format(Locale.ROOT, "Illegal parameter amount for function %s: %d (expected %d)", tokens[0], tokens.length, expect));
	}

	private static void assertParamSize(String[] tokens, int expectMin, int expectMax) throws IllegalArgumentException {
		if(tokens.length > expectMax || tokens.length < expectMin)
			throw new IllegalArgumentException(String.format(Locale.ROOT, "Illegal parameter amount for function %s: %d (expected between %d and %d)", tokens[0], tokens.length, expectMin, expectMax));
	}

	private static void assertParamSize(String mod, String[] tokens, int expect, int startingFrom) throws IllegalArgumentException {
		if(tokens.length - startingFrom < expect)
			throw new IllegalArgumentException(String.format(Locale.ROOT, "Illegal parameter amount for move modifier %s: %d (expected at least %d)", mod, tokens.length, expect));
	}

	public boolean usesBodyPart(int part) {
		return usedParts.contains(part);
	}

	private interface Function {
		Timeline invoke(EmoteTemplate em, HumanoidModel<?> model, Player player, Timeline timeline, String[] tokens) throws IllegalArgumentException;
	}

}

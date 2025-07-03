package org.violetmoon.quark.content.experimental.config;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.config.ConfigFlagManager;
import org.violetmoon.zeta.config.type.IConfigType;
import org.violetmoon.zeta.module.ZetaModule;

import java.util.*;
import java.util.Map.Entry;

public class VariantsConfig implements IConfigType {

	private static final VariantMap EMPTY_VARIANT_MAP = new VariantMap(new HashMap<>());

	@Config(
		description = "The list of all variant types available for players to use. Values are treated as suffixes to block IDs for scanning.\n"
				+ "Prefix any variant type with ! to make it show up for Manual Variants but not be automatically scanned for. (e.g. '!polish')"
	)
	private List<String> variantTypes = Arrays.asList("slab", "stairs", "wall", "fence", "fence_gate", "vertical_slab");

	@Config(
		description = "By default, only a mod's namespace is scanned for variants for its items (e.g. if coolmod adds coolmod:fun_block, it'll search only for coolmod:fun_block_stairs).\n"
				+ " Mods in this list are also scanned for variants if none are found in itself (e.g. if quark is in the list and coolmod:fun_block_stairs doesn't exist, it'll try to look for quark:fun_block_stairs next)"
	)
	private List<String> testedMods = Arrays.asList("quark", "everycomp", "v_slab_compat");

	@Config
	private boolean printVariantMapToLog = false;

	@Config(description = "Format is 'alias=original' in each value (e.g. 'wall=fence' means that a failed search for, minecraft:cobblestone_fence will try cobblestone_wall next)")
	private List<String> aliases = Arrays.asList("carpet=slab", "pane=fence");

	@Config(description = "Ends of block IDs to try and remove when looking for variants. (e.g. minecraft:oak_planks goes into minecraft:oak_stairs, so we have to include '_planks' in this list for it to find them or else it'll only look for minecraft:oak_planks_stairs)")
	private List<String> stripCandidates = Arrays.asList("_planks", "_wool", "_block", "s");

	@Config(description = "Add manual variant overrides here, the format is 'type,block,output' (e.g. polish,minecraft:stone_bricks,minecraft:chiseled_stone_bricks). The type must be listed in Variant Types")
	private List<String> manualVariants = new ArrayList<>();

	@Config(
		description = " A list of block IDs and mappings to be excluded from variant selection.\n"
				+ "To exclude a block from being turned into other blocks, just include the block ID (e.g. minecraft:cobblestone).\n"
				+ "To exclude a block from having other blocks turned into it, suffix it with = (e.g. =minecraft:cobblestone_stairs)\n"
				+ "To exclude a specific block->variant combination, put = between the two (e.g. minecraft:cobblestone=minecraft:cobblestone_stairs)"
	)
	private List<String> blacklist = Arrays.asList("minecraft:snow", "minecraft:bamboo", "minecraft:bamboo_block");

	private final Map<Block, VariantMap> blockVariants = new HashMap<>();
	private final Map<Block, Block> originals = new HashMap<>();
	private final Multimap<String, String> aliasMap = HashMultimap.create();
	private final Multimap<Block, ManualVariant> manualVariantMap = HashMultimap.create();

	private final List<String> visibleVariants = new ArrayList<>();
	private final List<String> sortedSuffixes = new ArrayList<>();

	public VariantsConfig() {}

	@Override
	public void onReload(ZetaModule module, ConfigFlagManager flagManager) {
		blockVariants.clear();
		visibleVariants.clear();
		originals.clear();
		aliasMap.clear();
		manualVariantMap.clear();
		sortedSuffixes.clear();

		if(module != null && !module.isEnabled())
			return;

		for(String s : variantTypes)
			visibleVariants.add(s.replaceAll("!", ""));

		sortedSuffixes.addAll(visibleVariants);
		sortedSuffixes.sort((s1, s2) -> { // sort by amount of _
			int ct1 = s1.replaceAll("[^_]", "").length();
			int ct2 = s2.replaceAll("[^_]", "").length();

			return ct2 - ct1;
		});

		for(String s : aliases) {
			String[] toks = s.split("=");
			aliasMap.put(toks[1], toks[0]);
		}

		for(String s : manualVariants) {
			String[] toks = s.split(",");

			Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(toks[1]));
			Block out = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(toks[2]));
			manualVariantMap.put(block, new ManualVariant(toks[0], out));
		}

		// Map all variants
		BuiltInRegistries.BLOCK.forEach(this::getVariants);

		if(printVariantMapToLog)
			logVariantMap();
	}

	@Nullable
	public String findVariantForBlock(Block variantBlock) {
		String name = BuiltInRegistries.BLOCK.getKey(variantBlock).getPath();

		for(String suffix : sortedSuffixes) {
			if(name.endsWith("_" + suffix))
				return suffix;

			if(aliasMap.containsKey(suffix))
				for(String alias : aliasMap.get(suffix))
					if(name.endsWith("_" + alias))
						return suffix;
		}

		return null;
	}

	// Null if not valid string, Not an original block or block wasn't changed (variant wasnt found)
	@Nullable
	public Block getBlockOfVariant(Block baseBlock, @NotNull String variant) {
		//not a valid string
		if(!sortedSuffixes.contains(variant))
			return null;

		VariantMap map = getVariants(baseBlock);
		return map.variants.get(variant);
	}

	// gets variant key for a block given its base block. Inverse of above
	@Nullable
	public String getVariantOfBlock(Block baseBlock, Block possibleVariant) {
		VariantMap map = getVariants(baseBlock);
		if(map != null){
			for(Entry<String, Block> entry : map.variants.entrySet())
				if(entry.getValue().equals(possibleVariant))
					return entry.getKey();
		}
		return null;
	}

	public boolean hasVariants(Block block) {
		return !getVariants(block).isEmpty();
	}

	public Collection<Block> getAllVariants(Block block) {
		Map<String, Block> map = getVariants(block).variants;
		List<Block> blocks = new ArrayList<>();
		for(String s : variantTypes) {
			if(s.startsWith("!"))
				s = s.substring(1);

			if(map.containsKey(s))
				blocks.add(map.get(s));
		}

		return blocks;
	}

	public Block getOriginalBlock(Block block) {
		return originals.getOrDefault(block, block);
	}

	public boolean isOriginal(Block block) {
		return originals.containsValue(block);
	}

	public boolean isVariant(Block block) {
		return originals.containsKey(block);
	}

	private VariantMap getVariants(Block block) {
		if(blockVariants.containsKey(block))
			return blockVariants.get(block);

		Map<String, Block> newVariants = new HashMap<>();

		if(!isBlacklisted(block, null))
			for(String s : sortedSuffixes) {
				if(!variantTypes.contains(s))
					continue; // this means its marked with ! so it won't be searched

				Block suffixed = getSuffixedBlock(block, s);
				if(suffixed != null && !isBlacklisted(null, suffixed) && !isBlacklisted(block, suffixed)) {
					newVariants.put(s, suffixed);
					originals.put(suffixed, block);
				}
			}

		if(manualVariantMap.containsKey(block))
			for(ManualVariant mv : manualVariantMap.get(block)) {
				newVariants.put(mv.type, mv.out);
				originals.put(mv.out, block);
			}

		if(newVariants.isEmpty())
			blockVariants.put(block, EMPTY_VARIANT_MAP);
		else
			blockVariants.put(block, new VariantMap(newVariants));

		return getVariants(block);
	}

	private Block getSuffixedBlock(Block ogBlock, String suffix) {
		ResourceLocation resloc = BuiltInRegistries.BLOCK.getKey(ogBlock);
		String namespace = resloc.getNamespace();
		String name = resloc.getPath();

		Block ret = getSuffixedBlock(namespace, name, suffix, "%s:%s_%s");
		if(ret != null)
			return ret;

		for(String mod : testedMods) {
			if(!Quark.ZETA.isModLoaded(mod))continue;
			ret = getSuffixedBlock(mod, name, suffix, "%s:%s_%s");
			if(ret != null)
				return ret;
			//for ec format. this could have its own config... bad code
			ret = getSuffixedBlock(mod, name, suffix, "%s:"+namespace+"/%s_%s");
			if(ret != null)
				return ret;
		}

		return null;
	}

	private Block getSuffixedBlock(String namespace, String name, String suffix, String format) {
		for(String strip : stripCandidates)
			if(name.endsWith(strip)) {
				String stripped = name.substring(0, name.length() - strip.length());
				Block strippedAttempt = getSuffixedBlock(namespace, stripped, suffix, format);
				if(strippedAttempt != null)
					return strippedAttempt;
			}

		String targetStr = String.format(Locale.ROOT, format, namespace, name, suffix);
        ResourceLocation target = ResourceLocation.parse(targetStr);
		Block ret = BuiltInRegistries.BLOCK.get(target);

		if(ret == Blocks.AIR) {
			if(aliasMap.containsKey(suffix))
				for(String alias : aliasMap.get(suffix)) {
					Block aliasAttempt = getSuffixedBlock(namespace, name, alias, format);
					if(aliasAttempt != null)
						return aliasAttempt;
				}

			return null;
		}

		return ret;
	}

	private boolean isBlacklisted(Block block, Block result) {
		if(blacklist.isEmpty())
			return false;

		String search = "";
		if(block != null)
			search += BuiltInRegistries.BLOCK.getKey(block).toString();
		if(result != null)
			search += ("=" + BuiltInRegistries.BLOCK.getKey(result).toString());

		return !search.isEmpty() && blacklist.contains(search);
	}

	public boolean isKnownVariant(String variant) {
		return visibleVariants.contains(variant);
	}

	public List<String> getVisibleVariants() {
		return visibleVariants;
	}

	private void logVariantMap() {
		for(Entry<Block, Block> entry : originals.entrySet())
			Quark.LOG.info("{} is variant of {}", entry.getKey(), entry.getValue());
	}

	private record ManualVariant(String type, Block out) {
	}

	private record VariantMap(Map<String, Block> variants) {

		private boolean isEmpty() {
			return variants.isEmpty();
		}

	}

}

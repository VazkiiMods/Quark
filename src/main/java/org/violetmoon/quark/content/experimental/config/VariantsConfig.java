package org.violetmoon.quark.content.experimental.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.config.Config;
import org.violetmoon.quark.base.config.ConfigFlagManager;
import org.violetmoon.quark.base.config.type.IConfigType;
import org.violetmoon.zeta.module.ZetaModule;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class VariantsConfig implements IConfigType {

	private static final VariantMap EMPTY_VARIANT_MAP = new VariantMap(new HashMap<>());
	
	@Config(description = "The list of all variant types available for players to use. Values are treated as suffixes to block IDs for scanning.\n"
			+ "Prefix any variant type with ! to make it show up for Manual Variants but not be automatically scanned for. (e.g. '!polish')")
	private List<String> variantTypes = Arrays.asList("slab", "stairs", "wall", "fence", "fence_gate", "vertical_slab");
	
	@Config(description = "By default, only a mod's namespace is scanned for variants for its items (e.g. if coolmod adds coolmod:fun_block, it'll search only for coolmod:fun_block_stairs).\n"
			+ " Mods in this list are also scanned for variants if none are found in itself (e.g. if quark is in the list and coolmod:fun_block_stairs doesn't exist, it'll try to look for quark:fun_block_stairs next)")
	private List<String> testedMods = Arrays.asList("quark");
	
	@Config
	private boolean printVariantMapToLog = false;
	
	@Config(description = "Format is 'alias=original' in each value (e.g. 'wall=fence' means that a failed search for, minecraft:cobblestone_fence will try cobblestone_wall next)")
	private List<String> aliases = Arrays.asList("carpet=slab", "pane=fence");
	
	@Config(description = "Ends of block IDs to try and remove when looking for variants. (e.g. minecraft:oak_planks goes into minecraft:oak_stairs, so we have to include '_planks' in this list for it to find them or else it'll only look for minecraft:oak_planks_stairs)")
	private List<String> stripCandidates = Arrays.asList("_planks", "_wool", "_block", "s");
	
	@Config(description = "Add manual variant overrides here, the format is 'type,block,output' (e.g. polish,minecraft:stone_bricks,minecraft:chiseled_stone_bricks). The type must be listed in Variant Types")
	private List<String> manualVariants = new ArrayList<>();

	@Config(description = " A list of block IDs and mappings to be excluded from variant selection.\n"
		+ "To exclude a block from being turned into other blocks, just include the block ID (e.g. minecraft:cobblestone).\n"
		+ "To exclude a block from having other blocks turned into it, suffix it with = (e.g. =minecraft:cobblestone_stairs)\n"
		+ "To exclude a specific block->variant combination, put = between the two (e.g. minecraft:cobblestone=minecraft:cobblestone_stairs)")
	private List<String> blacklist = Arrays.asList("minecraft:snow", "minecraft:bamboo", "quark:bamboo_block");
	
	private Map<Block, VariantMap> blockVariants = new HashMap<>();
	private Map<Block, Block> originals = new HashMap<>();
	private Multimap<String, String> aliasMap = HashMultimap.create();
	private Multimap<Block, ManualVariant> manualVariantMap = HashMultimap.create();
	
	private List<String> visibleVariants = new ArrayList<>();
	private List<String> sortedSuffixes;
	
	public VariantsConfig() { }
	
	@Override
	public void onReload(ZetaModule module, ConfigFlagManager flagManager) {
		blockVariants.clear();
		visibleVariants.clear();
		originals.clear();
		aliasMap.clear();
		manualVariantMap.clear();
		
		if(module != null && !module.enabled)
			return;
		
		for(String s : variantTypes)
			visibleVariants.add(s.replaceAll("!", ""));
		
		sortedSuffixes = new ArrayList<>(visibleVariants);
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
			
			Block block = Registry.BLOCK.get(new ResourceLocation(toks[1]));
			Block out = Registry.BLOCK.get(new ResourceLocation(toks[2]));
			manualVariantMap.put(block, new ManualVariant(toks[0], out));
		}
		
		// Map all variants
		Registry.BLOCK.forEach(this::getVariants);
		
		if(printVariantMapToLog)
			logVariantMap();
	}
	
	public String getVariantForBlock(Block block) {
		String name = Registry.BLOCK.getKey(block).getPath();
		
		for(String suffix : sortedSuffixes) {
			if(name.endsWith(String.format("_%s", suffix)))
				return suffix;
			
			if(aliasMap.containsKey(suffix))
				for(String alias : aliasMap.get(suffix))
					if(name.endsWith(String.format("_%s", alias)))
						return suffix;
		}
		
		return null;
	}
	
	public Block getBlockForTarget(Block block, Block target) {
		return getBlockForVariant(block, getVariantForBlock(target));
	}
	
	public Block getBlockForVariant(Block block, String variant) {
		blockVariants.clear();
		if(variant == null || !sortedSuffixes.contains(variant))
			return block;
		
		VariantMap map = getVariants(block);
		Block ret = map.variants.get(variant);
		if(ret != null)
			return ret;
		
		return block;
	}
	
	public Collection<Block> getAllVariants(Block block) {
		Map<String, Block> map =  getVariants(block).variants;
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
		else blockVariants.put(block, new VariantMap(newVariants));
		
		return getVariants(block);
	}
	
	private Block getSuffixedBlock(Block ogBlock, String suffix) {
		ResourceLocation resloc = Registry.BLOCK.getKey(ogBlock);
		String namespace = resloc.getNamespace();
		String name = resloc.getPath();
		
		Block ret = getSuffixedBlock(namespace, name, suffix);
		if(ret != null)
			return ret;
		
		for(String mod : testedMods) {
			ret = getSuffixedBlock(mod, name, suffix);
			if(ret != null)
				return ret;
		}
		
		return null;
	}
	
	private Block getSuffixedBlock(String namespace, String name, String suffix) {
		for(String strip : stripCandidates)
		if(name.endsWith(strip)) {
			String stripped = name.substring(0, name.length() - strip.length());
			Block strippedAttempt = getSuffixedBlock(namespace, stripped, suffix);
			if(strippedAttempt != null)
				return strippedAttempt;
		}
		
		String targetStr = String.format("%s:%s_%s", namespace, name, suffix);
		ResourceLocation target = new ResourceLocation(targetStr);
		Block ret = Registry.BLOCK.get(target);
		
		if(ret == Blocks.AIR) {
			if(aliasMap.containsKey(suffix))
				for(String alias : aliasMap.get(suffix)) {
					Block aliasAttempt = getSuffixedBlock(namespace, name, alias);
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
			search += Registry.BLOCK.getKey(block).toString();
		if(result != null)
			search += ("=" + Registry.BLOCK.getKey(result).toString());

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
	
	private static record ManualVariant(String type, Block out) {} 
	
	private static record VariantMap(Map<String, Block> variants) {
		
		private boolean isEmpty() {
			return variants.isEmpty();
		}
		
	}
	
}

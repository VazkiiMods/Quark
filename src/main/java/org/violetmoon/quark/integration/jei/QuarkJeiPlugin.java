package org.violetmoon.quark.integration.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.*;
import mezz.jei.library.ingredients.subtypes.SubtypeInterpreters;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.addons.oddities.block.be.MatrixEnchantingTableBlockEntity;
import org.violetmoon.quark.addons.oddities.client.screen.BackpackInventoryScreen;
import org.violetmoon.quark.addons.oddities.client.screen.CrateScreen;
import org.violetmoon.quark.addons.oddities.module.MatrixEnchantingModule;
import org.violetmoon.quark.addons.oddities.util.Influence;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.building.module.VariantFurnacesModule;
import org.violetmoon.quark.content.tools.item.AncientTomeItem;
import org.violetmoon.quark.content.tools.module.AncientTomesModule;
import org.violetmoon.quark.content.tools.module.PickarangModule;
import org.violetmoon.quark.content.tweaks.module.DiamondRepairModule;
import org.violetmoon.quark.content.tweaks.recipe.ElytraDuplicationRecipe;
import org.violetmoon.quark.content.tweaks.recipe.SlabToBlockRecipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@JeiPlugin
public class QuarkJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = Quark.asResource(Quark.MOD_ID);

    public static final RecipeType<InfluenceEntry> INFLUENCING =
            RecipeType.create(Quark.MOD_ID, "influence", InfluenceEntry.class);

    @NotNull
    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerItemSubtypes(@NotNull ISubtypeRegistration registration) {
        //registration.registerSubtypeInterpreter(AncientTomesModule.ancient_tome);
    }

    @Override
    public void registerVanillaCategoryExtensions(@NotNull IVanillaCategoryExtensionRegistration registration) {
        //registration.getCraftingCategory().addExtension(ElytraDuplicationRecipe.class, ElytraDuplicationExtension::new);
        //registration.getCraftingCategory().addExtension(SlabToBlockRecipe.class, SlabToBlockExtension::new);
    }

    private boolean matrix() {
        return Quark.ZETA.modules.isEnabled(MatrixEnchantingModule.class) && MatrixEnchantingModule.allowInfluencing && !MatrixEnchantingModule.candleInfluencingFailed;
    }

    @Override
    public void registerCategories(@NotNull IRecipeCategoryRegistration registration) {
        if (matrix())
            registration.addRecipeCategories(new InfluenceCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        IVanillaRecipeFactory factory = registration.getVanillaRecipeFactory();

        if (Quark.ZETA.modules.isEnabled(AncientTomesModule.class))
            registerAncientTomeAnvilRecipes(registration, factory);

        if (Quark.ZETA.modules.isEnabled(PickarangModule.class)) {
            registerPickarangAnvilRepairs(PickarangModule.pickarang, Items.DIAMOND, registration, factory);
            registerPickarangAnvilRepairs(PickarangModule.flamerang, Items.NETHERITE_INGOT, registration, factory);
        }

        if (matrix())
            registerInfluenceRecipes(registration);

        if (Quark.ZETA.modules.isEnabled(DiamondRepairModule.class))
            registerCustomAnvilRecipes(registration, factory);
    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
        if (Quark.ZETA.modules.isEnabled(VariantFurnacesModule.class)) {
            registration.addRecipeCatalyst(new ItemStack(VariantFurnacesModule.deepslateFurnace), RecipeTypes.FUELING, RecipeTypes.SMELTING);
            registration.addRecipeCatalyst(new ItemStack(VariantFurnacesModule.blackstoneFurnace), RecipeTypes.FUELING, RecipeTypes.SMELTING);
        }

        if (matrix()) {
            if (MatrixEnchantingModule.automaticallyConvert)
                registration.addRecipeCatalyst(new ItemStack(Blocks.ENCHANTING_TABLE), INFLUENCING);
            else
                registration.addRecipeCatalyst(new ItemStack(MatrixEnchantingModule.matrixEnchanter), INFLUENCING);
        }
    }

    @Override
    public void registerGuiHandlers(@NotNull IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(CrateScreen.class, new CrateGuiHandler());
        registration.addRecipeClickArea(BackpackInventoryScreen.class, 137, 29, 10, 13, RecipeTypes.CRAFTING);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(new BackpackRecipeTransferHandler(registration.getTransferHelper()), RecipeTypes.CRAFTING);
    }

    private void registerAncientTomeAnvilRecipes(@NotNull IRecipeRegistration registration, @NotNull IVanillaRecipeFactory factory) {
        List<IJeiAnvilRecipe> recipes = new ArrayList<>();
        for (Holder<Enchantment> enchant : AncientTomesModule.validEnchants) {
            EnchantmentInstance data = new EnchantmentInstance(enchant, enchant.value().getMaxLevel());
            recipes.add(factory.createAnvilRecipe(EnchantedBookItem.createForEnchantment(data),
                    Collections.singletonList(AncientTomeItem.getEnchantedItemStack(enchant)),
                    Collections.singletonList(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(data.enchantment, data.level + 1)))));
        }
        registration.addRecipes(RecipeTypes.ANVIL, recipes);
    }

    private void registerPickarangAnvilRepairs(Item pickarang, Item repairMaterial, @NotNull IRecipeRegistration registration, @NotNull IVanillaRecipeFactory factory) {
        //Repair ratios taken from JEI anvil maker
        ItemStack nearlyBroken = new ItemStack(pickarang);
        nearlyBroken.setDamageValue(nearlyBroken.getMaxDamage());
        ItemStack veryDamaged = nearlyBroken.copy();
        veryDamaged.setDamageValue(veryDamaged.getMaxDamage() * 3 / 4);
        ItemStack damaged = nearlyBroken.copy();
        damaged.setDamageValue(damaged.getMaxDamage() * 2 / 4);

        IJeiAnvilRecipe materialRepair = factory.createAnvilRecipe(nearlyBroken,
                Collections.singletonList(new ItemStack(repairMaterial)), Collections.singletonList(veryDamaged));
        IJeiAnvilRecipe toolRepair = factory.createAnvilRecipe(veryDamaged,
                Collections.singletonList(veryDamaged), Collections.singletonList(damaged));

        registration.addRecipes(RecipeTypes.ANVIL, Arrays.asList(materialRepair, toolRepair));
    }

    private void registerInfluenceRecipes(@NotNull IRecipeRegistration registration) {
        registration.addRecipes(INFLUENCING,
                Arrays.stream(DyeColor.values()).map(color -> {
                    Block candle = MatrixEnchantingTableBlockEntity.CANDLES.get(color.getId());
                    Influence influence = MatrixEnchantingModule.candleInfluences.get(color).toInfluence();

                    return new InfluenceEntry(candle, influence);
                }).filter(InfluenceEntry::hasAny).collect(Collectors.toList()));

        registration.addRecipes(INFLUENCING,
                MatrixEnchantingModule.customInfluences.entrySet().stream().map(entry -> {
                    Block block = entry.getKey().getBlock();
                    Influence influence = entry.getValue().influence().toInfluence();

                    return new InfluenceEntry(block, influence);
                }).filter(InfluenceEntry::hasAny).collect(Collectors.toList()));
    }

    private void hideAnvilRepairRecipes(@NotNull IRecipeManager manager) {
        Stream<IJeiAnvilRecipe> anvilRecipe = manager.createRecipeLookup(RecipeTypes.ANVIL).get();
        List<IJeiAnvilRecipe> hidden =
                anvilRecipe.filter(r -> {
                    ItemStack left = r.getLeftInputs().stream()
                            .filter(st -> {
                                Item i = st.getItem();
                                return DiamondRepairModule.repairChanges.containsKey(i) || DiamondRepairModule.unrepairableItems.contains(i);
                            })
                            .findFirst()
                            .orElse(null);

                    if (left != null) {
                        for (ItemStack right : r.getRightInputs()) {
                            Item item = left.getItem();
                            if (item.isValidRepairItem(left, right))
                                return true;
                        }
                    }

                    return false;
                }).collect(Collectors.toList());

        manager.hideRecipes(RecipeTypes.ANVIL, hidden);
    }

    private void registerCustomAnvilRecipes(@NotNull IRecipeRegistration registration, @NotNull IVanillaRecipeFactory factory) {
        for (Item item : DiamondRepairModule.repairChanges.keySet()) {
            ItemStack left = new ItemStack(item);
            ItemStack out = left.copy();

            int max = left.get(DataComponents.MAX_DAMAGE);

            left.setDamageValue(max - 1);
            out.setDamageValue(max - max / 4);

            for (Item repair : DiamondRepairModule.repairChanges.get(item)) {
                IJeiAnvilRecipe toolRepair = factory.createAnvilRecipe(left, Collections.singletonList(new ItemStack(repair)), Collections.singletonList(out));

                registration.addRecipes(RecipeTypes.ANVIL, List.of(toolRepair));
            }
        }
    }

    private static class CrateGuiHandler implements IGuiContainerHandler<CrateScreen> {

        @NotNull
        @Override
        public List<Rect2i> getGuiExtraAreas(@NotNull CrateScreen containerScreen) {
            return containerScreen.getExtraAreas();
        }

    }
}

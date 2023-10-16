package vazkii.quark.addons.oddities.block.be;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.zeta.util.ItemNBTHelper;
import vazkii.quark.addons.oddities.inventory.EnchantmentMatrix;
import vazkii.quark.addons.oddities.inventory.EnchantmentMatrix.Piece;
import vazkii.quark.addons.oddities.inventory.MatrixEnchantingMenu;
import vazkii.quark.addons.oddities.module.MatrixEnchantingModule;
import vazkii.quark.addons.oddities.util.Influence;
import vazkii.quark.api.IEnchantmentInfluencer;

public class MatrixEnchantingTableBlockEntity extends AbstractEnchantingTableBlockEntity implements MenuProvider {

	public static final List<Block> CANDLES = Lists.newArrayList(Blocks.WHITE_CANDLE, Blocks.ORANGE_CANDLE, Blocks.MAGENTA_CANDLE, Blocks.LIGHT_BLUE_CANDLE, Blocks.YELLOW_CANDLE, Blocks.LIME_CANDLE, Blocks.PINK_CANDLE, Blocks.GRAY_CANDLE, Blocks.LIGHT_GRAY_CANDLE, Blocks.CYAN_CANDLE, Blocks.PURPLE_CANDLE, Blocks.BLUE_CANDLE, Blocks.BROWN_CANDLE, Blocks.GREEN_CANDLE, Blocks.RED_CANDLE, Blocks.BLACK_CANDLE);

	public static final int OPER_ADD = 0;
	public static final int OPER_PLACE = 1;
	public static final int OPER_REMOVE = 2;
	public static final int OPER_ROTATE = 3;
	public static final int OPER_MERGE = 4;

	public static final String TAG_STACK_MATRIX = "quark:enchantingMatrix";

	private static final String TAG_MATRIX = "matrix";
	private static final String TAG_MATRIX_UUID_LESS = "uuidLess";
	private static final String TAG_MATRIX_UUID_MOST = "uuidMost";
	private static final String TAG_CHARGE = "charge";

	public EnchantmentMatrix matrix;
	private boolean matrixDirty = false;
	public boolean clientMatrixDirty = false;
	private UUID matrixId;

	public final Map<Enchantment, Integer> influences = new HashMap<>();
	public int bookshelfPower, enchantability, charge;

	public MatrixEnchantingTableBlockEntity(BlockPos pos, BlockState state) {
		super(MatrixEnchantingModule.blockEntityType, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, MatrixEnchantingTableBlockEntity be) {
		be.tick();
	}

	@Override
	public void tick() {
		super.tick();

		ItemStack item = getItem(0);
		if(item.isEmpty()) {
			if(matrix != null) {
				matrixDirty = true;
				matrix = null;
			}
		} else {
			loadMatrix(item);

			if(level.getGameTime() % 20 == 0 || matrixDirty)
				updateEnchantPower();
		}

		if(charge <= 0 && !level.isClientSide) {
			ItemStack lapis = getItem(1);
			if(!lapis.isEmpty()) {
				lapis.shrink(1);
				charge += MatrixEnchantingModule.chargePerLapis;
				sync();
			}
		}

		if(matrixDirty) {
			makeOutput();
			matrixDirty = false;
		}
	}

	public void onOperation(Player player, int operation, int arg0, int arg1, int arg2) {
		if(matrix == null)
			return;

		switch (operation) {
			case OPER_ADD -> apply(m -> generateAndPay(m, player));
			case OPER_PLACE -> apply(m -> m.place(arg0, arg1, arg2));
			case OPER_REMOVE -> apply(m -> m.remove(arg0));
			case OPER_ROTATE -> apply(m -> m.rotate(arg0));
			case OPER_MERGE -> apply(m -> m.merge(arg0, arg1));
		}
	}
	
	public boolean isMatrixInfluenced() {
		return matrix.isInfluenced();
	}

	private void apply(Predicate<EnchantmentMatrix> oper) {
		if(oper.test(matrix)) {
			ItemStack item = getItem(0);
			commitMatrix(item);
		}
	}

	private boolean generateAndPay(EnchantmentMatrix matrix, Player player) {
		if(matrix.canGeneratePiece(influences, bookshelfPower, enchantability) && matrix.validateXp(player, bookshelfPower)) {
			boolean creative = player.getAbilities().instabuild;
			int cost = matrix.getNewPiecePrice();
			if(charge > 0 || creative) {
				if (matrix.generatePiece(influences, bookshelfPower, getItem(0).is(Items.BOOK), false)) {
					if (!creative) {
						player.giveExperienceLevels(-cost);
						charge = Math.max(charge - 1, 0);
					}
				}
			}
		}

		return true;
	}

	private void makeOutput() {
		if(level.isClientSide)
			return;

		setItem(2, ItemStack.EMPTY);
		ItemStack in = getItem(0);
		if(!in.isEmpty() && matrix != null && !matrix.placedPieces.isEmpty()) {
			ItemStack out = in.copy();
			boolean book = false;
			if(out.getItem() == Items.BOOK) {
				out = new ItemStack(Items.ENCHANTED_BOOK);
				book = true;
			}

			Map<Enchantment, Integer> enchantments = new HashMap<>();

			for(int i : matrix.placedPieces) {
				Piece p = matrix.pieces.get(i);

				if (p != null && p.enchant != null) {
					for (Enchantment o : enchantments.keySet())
						if (o == p.enchant || !p.enchant.isCompatibleWith(o) || !o.isCompatibleWith(p.enchant))
							return; // Incompatible

					enchantments.put(p.enchant, p.level);
				}
			}

			if(book)
				for(Entry<Enchantment, Integer> e : enchantments.entrySet())
					EnchantedBookItem.addEnchantment(out, new EnchantmentInstance(e.getKey(), e.getValue()));
			else {
				EnchantmentHelper.setEnchantments(enchantments, out);
				ItemNBTHelper.getNBT(out).remove(TAG_STACK_MATRIX);
			}

			setItem(2, out);
		}
	}

	private void loadMatrix(ItemStack stack) {
		if(matrix == null || matrix.target != stack) {
			if(matrix != null)
				matrixDirty = true;
			matrix = null;

			if(stack.isEnchantable()) {
				matrix = new EnchantmentMatrix(stack, level.random);
				matrixDirty = true;
				makeUUID();

				if(ItemNBTHelper.verifyExistence(stack, TAG_STACK_MATRIX)) {
					CompoundTag cmp = ItemNBTHelper.getCompound(stack, TAG_STACK_MATRIX, true);
					if(cmp != null)
						matrix.readFromNBT(cmp);
				}
			}
		}
	}

	private void commitMatrix(ItemStack stack) {
		if(level.isClientSide)
			return;

		CompoundTag cmp = new CompoundTag();
		matrix.writeToNBT(cmp);
		ItemNBTHelper.setCompound(stack, TAG_STACK_MATRIX, cmp);

		matrixDirty = true;
		makeUUID();
		sync();
		setChanged();
	}

	private void makeUUID() {
		if(!level.isClientSide)
			matrixId = UUID.randomUUID();
	}

	private void updateEnchantPower() {
		ItemStack item = getItem(0);
		influences.clear();
		if(item.isEmpty())
			return;

		enchantability = item.getItem().getEnchantmentValue(item);

		boolean allowWater = MatrixEnchantingModule.allowUnderwaterEnchanting;
		boolean allowShort = MatrixEnchantingModule.allowShortBlockEnchanting;
		
		float power = 0;
		for (int j = -1; j <= 1; ++j) {
			for (int k = -1; k <= 1; ++k) {
				if(isAirGap(j, k, allowWater, allowShort)) {
					power += getEnchantPowerAt(level, worldPosition.offset(k * 2, 0, j * 2));
					power += getEnchantPowerAt(level, worldPosition.offset(k * 2, 1, j * 2));
					if (k != 0 && j != 0) {
						power += getEnchantPowerAt(level, worldPosition.offset(k * 2, 0, j));
						power += getEnchantPowerAt(level, worldPosition.offset(k * 2, 1, j));
						power += getEnchantPowerAt(level, worldPosition.offset(k, 0, j * 2));
						power += getEnchantPowerAt(level, worldPosition.offset(k, 1, j * 2));
					}
				}
			}
		}

		bookshelfPower = Math.min((int) power, MatrixEnchantingModule.maxBookshelves);
	}

	private boolean isAirGap(int j, int k, boolean allowWater, boolean allowShortBlock) {
		if(j != 0 || k != 0) {
			BlockPos test = worldPosition.offset(k, 0, j);
			BlockPos testUp = test.above();

			return (level.isEmptyBlock(test) || (allowWater && level.getBlockState(test).getBlock() == Blocks.WATER) || (allowShortBlock && isShortBlock(level, test)))
					&& (level.isEmptyBlock(testUp) || (allowWater && level.getBlockState(testUp).getBlock() == Blocks.WATER) || (allowShortBlock && isShortBlock(level, testUp)));
		}

		return false;
	}
	
	public static boolean isShortBlock(Level level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		Block block = state.getBlock();
		VoxelShape shape = block.getShape(state, level, pos, CollisionContext.empty());
		AABB bounds = shape.bounds();
		
		float f = (1F / 16F) * 3F;
		return (bounds.minY == 0 && bounds.maxY <= f) || (bounds.maxY == 1F && bounds.minY >= (1F - f)); 
	}

	private float getEnchantPowerAt(Level world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);

		if(MatrixEnchantingModule.allowInfluencing) {
			IEnchantmentInfluencer influencer = getInfluencerFromBlock(state, world, pos);

			if(influencer != null) {
				int count = influencer.getInfluenceStack(world, pos, state);

				List<Enchantment> influencedEnchants = ForgeRegistries.ENCHANTMENTS.getValues().stream()
						.filter((it) -> influencer.influencesEnchantment(world, pos, state, it)).toList();
				List<Enchantment> dampenedEnchants = ForgeRegistries.ENCHANTMENTS.getValues().stream()
						.filter((it) -> influencer.dampensEnchantment(world, pos, state, it)).toList();
				if(!influencedEnchants.isEmpty() || !dampenedEnchants.isEmpty()) {
					for(Enchantment e : influencedEnchants) {
						int curr = influences.getOrDefault(e, 0);
						influences.put(e, curr + count);
					}

					for(Enchantment e : dampenedEnchants) {
						int curr = influences.getOrDefault(e, 0);
						influences.put(e, curr - count);
					}

					return 1;
				}
			}
		}

		return state.getEnchantPowerBonus(world, pos);
	}

	@Override
	public void writeSharedNBT(CompoundTag cmp) {
		super.writeSharedNBT(cmp);

		CompoundTag matrixCmp = new CompoundTag();
		if(matrix != null) {
			matrix.writeToNBT(matrixCmp);

			cmp.put(TAG_MATRIX, matrixCmp);
			if(matrixId != null) {
				cmp.putLong(TAG_MATRIX_UUID_LESS, matrixId.getLeastSignificantBits());
				cmp.putLong(TAG_MATRIX_UUID_MOST, matrixId.getMostSignificantBits());
			}
		}
		cmp.putInt(TAG_CHARGE, charge);
	}

	@Override
	public void readSharedNBT(CompoundTag cmp) {
		super.readSharedNBT(cmp);

		if(cmp.contains(TAG_MATRIX)) {
			long least = cmp.getLong(TAG_MATRIX_UUID_LESS);
			long most = cmp.getLong(TAG_MATRIX_UUID_MOST);
			UUID newId = new UUID(most, least);

			if(!newId.equals(matrixId)) {
				CompoundTag matrixCmp = cmp.getCompound(TAG_MATRIX);
				matrixId = newId;
				matrix = new EnchantmentMatrix(getItem(0), RandomSource.create());
				matrix.readFromNBT(matrixCmp);
			}
			clientMatrixDirty = true;
		} else matrix = null;

		charge = cmp.getInt(TAG_CHARGE);
	}

	@Override
	public AbstractContainerMenu createMenu(int id, @Nonnull Inventory inv, @Nonnull Player player) {
		return new MatrixEnchantingMenu(id, inv, this);
	}

	@Nonnull
	@Override
	public Component getDisplayName() {
		return getName();
	}

	@Nullable
	public static IEnchantmentInfluencer getInfluencerFromBlock(BlockState state, Level world, BlockPos pos) {
		if (state.getBlock() instanceof IEnchantmentInfluencer influencer)
			return influencer;
		else if (MatrixEnchantingModule.customInfluences.containsKey(state))
			return MatrixEnchantingModule.customInfluences.get(state);
		return CandleInfluencer.forBlock(state.getBlock(), world, pos);
	}

	private record CandleInfluencer(boolean inverted) implements IEnchantmentInfluencer {

		private static final CandleInfluencer INSTANCE = new CandleInfluencer(false);
		private static final CandleInfluencer INVERTED_INSTANCE = new CandleInfluencer(true);

		@Nullable
		public static CandleInfluencer forBlock(Block block, Level world, BlockPos pos) {
			if (MatrixEnchantingModule.candleInfluencingFailed)
				return null;

			if (CANDLES.contains(block)) {
				if (MatrixEnchantingModule.soulCandlesInvert) {
					BlockPos posBelow = pos.below();
					BlockState below = world.getBlockState(posBelow);
					if (below.is(BlockTags.SOUL_FIRE_BASE_BLOCKS))
						return INVERTED_INSTANCE;
					else if (below.getEnchantPowerBonus(world, posBelow) > 0) {
						posBelow = posBelow.below();
						below = world.getBlockState(posBelow);
						if (below.is(BlockTags.SOUL_FIRE_BASE_BLOCKS))
							return INVERTED_INSTANCE;
					}
				}

				return INSTANCE;
			}

			return null;
		}

		private DyeColor getColor(BlockState state) {
			if (!state.getValue(CandleBlock.LIT))
				return null;

			int index = CANDLES.indexOf(state.getBlock());
			return index >= 0 ? DyeColor.values()[index] : null;
		}

		@Override
		public float[] getEnchantmentInfluenceColor(BlockGetter world, BlockPos pos, BlockState state) {
			DyeColor color = getColor(state);
			return color == null ? null : color.getTextureDiffuseColors();
		}

		@Nullable
		@Override
		public ParticleOptions getExtraParticleOptions(BlockGetter world, BlockPos pos, BlockState state) {
			if (inverted && state.getValue(CandleBlock.LIT))
				return ParticleTypes.SOUL;
			return null;
		}

		@Override
		public double getExtraParticleChance(BlockGetter world, BlockPos pos, BlockState state) {
			return 0.25;
		}

		@Override
		public int getInfluenceStack(BlockGetter world, BlockPos pos, BlockState state) {
			return state.getValue(CandleBlock.LIT) ? state.getValue(CandleBlock.CANDLES) : 0;
		}

		@Override
		public boolean influencesEnchantment(BlockGetter world, BlockPos pos, BlockState state, Enchantment enchantment) {
			DyeColor color = getColor(state);
			if (color == null)
				return false;
			Influence influence = MatrixEnchantingModule.candleInfluences.get(color);
			List<Enchantment> boosts = inverted ? influence.dampen() : influence.boost();
			return boosts.contains(enchantment);
		}

		@Override
		public boolean dampensEnchantment(BlockGetter world, BlockPos pos, BlockState state, Enchantment enchantment) {
			DyeColor color = getColor(state);
			if (color == null)
				return false;
			Influence influence = MatrixEnchantingModule.candleInfluences.get(color);
			List<Enchantment> dampens = inverted ? influence.boost() : influence.dampen();
			return dampens.contains(enchantment);
		}
	}

}

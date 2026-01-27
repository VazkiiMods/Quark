package org.violetmoon.quark.addons.oddities.block.be;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.connection.ConnectionType;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.violetmoon.quark.addons.oddities.block.pipe.BasePipeBlock;
import org.violetmoon.quark.addons.oddities.module.PipesModule;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.handler.QuarkSounds;
import org.violetmoon.quark.content.automation.block.be.ChuteBlockEntity;
import org.violetmoon.quark.content.automation.module.ChuteModule;
import org.violetmoon.zeta.util.MiscUtil;
import org.violetmoon.zeta.util.SimpleInventoryBlockEntity;

import java.util.*;
import java.util.function.Predicate;

public class PipeBlockEntity extends SimpleInventoryBlockEntity {

	public PipeBlockEntity(BlockPos pos, BlockState state) {
		super(PipesModule.blockEntityType, pos, state);
	}

	private static final String TAG_PIPE_ITEMS = "pipeItems";
	private static final String TAG_CONNECTIONS = "connections";

	private boolean iterating = false;
	public final List<PipeItem> pipeItems = new LinkedList<>();
	public final List<PipeItem> queuedItems = new LinkedList<>();

	private boolean skipSync = false;

	private final ConnectionType[] connectionsCache = new ConnectionType[6];
	private boolean convert = false; //used to convert old pipes

	public static boolean isTheGoodDay() {
		Calendar calendar = Calendar.getInstance();
		return calendar.get(Calendar.MONTH) + 1 == 4 && calendar.get(Calendar.DAY_OF_MONTH) == 1;
	}

	public static void tick(Level level, BlockPos pos, BlockState state, PipeBlockEntity be) {
		be.tick();
	}

	public void tick() {
		//convert old pipes
		if(convert) {
			convert = false;
			refreshVisualConnections();
		}
		boolean enabled = isPipeEnabled();
		if(!enabled && level.getGameTime() % 10 == 0 && level instanceof ServerLevel serverLevel)
			serverLevel.sendParticles(new DustParticleOptions(new Vector3f(1.0F, 0.0F, 0.0F), 1.0F), worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, 3, 0.2, 0.2, 0.2, 0);

		BlockState blockAt = level.getBlockState(worldPosition);
		if(!level.isClientSide && enabled && blockAt.is(PipesModule.pipesTag)) {
			for(Direction side : Direction.values()) {
				if(connectionsCache[side.ordinal()] == ConnectionType.OPENING) {
					double minX = worldPosition.getX() + 0.25 + 0.5 * Math.min(0, side.getStepX());
					double minY = worldPosition.getY() + 0.25 + 0.5 * Math.min(0, side.getStepY());
					double minZ = worldPosition.getZ() + 0.25 + 0.5 * Math.min(0, side.getStepZ());
					double maxX = worldPosition.getX() + 0.75 + 0.5 * Math.max(0, side.getStepX());
					double maxY = worldPosition.getY() + 0.75 + 0.5 * Math.max(0, side.getStepY());
					double maxZ = worldPosition.getZ() + 0.75 + 0.5 * Math.max(0, side.getStepZ());

					Direction opposite = side.getOpposite();

					boolean pickedItemsUp = false;
					Predicate<ItemEntity> predicate = entity -> {
						if(entity == null || !entity.isAlive())
							return false;

						Vec3 motion = entity.getDeltaMovement();
						Direction dir = Direction.getNearest(motion.x, motion.y, motion.z);

						return dir == opposite;
					};

					for(ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(minX, minY, minZ, maxX, maxY, maxZ), predicate)) {
						passIn(item.getItem().copy(), side);

						if(PipesModule.doPipesWhoosh) {
							if(isTheGoodDay())
								level.playSound(null, item.getX(), item.getY(), item.getZ(), QuarkSounds.BLOCK_PIPE_PICKUP_LENNY, SoundSource.BLOCKS, 1f, 1f);
							else
								level.playSound(null, item.getX(), item.getY(), item.getZ(), QuarkSounds.BLOCK_PIPE_PICKUP, SoundSource.BLOCKS, 1f, 1f);
						}

						if(PipesModule.emitVibrations)
							getLevel().gameEvent(GameEvent.PROJECTILE_LAND, getBlockPos(), Context.of(getBlockState()));

						pickedItemsUp = true;
						item.discard();
					}

					if(pickedItemsUp)
						sync();
				}
			}
		}

		int currentOut = getComparatorOutput();

		if(!pipeItems.isEmpty()) {
			if(PipesModule.maxPipeItems > 0 && pipeItems.size() > PipesModule.maxPipeItems && !level.isClientSide) {
				level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, worldPosition, Block.getId(level.getBlockState(worldPosition)));
				dropItem(new ItemStack(getBlockState().getBlock()));
				level.removeBlock(getBlockPos(), false);
			}

			ListIterator<PipeItem> itemItr = pipeItems.listIterator();
			iterating = true;
			while(itemItr.hasNext()) {
				PipeItem item = itemItr.next();
				Direction lastFacing = item.outgoingFace;
				if(item.tick(this)) {
					itemItr.remove();

					if(item.valid)
						passOut(item);
					else if(!level.isClientSide) {
						dropItem(item.stack, lastFacing, true);
					}
				}
			}
			iterating = false;

			if(!queuedItems.isEmpty()) {
				pipeItems.addAll(queuedItems);
				queuedItems.clear();
                sync();
            }
		}



		if(getComparatorOutput() != currentOut)
			level.updateNeighbourForOutputSignal(getBlockPos(), getBlockState().getBlock());
	}

	public int getComparatorOutput() {
		return Math.min(15, pipeItems.size());
	}

	public Iterator<PipeItem> getItemIterator() {
		return pipeItems.iterator();
	}

	public boolean allowsFullConnection(PipeBlockEntity.ConnectionType conn) {
		return getBlockState().getBlock() instanceof BasePipeBlock pipe && pipe.allowsFullConnection(conn);
	}

	public boolean passIn(ItemStack stack, Direction face, Direction backlog, long seed, int time) {
		PipeItem item = new PipeItem(stack, face, seed);
		item.lastTickUpdated = level.getGameTime();
		item.backloggedFace = backlog;
		if(!iterating) {
			int currentOut = getComparatorOutput();
			pipeItems.add(item);
			item.timeInWorld = time;
			if(getComparatorOutput() != currentOut)
				level.updateNeighbourForOutputSignal(getBlockPos(), getBlockState().getBlock());
		} else {
            queuedItems.add(item);
        }

		return true;
	}

	public boolean passIn(ItemStack stack, Direction face) {
		return passIn(stack, face, null, level.random.nextLong(), 0);
	}

	protected void passOut(PipeItem item) {
		boolean did = false;

		BlockPos targetPos = getBlockPos().relative(item.outgoingFace);
		if(level.getBlockState(targetPos).getBlock() instanceof WorldlyContainerHolder) {
			ItemStack result = MiscUtil.putIntoInv(item.stack, level, targetPos, null, item.outgoingFace.getOpposite(), false, false);
			if(result.getCount() != item.stack.getCount()) {
				did = true;
				if(!result.isEmpty())
					bounceBack(item, result);
			}
		} else {
			BlockEntity tile = level.getBlockEntity(targetPos);
			if(tile != null) {
				if(tile instanceof PipeBlockEntity pipe)
					did = pipe.passIn(item.stack, item.outgoingFace.getOpposite(), null, item.rngSeed, item.timeInWorld);
				else if(!level.isClientSide) {
					ItemStack result = MiscUtil.putIntoInv(item.stack, level, targetPos, tile, item.outgoingFace.getOpposite(), false, false);
					if(result.getCount() != item.stack.getCount()) {
						did = true;
						if(!result.isEmpty())
							bounceBack(item, result);
					}
				}
			}
		}

		if(!did)
			bounceBack(item, null);
	}

	private void bounceBack(PipeItem item, ItemStack stack) {
		if(!level.isClientSide) {
            passIn(stack == null ? item.stack : stack, item.outgoingFace, item.incomingFace, item.rngSeed, item.timeInWorld);
        }
	}

	public void dropItem(ItemStack stack) {
		dropItem(stack, null, false);
	}

	public void dropItem(ItemStack stack, Direction facing, boolean playSound) {
		if(!level.isClientSide) {
			double posX = worldPosition.getX() + 0.5;
			double posY = worldPosition.getY() + 0.25;
			double posZ = worldPosition.getZ() + 0.5;

			if(facing != null) {
				double shift = allowsFullConnection(ConnectionType.OPENING) ? 0.7 : 0.4;
				posX -= facing.getStepX() * shift;
				posY -= facing.getStepY() * (shift + 0.15);
				posZ -= facing.getStepZ() * shift;
			}

			boolean shootOut = isPipeEnabled();

			float pitch = 1f;
			if(!shootOut)
				pitch = 0.025f;

			if(playSound) {
				if(PipesModule.doPipesWhoosh) {
					if(isTheGoodDay())
						level.playSound(null, posX, posY, posZ, QuarkSounds.BLOCK_PIPE_SHOOT_LENNY, SoundSource.BLOCKS, 1f, pitch);
					else
						level.playSound(null, posX, posY, posZ, QuarkSounds.BLOCK_PIPE_SHOOT, SoundSource.BLOCKS, 1f, pitch);
				}

				if(PipesModule.emitVibrations)
					getLevel().gameEvent(GameEvent.PROJECTILE_SHOOT, getBlockPos(), Context.of(getBlockState()));
			}

			ItemEntity entity = new ItemEntity(level, posX, posY, posZ, stack);
			entity.setDefaultPickUpDelay();

			double velocityMod = 0.5;
			if(!shootOut)
				velocityMod = 0.125;

			if(facing != null) {
				double mx = -facing.getStepX() * velocityMod;
				double my = -facing.getStepY() * velocityMod;
				double mz = -facing.getStepZ() * velocityMod;
				entity.setDeltaMovement(mx, my, mz);
			}

			level.addFreshEntity(entity);
		}
	}

	public void dropAllItems() {
		for(PipeItem item : pipeItems)
			dropItem(item.stack);
		pipeItems.clear();
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void readSharedNBT(CompoundTag tag, HolderLookup.Provider provider) {
		skipSync = true;
		super.readSharedNBT(tag, provider);
		skipSync = false;

		ListTag pipeItemList = tag.getList(TAG_PIPE_ITEMS, tag.getId());
		pipeItems.clear();
		pipeItemList.forEach(listCmp -> {
			PipeItem item = PipeItem.readFromNBT((CompoundTag) listCmp, provider);
			pipeItems.add(item);
		});

		if(tag.contains(TAG_CONNECTIONS)) {
			var c = tag.getByteArray(TAG_CONNECTIONS);
			for(int i = 0; i < c.length; i++) {
				connectionsCache[i] = ConnectionType.values()[c[i]];
			}
		}
	}

	@Override
	public void writeSharedNBT(CompoundTag tag, HolderLookup.Provider provider) {
		super.writeSharedNBT(tag, provider);

		ListTag pipeItemList = new ListTag();
		for(PipeItem item : pipeItems) {
			CompoundTag listCmp = new CompoundTag();
			item.writeToNBT(listCmp, provider);
			pipeItemList.add(listCmp);
		}
		tag.put(TAG_PIPE_ITEMS, pipeItemList);

		for(int i = 0; i < connectionsCache.length; i++) {
			if(connectionsCache[i] == null) {
				connectionsCache[i] = ConnectionType.NONE;
				this.convert = true;
			}
		}
		tag.putByteArray(TAG_CONNECTIONS, (Arrays.stream(connectionsCache).map(c -> (byte) c.ordinal()).toList()));
	}

	protected boolean canFit(ItemStack stack, BlockPos pos, Direction face) {
		if(level.getBlockState(pos).getBlock() instanceof WorldlyContainerHolder)
			return MiscUtil.canPutIntoInv(stack, level, pos, null, face, false);

		BlockEntity tile = level.getBlockEntity(pos);
		if(tile == null)
			return false;

		if(tile instanceof PipeBlockEntity pipe)
			return pipe.isPipeEnabled();
		else
			return MiscUtil.canPutIntoInv(stack, level, pos, tile, face, false);
	}

	protected boolean isPipeEnabled() {
		BlockState state = level.getBlockState(worldPosition);
		return state.is(PipesModule.pipesTag) && !level.hasNeighborSignal(worldPosition);
	}

	@Override
	public boolean canPlaceItemThroughFace(int index, @NotNull ItemStack itemStackIn, @NotNull Direction direction) {
		return direction != null && index == direction.ordinal() && isPipeEnabled();
	}

	@Override
	public void setItem(int i, @NotNull ItemStack itemstack) {
		if(!itemstack.isEmpty()) {
			Direction side = Direction.values()[i];
			passIn(itemstack, side);

			if(!level.isClientSide && !skipSync)
				sync();
		}
	}

	@Override
	public void startOpen(@NotNull Player player) {

	}

	@Override
	public void stopOpen(@NotNull Player player) {

	}

	@Override
	public void inventoryChanged(int i) {

	}

	@Override
	public int getContainerSize() {
		return 6;
	}

	@Override
	protected boolean needsToSyncInventory() {
		return true;
	}

	@Override
	public void sync() {
		MiscUtil.syncTE(this);
	}

	public void refreshVisualConnections() {
		for (Direction direction : Direction.values())
			updateConnection(direction);
	}

	public ConnectionType updateConnection(Direction facing) {
		var c = computeConnectionTo(level, worldPosition, facing);
		connectionsCache[facing.ordinal()] = c;
		return c;
	}

	public ConnectionType getConnectionTo(Direction side) {
		var c = connectionsCache[side.ordinal()];
		if(c == null) {
			//backwards compat
			c = updateConnection(side);
		}
		return c;
	}

	public static ConnectionType computeConnectionTo(BlockGetter world, BlockPos pos, Direction face) {
		return computeConnectionTo(world, pos, face, false);
	}

	private static ConnectionType computeConnectionTo(BlockGetter world, BlockPos pos, Direction face, boolean recursed) {
		BlockPos truePos = pos.relative(face);

		BlockState state = world.getBlockState(truePos);
		if(state.getBlock() instanceof WorldlyContainerHolder)
			return ConnectionType.TERMINAL;

		BlockEntity tile = world.getBlockEntity(truePos);

		if(tile != null) {
			if (tile instanceof PipeBlockEntity)
				return ConnectionType.PIPE;
			else if (tile instanceof Container)
				return canHaveOffset(state, pos, world, face) ? ConnectionType.TERMINAL_OFFSET : ConnectionType.TERMINAL;
		}

		checkSides: if(!recursed) {
			ConnectionType other = computeConnectionTo(world, pos, face.getOpposite(), true);
			if(other.isSolid) {
				for(Direction d : Direction.values())
					if(d.getAxis() != face.getAxis()) {
						other = computeConnectionTo(world, pos, d, true);
						if(other.isSolid)
							break checkSides;
					}

				return ConnectionType.OPENING;
			}
		}

		return ConnectionType.NONE;
	}

	private static boolean canHaveOffset(BlockState state, BlockPos pos, BlockGetter world, Direction dir) {
		var shape = state.getCollisionShape( world,pos);
		if(dir.getAxisDirection() == Direction.AxisDirection.NEGATIVE){
			return shape.max(dir.getAxis())<1;
		}else return shape.min(dir.getAxis())>0;
	}

	public static class PipeItem {

		private static final String TAG_TICKS = "ticksInPipe";
		private static final String TAG_INCOMING = "incomingFace";
		private static final String TAG_OUTGOING = "outgoingFace";
		private static final String TAG_BACKLOGGED = "backloggedFace";
		private static final String TAG_RNG_SEED = "rngSeed";
		private static final String TAG_TIME_IN_WORLD = "timeInWorld";

		private static final List<Direction> HORIZONTAL_SIDES_LIST = Arrays.asList(MiscUtil.HORIZONTALS);

		public final ItemStack stack;
		public int ticksInPipe;
		public final Direction incomingFace;
		public Direction outgoingFace;
		public Direction backloggedFace;
		public long rngSeed;
		public int timeInWorld = 0;
		public boolean valid = true;

		protected long lastTickUpdated = 0;

		public PipeItem(ItemStack stack, Direction face, long rngSeed) {
			this.stack = stack;
			ticksInPipe = 0;
			incomingFace = outgoingFace = face;
			this.rngSeed = rngSeed;
		}

		protected boolean tick(PipeBlockEntity pipe) {
			long gameTime = pipe.level.getGameTime();
			if(lastTickUpdated != gameTime) {
				lastTickUpdated = gameTime;

				ticksInPipe++;
				timeInWorld++;

				if(ticksInPipe == PipesModule.effectivePipeSpeed / 2 - 1) {
					outgoingFace = getTargetFace(pipe);
				}

				if(outgoingFace == null) {
					valid = false;
					return true;
				}
			}

			return ticksInPipe >= PipesModule.effectivePipeSpeed;
		}

		protected Direction getTargetFace(PipeBlockEntity pipe) {
			BlockPos pipePos = pipe.getBlockPos();
			if(incomingFace != Direction.DOWN && backloggedFace != Direction.DOWN && pipe.canFit(stack, pipePos.relative(Direction.DOWN), Direction.UP))
				return Direction.DOWN;

			Direction incomingOpposite = incomingFace; // init as same so it doesn't break in the remove later
			if(incomingFace.getAxis() != Axis.Y) {
				incomingOpposite = incomingFace.getOpposite();
				if(incomingOpposite != backloggedFace && pipe.canFit(stack, pipePos.relative(incomingOpposite), incomingFace))
					return incomingOpposite;
			}

			List<Direction> sides = new ArrayList<>(HORIZONTAL_SIDES_LIST);
			sides.remove(incomingFace);
			sides.remove(incomingOpposite);

			Random rng = new Random(rngSeed);
			rngSeed = rng.nextLong();
			Collections.shuffle(sides, rng);
			for(Direction side : sides) {
				if(side != backloggedFace && pipe.canFit(stack, pipePos.relative(side), side.getOpposite()))
					return side;
			}

			if(incomingFace != Direction.UP && backloggedFace != Direction.UP && pipe.canFit(stack, pipePos.relative(Direction.UP), Direction.DOWN))
				return Direction.UP;

			if(backloggedFace != null)
				return backloggedFace;

			return null;
		}

		public float getTimeFract(float partial) {
			return (ticksInPipe + partial) / PipesModule.effectivePipeSpeed;
		}

		public void writeToNBT(CompoundTag cmp, HolderLookup.Provider provider) {
			if (!stack.isEmpty()) cmp.merge((CompoundTag) stack.save(provider));
			stack.save(provider, cmp);
			cmp.putInt(TAG_TICKS, ticksInPipe);
			cmp.putInt(TAG_INCOMING, incomingFace.ordinal());
			cmp.putInt(TAG_OUTGOING, outgoingFace.ordinal());
			cmp.putInt(TAG_BACKLOGGED, backloggedFace != null ? backloggedFace.ordinal() : -1);
			cmp.putLong(TAG_RNG_SEED, rngSeed);
			cmp.putInt(TAG_TIME_IN_WORLD, timeInWorld);
		}

		public static PipeItem readFromNBT(CompoundTag cmp, HolderLookup.Provider provider) {
			ItemStack stack = ItemStack.parseOptional(provider, cmp);
			Direction inFace = Direction.values()[cmp.getInt(TAG_INCOMING)];
			long rngSeed = cmp.getLong(TAG_RNG_SEED);

			PipeItem item = new PipeItem(stack, inFace, rngSeed);
			item.ticksInPipe = cmp.getInt(TAG_TICKS);
			item.outgoingFace = Direction.values()[cmp.getInt(TAG_OUTGOING)];
			item.timeInWorld = cmp.getInt(TAG_TIME_IN_WORLD);

			int backloggedId = cmp.getInt(TAG_BACKLOGGED);
			item.backloggedFace = backloggedId == -1 ? null : Direction.values()[backloggedId];

			return item;
		}

	}

	public enum ConnectionType {
		NONE(false, false, false, 0),
		PIPE(true, true, false, 0),
		OPENING(false, true, true, -0.125, 0.1875),
		TERMINAL(true, true, true, 0.125),
		TERMINAL_OFFSET(true, true, true, 0.1875);

		ConnectionType(boolean isSolid, boolean allowsItems, boolean isFlared, double flareShift, double fullFlareShift) {
			this.isSolid = isSolid;
			this.allowsItems = allowsItems;
			this.isFlared = isFlared;
			this.flareShift = flareShift;
			this.fullFlareShift = fullFlareShift;
		}

		ConnectionType(boolean isSolid, boolean allowsItems, boolean isFlared, double flareShift) {
			this(isSolid, allowsItems, isFlared, flareShift, flareShift);
		}

		public double getFlareShift(PipeBlockEntity pipe) {
			return pipe.allowsFullConnection(this) ? fullFlareShift : flareShift;
		}

		public final boolean isSolid, allowsItems, isFlared;
		private final double flareShift, fullFlareShift;
	}
}

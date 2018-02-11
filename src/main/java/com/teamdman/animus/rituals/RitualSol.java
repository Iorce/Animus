package com.teamdman.animus.rituals;

import WayofTime.bloodmagic.api.ritual.*;
import WayofTime.bloodmagic.api.saving.SoulNetwork;
import WayofTime.bloodmagic.api.util.helper.NetworkHelper;
import WayofTime.bloodmagic.core.RegistrarBloodMagicBlocks;
import WayofTime.bloodmagic.core.RegistrarBloodMagicItems;
import com.teamdman.animus.Animus;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Stream;


/**
 * Created by TeamDman on 2015-05-28.
 */
public class RitualSol extends Ritual {
	public static final String EFFECT_RANGE = "effect";
	public static final String CHEST_RANGE = "chest";


	public RitualSol() {
		super("ritualSol", 0, 1000, "ritual." + Animus.MODID + ".sol");

		addBlockRange(EFFECT_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-32, -32, -32), 65));
		addBlockRange(CHEST_RANGE, new AreaDescriptor.Rectangle(new BlockPos(0, 1, 0), 1));

		setMaximumVolumeAndDistanceOfRange(EFFECT_RANGE, 0, 128, 128);
		setMaximumVolumeAndDistanceOfRange(CHEST_RANGE, 1, 3, 3);
	}


	@Override
	public void performRitual(IMasterRitualStone masterRitualStone) {
		World world = masterRitualStone.getWorldObj();
		SoulNetwork network = NetworkHelper.getSoulNetwork(masterRitualStone.getOwner());
		BlockPos masterPos = masterRitualStone.getBlockPos();
		AreaDescriptor chestRange = getBlockRange(CHEST_RANGE);
		TileEntityChest tileInventory = (TileEntityChest) world.getTileEntity(chestRange.getContainedPositions(masterPos).get(0));
		if (tileInventory == null)
			return;
		IItemHandler handler = tileInventory.getSingleChestHandler();

		if (!masterRitualStone.getWorldObj().isRemote) {
			Optional<Integer> slot = Stream.iterate(0, n -> ++n)
					.limit(handler.getSlots() - 1)
					.filter((e) -> handler.getStackInSlot(e) != null)
					.filter((e) -> this.isOkayToUse(handler.getStackInSlot(e)))
					.findAny();
			if (!slot.isPresent())
				return;
			Optional<BlockPos> toPlace = getBlockRange(EFFECT_RANGE).getContainedPositions(masterRitualStone.getBlockPos()).stream()
					.filter(world::isAirBlock)
					.filter((e) -> world.getLightFromNeighbors(e) < 8)
					.filter((e) -> world.isSideSolid(e.down(1), EnumFacing.UP))
					.findFirst();

			if (!toPlace.isPresent())
				return;
			IBlockState state = getStateToUse(handler.getStackInSlot(slot.get()));
			world.setBlockState(toPlace.get(), state);
			if (state.getBlock() != RegistrarBloodMagicBlocks.BLOOD_LIGHT) {
				handler.extractItem(slot.get(), 1, false);
			}
			network.syphon(getRefreshCost());
		}
	}

	private boolean isOkayToUse(ItemStack in) {
		return in != null && (in.getItem() == RegistrarBloodMagicItems.SIGIL_BLOOD_LIGHT || Block.getBlockFromItem(in.getItem()) != null);
	}

	@SuppressWarnings("deprecation")
	private IBlockState getStateToUse(ItemStack in) {
		if (in.getItem() == RegistrarBloodMagicItems.SIGIL_BLOOD_LIGHT) {
			return RegistrarBloodMagicBlocks.BLOOD_LIGHT.getDefaultState();
		} else {
			return Block.getBlockFromItem(in.getItem()).getStateFromMeta(in.getItemDamage());
		}
	}

	@Override
	public int getRefreshCost() {
		return 1;
	}

	@Override
	public int getRefreshTime() {
		return 5;
	}

	@Override
	public ArrayList<RitualComponent> getComponents() {
		ArrayList<RitualComponent> components = new ArrayList<RitualComponent>();
		this.addParallelRunes(components, 0, -1, EnumRuneType.AIR);
		this.addParallelRunes(components, 0, -2, EnumRuneType.AIR);
		this.addParallelRunes(components, 0, -3, EnumRuneType.AIR);
		this.addParallelRunes(components, 1, -3, EnumRuneType.AIR);

		return components;
	}

	@Override
	public Ritual getNewCopy() {
		return new RitualSol();
	}

}
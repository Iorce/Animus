package com.teamdman.animus.items.sigils;

import WayofTime.bloodmagic.api.impl.ItemSigil;
import WayofTime.bloodmagic.client.IVariantProvider;
import com.teamdman.animus.blocks.BlockAntimatter;
import com.teamdman.animus.registry.AnimusBlocks;
import com.teamdman.animus.tiles.TileAntimatter;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TeamDman on 2015-06-09.
 */
public class ItemSigilConsumption extends ItemSigil implements IVariantProvider {
	public ItemSigilConsumption() {
		super(200);
	}

	@SuppressWarnings("deprecation")
	@Override
	public EnumActionResult onItemUse(EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (worldIn.getTileEntity(pos) != null || worldIn.getBlockState(pos).getBlock().getBlockHardness(null, null, null) == -1.0F)
			return EnumActionResult.SUCCESS;
		Block seeking = worldIn.getBlockState(pos).getBlock();
		worldIn.setBlockState(pos, AnimusBlocks.blockAntimatter.getDefaultState().withProperty(BlockAntimatter.DECAYING, false));
		((TileAntimatter) worldIn.getTileEntity(pos)).seeking = seeking;
		((TileAntimatter) worldIn.getTileEntity(pos)).player = playerIn;

		worldIn.scheduleBlockUpdate(pos, AnimusBlocks.blockAntimatter, 5, 0);
		return EnumActionResult.SUCCESS;
	}

	@Override
	public List<Pair<Integer, String>> getVariants() {
		List<Pair<Integer, String>> ret = new ArrayList<Pair<Integer, String>>();
		ret.add(new ImmutablePair<Integer, String>(0, "type=normal"));
		return ret;
	}
}
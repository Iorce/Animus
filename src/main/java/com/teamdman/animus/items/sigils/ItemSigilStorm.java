package com.teamdman.animus.items.sigils;

import WayofTime.bloodmagic.api.impl.ItemSigil;
import WayofTime.bloodmagic.api.ritual.AreaDescriptor;
import WayofTime.bloodmagic.api.util.helper.NetworkHelper;
import WayofTime.bloodmagic.client.IVariantProvider;
import com.teamdman.animus.AnimusConfig;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ItemSigilStorm extends ItemSigil implements IVariantProvider {
    protected final Map<String, AreaDescriptor> modableRangeMap = new HashMap<String, AreaDescriptor>();
	public static final String EFFECT_RANGE = "effect";
	public ItemSigilStorm() {
		super(AnimusConfig.stormConsumption);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		Random rand = new Random();
		BlockPos pos = null;
		int damage;
		
		
		RayTraceResult result = ItemSigilStorm.raytraceFromEntity(world, player, true, 64);
		if (result != null) {
			
			if (result.typeOfHit == RayTraceResult.Type.BLOCK)
				pos = result.getBlockPos();
			if (result.typeOfHit == RayTraceResult.Type.ENTITY)
				pos = result.entityHit.getPosition();
				
			if (pos == null){
				return new ActionResult<>(EnumActionResult.FAIL, stack);
			}
			
				world.addWeatherEffect(new EntityLightningBolt(world, pos.getX(), pos.getY() + .5, pos.getZ(), false));

				IBlockState state = world.getBlockState(pos);
				if (state.getBlock() == Blocks.WATER && !world.isRemote) {
					EntityItem fish = new EntityItem(world, pos.getX(), pos.getY() - rand.nextInt(2), pos.getZ(), new ItemStack(Items.FISH, 1 + rand.nextInt(2)));
					fish.setVelocity(rand.nextDouble() * .25, -.25, rand.nextDouble() * .25);
					fish.setEntityInvulnerable(true); 
					world.spawnEntity(fish);
				}
			
			
			if (world.isRaining()){
				
				
				addBlockRange(EFFECT_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-1,-1,-1), 4));
				AreaDescriptor damageRange = getBlockRange(EFFECT_RANGE);
				AxisAlignedBB range = damageRange.getAABB(pos);
				List<EntityLivingBase> list = world.getEntitiesWithinAABB(EntityLivingBase.class, range);
				DamageSource storm = new DamageSource("animus.storm").setDamageBypassesArmor().setDamageIsAbsolute();
				for (EntityLivingBase livingEntity : list) {
					if (livingEntity == player)
						continue;
					damage = Math.max(6,rand.nextInt(15));
					livingEntity.attackEntityFrom(storm, damage);
				}
			}
			
			NetworkHelper.getSoulNetwork(player).syphonAndDamage(player, getLpUsed());

		}
		return new ActionResult<>(EnumActionResult.SUCCESS, stack);
	}

	
	/**
	 * @author mDiyo
	 */
	public static RayTraceResult raytraceFromEntity(World world, Entity player, boolean useLiquids, double range) {
		float f = 1.0F;
		float f1 = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * f;
		float f2 = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * f;
		double d0 = player.prevPosX + (player.posX - player.prevPosX) * f;
		double d1 = player.prevPosY + (player.posY - player.prevPosY) * f;
		if (player instanceof EntityPlayer)
			d1 += ((EntityPlayer) player).eyeHeight;
		double d2 = player.prevPosZ + (player.posZ - player.prevPosZ) * f;
		Vec3d vec3 = new Vec3d(d0, d1, d2);
		float f3 = MathHelper.cos(-f2 * 0.017453292F - (float) Math.PI);
		float f4 = MathHelper.sin(-f2 * 0.017453292F - (float) Math.PI);
		float f5 = -MathHelper.cos(-f1 * 0.017453292F);
		float f6 = MathHelper.sin(-f1 * 0.017453292F);
		float f7 = f4 * f5;
		float f8 = f3 * f5;
		double d3 = range;
		Vec3d vec31 = vec3.addVector(f7 * d3, f6 * d3, f8 * d3);
		return world.rayTraceBlocks(vec3, vec31, useLiquids);
	}

	@Override
	public List<Pair<Integer, String>> getVariants() {
		List<Pair<Integer, String>> ret = new ArrayList<Pair<Integer, String>>();
		ret.add(new ImmutablePair<Integer, String>(0, "type=normal"));
		return ret;
	}

	public void addBlockRange(String range, AreaDescriptor defaultRange)
    {
        modableRangeMap.put(range, defaultRange);
    }

    public AreaDescriptor getBlockRange(String range)
    {
        if (modableRangeMap.containsKey(range))
        {
            return modableRangeMap.get(range);
        }

        return null;
    }
	
}

package com.teamdman.animus.rituals;

import WayofTime.bloodmagic.core.data.SoulNetwork;
import WayofTime.bloodmagic.core.data.SoulTicket;
import WayofTime.bloodmagic.demonAura.WorldDemonWillHandler;
import WayofTime.bloodmagic.ritual.*;
import WayofTime.bloodmagic.soul.EnumDemonWillType;
import WayofTime.bloodmagic.util.helper.NetworkHelper;
import com.teamdman.animus.Constants;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

import java.util.List;
import java.util.function.Consumer;

@RitualRegister(Constants.Rituals.STEADFAST)
public class RitualSteadfastHeart extends Ritual {
	public static final String EFFECT_RANGE = "effect";
	public final        int    maxWill      = 100;
	public              double willBuffer   = 0;

	public RitualSteadfastHeart() {

		super(Constants.Rituals.STEADFAST, 0, 20000, "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.STEADFAST);
		addBlockRange(EFFECT_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-16, -16, -16), 32));
		setMaximumVolumeAndDistanceOfRange(EFFECT_RANGE, 0, 15, 15);

	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		willBuffer = tag.getDouble("willBuffer");

	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		tag.setDouble("willBuffer", willBuffer);
	}

	@Override
	public void performRitual(IMasterRitualStone masterRitualStone) {
		SoulNetwork network = NetworkHelper.getSoulNetwork(masterRitualStone.getOwner());
		if (network == null) {
			return;
		}


		World world = masterRitualStone.getWorldObj();

		EnumDemonWillType type          = EnumDemonWillType.STEADFAST;
		BlockPos          pos           = masterRitualStone.getBlockPos();
		double            currentAmount = WorldDemonWillHandler.getCurrentWill(world, pos, type);


		AreaDescriptor damageRange = getBlockRange(EFFECT_RANGE);
		AxisAlignedBB  range       = damageRange.getAABB(pos);

		List<EntityLivingBase> list = world.getEntitiesWithinAABB(EntityLivingBase.class, range);

		int    entityCount = 0;
		Potion absPotion   = MobEffects.ABSORPTION;


		for (EntityLivingBase livingEntity : list) {
			if (!(livingEntity instanceof EntityPlayer) || livingEntity instanceof FakePlayer)
				continue;

			entityCount++;
			PotionEffect abs = livingEntity.getActivePotionEffect(absPotion);
			//			if (abs == null)
			//				livingEntity.addPotionEffect(new PotionEffect(absPotion, 800, 0, true, false));
			//			else
			//				livingEntity.addPotionEffect(new PotionEffect(absPotion, Math.min(((abs.getDuration() + 800) * 2), 36000), Math.min(1 + ((5 * abs.getDuration() + 60) / 36000), 4), true, false));
			int dur = 0;
			if (abs != null) {
				dur = abs.getDuration();
				livingEntity.removePotionEffect(abs.getPotion());
			}
			int newdur = Math.min(((dur + 800) * 2), 30000);
			int pow    = Math.min((5 * (1 + (newdur + 60)) / 36000), 4);
			livingEntity.addPotionEffect(new PotionEffect(absPotion, newdur, pow, true, false));


		}
		network.syphon(new SoulTicket(new TextComponentTranslation(Constants.Localizations.Text.TICKET_STEADFAST), getRefreshCost() * entityCount));
		double drainAmount = 2 * Math.min((maxWill - currentAmount) + 1, Math.min(entityCount / 2, 10));


		double filled = WorldDemonWillHandler.fillWillToMaximum(world, pos, type, drainAmount, maxWill, false);
		if (filled > 0)
			WorldDemonWillHandler.fillWillToMaximum(world, pos, type, filled, maxWill, true);


	}

	@Override
	public int getRefreshCost() {
		return 100;
	}

	@Override
	public int getRefreshTime() {
		return 600;
	}

	@Override
	public void gatherComponents(Consumer<RitualComponent> components) {
		components.accept(new RitualComponent(new BlockPos(1, 0, 1), EnumRuneType.EARTH));
		components.accept(new RitualComponent(new BlockPos(-1, 0, 1), EnumRuneType.WATER));
		components.accept(new RitualComponent(new BlockPos(1, 0, -1), EnumRuneType.EARTH));
		components.accept(new RitualComponent(new BlockPos(-1, 0, -1), EnumRuneType.WATER));
		components.accept(new RitualComponent(new BlockPos(0, -1, 0), EnumRuneType.AIR));
		components.accept(new RitualComponent(new BlockPos(2, -1, 2), EnumRuneType.EARTH));
		components.accept(new RitualComponent(new BlockPos(2, -1, -2), EnumRuneType.EARTH));
		components.accept(new RitualComponent(new BlockPos(-2, -1, 2), EnumRuneType.WATER));
		components.accept(new RitualComponent(new BlockPos(-2, -1, -2), EnumRuneType.WATER));
	}

	@Override
	public Ritual getNewCopy() {
		return new RitualSteadfastHeart();
	}


}

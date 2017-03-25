package com.teamdman.animus.handlers;

import java.util.Random;

import com.teamdman.animus.Animus;
import com.teamdman.animus.AnimusConfig;
import com.teamdman.animus.entity.EntityVengefulSpirit;
import com.teamdman.animus.registry.AnimusItems;
import com.teamdman.animus.registry.AnimusPotions;
import com.teamdman.animus.slots.SlotNoPickup;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EventHandler {
	
	@SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = false)
    public void onPlaySoundEvent(PlaySoundEvent e) {
		if (AnimusConfig.muteWither && (e.getName().equals("entity.wither.spawn"))) {
			e.setResultSound(null);
		}
		if (AnimusConfig.muteDragon && (e.getName().equals("entity.enderdragon.death"))) {
			e.setResultSound(null);
		}
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent e) {
		if (e.getModID().equals(Animus.MODID)) {
			AnimusConfig.syncConfig();
		}
	}
	
	@SubscribeEvent
    public void onAttacked(LivingAttackEvent event) {
		EntityLivingBase el;
		el = event.getEntityLiving();
        if(!(el instanceof EntityPlayer))
            return;

        PotionEffect vPotion = (el.getActivePotionEffect(AnimusPotions.VENGEFULSPIRITS));
        if (vPotion.equals(null))
        	return;
        
        int count = vPotion.getAmplifier();
        Random rand = new Random();
        
		EntityVengefulSpirit spirit;
		World ew = el.getEntityWorld();
        
        for (int i = 0; i < count; i++){
  
        	spirit = new EntityVengefulSpirit(ew);
        	double posX = el.posX+0.5+rand.nextInt(3);
        	double posY = el.posY;
        	double posZ = el.posZ+0.5+rand.nextInt(3);
        	spirit.setRevengeTarget(el.getAttackingEntity());
        	spirit.setPosition(posX, posY, posZ);
        	ew.spawnEntity(spirit);
        	
        }
        
	}

	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent eventArgs) {
		Container open = eventArgs.player.openContainer;
		if (open == null)
			return;
		int frags = 0;
		for (int i = 0; i < open.inventorySlots.size(); i++) {
			Slot slot = (Slot) open.inventorySlots.get(i);
			if (slot.getHasStack() && slot.getStack().getItem() == AnimusItems.fragmentHealing) {
				frags++;
				if (!eventArgs.player.capabilities.isCreativeMode && slot.getClass() == Slot.class) {
					open.inventorySlots.set(i, new SlotNoPickup(slot.inventory, slot.getSlotIndex(), slot.xPos, slot.yPos));
				}
			}
		}
		if (eventArgs.player.world.getWorldTime() % 20 == 0 && frags >= 9 && !eventArgs.player.world.isRemote) {
			eventArgs.player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 20, frags / 9 - 1));
			if (frags >= 35 && eventArgs.player.world.getWorldTime() % 200 == 0)
				eventArgs.player.addPotionEffect(new PotionEffect(MobEffects.ABSORPTION, 200, 4));
		}
	}
}

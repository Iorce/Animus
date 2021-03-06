package com.teamdman.animus.rituals;

import WayofTime.bloodmagic.core.data.SoulNetwork;
import WayofTime.bloodmagic.core.data.SoulTicket;
import WayofTime.bloodmagic.ritual.*;
import WayofTime.bloodmagic.util.helper.NetworkHelper;
import com.teamdman.animus.Constants;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.ShapedOreRecipe;

import java.util.*;
import java.util.function.Consumer;

/**
 * Created by TeamDman on 2015-05-28.
 */
public class RitualEntropy extends Ritual {
	public static final String                 CHEST_RANGE = "chest";
	final               HashMap<Item, Integer> indexed     = new HashMap<>();

	public RitualEntropy() {
		super(Constants.Rituals.ENTROPY, 0, 1000, "ritual." + Constants.Mod.MODID + "." + Constants.Rituals.ENTROPY);

		addBlockRange(CHEST_RANGE, new AreaDescriptor.Rectangle(new BlockPos(0, 1, 0), 1));
		setMaximumVolumeAndDistanceOfRange(CHEST_RANGE, 1, 3, 3);
	}

	@Override
	public void performRitual(IMasterRitualStone masterRitualStone) {
		World          world          = masterRitualStone.getWorldObj();
		SoulNetwork    network        = NetworkHelper.getSoulNetwork(masterRitualStone.getOwner());
		int            currentEssence = network.getCurrentEssence();
		BlockPos       masterPos      = masterRitualStone.getBlockPos();
		AreaDescriptor chestRange     = getBlockRange(CHEST_RANGE);
		TileEntity     tileInventory  = world.getTileEntity(chestRange.getContainedPositions(masterPos).get(0));


		if (!masterRitualStone.getWorldObj().isRemote && tileInventory != null && tileInventory.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
			if (currentEssence < getRefreshCost()) {
				network.causeNausea();
				return;
			}
			for (int slot = 0; slot < ((IInventory) tileInventory).getSizeInventory(); slot++) {
				ItemStack stack = ((IInventory) tileInventory).getStackInSlot(slot);
				if (stack.isEmpty())
					continue;
				if (!stack.isItemEqual(new ItemStack(Blocks.COBBLESTONE))) {
					int cobble = getCobbleValue(new ArrayList<>(), stack, 0);
					if (cobble > 0) {
						((IInventory) tileInventory).decrStackSize(slot, 1);
						while (cobble > 0) {
							ItemHandlerHelper.insertItemStacked(tileInventory.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null), new ItemStack(Blocks.COBBLESTONE, cobble > 64 ? 64 : cobble), false);
							cobble -= cobble > 64 ? 64 : cobble;
						}
					}
				}
			}
			network.syphon(new SoulTicket(new TextComponentTranslation(Constants.Localizations.Text.TICKET_ENTROPY), getRefreshCost()));

		}
	}

	@Override
	public int getRefreshCost() {
		return 1;
	}

	@Override
	public int getRefreshTime() {
		return 1;
	}

	@Override
	public void gatherComponents(Consumer<RitualComponent> components) {
		for (int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				if (x == 0 && y == 0)
					continue;
				components.accept(new RitualComponent(new BlockPos(x, 0, y), EnumRuneType.EARTH));
			}
		}
		components.accept(new RitualComponent(new BlockPos(-2, 0, -2), EnumRuneType.EARTH));
		components.accept(new RitualComponent(new BlockPos(-2, 0, 2), EnumRuneType.EARTH));
		components.accept(new RitualComponent(new BlockPos(2, 0, -2), EnumRuneType.EARTH));
		components.accept(new RitualComponent(new BlockPos(2, 0, 2), EnumRuneType.EARTH));
	}

	@Override
	public Ritual getNewCopy() {
		return new RitualEntropy();
	}

	@SuppressWarnings("rawtypes")
	public int getCobbleValue(List<Item> fetchList, ItemStack input, int layer) {
		//		System.out.printf("%s requested on layer %d\n", input.getDisplayName(), layer);
		if (indexed.get(input.getItem()) != null)
			return indexed.get(input.getItem());
		if (fetchList.contains(input.getItem()))
			return 1;
		if (layer > 8)
			return 0;
		layer++;
		fetchList.add(input.getItem());
		int rtn = 1;

		for (IRecipe recipe : ForgeRegistries.RECIPES.getValuesCollection()) {
			if (!recipe.getRecipeOutput().isEmpty() && recipe.getRecipeOutput().isItemEqual(input)) {
				rtn += recipe.getIngredients().size();
				List components;
				if (recipe instanceof ShapelessRecipes) {
					components = ((ShapelessRecipes) recipe).recipeItems;
				} else if (recipe instanceof ShapedRecipes) {
					components = Collections.singletonList(((ShapedRecipes) recipe).recipeItems);
				} else if (recipe instanceof ShapedOreRecipe) {
					components = Collections.singletonList((recipe).getIngredients());
				} else {
					continue;
				}
				if (components == null) {
					continue;
				}
				for (Object component : components) {
					if (component instanceof ItemStack) {
						rtn += getCobbleValue(fetchList, (ItemStack) component, layer);
					} else if (component instanceof Collection) {
						if (((Collection) component).contains(new ItemStack(Blocks.COBBLESTONE))) {
							rtn += 1;
						} else {
							Collection recipeItemCollection = ((Collection) component);
							int        value                = -1;
							for (Object option : recipeItemCollection) {
								if (option instanceof ItemStack) {
									int v = getCobbleValue(fetchList, (ItemStack) option, layer);
									value = (value == -1 || v < value && v > 1) ? v : value;//value < v ? v : value;
								}
							}
							rtn += (value == -1 ? 1 : value);
						}
					}
				}
				break;
			}
		}
		System.out.printf("Returning %d for item %s on layer %d\n", rtn, input.getDisplayName(), layer);
		indexed.put(input.getItem(), rtn);
		return rtn;
	}

}
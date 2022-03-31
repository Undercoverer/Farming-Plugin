package com.undercover.farming;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bukkit.GameMode.CREATIVE;
import static org.bukkit.Material.*;

/**
 * The type Plant command exec.
 */
public class PlantCommandExec implements CommandExecutor {
    /**
     * The Plantable surfaces.
     */
    public final static Map<Material, List<Material>> PLANTABLE_SURFACES;
    /**
     * The Item block plant map.
     */
    public final static Map<Material, Material> ITEM_BLOCK_PLANT_MAP;

    static {
        PLANTABLE_SURFACES = new HashMap<>();
        PLANTABLE_SURFACES.put(BEETROOTS, List.of(FARMLAND));
        PLANTABLE_SURFACES.put(CARROTS, List.of(FARMLAND));
        PLANTABLE_SURFACES.put(MELON_STEM, List.of(FARMLAND));
        PLANTABLE_SURFACES.put(NETHER_WART, List.of(SOUL_SAND));
        PLANTABLE_SURFACES.put(POTATOES, List.of(FARMLAND));
        PLANTABLE_SURFACES.put(PUMPKIN_STEM, List.of(FARMLAND));
        PLANTABLE_SURFACES.put(WHEAT, List.of(FARMLAND));

        ITEM_BLOCK_PLANT_MAP = new HashMap<>();
        ITEM_BLOCK_PLANT_MAP.put(BEETROOT_SEEDS, BEETROOTS);
        ITEM_BLOCK_PLANT_MAP.put(CARROT, CARROTS);
        ITEM_BLOCK_PLANT_MAP.put(MELON_SEEDS, MELON_STEM);
        ITEM_BLOCK_PLANT_MAP.put(NETHER_WART, NETHER_WART);
        ITEM_BLOCK_PLANT_MAP.put(POTATO, POTATOES);
        ITEM_BLOCK_PLANT_MAP.put(PUMPKIN_SEEDS, PUMPKIN_STEM);
        ITEM_BLOCK_PLANT_MAP.put(WHEAT_SEEDS, WHEAT);


    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            Material plant_item = player.getInventory().getItemInMainHand().getType();
            if (!ITEM_BLOCK_PLANT_MAP.containsKey(plant_item)) {
                sender.sendMessage("Item in main hand must be plantable");
            } else {
                Block targetBlock = player.getTargetBlockExact(8);

                if (targetBlock != null && plantableOn(ITEM_BLOCK_PLANT_MAP.get(plant_item), targetBlock)) {
                    plantStart(player, plant_item, targetBlock);

                } else {
                    sender.sendMessage("Must be looking at a block which %s can be planted on".formatted(plant_item.toString()));
                }
            }
        } else {
            sender.sendMessage("Must be run by a player");
        }
        return true;
    }

    private void plantStart(Player player, Material plant_item, Block targetBlock) {
        int totalItemCount = 0;
        ItemStack[] contents = player.getInventory().getContents();
        for (ItemStack content : contents) {

            if (content != null && content.getType() == plant_item) {
                totalItemCount += content.getAmount();
            }
        }
        int amountPlaced = plantFromBlock(targetBlock, ITEM_BLOCK_PLANT_MAP.get(plant_item), 0, player.getGameMode().equals(CREATIVE) ? Integer.MAX_VALUE : totalItemCount, new ArrayList<>());
        player.sendMessage("%d crop%s planted".formatted(amountPlaced, amountPlaced == 1 ? " was" : "s were"));
        if (player.getGameMode() != CREATIVE) {
            for (ItemStack content : contents) {
                if (content != null && content.getType() == plant_item) {
                    int amount = content.getAmount();
                    amountPlaced -= amount;
                    if (amountPlaced < 0) {
                        content.setAmount(-amountPlaced);
                        break;
                    } else {
                        content.setAmount(0);
                    }
                }
            }
        }
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_COW_BELL, 1f, 1f);
    }

    private int plantFromBlock(Block targetBlock, Material plant_block, int totalPlaced, int totalItemCount, List<Block> placed) {
        if (totalPlaced == totalItemCount)
            return totalPlaced;
        if (plantableOn(plant_block, targetBlock)) {
            targetBlock.getRelative(0, 1, 0).setType(plant_block);
            totalPlaced++;
            placed.add(targetBlock);

            for (Block i : getNeighbors(targetBlock)) {
                if (!placed.contains(i))
                    totalPlaced = plantFromBlock(i, plant_block, totalPlaced, totalItemCount, placed);
            }
        }
        return totalPlaced;
    }


    private boolean plantableOn(Material plant_block, Block block) {
        if (PLANTABLE_SURFACES.get(plant_block).contains(block.getType())) {
            return block.getLocation().add(0, 1, 0).getBlock().isEmpty();
        }
        return false;
    }

    public static boolean isPlantBlock(Block b) {
        return PlantCommandExec.ITEM_BLOCK_PLANT_MAP.containsValue(b.getType());
    }
    public static Block[] getNeighbors(Block clickedBlock) {
        Block[] blocks = new Block[4];
        blocks[0] = clickedBlock.getRelative(0,0,1);
        blocks[1] = clickedBlock.getRelative(0,0,-1);
        blocks[2] = clickedBlock.getRelative(1,0,0);
        blocks[3] = clickedBlock.getRelative(-1,0,0);
        return blocks;
    }
}

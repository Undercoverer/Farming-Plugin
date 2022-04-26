package com.undercover.farming.util;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static org.bukkit.Material.*;

public class UtilStuff {
    public final static Map<Material, List<Material>> PLANTABLE_SURFACES;

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

    public static boolean isPlantBlock(Block b) {
        return UtilStuff.ITEM_BLOCK_PLANT_MAP.containsValue(b.getType());
    }

    public static int getItemCount(Material item, ItemStack[] contents) {
        return Arrays.stream(contents)
                .filter(Objects::nonNull)
                .filter(e -> e.getType() == item)
                .mapToInt(ItemStack::getAmount)
                .sum();
    }

    public static void removeNItems(int n, ItemStack[] contents, Material item) {
        for (int i = contents.length - 1; i >= 0; i--) {
            ItemStack content = contents[i];
            if (content != null && content.getType() == item) {
                n -= content.getAmount();
                if (n >= 0) {
                    content.setAmount(0);
                } else {
                    content.setAmount(-n);
                    break;
                }
            }
        }
    }

    public static ArrayList<Block> getNeighbors(Block clickedBlock) {
        ArrayList<Block> blocks = new ArrayList<>(4);
        blocks.add(clickedBlock.getRelative(0, 0, 1));
        blocks.add(clickedBlock.getRelative(0, 0, -1));
        blocks.add(clickedBlock.getRelative(1, 0, 0));
        blocks.add(clickedBlock.getRelative(-1, 0, 0));
        return blocks;
    }

    public static boolean plantableOn(Material plant_block, Block block) {
        if (UtilStuff.PLANTABLE_SURFACES.containsKey(plant_block)) {
            if (UtilStuff.PLANTABLE_SURFACES.get(plant_block).contains(block.getType())) {
                return block.getLocation().add(0, 1, 0).getBlock().isEmpty();
            }
        }
        return false;
    }
}

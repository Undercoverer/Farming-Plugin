package com.undercover.farming;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.*;

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

    Plugin plugin = Bukkit.getPluginManager().getPlugin("Farming");

    public static boolean isPlantBlock(Block b) {
        return PlantCommandExec.ITEM_BLOCK_PLANT_MAP.containsValue(b.getType());
    }

    public static ArrayList<Block> getNeighbors(Block clickedBlock) {
        ArrayList<Block> blocks = new ArrayList<>(4);
        blocks.add(clickedBlock.getRelative(0, 0, 1));
        blocks.add(clickedBlock.getRelative(0, 0, -1));
        blocks.add(clickedBlock.getRelative(1, 0, 0));
        blocks.add(clickedBlock.getRelative(-1, 0, 0));
        return blocks;
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
                    int speed = 10;
                    if (args.length == 1) {
                        try {
                            speed = Math.min(Integer.parseInt(args[0]), 32);
                        } catch (NumberFormatException e) {
                            sender.sendMessage("Argument must be a non negative integer");
                        }
                    }
                    plantStart(player, plant_item, targetBlock, speed);

                } else {
                    sender.sendMessage("Must be looking at a block which %s can be planted on".formatted(plant_item.toString().toLowerCase().replaceAll("_", " ")));
                }
            }
        } else {
            sender.sendMessage("Must be run by a player");
        }
        return true;
    }


    private void plantStart(Player player, Material plant_item, Block targetBlock, int speed) {
        ItemStack[] contents = player.getInventory().getContents();
        int sum = Arrays.stream(contents).filter(Objects::nonNull).filter(e -> e.getType() == plant_item).mapToInt(ItemStack::getAmount).sum();
        ArrayList<Block> plantingSurfaces = new ArrayList<>();
        int count = Math.min(sum, blocksToPlant(targetBlock, ITEM_BLOCK_PLANT_MAP.get(plant_item), new HashSet<>(128), plantingSurfaces));
        plantingSurfaces.sort(Comparator.comparingDouble(o -> o.getLocation().distanceSquared(targetBlock.getLocation())));
        for (int i = 0; i < (player.getGameMode().equals(CREATIVE) ? plantingSurfaces.size() : count); i++) {
            int finalI = i;
            Bukkit.getScheduler().runTaskLater(plugin, () -> process(plantingSurfaces.get(finalI), ITEM_BLOCK_PLANT_MAP.get(plant_item)), (long) (speed * Math.sqrt(i / (2 * Math.PI))));
        }
        if (player.getGameMode() != CREATIVE) {
            for (int i = contents.length - 1; i >= 0; i--) {
                ItemStack content = contents[i];
                if (content != null && content.getType() == plant_item) {
                    count -= content.getAmount();
                    if (count >= 0) {
                        content.setAmount(0);
                    } else {
                        content.setAmount(-count);
                        break;
                    }
                }
            }
        }
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_COW_BELL, 1f, 1f);
    }

    private void process(Block block, Material toPlant) {
        block.getRelative(0, 1, 0).setType(toPlant);
    }

    private int blocksToPlant(Block start, Material plantBlock, HashSet<Block> visited, List<Block> blocksToPlaceOn) {
        int i = 0;
        Queue<Block> enqueued = new LinkedList<>();
        enqueued.add(start);
        while (!enqueued.isEmpty()) {
            Block b = enqueued.poll();
            if (plantableOn(plantBlock, b) && !visited.contains(b)) {
                blocksToPlaceOn.add(b);
                if (visited.add(b)) {
                    enqueued.addAll(getNeighbors(b));
                }
                i++;
            }
        }
        return i;
    }

    private boolean plantableOn(Material plant_block, Block block) {
        if (PLANTABLE_SURFACES.get(plant_block).contains(block.getType())) {
            return block.getLocation().add(0, 1, 0).getBlock().isEmpty();
        }
        return false;
    }
}

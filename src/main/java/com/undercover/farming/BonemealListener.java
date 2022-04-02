package com.undercover.farming;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.*;

import static org.bukkit.GameMode.CREATIVE;

public class BonemealListener implements Listener {
    Plugin plugin = Bukkit.getPluginManager().getPlugin("Farming");

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();
        if (player.isSneaking() && event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && clickedBlock != null) {
            if (PlantCommandExec.ITEM_BLOCK_PLANT_MAP.containsValue(clickedBlock.getType()) && event.getMaterial() == Material.BONE_MEAL) {
                ItemStack[] contents = player.getInventory().getContents();
                int sum = Arrays.stream(contents).filter(Objects::nonNull).filter(e -> e.getType() == Material.BONE_MEAL).mapToInt(ItemStack::getAmount).sum();
                ArrayList<Block> blocksToBone = new ArrayList<>();
                int count = Math.min(sum, blocksToBonemeal(clickedBlock, new HashSet<>(256), blocksToBone));
                blocksToBone.sort(Comparator.comparingDouble(o -> o.getLocation().distanceSquared(clickedBlock.getLocation())));
                for (int i = 0; i < (player.getGameMode().equals(CREATIVE) ? blocksToBone.size() : count); i++) {
                    int finalI = i;
                    Bukkit.getScheduler().runTaskLater(plugin, () -> process(blocksToBone.get(finalI)), (long) (10 * Math.sqrt(i / (2 * Math.PI))));
                }
                if (player.getGameMode() != CREATIVE) {
                    for (int i = contents.length - 1; i >= 0; i--) {
                        ItemStack content = contents[i];
                        if (content != null && content.getType() == Material.BONE_MEAL) {
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
                event.setCancelled(true);
            }
        }
    }

    private int blocksToBonemeal(Block block, HashSet<Block> visited, List<Block> processing) {
        int i = 0;
        Queue<Block> enqueued = new LinkedList<>();
        enqueued.add(block);
        while (!enqueued.isEmpty()) {
            Block b = enqueued.poll();
            BlockData blockData = b.getBlockData();
            if (blockData instanceof Ageable ageable)
                if (ageable.getAge() < ageable.getMaximumAge() && !visited.contains(b)) {
                    processing.add(b);
                    if (visited.add(b)) {
                        enqueued.addAll(PlantCommandExec.getNeighbors(b));
                    }
                    i++;
                }
        }
        return i;
    }

    private void process(Block targetBlock) {
        Ageable blockData = (Ageable) targetBlock.getBlockData();
        blockData.setAge(Math.min(blockData.getAge() + 2, 7));
        targetBlock.setBlockData(blockData);
        targetBlock.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, targetBlock.getLocation().add(0.5, 0.5, 0.5), 3);
        targetBlock.getWorld().playSound(targetBlock.getLocation().add(0.5, 0.5, 0.5), Sound.ITEM_BONE_MEAL_USE, 1, 1);
    }
}

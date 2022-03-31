package com.undercover.farming;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.GameMode.CREATIVE;

public class BonemealListener implements Listener {
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!player.isSneaking()){
            return;
        }
        Block clickedBlock = event.getClickedBlock();
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || clickedBlock == null) {
            return;
        }
        if (PlantCommandExec.ITEM_BLOCK_PLANT_MAP.containsValue(clickedBlock.getType()) && event.getMaterial() == Material.BONE_MEAL) {
            int boneMealCount = 0;
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() == Material.BONE_MEAL) {
                    boneMealCount += item.getAmount();
                }
            }
            int bonemealedCount = boneMealRecursive(clickedBlock, 0, player.getGameMode().equals(CREATIVE) ? Integer.MAX_VALUE : boneMealCount, new ArrayList<>());
            if (player.getGameMode() != CREATIVE) {
                for (ItemStack content : player.getInventory()) {
                    if (content != null && content.getType() == Material.BONE_MEAL) {
                        int amount = content.getAmount();
                        bonemealedCount -= amount;
                        if (bonemealedCount < 0) {
                            content.setAmount(-bonemealedCount);
                            break;
                        } else {
                            content.setAmount(0);
                        }
                    }
                }
            }
        }
    }

    private int boneMealRecursive(Block targetBlock, int totalTimes, int totalBonemealCount, List<Block> visited) {
        if (totalTimes == totalBonemealCount) {
            return totalTimes;
        }
        if (PlantCommandExec.isPlantBlock(targetBlock)) {
            Ageable blockData = (Ageable) targetBlock.getBlockData();
            if (blockData.getMaximumAge() != blockData.getAge()) {
                totalTimes++;
                blockData.setAge(blockData.getAge() + 1);
                System.out.println(blockData.getAge());
                targetBlock.setBlockData(blockData);
                targetBlock.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, targetBlock.getLocation().add(0.5,0.5,0.5), 3);
                targetBlock.getWorld().playSound(targetBlock.getLocation().add(0.5,0.5,0.5), Sound.ITEM_BONE_MEAL_USE, 1, 1);
            }
            visited.add(targetBlock);
            for (Block e : PlantCommandExec.getNeighbors(targetBlock)) {
                if (!visited.contains(e)) {
                    totalTimes = boneMealRecursive(e, totalTimes, totalBonemealCount, visited);
                }
            }
        }
        return totalTimes;
    }
}

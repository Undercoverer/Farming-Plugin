package com.undercover.farming;

import com.undercover.farming.util.BlockUtil;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

public class BonemealListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        BlockUtil bonemealer = new BlockUtil();
        bonemealer.setPlayer(event.getPlayer());
        bonemealer.setStartBlock(event.getClickedBlock());

        bonemealer.setMaterialPair(null, Material.BONE_MEAL);


        bonemealer.setProcessCondition(s -> (s.getBlockData() instanceof Ageable a && a.getAge() != a.getMaximumAge()));
        bonemealer.setProcessConsumer((block) -> {
            Ageable blockData = (Ageable) block.getBlockData();
            blockData.setAge(Math.min(blockData.getAge() + 2, blockData.getMaximumAge()));
            block.setBlockData(blockData);
            block.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, block.getLocation().add(Vector.getRandom()), 3);
            block.getWorld().playSound(block.getLocation().add(0.5, 0.5, 0.5), Sound.ITEM_BONE_MEAL_USE, 1, 1);
        });

        bonemealer.addPrecondition(bonemealer.player.isSneaking());
        bonemealer.addPrecondition(event.getAction().equals(Action.RIGHT_CLICK_BLOCK));
        bonemealer.addPrecondition(bonemealer.startBlock != null);

        if (bonemealer.testPreconditions() != -1) {
            return;
        }

        bonemealer.setSpeed(10);
        bonemealer.start();

        event.setCancelled(true);
    }
}

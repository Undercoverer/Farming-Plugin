package com.undercover.farming.util;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.undercover.farming.util.UtilStuff.getItemCount;

public class BlockUtil {
    private final List<Block> processing = new ArrayList<>(128);
    private final HashSet<Block> visited = new HashSet<>(256);
    private final List<Boolean> runnable = new ArrayList<>();
    public Pair<Material, Material> materialPair;
    public Player player;
    public Block startBlock;
    Plugin plugin = Bukkit.getPluginManager().getPlugin("Farming");
    private int speed = 10;
    private int placedCount;
    private Consumer<Block> processConsumer;
    private Predicate<Block> processable;

    public int start() {
        int valid = testPreconditions();
        if (valid == -1) {
            boolean creative = player.getGameMode().equals(GameMode.CREATIVE);
            int itemCount = creative ? Integer.MAX_VALUE : getItemCount(materialPair.item(), player.getInventory().getContents());
            int validBlockCount = getValidBlockCount(itemCount);
            placedCount = Math.min(itemCount, validBlockCount);
            processing.sort(Comparator.comparingDouble(o -> o.getLocation().distanceSquared(startBlock.getLocation())));
            for (int i = 0; i < (creative ? processing.size() : placedCount); i++) {
                int finalI = i;
                Bukkit.getScheduler().runTaskLater(plugin, () -> processConsumer.accept(processing.get(finalI)), (long) (speed * Math.sqrt(i / (2 * Math.PI))));
            }
            if (player.getGameMode() != GameMode.CREATIVE) {
                UtilStuff.removeNItems(placedCount, player.getInventory().getContents(), materialPair.item());
            }
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_COW_BELL, 1f, 1f);
            return -1;
        }
        return valid;
    }

    private int getValidBlockCount(int itemCount) {
        int validBlocks = 0;
        Queue<Block> blockQueue = new LinkedList<>();
        blockQueue.add(startBlock);
        while (!blockQueue.isEmpty() && visited.size() < (int) (itemCount * 1.5)) {
            Block b = blockQueue.poll();
            if (b != null && !visited.contains(b) && processable.test(b)) {
                processing.add(b);
                visited.add(b);
                blockQueue.addAll(UtilStuff.getNeighbors(b));
                validBlocks++;
            }
        }
        return validBlocks;
    }


    public void addPrecondition(boolean b) {
        runnable.add(b);
    }

    public void setProcessCondition(Predicate<Block> condition) {
        this.processable = condition;
    }

    public void setProcessConsumer(Consumer<Block> r) {
        this.processConsumer = r;
    }

    public void setMaterialPair(Material block, Material item) {
        this.materialPair = new Pair<>(block, item);
    }

    public void setPlayer(Player p) {
        this.player = p;
    }

    public void setStartBlock(Block clickedBlock) {
        this.startBlock = clickedBlock;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int testPreconditions() {
        return this.runnable.indexOf(false);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BlockUtil{");
        sb.append("processing=").append(processing).append('\n');
        sb.append(", visited=").append(visited).append('\n');
        sb.append(", runnable=").append(runnable).append('\n');
        sb.append(", materialPair=").append(materialPair).append('\n');
        sb.append(", player=").append(player).append('\n');
        sb.append(", startBlock=").append(startBlock).append('\n');
        sb.append(", speed=").append(speed).append('\n');
        sb.append(", placedCount=").append(placedCount).append('\n');
        sb.append(", plugin=").append(plugin).append('\n');
        sb.append(", processConsumer=").append(processConsumer).append('\n');
        sb.append(", processable=").append(processable).append('\n');
        sb.append('}');
        return sb.toString();
    }
}

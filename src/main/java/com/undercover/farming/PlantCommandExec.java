package com.undercover.farming;

import com.undercover.farming.util.BlockUtil;
import com.undercover.farming.util.UtilStuff;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class PlantCommandExec implements CommandExecutor {


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        BlockUtil planter = new BlockUtil();
        if (sender instanceof Player p) {
            planter.setPlayer(p);
            planter.setStartBlock(p.getTargetBlockExact(8));
        } else {
            sender.sendMessage("Command must be sent by a player");
            return true;
        }

        Material type = p.getInventory().getItemInMainHand().getType();
        planter.setMaterialPair(UtilStuff.ITEM_BLOCK_PLANT_MAP.get(type), type);
        planter.addPrecondition(UtilStuff.ITEM_BLOCK_PLANT_MAP.containsKey(planter.materialPair.item()));
        planter.addPrecondition(planter.startBlock != null && UtilStuff.plantableOn(planter.materialPair.block(), planter.startBlock));


        planter.setProcessConsumer(block -> block.getRelative(0, 1, 0).setType(planter.materialPair.block()));
        planter.setProcessCondition(block -> UtilStuff.plantableOn(planter.materialPair.block(), block));


        int speed = 10;
        if (args.length == 1) {
            try {
                speed = Math.min(Integer.parseInt(args[0]), 32);
            } catch (NumberFormatException e) {
                sender.sendMessage("Argument must be a non negative integer");
            }
        }
        planter.setSpeed(speed);


        switch (planter.start()) {
            case 0:
                sender.sendMessage("Item in main hand must be plantable");
                break;
            case 1:
                sender.sendMessage("Must be looking at a block which %s can be planted on".formatted(planter.materialPair.item().toString().toLowerCase().replaceAll("_", " ")));
                break;
            case 2:
        }
        return true;
    }
}

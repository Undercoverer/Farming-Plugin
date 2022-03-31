package com.undercover.farming;

import org.bukkit.plugin.java.JavaPlugin;

public final class Farming extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginCommand("plant").setExecutor(new PlantCommandExec());
        getServer().getPluginManager().registerEvents(new BonemealListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}

package me.bot;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class MainPlugin extends JavaPlugin implements Listener {
    public static MainPlugin instance;

    private RevivePeopleListener revivePeopleListener;
    public MainPlugin(){
//        this.mobListeners = new MobListeners();
        this.revivePeopleListener = new RevivePeopleListener();
    }

    @Override
    public void onEnable() {
        instance=this;
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this.revivePeopleListener, this);

        this.getCommand("kit").setExecutor(new CommandKit());
        this.getCommand("revive").setExecutor(this.revivePeopleListener);

    }

    @Override
    public void onDisable() {
        System.out.println("chamando custom entity onDisable");
//        this.mobListeners.onDisablePlugin();
    }
}

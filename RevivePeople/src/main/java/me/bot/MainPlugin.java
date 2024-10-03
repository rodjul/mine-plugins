package me.bot;

import me.bot.commands.CommandKit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class MainPlugin extends JavaPlugin implements Listener {
    public static MainPlugin instance;

    private RevivePeople revivePeopleListener;

    @Override
    public void onEnable() {
        instance=this;
        saveDefaultConfig();

        this.revivePeopleListener = new RevivePeople(instance);

        getServer().getPluginManager().registerEvents(this.revivePeopleListener, this);

        this.getCommand("kit").setExecutor(new CommandKit());
        this.getCommand("revive").setExecutor(this.revivePeopleListener);
        this.getCommand("respawn").setExecutor(this.revivePeopleListener);

    }

    @Override
    public void onDisable() {
        System.out.println("chamando custom entity onDisable");
        this.revivePeopleListener.disable();
//        this.mobListeners.onDisablePlugin();
    }
}
